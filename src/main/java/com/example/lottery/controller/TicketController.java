package com.example.lottery.controller;

import com.example.lottery.dto.PaymentRequest;
import com.example.lottery.dto.TicketResponse;
import com.example.lottery.exception.ServiceException;
import com.example.lottery.model.Ticket;
import com.example.lottery.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TicketController extends BaseHandler {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        boolean handled = false;

        try {
            if ("POST".equals(method) && uri.matches("/api/tickets/\\d+/reserve")) {
                reserveTicket(request, response, extractId(uri));
                handled = true;

            } else if ("POST".equals(method) && uri.matches("/api/tickets/\\d+/pay")) {
                payTicket(request, response, extractId(uri));
                handled = true;

            } else if ("GET".equals(method) && "/api/tickets".equals(uri)) {
                getUserTickets(request, response);
                handled = true;

            } else if ("GET".equals(method) && uri.matches("/api/tickets/\\d+")) {
                getTicket(request, response, extractId(uri));
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

    private void reserveTicket(HttpServletRequest request,
                               HttpServletResponse response,
                               Long ticketId)
            throws IOException, ServiceException {

        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            sendError(response, 401, "Unauthorized");
            return;
        }

        Ticket ticket = ticketService.reserveTicket(ticketId, userId);

        sendJson(response, 200, toResponse(ticket));
    }

    private void payTicket(HttpServletRequest request,
                           HttpServletResponse response,
                           Long ticketId)
            throws IOException, ServiceException {

        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            sendError(response, 401, "Unauthorized");
            return;
        }

        PaymentRequest req = readJson(request, PaymentRequest.class);

        Ticket ticket = ticketService.payTicket(ticketId, userId, req.isSuccess());

        sendJson(response, 200, toResponse(ticket));
    }

    private void getUserTickets(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException, ServiceException {

        Long userId = (Long) request.getAttribute("userId");
        System.out.println(userId);

        List<Ticket> tickets = ticketService.getUserTickets(userId);

        List<TicketResponse> responses = tickets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        sendJson(response, 200, responses);
    }

    private void getTicket(HttpServletRequest request,
                           HttpServletResponse response,
                           Long ticketId)
            throws IOException, ServiceException {

        Long userId = (Long) request.getAttribute("userId");

        Ticket ticket = ticketService.getTicketResult(ticketId, userId);

        sendJson(response, 200, toResponse(ticket));
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse resp = new TicketResponse();
        resp.setId(ticket.getId());
        resp.setDrawId(ticket.getDrawId());
        resp.setTicketNumber(ticket.getTicketNumber());
        resp.setCombination(ticket.getCombination());
        resp.setStatus(ticket.getStatus());
        resp.setCreatedAt(ticket.getCreatedAt());
        return resp;
    }
}