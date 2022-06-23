package com.spring.sfPlatformPubSubBinder.properties;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PubSubConsumerProperties {

  private String replayformTip;
  private String topicName;
  private Integer maxFetchSize = 1;
}
