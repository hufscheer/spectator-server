package com.sports.server.command.player.dto;

public class PlayerRequest {
    public record Register(
            String name,
            String studentNumber
    ){}

    public record Update(
            String name,
            String studentNumber
    ){}
}
