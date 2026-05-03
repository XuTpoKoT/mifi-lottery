package com.example.lottery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DrawResponse {
    private Long id;
    private String name;
    private BigDecimal ticketPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String winningCombination;
    private List<TicketShortResponse> availableTickets;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWinningCombination() { return winningCombination; }
    public void setWinningCombination(String winningCombination) { this.winningCombination = winningCombination; }

    public List<TicketShortResponse> getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(List<TicketShortResponse> availableTickets) {
        this.availableTickets = availableTickets;
    }
}