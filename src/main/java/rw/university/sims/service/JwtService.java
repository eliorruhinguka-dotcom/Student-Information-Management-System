package rw.university.sims.service;

import rw.university.sims.entity.Permission;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long accessTokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.access-token-minutes:15}")
            long accessTokenMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UserAccount user) {
        Instant issuedAt = Instant.now();

        List<String> roles = user.getRoles()
            .stream()
            .map(Role::getName)
            .distinct()
            .sorted()
            .toList();

        List<String> permissions = user.getRoles()
            .stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getCode)
            .distinct()
            .sorted()
            .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("sims-api")
            .subject(user.getId().toString())
            .issuedAt(issuedAt)
            .expiresAt(
                issuedAt.plus(accessTokenMinutes, ChronoUnit.MINUTES)
            )
            .claim("email", user.getEmail())
            .claim("name", user.getFullName())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .claim("mustChangePassword", user.isMustChangePassword())
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .type("JWT")
            .build();

        return jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}