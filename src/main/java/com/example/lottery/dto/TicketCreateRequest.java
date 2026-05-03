package com.example.lottery.dto;

public class TicketCreateRequest {
    private Long drawId;
    private String combination;

    public Long getDrawId() { return drawId; }
    public void setDrawId(Long drawId) { this.drawId = drawId; }

    public String getCombination() { return combination; }
    public void setCombination(String combination) { this.combination = combination; }
}