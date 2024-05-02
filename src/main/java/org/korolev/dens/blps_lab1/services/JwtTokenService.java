package org.korolev.dens.blps_lab1.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenService {

    String generateToken(String userName);

    Boolean verifyToken(String token, UserDetails userDetails);

    String extractUserName(String token);

}
