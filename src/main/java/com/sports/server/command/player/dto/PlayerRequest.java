package com.sports.server.command.player.dto;

import com.sports.server.command.player.domain.Player;

public class PlayerRequest {
    public record Register(
            String name,
            String studentNumber
    ){
        public Player toEntity(String name, String studentNumber) {
            return new Player(name, studentNumber);
        }
    }

    public record Update(
            String name,
            String studentNumber
    ){}
}
