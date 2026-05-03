package com.example.lottery.dto;

public class TicketShortResponse {
    private Long id;
    private String ticketNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
}