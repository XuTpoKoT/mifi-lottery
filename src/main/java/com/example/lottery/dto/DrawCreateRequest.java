package com.example.lottery.dto;

import java.math.BigDecimal;

public class DrawCreateRequest {
    private String name;
    private BigDecimal ticketPrice;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
}