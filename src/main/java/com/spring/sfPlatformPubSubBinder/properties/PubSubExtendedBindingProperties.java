package com.spring.sfPlatformPubSubBinder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

@ConfigurationProperties("spring.cloud.stream.salesforce")
public class PubSubExtendedBindingProperties  extends AbstractExtendedBindingProperties<
    PubSubConsumerProperties, PubSubProducerProperties, PubSubBindingProperties> {

  private static final String DEFAULTS_PREFIX = "spring.cloud.stream.salesforce.default";

  @Override
  public String getDefaultsPrefix() {
    return DEFAULTS_PREFIX;
  }

  @Override
  public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
    return PubSubBindingProperties.class;
  }
}
