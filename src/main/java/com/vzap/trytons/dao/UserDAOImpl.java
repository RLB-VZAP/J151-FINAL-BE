package com.vzap.trytons.dao;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.User;
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
public class UserDAOImpl extends BaseDAO implements UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());

    //Override the interface's methods:

    //Get user by ID:
    @Override
    public Optional<User> getUserById(UUID userId){
        String query = "SELECT * FROM user WHERE userId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    User user = User.builder()
                            .userId(userId)
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .build();

                    return Optional.of(user);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find user by ID.", e);
        }
        return Optional.empty();
    }

    //Get user by email:
    @Override
    public Optional<User> getUserByEmail(String email) {
        String  query = "SELECT * FROM user WHERE email = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, email);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    User user = User.builder()
                            .userId(UUID.fromString(rs.getString("userId")))
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .build();

                    return Optional.of(user);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find user by email.", e);
        }
        return Optional.empty();
    }

    //Get user by username:
    @Override
    public Optional<User> getUserByUsername(String username) {
        String query = "SELECT * FROM user WHERE username = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    User user = User.builder()
                            .userId(UUID.fromString(rs.getString("userId")))
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .build();

                    return Optional.of(user);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find user by username.", e);
        }
        return Optional.empty();
    }

    //Register/create user:
    @Override
    public Optional<User> registerUser(User newUser) {
        String query = "INSERT INTO user (userId, email, passwordHash, username, displayName, role, isActive, profilePic) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection con = getConnection();PreparedStatement ps = con.prepareStatement(query);) {
            ps.setString(1, newUser.getUserId().toString());
            ps.setString(2, newUser.getEmail());
            ps.setString(3, newUser.getPasswordHash());
            ps.setString(4, newUser.getUsername());
            ps.setString(5, newUser.getDisplayName());
            ps.setString(6, newUser.getRole() != null ? newUser.getRole().toString() : UserRole.REGISTERED_USER.toString());
            ps.setBoolean(7, newUser.getIsActive() != null ? newUser.getIsActive() : true);
            ps.setString(8, newUser.getProfilePic());
            if (ps.executeUpdate() > 0){
                return Optional.of(newUser);
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to register user.", e);
        }
        return Optional.empty();
    }

    //Update user:
    @Override
    public Optional<User> updateUser(User user) {
        String query = "UPDATE user SET email = ?, passwordHash = ? WHERE userId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getUserId().toString());

            if (ps.executeUpdate() > 0){
                return Optional.of(user);
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to update user.", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, email);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Error checking if email exists.", e);
        }
        return false;
    }

    @Override
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Error checking if username exists.", e);
        }
        return false;
    }
}
