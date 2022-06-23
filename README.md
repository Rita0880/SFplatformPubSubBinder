# SFplatformPubSubBinder

Spring cloud stream custom binder for saleforce platform event bus(in draft mode)

# Overview of Binder api:

The main binder implementation class : PubSubMessageChannelBinder

# The issue is related to configure consumer group on the subscription of salesforce platform event:

method which implements the subscription logic: createConsumerEndpoint (spring cloud stream binder method) which has reference of SalesforceEventInboundChannelAdaptor, it has an empconnecotr refrence to create the long polling connection to salefroce platform event bus
EmpConnector has function bearerTokenProvider which is used to get token first to set up connection to saleforce platform event and connect() function to connect to platform event

salesforce refernce: https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_subscribe_emp_connector.htm

maven command to build the project 
mvn clean install

On client service which use the binder to process the message is

add the pom dependancy

 <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>sfPlatformPubSubBinder</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        
spring cloud function to process the payloads

 @Bean
 # public Function<String,String> processMessage() {

    return value -> {
      System.out.println("payLoad = " + value);
      try {
        String response = processEventService.createBusinessCase(value);
        System.out.println("status of Business case creation: "+response);
        return response;
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();

      }
      return null;
    };
  }
}
   
   
   application.yaml file to set the property
   
   spring:
  cloud:
    config:
      enabled: false
    stream:
      salesforce:
        bindings:
          processMessage-in-0:
            binder: local_salesforce
            consumer:
              replayform-tip: -1
              topic-name: /event/xxxxxx
          processMessage-out-0:
            binder: local_salesforce
      binders:
        local_salesforce:
          type: salesforce
          environment:
            salesforce:
              platform:
                event-url: https://xxxxxxxxxxxx/oauth2/token
                userName: xxxxxx
                password: xxxxxxxxxx
                clientId: xxxxxxxxxxxxx
                clientSecret: xxxxxxxxx
      bindings:
        processMessage-in-0:
          destination: /event/xxxx__e
          group: abc
        processMessage-out-0:
          destination: /xxxx__e



management:
  endpoint:
    health:
      show-details: always

server:
  port: 0


 the custom binder is working fine as expected except the consumer group


