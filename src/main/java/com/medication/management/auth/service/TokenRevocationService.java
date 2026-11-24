package com.medication.management.auth.service;

import com.medication.management.auth.entity.RevokedToken;
import com.medication.management.auth.jwt.JwtService;
import com.medication.management.auth.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void revokeToken(String token) {

        String tokenHash = jwtService.hashToken(token);

        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        Date expiration = jwtService.extractExpiration(token);

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setTokenHash(tokenHash);
        revokedToken.setExpiresAt(expiration.toInstant());

        revokedTokenRepository.save(revokedToken);
    }

    public boolean isRevoked(String token) {

        String tokenHash = jwtService.hashToken(token);

        return revokedTokenRepository.existsByTokenHash(tokenHash);
    }
}