package com.example.lottery.model;

import java.time.LocalDateTime;

public class Ticket {
    private Long id;
    private Long drawId;
    private Long userId;
    private String ticketNumber;
    private String combination;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDrawId() { return drawId; }
    public void setDrawId(Long drawId) { this.drawId = drawId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getCombination() { return combination; }
    public void setCombination(String combination) { this.combination = combination; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}