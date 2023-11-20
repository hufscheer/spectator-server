package com.sports.server.comment.infra;

import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.stereotype.Component;

@Component
public class BadWordFilterImpl implements com.sports.server.comment.domain.BadWordFilter {

    private final BadWordFiltering badWordFiltering;

    public BadWordFilterImpl() {
        this.badWordFiltering = new BadWordFiltering();
    }

    public boolean containsBadWord(final String content) {
        return badWordFiltering.blankCheck(content);
    }
}
