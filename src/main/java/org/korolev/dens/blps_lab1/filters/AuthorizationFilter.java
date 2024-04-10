package org.korolev.dens.blps_lab1.filters;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.korolev.dens.blps_lab1.exceptions.InvalidTokenException;
import org.korolev.dens.blps_lab1.services.JwtTokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@WebFilter(filterName = "authorizationFilter", urlPatterns = {"/chapter/add", "/topic/add/*", "/topic/update",
                        "/comment/add/*", "/topic/get/rating/*", "/topic/delete/*", "/client/get/notifications"})
@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public AuthorizationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nullable HttpServletResponse response,
                                    @Nullable FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");
        Integer clientId;
        try {
            clientId = jwtTokenService.verifyToken(token);
        } catch (InvalidTokenException | ExpiredJwtException e) {
            assert response != null;
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }
        logger.info("Good user: id: " + clientId);
        request.setAttribute("Cid", clientId);
        assert filterChain != null;
        filterChain.doFilter(request, response);
    }

}