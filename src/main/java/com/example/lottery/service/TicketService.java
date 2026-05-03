package com.example.lottery.service;

import com.example.lottery.exception.ServiceException;
import com.example.lottery.model.Draw;
import com.example.lottery.model.Payment;
import com.example.lottery.model.Ticket;
import com.example.lottery.repository.DrawRepository;
import com.example.lottery.repository.PaymentRepository;
import com.example.lottery.repository.TicketRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final DrawRepository drawRepository;
    private final DataSource dataSource;

    public TicketService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.ticketRepository = new TicketRepository(dataSource);
        this.paymentRepository = new PaymentRepository(dataSource);
        this.drawRepository = new DrawRepository(dataSource);
    }

    public Ticket reserveTicket(Long ticketId, Long userId) throws ServiceException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Ticket ticket = ticketRepository.findById(ticketId);
                if (ticket == null) throw new ServiceException("Ticket not found");
                if (!"AVAILABLE".equals(ticket.getStatus())) {
                    throw new ServiceException("Ticket not available");
                }

                ticket.setUserId(userId);
                ticket.setStatus("RESERVED");
                ticketRepository.save(ticket);
                conn.commit();

                return ticket;

            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Reserve failed", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new ServiceException("DB error", e);
        }
    }

    public Ticket payTicket(Long ticketId, Long userId, boolean success)
            throws ServiceException {

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Ticket ticket = ticketRepository.findById(ticketId);

                if (ticket == null) throw new ServiceException("Ticket not found");

                if (!ticket.getUserId().equals(userId)) {
                    throw new ServiceException("Access denied");
                }

                if (!"RESERVED".equals(ticket.getStatus())) {
                    throw new ServiceException("Ticket not reserved");
                }

                Draw draw = drawRepository.findById(ticket.getDrawId());
                if (draw == null) throw new ServiceException("Draw not found");

                // создаём платёж
                Payment payment = new Payment();
                payment.setTicketId(ticketId);
                payment.setAmount(draw.getTicketPrice());
                payment.setStatus(success ? "SUCCESS" : "FAILED");

                paymentRepository.save(payment);

                // обновляем билет
                ticketRepository.updateStatus(ticketId,
                        success ? "PAID" : "CANCELLED"
                );

                conn.commit();

                return ticketRepository.findById(ticketId);

            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Payment failed", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new ServiceException("DB error", e);
        }
    }

    public List<Ticket> getUserTickets(Long userId) throws ServiceException {
        try {
            return ticketRepository.findAllByUserId(userId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get tickets", e);
        }
    }

    public Ticket getTicketResult(Long ticketId, Long userId) throws ServiceException {
        try {
            Ticket ticket = ticketRepository.findById(ticketId);

            if (ticket == null) throw new ServiceException("Ticket not found");

            if (!ticket.getUserId().equals(userId)) {
                throw new ServiceException("Access denied");
            }

            return ticket;

        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch ticket", e);
        }
    }
}