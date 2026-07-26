package com.vzap.trytons.dao.message;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.message.BlockedPhrase;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class BlockedPhraseDAOImpl extends BaseDAO implements BlockedPhraseDAO {

    private static final Logger LOG = Logger.getLogger(BlockedPhraseDAOImpl.class.getName());

    private static BlockedPhrase mapRow(ResultSet rs) {
        try {
            BlockedPhrase phrase = new BlockedPhrase();
            phrase.setBlocklistId(UUID.fromString(rs.getString("blocklistId")));
            phrase.setPhrase(rs.getString("phrase"));
            String createdBy = rs.getString("created_by_user_id");
            phrase.setCreatedByUserId(createdBy != null ? UUID.fromString(createdBy) : null);
            phrase.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            return phrase;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public List<BlockedPhrase> findAll() {
        String query = "SELECT * FROM message_blocklist ORDER BY phrase ASC";

        List<BlockedPhrase> phrases = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                phrases.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to load blocklist", e);
            throw new DataAccessException("Unable to load blocklist", e);
        }
        return phrases;
    }

    @Override
    public BlockedPhrase create(BlockedPhrase phrase) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO message_blocklist (blocklistId, phrase, created_by_user_id) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, phrase.getPhrase());
            ps.setString(3, phrase.getCreatedByUserId() != null ? phrase.getCreatedByUserId().toString() : null);

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException("No row inserted for blocklist phrase");
            }

            phrase.setBlocklistId(newId);
            return phrase;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create blocklist phrase", e);
            throw new DataAccessException("Unable to create blocklist phrase", e);
        }
    }

    @Override
    public boolean deleteById(UUID blocklistId) {
        String query = "DELETE FROM message_blocklist WHERE blocklistId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, blocklistId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to delete blocklist phrase", e);
            throw new DataAccessException("Unable to delete blocklist phrase", e);
        }
    }
}
