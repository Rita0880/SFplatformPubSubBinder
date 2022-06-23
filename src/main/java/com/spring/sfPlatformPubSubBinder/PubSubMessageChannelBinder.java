package com.spring.sfPlatformPubSubBinder;
import static org.cometd.bayeux.Channel.META_CONNECT;
import static org.cometd.bayeux.Channel.META_DISCONNECT;
import static org.cometd.bayeux.Channel.META_HANDSHAKE;
import static org.cometd.bayeux.Channel.META_SUBSCRIBE;
import static org.cometd.bayeux.Channel.META_UNSUBSCRIBE;

import com.spring.sfPlatformPubSubBinder.connector.BearerTokenProvider;
import com.spring.sfPlatformPubSubBinder.connector.EmpConnector;
import com.spring.sfPlatformPubSubBinder.connector.LoggingListener;
import com.spring.sfPlatformPubSubBinder.operation.SalesforceEventMessageHandlers;
import com.spring.sfPlatformPubSubBinder.operation.SalesforceEventInboundChannelAdaptor;
import com.spring.sfPlatformPubSubBinder.properties.PlatformProperty;
import com.spring.sfPlatformPubSubBinder.properties.PubSubConsumerProperties;
import com.spring.sfPlatformPubSubBinder.properties.PubSubExtendedBindingProperties;
import com.spring.sfPlatformPubSubBinder.properties.PubSubProducerProperties;
import com.spring.sfPlatformPubSubBinder.provisioning.PubSubChannelProvisioner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

public class PubSubMessageChannelBinder extends
    AbstractMessageChannelBinder<ExtendedConsumerProperties<PubSubConsumerProperties>, ExtendedProducerProperties<PubSubProducerProperties>, PubSubChannelProvisioner>
    implements ExtendedPropertiesBinder<
        MessageChannel, PubSubConsumerProperties, PubSubProducerProperties> {

  private final PubSubChannelProvisioner pubSubChannelProvisioner;
  private final PubSubExtendedBindingProperties pubSubExtendedBindingProperties;
  private final PlatformProperty platformProperty;
  private final EmpConnector empConnector;
  private final BearerTokenProvider bearerTokenProvider;


  public PubSubMessageChannelBinder(
      String[] headersToEmbed,
      PubSubChannelProvisioner provisioningProvider,
      PubSubExtendedBindingProperties pubSubExtendedBindingProperties,PlatformProperty platformProperty,EmpConnector empConnector,BearerTokenProvider bearerTokenProvider)
      throws Exception {

    super(headersToEmbed, provisioningProvider);
    this.pubSubChannelProvisioner = provisioningProvider;
    this.pubSubExtendedBindingProperties = pubSubExtendedBindingProperties;
    this.platformProperty = platformProperty;
    this.empConnector = empConnector;
    this.bearerTokenProvider = bearerTokenProvider;

  }

  @Override
  public void onInit() throws Exception {
    super.onInit();
    empConnector.setBearerTokenProvider(this.bearerTokenProvider :: apply);
    empConnector.setParameters(this.bearerTokenProvider.login());
    initiateConnection(empConnector);
  }



  @Override
  protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                        ExtendedProducerProperties<PubSubProducerProperties> producerProperties,
                                                        MessageChannel errorChannel) throws Exception {
    return new SalesforceEventMessageHandlers(producerProperties.getExtension(),destination.getName(),bearerTokenProvider);
  }

  @Override
  protected MessageProducer createConsumerEndpoint(
      ConsumerDestination destination,
      String group,
      ExtendedConsumerProperties<PubSubConsumerProperties> properties) {
    SalesforceEventInboundChannelAdaptor adapter = new SalesforceEventInboundChannelAdaptor(properties.getExtension(),destination.getName(),empConnector);
    ErrorInfrastructure errorInfrastructure =
        registerErrorInfrastructure(destination, group, properties);
    adapter.setErrorChannel(errorInfrastructure.getErrorChannel());

    return adapter;
    }

  @Override
  public PubSubConsumerProperties getExtendedConsumerProperties(String channelName) {
    return this.pubSubExtendedBindingProperties.getExtendedConsumerProperties(channelName);
  }

  @Override
  public PubSubProducerProperties getExtendedProducerProperties(String channelName) {
    return this.pubSubExtendedBindingProperties.getExtendedProducerProperties(channelName);
  }

  @Override
  public String getDefaultsPrefix() {
    return this.pubSubExtendedBindingProperties.getDefaultsPrefix();
  }

  @Override
  public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
    return this.pubSubExtendedBindingProperties.getExtendedPropertiesEntryClass();
  }

  private void initiateConnection(final EmpConnector empConnector)
      throws ExecutionException, InterruptedException, TimeoutException {
    Assert.notNull(empConnector, "The connector can't be null.");
    LoggingListener loggingListener =  new LoggingListener();
    empConnector.addListener(META_HANDSHAKE, loggingListener)
        .addListener(META_CONNECT, loggingListener)
        .addListener(META_DISCONNECT, loggingListener)
        .addListener(META_SUBSCRIBE, loggingListener)
        .addListener(META_UNSUBSCRIBE, loggingListener);
  }




}
