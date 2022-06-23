package com.spring.sfPlatformPubSubBinder.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "salesforce.platform")
@Getter
@Setter
public class PlatformProperty {

  private String eventUrl;
  private String userName;
  private String password;
  private String clientId;
  private String clientSecret;
  private String replayformTip;
  private String topicName;

}
