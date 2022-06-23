package com.spring.sfPlatformPubSubBinder.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


public class SalesForceEventProcessor {


  RestTemplate restTemplate;

   public <T> ResponseEntity<String> processMessage(T payLoad){
     System.out.println("payLoad = " + payLoad);
     return restTemplate.postForEntity("http://localhost:8080//api/processEvent", payLoad, String.class);

  }
}
