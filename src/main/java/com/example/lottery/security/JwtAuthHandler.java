package com.example.lottery.security;

import com.example.lottery.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class JwtAuthHandler extends HandlerWrapper {
    private final JwtUtil jwtUtil;

    private final Set<String> publicPostPaths = Set.of(
            "/api/auth/register",
            "/api/auth/login"
    );

    private final List<String> publicPrefixes = List.of(
            "/swagger",
            "/openapi",
            "/favicon",
            "/static",
            "/api/draws"
    );

    public JwtAuthHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicGet(uri, method)) {
            super.handle(target, baseRequest, request, response);
            return;
        }

        if (isPublicPost(uri, method)) {
            super.handle(target, baseRequest, request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing or invalid Authorization header");
            baseRequest.setHandled(true);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);

            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));
            System.out.println(claims.get("role", String.class));

            super.handle(target, baseRequest, request, response);

        } catch (JwtException e) {
            e.printStackTrace();
            sendUnauthorized(response, "Invalid or expired token");
            baseRequest.setHandled(true);
        }
    }

    private boolean isPublicPost(String uri, String method) {
        return "POST".equals(method) && publicPostPaths.contains(uri);
    }

    private boolean isPublicGet(String uri, String method) {
        return publicPrefixes.stream().anyMatch(uri::startsWith) && "GET".equals(method);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}