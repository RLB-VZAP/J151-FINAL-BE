package com.vzap.trytons.dao.message;

import com.vzap.trytons.model.message.BlockedPhrase;

import java.util.List;
import java.util.UUID;

public interface BlockedPhraseDAO {

    List<BlockedPhrase> findAll();

    BlockedPhrase create(BlockedPhrase phrase);

    boolean deleteById(UUID blocklistId);
}
