package com.spring.sfPlatformPubSubBinder.model;

import com.spring.sfPlatformPubSubBinder.connector.BayeuxParameters;
import com.spring.sfPlatformPubSubBinder.connector.TopicSubscription;
import com.spring.sfPlatformPubSubBinder.constants.PlatformEventConstants;
import com.spring.sfPlatformPubSubBinder.exception.CannotSubscribe;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;

@Getter
@Setter
@Builder
public class Subscriber {

  private String topic;
  private Consumer<Map<String, Object>> consumer;
  private long replayFrom;



 public  Future<TopicSubscription> subscribe(BayeuxClient client, BayeuxParameters parameters , TopicSubscription topicSubscription) {
    ClientSessionChannel channel = client.getChannel(topic);
    CompletableFuture<TopicSubscription> future = new CompletableFuture<>();
     channel.subscribe((c, message) -> consumer.accept(message.getDataAsMap()), (message) -> {
      if (message.isSuccessful()) {
        future.complete( topicSubscription);
      } else {
        Object error = message.get(PlatformEventConstants.ERROR);
        if (error == null) {
          error = message.get(PlatformEventConstants.FAILURE);
        }
        future.completeExceptionally(
            new CannotSubscribe(parameters.getEndpoint(), topic, replayFrom, error != null ? error : message));
      }
    });
    return future;
  }



}
