/*
 * Copyright (c) 2016, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.TXT file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.spring.sfPlatformPubSubBinder.connector;


import static com.spring.sfPlatformPubSubBinder.SaleforcePlatformUtils.topicWithoutQueryString;

import com.spring.sfPlatformPubSubBinder.SaleforcePlatformUtils;
import com.spring.sfPlatformPubSubBinder.constants.PlatformEventConstants;
import com.spring.sfPlatformPubSubBinder.model.Subscriber;
import java.net.ConnectException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author hal.hildebrand
 * @since API v37.0
 */

public class EmpConnector {

    private static final Logger log = LoggerFactory.getLogger(EmpConnector.class);

    private Function<Boolean, String> bearerTokenProvider;
    private  BayeuxParameters parameters ;
    private final Set<Subscriber> subscribers = new CopyOnWriteArraySet<>();
    private volatile BayeuxClient client;
    private final HttpClient httpClient;
    private final ConcurrentMap<String, Long> replay = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private final Set<MessageListenerInfo> listenerInfos = new CopyOnWriteArraySet<>();
    private AtomicBoolean reauthenticate = new AtomicBoolean(false);

    public EmpConnector() {
        this.parameters = new BayeuxParameters();
        this.httpClient = new HttpClient(parameters.sslContextFactory());
        this.httpClient.getProxyConfiguration().getProxies().addAll(parameters.proxies());
    }

    /**
     * Start the connector.
     * @return true if connection was established, false otherwise
     */
    public Future<Boolean> start() {
        if (running.compareAndSet(false, true)) {
            addListener(Channel.META_CONNECT, new AuthFailureListener());
            addListener(Channel.META_HANDSHAKE, new AuthFailureListener());
            replay.clear();
            return connect();
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(true);
        return future;
    }

    /**
     * Disconnecting Bayeux Client in Emp Connector
     */
    private void disconnect() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (client != null) {
            log.info("Disconnecting Bayeux Client in EmpConnector");
            client.disconnect();
            client = null;
        }
    }

    /**
     * Stop the connector
     */
    public void stop(String endpoint) {
        disconnect();
        if (httpClient != null) {
            try {
                log.info("Stopping the http client!");
                httpClient.stop();
            } catch (Exception e) {
                log.error("Unable to stop HTTP transport[{}]", parameters.getEndpoint(), e);
            }
        }
    }

    /**
     * Subscribe to a topic, receiving events after the replayFrom position
     *
     * @param subscriber
     *            - the topic to subscribe to
     *
     * @return a Future returning the Subscription - on completion returns a Subscription or throws a CannotSubscribe
     *         exception
     */
    public Future<TopicSubscription> subscribe(Subscriber subscriber) throws ExecutionException, InterruptedException {
        if (!running.get()) {
            throw new IllegalStateException(String.format("Connector[%s} has not been started",
                parameters.getEndpoint()));
        }
        String topic = subscriber.getTopic().replaceAll("/$", "");

        final String topicWithoutQueryString = SaleforcePlatformUtils.topicWithoutQueryString(topic);
        if (replay.putIfAbsent(topicWithoutQueryString, subscriber.getReplayFrom()) != null) {
            throw new IllegalStateException(String.format("Already subscribed to %s [%s]",
                topic, parameters.getEndpoint()));
        }
        subscribers.add(subscriber);
        return subscriber.subscribe(client, parameters,getTopicSubscrition());
    }

    /**
     * Set a bearer token / session id provider function that takes a boolean as input and returns a valid token.
     * If the input is true, the provider function is supposed to re-authenticate with the Salesforce server
     * and get a fresh session id or token.
     *
     * @param bearerTokenProvider a bearer token provider function.
     */
    public void setBearerTokenProvider(Function<Boolean, String> bearerTokenProvider) {
        this.bearerTokenProvider = bearerTokenProvider;
    }



    public long getReplayFrom(String topic) {

        return replay.getOrDefault(topicWithoutQueryString(topic), PlatformEventConstants.REPLAY_FROM_TIP);
    }
    public Set<Subscriber> getSubscribers() {
        return subscribers;
    }
    public AtomicBoolean getRunning() {
        return running;
    }


    public EmpConnector addListener(String channel, ClientSessionChannel.MessageListener messageListener) {
        listenerInfos.add(new MessageListenerInfo(channel, messageListener));
        return this;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public boolean isDisconnected() {
        return client == null || client.isDisconnected();
    }

    public boolean isHandshook() {
        return client != null && client.isHandshook();
    }

    public long getLastReplayId(String topic) {
        return replay.get(topic);
    }

    public BayeuxClient getClient() {
        return client;
    }

    private Future<Boolean> connect() {
        log.info("EmpConnector connecting");
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            if (!httpClient.isStarted()) {
                httpClient.start();
            }
        } catch (Exception e) {
            log.error("Unable to start HTTP transport[{}]", parameters.getEndpoint(), e);
            running.set(false);
            future.complete(false);
            return future;
        }

        final String bearerToken = bearerToken();
        LongPollingTransport httpTransport = new LongPollingTransport(parameters.longPollingOptions(), httpClient) {
            @Override
            protected void customize(Request request) {
                request.header(PlatformEventConstants.AUTHORIZATION, bearerToken);
            }
        };

        client = new BayeuxClient(parameters.getEndpoint().toExternalForm(), httpTransport);

        client.addExtension(new ReplayExtension(replay));


        addListeners(client);

        client.handshake((m) -> {
            if (!m.isSuccessful()) {
                Object error = m.get(PlatformEventConstants.ERROR);
                if (error == null) {
                    error = m.get(PlatformEventConstants.FAILURE);
                }
                future.completeExceptionally(new ConnectException(
                        String.format("Cannot connect [%s] : %s", parameters.getEndpoint(), error)));
                running.set(false);
            } else {
                subscribers.forEach( (s) -> s.subscribe(client,parameters,getTopicSubscrition()));
                future.complete(true);
            }
        });

        return future;
    }

    public BayeuxParameters getParameters() {
        return parameters;
    }

    public void setParameters(BayeuxParameters bayeuxParameters) {
                this.parameters = bayeuxParameters;
    }

    private void addListeners(BayeuxClient client) {
        for (MessageListenerInfo info : listenerInfos) {
            client.getChannel(info.getChannelName()).addListener(info.getMessageListener());
        }
    }

    private String bearerToken() {
        String bearerToken;
        if (bearerTokenProvider != null) {
            bearerToken = bearerTokenProvider.apply(reauthenticate.get());
            reauthenticate.compareAndSet(true, false);
        } else {
            bearerToken = parameters.getBearerToken();
        }

        return bearerToken;
    }

    private void reconnect() {
        if (running.compareAndSet(false, true)) {
            connect();
        } else {
            log.error("The current value of running is not as we expect, this means our reconnection may not happen");
        }
    }


    /**
     * Listens to /meta/connect channel messages and handles 401 errors, where client needs
     * to reauthenticate.
     */
    private class AuthFailureListener implements ClientSessionChannel.MessageListener {
        private static final String ERROR_401 = "401";
        private static final String ERROR_403 = "403";

        @Override
        public void onMessage(ClientSessionChannel channel, Message message) {
            if (!message.isSuccessful()) {
                if (isError(message, ERROR_401) || isError(message, ERROR_403)) {
                    reauthenticate.set(true);
                    disconnect();
                    reconnect();
                }
            }
        }

        private boolean isError(Message message, String errorCode) {
            String error = (String)message.get(Message.ERROR_FIELD);
            String failureReason = getFailureReason(message);

            return (error != null && error.startsWith(errorCode)) ||
                    (failureReason != null && failureReason.startsWith(errorCode));
        }

        private String getFailureReason(Message message) {
            String failureReason = null;
            Map<String, Object> ext = message.getExt();
            if (ext != null) {
                Map<String, Object> sfdc = (Map<String, Object>)ext.get("sfdc");
                if (sfdc != null) {
                    failureReason = (String)sfdc.get("failureReason");
                }
            }
            return failureReason;
        }
    }

    private static class MessageListenerInfo {
        private String channelName;
        private ClientSessionChannel.MessageListener messageListener;

        MessageListenerInfo(String channelName, ClientSessionChannel.MessageListener messageListener) {
            this.channelName = channelName;
            this.messageListener = messageListener;
        }

        String getChannelName() {
            return channelName;
        }

        ClientSessionChannel.MessageListener getMessageListener() {
            return messageListener;
        }
    }

     public TopicSubscription getTopicSubscrition(){

        return new TopicSubscription() {
            @Override
            public void cancel(Subscriber subscriber) {
                replay.remove(topicWithoutQueryString(subscriber.getTopic()));
                if (running.get() && client != null) {
                    client.getChannel(subscriber.getTopic()).unsubscribe();
                    subscribers.remove(subscriber);
                }
            }
        };
    }
}
