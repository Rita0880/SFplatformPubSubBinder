package com.spring.sfPlatformPubSubBinder;

public class SaleforcePlatformUtils {

  public static String topicWithoutQueryString(String fullTopic) {
    return fullTopic.split("\\?")[0];
  }
}
