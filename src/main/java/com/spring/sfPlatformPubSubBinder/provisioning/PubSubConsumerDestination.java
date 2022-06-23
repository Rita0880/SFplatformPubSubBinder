package com.spring.sfPlatformPubSubBinder.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

public class PubSubConsumerDestination implements ConsumerDestination {
  private String name;

  public PubSubConsumerDestination(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }
}


