package com.webo.warehouse.sso;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;

public class JwtUtil {
	
    public static String generateToken(String signingKey, String subject) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, signingKey);

        String token = builder.compact();

        LoggedState.INSTANCE.add(subject);

        return token;
    }

    public static String parseToken(HttpServletRequest httpServletRequest, String jwtTokenCookieName, String signingKey){
        String token = CookieUtil.getValue(httpServletRequest, jwtTokenCookieName);
        if(token == null) {
            return null;
        }

        String subject = Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token).getBody().getSubject();
        if (!LoggedState.INSTANCE.isMember(subject)) {
            return null;
        }

        return subject;
    }

    public static void invalidateRelatedTokens(HttpServletRequest httpServletRequest) {
    	LoggedState.INSTANCE.remove((String) httpServletRequest.getAttribute("username"));
    }
}
