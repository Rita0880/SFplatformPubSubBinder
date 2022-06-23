package com.spring.sfPlatformPubSubBinder.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

public class PubSubProducerDestination implements ProducerDestination {
  private String name;

  public PubSubProducerDestination(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getNameForPartition(int partition) {
    throw new UnsupportedOperationException("Partitioning is not implemented for salesforcplatform.");
  }
}

