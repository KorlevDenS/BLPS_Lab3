package org.korolev.dens.blps_lab1.services;

import org.korolev.dens.blps_lab1.exceptions.InvalidTokenException;

public interface JwtTokenService {

    public String generateToken(Integer subjectId, String role);

    public Integer verifyToken(String token) throws InvalidTokenException;

}
