package com.example.lottery.controller;

import com.example.lottery.dto.AuthRequest;
import com.example.lottery.dto.AuthResponse;
import com.example.lottery.model.User;
import com.example.lottery.service.AuthService;
import com.example.lottery.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.Map;

public class AuthController extends BaseHandler {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        boolean handled = false;
        try {
            if ("POST".equals(method)) {
                if ("/api/auth/register".equals(uri)) {
                    register(request, response);
                    handled = true;
                } else if ("/api/auth/login".equals(uri)) {
                    login(request, response);
                    handled = true;
                }
            }
        } catch (ServiceException e) {
            sendError(response, 400, e.getMessage());
            handled = true;
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Internal server error");
            handled = true;
        }
        if (handled) {
            baseRequest.setHandled(true);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServiceException {
        AuthRequest req = readJson(request, AuthRequest.class);
        User user = authService.register(req.getUsername(), req.getPassword());
        sendJson(response, 201, Map.of("id", user.getId(), "username", user.getUsername()));
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServiceException {
        AuthRequest req = readJson(request, AuthRequest.class);
        String token = authService.login(req.getUsername(), req.getPassword());
        sendJson(response, 200, new AuthResponse(token));
    }
}