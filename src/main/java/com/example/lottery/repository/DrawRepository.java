package com.example.lottery.repository;

import com.example.lottery.model.Draw;
import com.example.lottery.util.DBUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DrawRepository extends BaseRepository {
    private final DataSource dataSource;

    public DrawRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Draw save(Draw draw) throws SQLException {
        String sql = "INSERT INTO draws (name, ticket_price, start_time, end_time, status, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, draw.getName());
                stmt.setBigDecimal(2, draw.getTicketPrice());
                stmt.setTimestamp(3, DBUtil.getTimestamp(draw.getStartTime()));
                stmt.setTimestamp(4, DBUtil.getTimestamp(draw.getEndTime()));
                stmt.setString(5, draw.getStatus());
                stmt.setLong(6, draw.getCreatedBy());

                stmt.executeUpdate();

                Long id = getGeneratedKey(stmt);
                draw.setId(id);

                conn.commit();
                return draw;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Draw findById(Long id) throws SQLException {
        String sql = "SELECT id, name, ticket_price, start_time, end_time, " +
                "status, winning_combination, created_by, created_at " +
                "FROM draws WHERE id = ?";

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



    public List<Draw> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM draws WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Draw> draws = new ArrayList<>();
                while (rs.next()) {
                    draws.add(mapRow(rs));
                }
                return draws;
            }
        }
    }

    public List<Draw> findAll() throws SQLException {
        String sql = "SELECT * FROM draws ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Draw> draws = new ArrayList<>();

            while (rs.next()) {
                draws.add(mapRow(rs));
            }

            return draws;
        }
    }



    public boolean updateStatus(Long id, String status) throws SQLException {
        String sql = "UPDATE draws SET status = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setLong(2, id);

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

    public boolean completeDraw(Long id, String combination) throws SQLException {
        String sql = "UPDATE draws SET winning_combination = ?, end_time = NOW() WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, combination);
                stmt.setLong(2, id);

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

    private Draw mapRow(ResultSet rs) throws SQLException {
        Draw draw = new Draw();

        draw.setId(rs.getLong("id"));
        draw.setName(rs.getString("name"));
        draw.setTicketPrice(rs.getBigDecimal("ticket_price"));
        draw.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        draw.setEndTime(DBUtil.getLocalDateTime(rs.getTimestamp("end_time")));
        draw.setStatus(rs.getString("status"));
        draw.setWinningCombination(rs.getString("winning_combination"));
        draw.setCreatedBy(rs.getLong("created_by"));
        draw.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return draw;
    }
}