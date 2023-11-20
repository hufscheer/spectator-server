package com.sports.server.comment.domain;

public interface BadWordFilter {

    boolean containsBadWord(final String content);

}
