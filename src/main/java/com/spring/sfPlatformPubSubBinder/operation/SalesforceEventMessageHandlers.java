package com.spring.sfPlatformPubSubBinder.operation;

import com.spring.sfPlatformPubSubBinder.connector.BayeuxParameters;
import com.spring.sfPlatformPubSubBinder.connector.BearerTokenProvider;
import com.spring.sfPlatformPubSubBinder.connector.EmpConnector;
import com.spring.sfPlatformPubSubBinder.model.OauthResponse;
import com.spring.sfPlatformPubSubBinder.properties.PubSubConsumerProperties;
import com.spring.sfPlatformPubSubBinder.properties.PubSubProducerProperties;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;


@Slf4j
@AllArgsConstructor
public class SalesforceEventMessageHandlers extends AbstractMessageHandler {

  private PubSubProducerProperties pubSubProducerProperties;
  private String destination;
  private final BearerTokenProvider bearerTokenProvider;





  @SneakyThrows
  @Override
  protected void handleMessageInternal(Message<?> message) {

    try {

      System.out.println("message handler todo = " + message);

      RestTemplate restTemplate = new RestTemplate();
      BayeuxParameters bayeuxParameters = bearerTokenProvider.login();
      String token = bayeuxParameters.getBearerToken();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.add("Authorization", "Bearer " + token);
      headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
      HttpEntity<String> request = new HttpEntity<String>(new String((byte[]) message.getPayload(), StandardCharsets.UTF_8), headers);
      String publishUrl = bayeuxParameters.getPublishEndPointHost().toString() + "/" + destination + "?";
      String response = restTemplate.postForObject(publishUrl, request, String.class);
      System.out.println("response from salesforce = " + response);
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
