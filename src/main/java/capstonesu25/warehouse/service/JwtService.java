package capstonesu25.warehouse.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import capstonesu25.warehouse.enums.TokenType;
import capstonesu25.warehouse.entity.Account;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${spring.application.security.jwt.access_token.secret}")
    private String accessTokenSecret;
    @Value("${spring.application.security.jwt.refresh_token.secret}")
    private String refreshTokenSecret;
    @Value("${spring.application.security.jwt.access_token.expiration}")
    private Long jwtTokenExpiration;
    @Value("${spring.application.security.jwt.refresh_token.expiration}")
    private Long jwtRefreshTokenExpiration;

    public String extractAccountId(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, TokenType tokenType) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(tokenType.equals(TokenType.ACCESS) ? getAccessSecretKey() : getRefreshSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(UserDetails userDetails) {
        Account account = (Account) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "ACCESS");
        claims.put("role", extractRoleFromUserDetails(userDetails));
        claims.put("email", account.getEmail());
        claims.put("full_name", account.getFullName());
        
        return buildToken(
            claims,
            account.getId().toString(),
            jwtTokenExpiration,
            getAccessSecretKey()
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Account account = (Account) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "REFRESH");
        claims.put("email", account.getEmail());
        
        return buildToken(
            claims,
            account.getId().toString(),
            jwtRefreshTokenExpiration,
            getRefreshSecretKey()
        );
    }

    private String extractRoleFromUserDetails(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, Long expiration, Key secretKey) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getAccessSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getRefreshSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getAccessSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        final String email = extractClaim(token, claims -> claims.get("email", String.class), tokenType);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token, tokenType));
    }

    public boolean isTokenExpired(String token, TokenType tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    private Date extractExpiration(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType);
    }
}

