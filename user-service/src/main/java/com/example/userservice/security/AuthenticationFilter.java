package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment environment, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.env = environment;
    }

    /**
     * Performs actual authentication.
     * The implementation should do one of the following:
     * 1. Return a populated authentication token for the authenticated user, indicating successful authentication
     * 2. Return null, indicating that the authentication process is still in progress. Before returning, the implementation should perform any additional work required to complete the process.
     * 3. Throw an AuthenticationException if the authentication process fails
     *
     * @param request - from which to extract parameters and perform the authentication
     * @param response - the response, which may be needed if the implementation has to do a redirect as part of a multi-stage authentication process (such as OIDC).
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // Login 요청의 경우 POST로 전달된다.
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            // 실제 인증정보로 만들고, UsernamePasswordAuthenticationFilter(AuthenticationManager)로 전달해야한다.
            // 따라서 UsernamePasswordAuthenticationToken으로 변환할 필요가 있다
            // 사용자가 전달한 요청인 email, password 값을 SpringSecurity에서 사용할 수 있는 형태의 값으로 변환

            // Doing (1), (2)

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));
            /**
             * new UsernamePasswordAuthenticationToken
             * This constructor should only be used by AuthenticationManager or AuthenticationProvider implementations
             * that are satisfied with producing a trusted (i.e. AbstractAuthenticationToken.isAuthenticated() = true) authentication token.
             */

            /**
             * getAuthenticationManager()
             * AbstractAuthenticationProcessingFilter의 메소드인 getAuthenticationManager()
             *
             * getAuthenticationManager()는 인터페이스인 AuthenticationManager를 반환한다.
             * AuthenticationManager의 authenticate() 메소드는 Authentication 인터페이스를 구현한 클래스의 객체를 매개변수로 받는다.
             * UsernamePasswordAuthenticationToken -> AbstractAuthenticationToken -> Authentication의 관계가 된다.
             */

            // 리스트는 권한 관련

        } catch (IOException e) {
            // Doing (3)
            throw new RuntimeException(e);
        }
    }

    /**
     * Default behaviour for successful authentication.
     * 1. Sets the successful Authentication object on the SecurityContextHolder
     * 2. Informs the configured RememberMeServices of the successful login
     * 3. Fires an InteractiveAuthenticationSuccessEvent via the configured ApplicationEventPublisher
     * 4. Delegates additional behaviour to the AuthenticationSuccessHandler.
     * Subclasses can override this method to continue the FilterChain after successful authentication.
     *
     * @param req
     * @param res
     * @param chain
     * @param auth
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
//        log.info("Auth Result userName: {}", ((User)auth.getPrincipal()).getUsername());
        String userName = ((User) auth.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserDetailsByEmail(userName);
        log.info("userDto: {}", userDto);

        String token = jwtUtil.issueAccessToken(userDto.getUserId());

        res.addHeader("token", token);
        res.addHeader("userId", userDto.getUserId());

    }
}
