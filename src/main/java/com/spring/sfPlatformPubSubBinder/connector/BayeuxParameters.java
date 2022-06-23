package com.spring.sfPlatformPubSubBinder.connector;

import com.spring.sfPlatformPubSubBinder.constants.PlatformEventConstants;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;


@Getter
@Setter
public class BayeuxParameters {

  private String bearerToken;
  private URL endpoint;
  private URL host;
  private URL publishEndPointHost;
  private long keepAlive = 60;
  private TimeUnit keepAliveUnit = TimeUnit.MINUTES;
  private int maxBufferSize = 10485760;
  private int maxNetworkDelay = 15000;
  private String version = "43.0";



  public Map<String, Object> longPollingOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put("maxNetworkDelay", getMaxNetworkDelay());
    options.put("maxMessageSize", getMaxBufferSize());
    return options;
  }


  /**
   * @return a list of proxies to use for outbound connections
   */
  public Collection<? extends ProxyConfiguration.Proxy> proxies() {
    return Collections.emptyList();
  }

  /**
   * @return the SslContextFactory for establishing secure outbound connections
   */
  public SslContextFactory sslContextFactory() {
    return new SslContextFactory.Client();
  }


}
