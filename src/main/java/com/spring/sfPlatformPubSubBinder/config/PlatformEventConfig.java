package com.spring.sfPlatformPubSubBinder.config;

import com.spring.sfPlatformPubSubBinder.PubSubMessageChannelBinder;
import com.spring.sfPlatformPubSubBinder.connector.BearerTokenProvider;
import com.spring.sfPlatformPubSubBinder.connector.EmpConnector;
import com.spring.sfPlatformPubSubBinder.connector.LoginHelper;
import com.spring.sfPlatformPubSubBinder.operation.SalesforceEventMessageHandlers;
import com.spring.sfPlatformPubSubBinder.operation.SalesforceEventInboundChannelAdaptor;
import com.spring.sfPlatformPubSubBinder.properties.PlatformProperty;
import com.spring.sfPlatformPubSubBinder.properties.PubSubExtendedBindingProperties;
import com.spring.sfPlatformPubSubBinder.provisioning.PubSubChannelProvisioner;
import java.net.URL;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.config.BindingHandlerAdvise;
import org.springframework.cloud.stream.config.ConsumerEndpointCustomizer;
import org.springframework.cloud.stream.config.ProducerMessageHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({PubSubExtendedBindingProperties.class, PlatformProperty.class})
public class PlatformEventConfig {

  @Autowired
  private PlatformProperty platformProperty;

  @Bean
  public RestTemplate getRestTemplate(RestTemplateBuilder restTemplateBuilder) throws Exception {

    return restTemplateBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();

  }

  @Bean
  public PubSubChannelProvisioner pubSubChannelProvisioner() {
    return new PubSubChannelProvisioner();
  }

  @Bean
  public PubSubMessageChannelBinder pubSubMessageChannelBinder(
      PubSubChannelProvisioner pubSubChannelProvisioner,
      PubSubExtendedBindingProperties pubSubExtendedBindingProperties,
      EmpConnector empConnector,BearerTokenProvider bearerTokenProvider,
      @Nullable ProducerMessageHandlerCustomizer<SalesforceEventMessageHandlers> salesforceEventMessageHandlers,
      @Nullable ConsumerEndpointCustomizer<SalesforceEventInboundChannelAdaptor> salesforceEventProducer) throws Exception {
    PubSubMessageChannelBinder binder =
        new PubSubMessageChannelBinder(null, pubSubChannelProvisioner, pubSubExtendedBindingProperties, platformProperty,empConnector,bearerTokenProvider);
    binder.setProducerMessageHandlerCustomizer(salesforceEventMessageHandlers);
    binder.setConsumerEndpointCustomizer(salesforceEventProducer);


    return binder;
  }

  @Bean
  public BindingHandlerAdvise.MappingsProvider pubSubExtendedPropertiesDefaultMappingsProvider() {
    return () ->
        Collections.singletonMap(
            ConfigurationPropertyName.of("spring.cloud.stream.salesforce.bindings"),
            ConfigurationPropertyName.of("spring.cloud.stream.salesforce.default"));
  }



  @Bean
  public EmpConnector getEmpConnector(){
    return new EmpConnector();
  }

  @Bean
  public BearerTokenProvider getBearerTokenProvider()  {
    return new BearerTokenProvider(() -> {
      try {
        //return LoginHelper.login(new URL(salesforceUrl), userName, password);
        return LoginHelper.getAccessToken(new URL(platformProperty.getEventUrl()), platformProperty.getUserName(), platformProperty.getPassword());

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }



}
