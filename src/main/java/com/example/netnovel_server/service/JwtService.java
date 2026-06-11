package com.example.netnovel_server.service;

import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
        @Value("${app.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        this.secret = secret;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpirationSeconds, "ACCESS");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpirationSeconds, "REFRESH");
    }

    public Long extractUserId(String token) {
        Object subject = extractClaims(token).get("sub");
        if (subject == null) {
            throw new UnauthorizedException("Invalid token subject");
        }
        return Long.valueOf(subject.toString());
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(extractClaims(token).get("type"));
    }

    public Instant extractExpiration(String token) {
        Object expiresAt = extractClaims(token).get("exp");
        if (expiresAt == null) {
            throw new UnauthorizedException("Invalid token expiration");
        }
        return Instant.ofEpochSecond(Long.parseLong(expiresAt.toString()));
    }

    public Map<String, Object> extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid token");
            }

            String signedContent = parts[0] + "." + parts[1];
            String expectedSignature = sign(signedContent);
            if (!safeEquals(expectedSignature, parts[2])) {
                throw new UnauthorizedException("Invalid token signature");
            }

            String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = parseClaims(payload);

            Object expiresAt = claims.get("exp");
            if (expiresAt == null || Instant.now().isAfter(Instant.ofEpochSecond(Long.parseLong(expiresAt.toString())))) {
                throw new UnauthorizedException("Token expired");
            }

            return claims;
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private String generateToken(User user, long expirationSeconds, String type) {
        try {
            Instant now = Instant.now();
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payload = buildPayload(user, type, now, now.plusSeconds(expirationSeconds));

            String encodedHeader = URL_ENCODER.encodeToString(header.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            String signedContent = encodedHeader + "." + encodedPayload;

            return signedContent + "." + sign(signedContent);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate token", exception);
        }
    }

    private String buildPayload(User user, String type, Instant issuedAt, Instant expiresAt) {
        String roles = user.getRoles() == null
            ? ""
            : user.getRoles().stream()
                .map(Role::name)
                .map(role -> "\"" + escape(role) + "\"")
                .collect(Collectors.joining(","));

        return "{"
            + "\"sub\":" + user.getId() + ","
            + "\"email\":\"" + escape(user.getEmail()) + "\","
            + "\"type\":\"" + escape(type) + "\","
            + "\"iat\":" + issuedAt.getEpochSecond() + ","
            + "\"exp\":" + expiresAt.getEpochSecond() + ","
            + "\"roles\":[" + roles + "]"
            + "}";
    }

    private Map<String, Object> parseClaims(String payload) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", extractLong(payload, "sub"));
        claims.put("iat", extractLong(payload, "iat"));
        claims.put("exp", extractLong(payload, "exp"));
        claims.put("email", extractString(payload, "email"));
        claims.put("type", extractString(payload, "type"));
        return claims;
    }

    private Long extractLong(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.valueOf(matcher.group(1)) : null;
    }

    private String extractString(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : null;
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean safeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
            left.getBytes(StandardCharsets.UTF_8),
            right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
