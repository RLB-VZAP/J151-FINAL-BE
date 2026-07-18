package com.vzap.trytons.dao;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vzap.trytons.model.PlayerStatisticsCorrection;
import com.vzap.trytons.util.DBConnectionManager;
import jakarta.ejb.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
@Singleton
public class PlayerStatisticsCorrectionDAOImpl extends BaseDAO implements PlayerStatisticsCorrectionDAO {
    private static final Logger LOG = Logger.getLogger(PlayerStatisticsCorrectionDAOImpl.class.getName());
    private static DBConnectionManager dbConnectionManager ;
    private ObjectMapper objectMapper = new ObjectMapper();

//    private UUID correctionId;
//    private UUID statId;
//    private UUID ;
//    private String reason;
//    private HashMap<String , Object > oldValueJason;
//    private HashMap<String , Object > newValueJason;
//    private LocalDateTime correctionTime;
    private static final String SELECT_COLUMNS = """
        SELECT correctionId, statId, correctionByAdminUserId, reason, oldValueJason, newValueJason, correctionTime FROM playerStatistics
        """;

    private PlayerStatisticsCorrection mapRow(ResultSet rs) throws SQLException {
        Timestamp correctionTimestamp = rs.getTimestamp("correctionTime");
        return PlayerStatisticsCorrection.builder().correctionId(UUID.fromString(rs.getString("correctionId"))).statId(UUID.fromString(rs.getString("statId"))).correctionByAdminUserId(UUID.fromString(rs.getString("correctionByAdminUserId"))).reason(rs.getString("reason")).oldValueJason(parseJsonToMap(rs.getString("oldValueJason"))).newValueJason(parseJsonToMap(rs.getString("newValueJason"))).correctionTime(correctionTimestamp == null ? null : correctionTimestamp.toLocalDateTime()).build();
    }

    @Override
    public Optional<PlayerStatisticsCorrection> save(PlayerStatisticsCorrection playerStatisticsCorrection) {
        String query ="INSERT INTO player_statistics_correction (correctionId ,statId,correctionByAdminUserId,reason,oldValueJason,newValueJason,correctionTime) VALUES (?,?,?,?,?,?,?)";
        try(Connection con = DBConnectionManager.getConnection(); PreparedStatement ps = con.prepareStatement(query) ){
            ps.setString(1, playerStatisticsCorrection.getCorrectionId().toString());
            ps.setString(2, playerStatisticsCorrection.getStatId().toString());
            ps.setString(3, playerStatisticsCorrection.getCorrectionByAdminUserId().toString());
            ps.setString(4, playerStatisticsCorrection.getReason());
            ps.setObject(5, playerStatisticsCorrection.getOldValueJason());
            ps.setObject(6, playerStatisticsCorrection.getNewValueJason());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            return Optional.of(playerStatisticsCorrection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<PlayerStatisticsCorrection> findById(UUID correctionId) {
        String query ="SELECT * FROM player_statistics_correction WHERE correctionId = ?";
        try(Connection con = DBConnectionManager.getConnection(); PreparedStatement ps = con.prepareStatement(query) ){
            ps.setString(1, correctionId.toString());
            try(ResultSet rs =  ps.executeQuery()){
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }catch (SQLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<PlayerStatisticsCorrection>> findByAdminUserId(UUID correctionByAdminUserId) {
        String query = "SELECT correctionId, statId, correctionByAdminUserId, reason, oldValueJason, newValueJason, correctionTime FROM playerStatistics WHERE correctionByAdminUserId = ?";
        try (Connection con = DBConnectionManager.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, correctionByAdminUserId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<PlayerStatisticsCorrection> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list.isEmpty() ? Optional.empty() : Optional.of(list);
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

        @Override
    public Optional<PlayerStatisticsCorrection> findStatId(UUID StatId) {
            String query = "SELECT * FROM player_statistics_correction WHERE statId = ?";
            try (Connection con = DBConnectionManager.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, StatId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(mapRow(rs));
                }
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
    }
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON column to Map", e);
        }
    }
}
