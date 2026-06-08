package com.example.ebazaarobackend.config;

import com.example.ebazaarobackend.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getSubject();
        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaimAsString("username");

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(username != null ? username : email);

        return new UsernamePasswordAuthenticationToken(user, jwt, user.getAuthorities());
    }
}
