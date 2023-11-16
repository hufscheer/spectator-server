package com.sports.server.comment.util;

import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.stereotype.Component;

@Component
public class BadWordFilter {

    private final BadWordFiltering badWordFiltering;

    public BadWordFilter() {
        this.badWordFiltering = new BadWordFiltering();
    }

    public boolean containsBadWord(final String content) {
        return badWordFiltering.blankCheck(content);
    }
}
