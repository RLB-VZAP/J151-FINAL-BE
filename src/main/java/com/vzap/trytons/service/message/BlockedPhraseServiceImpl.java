package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.message.BlockedPhraseDAO;
import com.vzap.trytons.dto.message.BlockedPhraseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.message.BlockedPhrase;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockedPhraseServiceImpl implements BlockedPhraseService {

    private static final int MAX_PHRASE_LENGTH = 100;

    @Inject
    private BlockedPhraseDAO blockedPhraseDAO;

    @Override
    public List<BlockedPhraseDTO> list() {
        List<BlockedPhraseDTO> response = new ArrayList<>();
        for (BlockedPhrase phrase : blockedPhraseDAO.findAll()) {
            response.add(mapToResponse(phrase));
        }
        return response;
    }

    @Override
    public BlockedPhraseDTO add(UUID adminUserId, String phrase) {
        if (phrase == null || phrase.isBlank()) {
            throw new ValidationException("A phrase is required.");
        }
        String normalized = phrase.trim();
        if (normalized.length() > MAX_PHRASE_LENGTH) {
            throw new ValidationException("A phrase may be at most " + MAX_PHRASE_LENGTH + " characters.");
        }

        boolean exists = blockedPhraseDAO.findAll().stream()
                .anyMatch(existing -> existing.getPhrase() != null
                        && existing.getPhrase().equalsIgnoreCase(normalized));
        if (exists) {
            throw new ConflictException("That phrase is already on the blocklist.");
        }

        BlockedPhrase created = blockedPhraseDAO.create(BlockedPhrase.builder()
                .phrase(normalized)
                .createdByUserId(adminUserId)
                .build());
        return mapToResponse(created);
    }

    @Override
    public void remove(UUID blocklistId) {
        if (blocklistId == null) {
            throw new ValidationException("A blocklist entry is required.");
        }
        blockedPhraseDAO.deleteById(blocklistId);
    }

    private BlockedPhraseDTO mapToResponse(BlockedPhrase phrase) {
        return BlockedPhraseDTO.builder()
                .blocklistId(phrase.getBlocklistId())
                .phrase(phrase.getPhrase())
                .createdAt(phrase.getCreatedAt())
                .build();
    }
}
