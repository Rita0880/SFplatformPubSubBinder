package com.spring.sfPlatformPubSubBinder.model;

public class OauthResponse {
  private String access_token;
  private String instance_url;
  private String id;
  private String  token_type;

  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public String getInstance_url() {
    return instance_url;
  }

  public void setInstance_url(String instance_url) {
    this.instance_url = instance_url;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getToken_type() {
    return token_type;
  }

  public void setToken_type(String token_type) {
    this.token_type = token_type;
  }
}
