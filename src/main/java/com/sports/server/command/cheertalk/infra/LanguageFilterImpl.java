package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.domain.LanguageFilter;
import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.stereotype.Component;

@Component
public class LanguageFilterImpl implements LanguageFilter {

    private final BadWordFiltering badWordFiltering;

    public LanguageFilterImpl() {
        this.badWordFiltering = new BadWordFiltering();
    }

    @Override
    public boolean containsBadWord(final String content) {
        return badWordFiltering.blankCheck(content);
    }
}
