package org.example.springsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        /**
         *  *authorizeHttpRequests 설정*
         *  my 으로 시작하는 URL은 접근 권한 필요
         *  notices, contact는 누구나 접근 가능
         *
         */

        http.authorizeHttpRequests( (requests) ->
                        requests.requestMatchers("/myAccount","/myBalance","/myLoans","/myCards").authenticated()
                                .requestMatchers("/notices","/contact").permitAll() )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return http.build();

        /**
         *  모든 request 거절
         *  Configuration to deny all the requests
         */
        /*http.authorizeHttpRequests(requests -> requests.anyRequest().denyAll())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return http.build();*/

        /**
         * 	모든 request 허락
         *  Configuration to permit all the requests
         */
        /*http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return http.build();*/

    }

}