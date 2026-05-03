package com.example.lottery.controller;

import com.example.lottery.dto.DrawCreateRequest;
import com.example.lottery.dto.DrawResponse;
import com.example.lottery.dto.DrawWithTickets;
import com.example.lottery.dto.TicketShortResponse;
import com.example.lottery.exception.ServiceException;
import com.example.lottery.model.Draw;
import com.example.lottery.model.Ticket;
import com.example.lottery.service.DrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DrawController extends BaseHandler {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        boolean handled = false;

        try {
            if ("GET".equals(method) && "/api/draws".equals(uri)) {
                getDraws(request, response, request.getParameter("status"));
                handled = true;

            } else if ("GET".equals(method) && uri.matches("/api/draws/\\d+")) {
                getDrawById(response, extractId(uri));
                handled = true;

            } else if ("POST".equals(method) && "/api/draws".equals(uri)) {
                createDraw(request, response);
                handled = true;

            } else if ("POST".equals(method) && uri.matches("/api/draws/\\d+/complete")) {
                completeDraw(request, response, extractId(uri));
                handled = true;
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

    private Long extractId(String uri) {
        return Long.parseLong(uri.split("/")[3]);
    }

    private void getDraws(HttpServletRequest request,
                          HttpServletResponse response,
                          String status) throws IOException, ServiceException {

        if ("COMPLETED".equalsIgnoreCase(status)) {
            List<Draw> draws = drawService.findCompletedDraws();

            sendJson(response, 200,
                    draws.stream()
                            .map(d -> drawWithTicketsToResponse(d, List.of()))
                            .toList());

        } else {
            List<DrawWithTickets> draws = drawService.findActiveDrawsWithTickets();

            sendJson(response, 200,
                    draws.stream()
                            .map(d -> drawWithTicketsToResponse(d.getDraw(), d.getTickets()))
                            .toList());
        }
    }

    private void getDrawById(HttpServletResponse response, Long id)
            throws IOException, ServiceException {

        DrawWithTickets draw = drawService.getDrawById(id);

        if (draw == null) {
            sendError(response, 404, "Draw not found");
            return;
        }

        sendJson(response, 200,
                drawWithTicketsToResponse(draw.getDraw(), draw.getTickets()));
    }

    private void createDraw(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServiceException {

        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            sendError(response, 403, "Forbidden");
            return;
        }

        Long userId = (Long) request.getAttribute("userId");

        DrawCreateRequest req = readJson(request, DrawCreateRequest.class);

        Draw draw = new Draw();
        draw.setName(req.getName());
        draw.setTicketPrice(req.getTicketPrice());

        Draw created = drawService.createDraw(draw, userId);

        sendJson(response, 201, drawToResponse(created));
    }

    private void completeDraw(HttpServletRequest request,
                              HttpServletResponse response,
                              Long id) throws IOException, ServiceException {

        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            sendError(response, 403, "Forbidden");
            return;
        }

        drawService.completeDraw(id);

        sendJson(response, 200, Map.of("message", "Draw completed"));
    }

    private DrawResponse drawToResponse(Draw draw) {
        DrawResponse resp = new DrawResponse();
        resp.setId(draw.getId());
        resp.setName(draw.getName());
        resp.setTicketPrice(draw.getTicketPrice());
        resp.setStartTime(draw.getStartTime());
        resp.setEndTime(draw.getEndTime());
        resp.setStatus(draw.getStatus());
        resp.setWinningCombination(draw.getWinningCombination());
        return resp;
    }

    private DrawResponse drawWithTicketsToResponse(Draw draw, List<Ticket> tickets) {
        DrawResponse resp = drawToResponse(draw);

        if ("CREATED".equals(draw.getStatus())) {
            resp.setAvailableTickets(
                    tickets.stream().map(t -> {
                        TicketShortResponse dto = new TicketShortResponse();
                        dto.setId(t.getId());
                        dto.setTicketNumber(t.getTicketNumber());
                        return dto;
                    }).toList()
            );
        }

        return resp;
    }
}