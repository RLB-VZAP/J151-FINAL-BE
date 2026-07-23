package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.BlockedPhraseDTO;

import java.util.List;
import java.util.UUID;

public interface BlockedPhraseService {

    List<BlockedPhraseDTO> list();

    BlockedPhraseDTO add(UUID adminUserId, String phrase);

    void remove(UUID blocklistId);
}
