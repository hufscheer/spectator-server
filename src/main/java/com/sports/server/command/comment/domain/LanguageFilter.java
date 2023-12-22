package com.sports.server.command.comment.domain;

public interface LanguageFilter {

    boolean containsBadWord(final String content);

}
