package com.abc.behaviortracker.global.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String issuer;
    private String secret;
    private long accessTokenValiditySeconds;
    private long refreshTokenValiditySeconds;
}
