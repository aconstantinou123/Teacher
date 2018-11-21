package com.myteacher.Teacher.com.security;

import com.auth0.jwt.JWT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myteacher.Teacher.Models.Teacher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.myteacher.Teacher.com.security.SecurityConstants.EXPIRATION_TIME;
import static com.myteacher.Teacher.com.security.SecurityConstants.HEADER_STRING;
import static com.myteacher.Teacher.com.security.SecurityConstants.TOKEN_PREFIX;
import static com.myteacher.Teacher.com.security.SecurityConstants.SECRET;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
                Teacher creds = new ObjectMapper()
                    .readValue(req.getInputStream(),Teacher.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
//        String[] roles = new String[1];
//        roles[0] = "TEACHER";
        String token = JWT.create()
                .withClaim("role", "TEACHER")
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));
        Cookie cookie = new Cookie("userToken", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }
}
