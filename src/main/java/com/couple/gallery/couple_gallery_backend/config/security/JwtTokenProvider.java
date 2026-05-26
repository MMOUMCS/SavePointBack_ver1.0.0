package com.couple.gallery.couple_gallery_backend.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j // 로깅 사용
@Component
public class JwtTokenProvider {

    private final Key key;
    private static final String AUTHORITIES_KEY = "auth";

    // application.yml 파일에서 jwt.secret 값 받아 사용
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Authentication(인증 정보)을 기반으로 Access Token을 생성합니다.
     */
    public String generateToken(Authentication authentication) {
        // 1. 권한 정보 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        long now = (new Date()).getTime();

        // 2. Access Token 만료 시간 설정 (예: 30분)
        Date accessTokenExpiresIn = new Date(now + 1000 * 60 * 30); // 30분

        // 3. Access Token 생성
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .claim("coupleId", userDetails.getCoupleId())
                .claim("userId", userDetails.getUserId())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 인증 정보를 추출합니다. (요청이 올 때마다 사용)
     */
    public Authentication getAuthentication(String accessToken) {
        // 1. 토큰 복호화 및 클레임 추출
        Claims claims = parseClaims(accessToken);

        // 2. 권한 정보가 없으면 예외 발생
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 3. 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // ⭕ [수정] 토큰의 subject에 들어있는 이메일을 추출합니다.
        String email = claims.getSubject();
        Long coupleId = claims.get("coupleId", Long.class);
        Long userId = claims.get("userId", Long.class);

        com.couple.gallery.couple_gallery_backend.domain.User user =
                new com.couple.gallery.couple_gallery_backend.domain.User();
        user.setId(userId);
        user.setCoupleId(coupleId);
        user.setEmail(email); // ⭕ [수정] 빈 유저 객체에 이메일까지 확실하게 채워넣어 줍니다!

        // 4. UserDetails 객체를 생성하여 Authentication 리턴
        UserDetailsImpl principal = new UserDetailsImpl(user);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰의 유효성 검증을 수행합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * Access Token에서 Claims(정보 조각)만 파싱하는 내부 메서드
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 Claims는 가져올 수 있습니다.
            return e.getClaims();
        }
    }
}
