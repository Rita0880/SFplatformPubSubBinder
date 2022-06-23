package com.spring.sfPlatformPubSubBinder.connector;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Container for BayeuxParameters and the bearerToken.
 * Calls BayeuxParameters supplier in re-authentication scenarios.
 *
 * @author pbn-sfdc
 */
public class BearerTokenProvider {

    private Supplier<BayeuxParameters> sessionSupplier;
    private String bearerToken;

    public BearerTokenProvider(Supplier<BayeuxParameters> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    public BayeuxParameters login() throws Exception {
        BayeuxParameters parameters = sessionSupplier.get();
        bearerToken = parameters.getBearerToken();
        return parameters;
    }


    public String apply(Boolean reAuth) {
        if (reAuth) {
            try {
                bearerToken = sessionSupplier.get().getBearerToken();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bearerToken;
    }
}
