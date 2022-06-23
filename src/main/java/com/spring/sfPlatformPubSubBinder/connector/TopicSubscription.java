package com.spring.sfPlatformPubSubBinder.connector;

import com.spring.sfPlatformPubSubBinder.model.Subscriber;

public interface TopicSubscription {

  public void cancel(Subscriber subscriber);
}
