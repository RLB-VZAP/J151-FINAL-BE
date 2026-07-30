package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.message.BlockedPhraseDAO;
import com.vzap.trytons.model.message.BlockedPhrase;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.regex.Pattern;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageFilterServiceImpl implements MessageFilterService {

    @Inject
    private BlockedPhraseDAO blockedPhraseDAO;

    @Override
    public Optional<String> firstBlockedPhrase(String body) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }

        for (BlockedPhrase blocked : blockedPhraseDAO.findAll()) {
            String phrase = blocked.getPhrase();
            if (phrase == null || phrase.isBlank()) {
                continue;
            }
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(phrase.trim()) + "\\b",
                    Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(body).find()) {
                return Optional.of(phrase);
            }
        }
        return Optional.empty();
    }
}
