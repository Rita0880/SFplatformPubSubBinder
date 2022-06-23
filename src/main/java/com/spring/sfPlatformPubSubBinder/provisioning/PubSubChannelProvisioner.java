package com.spring.sfPlatformPubSubBinder.provisioning;

import com.spring.sfPlatformPubSubBinder.properties.PubSubConsumerProperties;
import com.spring.sfPlatformPubSubBinder.properties.PubSubProducerProperties;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;


public class PubSubChannelProvisioner implements ProvisioningProvider<ExtendedConsumerProperties<PubSubConsumerProperties>,
    ExtendedProducerProperties<PubSubProducerProperties>>  {

  private static final Log LOGGER = LogFactory.getLog(PubSubChannelProvisioner.class);


  private final Set<String> anonymousGroupSubscriptionNames = new HashSet<>();



  @Override
  public ProducerDestination provisionProducerDestination(
      String topic,  ExtendedProducerProperties<PubSubProducerProperties> properties) {

    return new PubSubProducerDestination(topic);
  }

  @Override
  public ConsumerDestination provisionConsumerDestination(
      String topicName,
      String group,
      ExtendedConsumerProperties<PubSubConsumerProperties> properties) {

    return new PubSubConsumerDestination(topicName);
  }


}



