package com.spring.sfPlatformPubSubBinder.operation;

import com.spring.sfPlatformPubSubBinder.connector.EmpConnector;
import com.spring.sfPlatformPubSubBinder.connector.TopicSubscription;
import com.spring.sfPlatformPubSubBinder.constants.PlatformEventConstants;
import com.spring.sfPlatformPubSubBinder.model.Subscriber;
import com.spring.sfPlatformPubSubBinder.properties.PubSubConsumerProperties;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;


@Slf4j
@AllArgsConstructor
public class SalesforceEventInboundChannelAdaptor extends MessageProducerSupport {

  private PubSubConsumerProperties pubSubConsumerProperties;
  private String destination;
  private final EmpConnector empConnector;


  // More than one thread can be used in the thread pool which leads to parallel processing of events which may be acceptable by the application
  // The main purpose of asynchronous event processing is to make sure that client is able to perform /meta/connect requests which keeps the session alive on the server side
  private final static ExecutorService workerThreadPool = Executors.newFixedThreadPool(5);


  @SneakyThrows
  @Override
  public  void doStart() {
    empConnector.start().get(5, TimeUnit.SECONDS);
    long replayFrom = PlatformEventConstants.REPLAY_FROM_TIP;
    replayFrom = pubSubConsumerProperties.getReplayformTip() != null ? Long.parseLong(pubSubConsumerProperties.getReplayformTip()) : replayFrom;

    Subscriber subscriber = Subscriber.builder().topic(destination)
        .consumer(getConsumer())
        .replayFrom(replayFrom)
        .build();

    TopicSubscription subscription;
    try {
      subscription = empConnector.subscribe(subscriber).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | InterruptedException e) {
      log.error("Failed to execute " , e);
      System.exit(1);
      throw e.getCause();
    } catch (TimeoutException e) {
      log.error("Timed out subscribing ", e);
      throw e.getCause();
    }

    log.info(String.format("Subscribed: %s", subscription));



  }

  private Consumer<Map<String, Object>> getConsumer() {
    return event -> CompletableFuture.runAsync(() -> {
      log.info(String.format("Received:\n%s, \nEvent processed by threadName:%s, threadId: %s", JSON.toString(event),
          Thread.currentThread().getName(), Thread.currentThread().getId()));
       sendMessage(MessageBuilder.withPayload(JSON.toString(event)).build());
    }, workerThreadPool);

  }



}

