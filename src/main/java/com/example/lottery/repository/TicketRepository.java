package com.example.lottery.repository;

import com.example.lottery.model.Ticket;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository extends BaseRepository {
    private final DataSource dataSource;

    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ticket save(Ticket ticket) throws SQLException {
        String sql = """
            INSERT INTO tickets (draw_id, user_id, ticket_number, combination, status)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (draw_id, ticket_number)
            DO UPDATE SET
                user_id = EXCLUDED.user_id,
                combination = EXCLUDED.combination,
                status = EXCLUDED.status
            RETURNING id
        """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, ticket.getDrawId());
                if (ticket.getUserId() != null) {
                    stmt.setLong(2, ticket.getUserId());
                } else {
                    stmt.setNull(2, java.sql.Types.BIGINT);
                }
                stmt.setString(3, ticket.getTicketNumber());
                stmt.setString(4, ticket.getCombination());
                stmt.setString(5, ticket.getStatus());
                stmt.executeUpdate();
                Long id = getGeneratedKey(stmt);
                ticket.setId(id);
                conn.commit();
                return ticket;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Ticket findById(Long id) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Ticket> findAvailableByDraw(Long drawId) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE draw_id = ? AND status = 'AVAILABLE'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, drawId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        }
    }

    public List<Ticket> findByDrawAndUser(Long drawId, Long userId) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE draw_id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, drawId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        }
    }

    public List<Ticket> findAllByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        }
    }

    public List<Ticket> findAllByDraw(Long drawId) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE draw_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, drawId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        }
    }

    public boolean updateStatus(Long ticketId, String status) throws SQLException {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setLong(2, ticketId);
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

    public int countByDraw(Long drawId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tickets WHERE draw_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, drawId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setDrawId(rs.getLong("draw_id"));
        ticket.setUserId(rs.getLong("user_id"));
        ticket.setTicketNumber(rs.getString("ticket_number"));
        ticket.setCombination(rs.getString("combination"));
        ticket.setStatus(rs.getString("status"));
        ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return ticket;
    }
}