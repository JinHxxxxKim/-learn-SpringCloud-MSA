package com.example.userservice.common.constant.jwt;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TokenType {

    ACCESS_TOKEN("accessToken", Duration.ofHours(1).toMillis()),
    REFRESH_TOKEN("refreshToken", Duration.ofDays(30).toMillis());

    private final String name;
    private final Long expirationTime;

}
