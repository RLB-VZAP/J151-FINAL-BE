package com.vzap.trytons.dao;

import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.model.RegisteredUser;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class RegisteredUserDAOImpl extends BaseDAO implements RegisteredUserDAO {

    private static final Logger LOG = Logger.getLogger(RegisteredUserDAOImpl.class.getName());

    @Override
    public Optional<RegisteredUser> getRegisteredUserById(UUID userId) {
        String query = "SELECT u.*, ru.registrationStatus FROM user u JOIN registeredUser ru ON u.userId = ru.userId WHERE u.userId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    RegisteredUser ru = RegisteredUser.builder()
                            .userId(userId)
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .username(rs.getString("username"))
                            .displayName(rs.getString("displayName"))
                            .isActive(rs.getBoolean("isActive"))
                            .profilePic(rs.getString("profilePic"))
                            .registrationStatus(RegistrationStatus.valueOf(rs.getString("registrationStatus")))
                            .build();

                    return Optional.of(ru);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find user by ID.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredUser> updateProfile(RegisteredUser registeredUser) {
    String query = "UPDATE user SET displayName = ?, profilePic = ? WHERE userId = ?";
    try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
        ps.setString(1, registeredUser.getDisplayName());
        ps.setString(2, registeredUser.getProfilePic());
        ps.setString(3, registeredUser.getUserId().toString());

        if (ps.executeUpdate() > 0){
            return Optional.of(registeredUser);
        }
    }catch(SQLException e){
        LOG.log(Level.SEVERE, "Unable to update user profile.", e);
    }
        return Optional.empty();
    }

    @Override
    public boolean deactivateAccount(UUID userId) {
        String query = "UPDATE user SET isActive = false WHERE userId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, userId.toString());

            if (ps.executeUpdate() > 0){
                return true;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to deactivate account.", e);
        }
        return false;
    }

    @Override
    public Optional<RegisteredUser> register(RegisteredUser newUser) {
        return Optional.empty();
    }
}
