package com.sports.server.command.comment.domain;


import org.springframework.data.repository.Repository;

public interface CommentRepository extends Repository<CheerTalk, Long> {
    void save(CheerTalk cheerTalk);
}
