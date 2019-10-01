package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.RefreshToken;
import pl.cyfronet.s4e.data.repository.RefreshTokenRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class PersistingJwtTokenStore implements TokenStore {
    private interface JwtTokenStoreExcluded {
        void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication);

        OAuth2RefreshToken readRefreshToken(String tokenValue);

        void removeRefreshToken(OAuth2RefreshToken token);
    }

    @Delegate(excludes = JwtTokenStoreExcluded.class)
    private final JwtTokenStore jwtTokenStore;

    private final ObjectMapper objectMapper;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        var refreshToken = jwtTokenStore.readRefreshToken(tokenValue);
        String jti = extractJtiFromEncodedRefreshToken(tokenValue);
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByJti(jti);
        // If the token with a matching jti isn't found in the db then treat the token as revoked.
        if (optionalRefreshToken.isEmpty()) {
            refreshToken = null;
        }
        return refreshToken;
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        AppUser appUser = ((AppUserDetails) authentication.getPrincipal()).getAppUser();
        String jti = extractJtiFromEncodedRefreshToken(refreshToken.getValue());
        LocalDateTime expiryTimestamp = extractExpirationFromEncodedRefreshToken(refreshToken.getValue());
        refreshTokenRepository.save(RefreshToken.builder()
                .appUser(appUser)
                .jti(jti)
                .expiryTimestamp(expiryTimestamp)
                .build());
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        String jti = extractJtiFromEncodedRefreshToken(token.getValue());
        refreshTokenRepository.deleteByJti(jti);
    }

    private String extractJtiFromEncodedRefreshToken(String tokenValue) {
        try {
            Map map = extractClaimsFromEncodedRefreshToken(tokenValue);
            return (String) map.get(AccessTokenConverter.JTI);
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot read claims from JSON", e);
        }
    }

    private LocalDateTime extractExpirationFromEncodedRefreshToken(String tokenValue) {
        try {
            Map map = extractClaimsFromEncodedRefreshToken(tokenValue);
            val instant = Instant.ofEpochSecond((Integer) map.get(AccessTokenConverter.EXP));
            return LocalDateTime.ofInstant(instant, Constants.ZONE_ID);
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot read claims from JSON", e);
        }
    }

    private Map<String, Object> extractClaimsFromEncodedRefreshToken(String tokenValue) throws IOException {
        Jwt jwt = JwtHelper.decode(tokenValue);
        return objectMapper.readValue(jwt.getClaims(), Map.class);
    }
}
