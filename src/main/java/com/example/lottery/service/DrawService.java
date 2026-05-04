package com.example.lottery.service;

import com.example.lottery.dto.DrawWithTickets;
import com.example.lottery.exception.ServiceException;
import com.example.lottery.model.Draw;
import com.example.lottery.model.Ticket;
import com.example.lottery.repository.DrawRepository;
import com.example.lottery.repository.TicketRepository;
import com.example.lottery.exception.ServiceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DrawService {
    private final DrawRepository drawRepository;
    private final TicketRepository ticketRepository;
    private final DataSource dataSource;
    private final Random random = new Random();

    public DrawService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.drawRepository = new DrawRepository(dataSource);
        this.ticketRepository = new TicketRepository(dataSource);
    }

    public Draw createDraw(Draw draw, Long adminId) throws ServiceException {
        draw.setStartTime(LocalDateTime.now());
        try {
            draw.setCreatedBy(adminId);
            draw.setStatus("CREATED");

            Draw saved = drawRepository.save(draw);

            for (int i = 1; i <= 30; i++) {
                Ticket t = new Ticket();
                t.setDrawId(saved.getId());
                t.setTicketNumber("T-" + i);
                t.setCombination(generateCombination());
                t.setStatus("AVAILABLE");

                ticketRepository.save(t);
            }

            return saved;
        } catch (SQLException e) {
            throw new ServiceException("Failed to create draw", e);
        }
    }

    public void completeDraw(Long drawId) throws ServiceException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Draw draw = drawRepository.findById(drawId);
                if (draw == null) throw new ServiceException("Draw not found");
                if ("COMPLETED".equals(draw.getStatus())) {
                    throw new ServiceException("Draw is already completed!");
                }

                // Генерируем выигрышную комбинацию (5 чисел от 1 до 25)
                Set<Integer> winSet = new HashSet<>();
                while (winSet.size() < 5) {
                    winSet.add(random.nextInt(25) + 1);
                }
                List<Integer> winNumbers = winSet.stream().sorted().collect(Collectors.toList());
                String winCombination = winNumbers.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                drawRepository.completeDraw(drawId, winCombination);

                List<Ticket> tickets = ticketRepository.findAllByDraw(drawId);
                for (Ticket ticket : tickets) {
                    boolean isWin = isWinningTicket(ticket.getCombination(), winNumbers);
                    String newStatus;
                    if (ticket.getStatus().equals("PAID")) {
                        newStatus = isWin ? "WIN" : "LOSE";
                    } else {
                        newStatus = isWin ? "COULD_WIN" : "COULD_LOSE";
                    }
                    ticketRepository.updateStatus(ticket.getId(), newStatus);
                }
                drawRepository.updateStatus(drawId, "COMPLETED");
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Failed to complete draw", e);
        }
    }

    private boolean isWinningTicket(String ticketCombination, List<Integer> winNumbers) {
        if (ticketCombination == null) return false;
        String[] parts = ticketCombination.split(",");
        if (parts.length != 5) return false;
        try {
            Set<Integer> ticketSet = new HashSet<>();
            for (String p : parts) {
                ticketSet.add(Integer.parseInt(p.trim()));
            }
            Set<Integer> winSet = new HashSet<>(winNumbers);
            ticketSet.retainAll(winSet);
            return ticketSet.size() >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<Draw> findCompletedDraws() throws ServiceException {
        try {
            return drawRepository.findByStatus("COMPLETED");
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch completed draws", e);
        }
    }

    public List<DrawWithTickets> findActiveDrawsWithTickets() throws ServiceException {
        try {
            List<Draw> draws = drawRepository.findByStatus("CREATED");

            List<DrawWithTickets> result = new ArrayList<>();

            for (Draw draw : draws) {
                List<Ticket> tickets = ticketRepository.findAvailableByDraw(draw.getId());
                result.add(new DrawWithTickets(draw, tickets));
            }

            return result;
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch draws", e);
        }
    }


    public DrawWithTickets getDrawById(Long id) throws ServiceException {
        try {
            var draw = drawRepository.findById(id);
            List<Ticket> tickets = ticketRepository.findAvailableByDraw(draw.getId());
            return new DrawWithTickets(draw, tickets);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch draw", e);
        }
    }

    private String generateCombination() {
        Set<Integer> numbers = new HashSet<>();

        while (numbers.size() < 5) {
            numbers.add(random.nextInt(25) + 1);
        }

        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}