/*
 * Copyright (c) 2016, salesforce.com, inc. All rights reserved. Licensed under the BSD 3-Clause license. For full
 * license text, see LICENSE.TXT file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.spring.sfPlatformPubSubBinder.connector;


import com.spring.sfPlatformPubSubBinder.constants.PlatformEventConstants;
import com.spring.sfPlatformPubSubBinder.model.OauthResponse;
import java.net.URL;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * A helper to obtain the Authentication bearer token via login
 *
 * @author hal.hildebrand
 * @since API v37.0
 */
@Slf4j
public class LoginHelper {


    public static BayeuxParameters login(String loginEndpoint, String username, String password) throws Exception {
        return getAccessToken(new URL(loginEndpoint), username, password);
    }

    public static BayeuxParameters login(String loginEndpoint, String username, String password, BayeuxParameters params) throws Exception {
        return getAccessToken(new URL(loginEndpoint), username, password, params);
    }


    public static BayeuxParameters getAccessToken(URL loginEndpoint, String username, String password) throws Exception {
        return getAccessToken(loginEndpoint, username, password, new BayeuxParameters() );

    }


    public static BayeuxParameters getAccessToken(URL loginEndpoint, String username, String password,
                                                                                         BayeuxParameters parameters) throws Exception{
        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        HttpEntity<String> request = new HttpEntity<String>(headers);

        String access_token_url = loginEndpoint + "?grant_type=password" ;
        access_token_url += "&client_id=3MVG9lcxCTdG2Vbv8hLhttjRuqKNGVvIJZzzi4_Z.wGK.UmSjulXIIqiz1xxc_oniZ9yPHnkeXo1N412AzMWP&client_secret=155612E5740EFC92CE6F8139899BCBA8147A06020AFE53E20AD26FB748B79776&username=" + username + "&password=" + password;


        ResponseEntity<OauthResponse> response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, OauthResponse.class);

        log.debug("Access Token Response ---------" + response.getBody().getAccess_token());
        URL sfEndpoint = new URL(response.getBody().getInstance_url());
        String cometdEndpoint = Float.parseFloat(parameters.getVersion()) < 37 ? PlatformEventConstants.COMETD_REPLAY_OLD : PlatformEventConstants.COMETD_REPLAY;
        URL replayEndpoint = new URL(sfEndpoint.getProtocol(), sfEndpoint.getHost(), sfEndpoint.getPort(),
            new StringBuilder().append(cometdEndpoint).append(parameters.getVersion()).toString());
        URL publishEndpoint = new URL(sfEndpoint.getProtocol(), sfEndpoint.getHost(), sfEndpoint.getPort(),
            new StringBuilder().append("/services/data/v52.0/sobjects/").toString());
        parameters.setBearerToken(response.getBody().getAccess_token());
        parameters.setEndpoint(replayEndpoint);
        parameters.setHost(loginEndpoint);
        parameters.setPublishEndPointHost(publishEndpoint);
        return parameters;
    }

}
