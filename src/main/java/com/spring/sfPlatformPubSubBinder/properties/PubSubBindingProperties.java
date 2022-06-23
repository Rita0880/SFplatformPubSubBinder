package com.spring.sfPlatformPubSubBinder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

public class PubSubBindingProperties implements BinderSpecificPropertiesProvider {
  private PubSubConsumerProperties consumer = new PubSubConsumerProperties();

  private PubSubProducerProperties producer = new PubSubProducerProperties();

  public PubSubConsumerProperties getConsumer() {
    return this.consumer;
  }

  public void setConsumer(PubSubConsumerProperties consumer) {
    this.consumer = consumer;
  }

  public PubSubProducerProperties getProducer() {
    return this.producer;
  }

  public void setProducer(PubSubProducerProperties producer) {
    this.producer = producer;
  }
}

