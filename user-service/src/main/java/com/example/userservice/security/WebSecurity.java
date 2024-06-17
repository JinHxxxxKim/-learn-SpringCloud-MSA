package com.example.userservice.security;

import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.Jar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

    private final UserService userService;
    private final Environment env;
    private final JwtUtil jwtUtil;

    private static final String[] WHITE_LIST = {
            "/h2-console/**",
            "/health_check/**",
            "/actuator/**",
            "/welcome"
    };

//    public static final String ALLOWED_IP_ADDRESS = "127.0.0.1";
    public static final String ALLOWED_IP_ADDRESS = "192.168.56.1";
    public static final String SUBNET = "/32";
    // Takes a specific IP address or a range specified using the IP/Netmask (e.g. 192.168.1.0/24 or 202.24.0.0/14).
    public static final IpAddressMatcher ALLOWED_IP_ADDRESS_MATCHER = new IpAddressMatcher(ALLOWED_IP_ADDRESS + SUBNET);

    /**
     * Spring Security를 적용시키지 않을 URI 지정
     * "/actuator", "/h2-console", "/user(post)"로 오는 요청에 대해서는 모두 허용한다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                    web.ignoring()
                            .requestMatchers(WHITE_LIST)
                            .requestMatchers(new AntPathRequestMatcher("/users", "POST"));
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http
                // csrf 공격 예방 설정 OFF
                .csrf(AbstractHttpConfigurer::disable)

                // 모든 경로는 request에 대해 ip주소를 확인 및 인증과정을 추가한다.
                // AuthorizeHttpRequestsConfigurer 클래스의 내부 클래스인 AuthorizedUrl 클래스의 access() 메소드를 통해 제어한다.
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/**").access(this::hasIpAddress)
                        .anyRequest()
                        .authenticated())

                // h2 DB 사용을 위한 헤더 옵션 설정
                .headers((headers) -> headers.frameOptions(FrameOptionsConfig::sameOrigin))

                // 선언한 필터 등록
                .addFilter(getAuthenticationFilter(authenticationManager))

                .build();
    }

    /**
     * [ AuthenticationManager ]
     * - AuthenticationConfiguration를 통해 Bean 등록
     * - userDetailService, PasswordEncoder는 Spring Security 6부터 자동 주입
     */
    @Bean
    public AuthenticationManager authenticationManager( AuthenticationConfiguration authenticationConfiguration ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private AuthorizationDecision hasIpAddress(Supplier<Authentication> authentication,
                                               RequestAuthorizationContext object) {
        // Decides whether the rule implemented by the strategy matches the supplied request.
        return new AuthorizationDecision(ALLOWED_IP_ADDRESS_MATCHER.matches(object.getRequest()));
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        return new AuthenticationFilter(authenticationManager, userService, env, jwtUtil);
    }


}
