package com.example.lottery.dto;

import com.example.lottery.model.Draw;
import com.example.lottery.model.Ticket;

import java.util.List;

public class DrawWithTickets {
    private Draw draw;
    private List<Ticket> tickets;

    public DrawWithTickets(Draw draw, List<Ticket> tickets) {
        this.draw = draw;
        this.tickets = tickets;
    }

    public Draw getDraw() { return draw; }
    public List<Ticket> getTickets() { return tickets; }
}