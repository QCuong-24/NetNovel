package com.example.netnovel_server.service;

import com.example.netnovel_server.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Google id token is required");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(TOKEN_INFO_URL + idToken, Map.class);
            if (response == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            String sub = stringValue(response.get("sub"));
            String email = stringValue(response.get("email"));
            String name = stringValue(response.get("name"));
            String picture = stringValue(response.get("picture"));
            boolean emailVerified = Boolean.parseBoolean(stringValue(response.get("email_verified")));

            if (sub == null || email == null || !emailVerified) {
                throw new UnauthorizedException("Google email is not verified");
            }

            return new GoogleUserInfo(sub, email, name, picture);
        } catch (RestClientException exception) {
            throw new UnauthorizedException("Invalid Google token");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    public record GoogleUserInfo(String providerId, String email, String name, String picture) {
    }
}
