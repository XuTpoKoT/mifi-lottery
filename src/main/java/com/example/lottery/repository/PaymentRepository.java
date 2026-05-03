package com.example.lottery.repository;

import com.example.lottery.model.Payment;

import javax.sql.DataSource;
import java.sql.*;

public class PaymentRepository extends BaseRepository {
    private final DataSource dataSource;

    public PaymentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Payment save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (ticket_id, amount, status) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, payment.getTicketId());
                stmt.setBigDecimal(2, payment.getAmount());
                stmt.setString(3, payment.getStatus());
                stmt.executeUpdate();
                Long id = getGeneratedKey(stmt);
                payment.setId(id);
                conn.commit();
                return payment;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Payment findByTicketId(Long ticketId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE ticket_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long paymentId, String status, String externalId) throws SQLException {
        String sql = "UPDATE payments SET status = ?, payment_time = ?, external_id = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, externalId);
                stmt.setLong(4, paymentId);
                int rows = stmt.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setTicketId(rs.getLong("ticket_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setStatus(rs.getString("status"));
        Timestamp paymentTime = rs.getTimestamp("payment_time");
        if (paymentTime != null) {
            payment.setPaymentTime(paymentTime.toLocalDateTime());
        }
        payment.setExternalId(rs.getString("external_id"));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return payment;
    }
}