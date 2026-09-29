package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.dao.UserProfileDAO;
import com.redsocial.exception.ConflictException;
import com.redsocial.exception.DBException;
import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.User;
import org.springframework.stereotype.Service;
import org.xmldb.api.base.XMLDBException;

@Service
public class UserService {
    private final UserDAO userDAO;
    private final UserProfileDAO userProfileDAO;
    private final CommentDAO commentDAO;

    public UserService(UserDAO userDAO, UserProfileDAO userProfileDAO, CommentDAO commentDAO) {
        this.userDAO = userDAO;
        this.userProfileDAO = userProfileDAO;
        this.commentDAO = commentDAO;
    }

    public boolean authenticateUser(String username, String password) {
        User user = userDAO.findByName(username);
        return user != null && user.getPassword().equals(password);
    }

    public void registerUser(String name, String email, String password, String profilePictureUrl, String bio) throws DBException, ConflictException {
        User newUser = new User(name, email, password);
        userDAO.createUser(newUser);
        userProfileDAO.createUserProfile(newUser, profilePictureUrl, bio);
    }

    public void deleteUser(long userId) throws DBException, ConflictException {
        userDAO.deleteUser(userId);
        commentDAO.removeAllCommentsFromUser(userId);

    }

    public String getProfile(long userId) throws ResourceNotFoundException, DBException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        return userProfileDAO.findUserProfileById(userId);
    }

    public void updateUser(long userId, String newUsername, String newEmail, String newPassword) throws ResourceNotFoundException, DBException, ConflictException {
        User user = userDAO.findById(userId);
        if (user == null) throw new ResourceNotFoundException("Usuario no encontrado");
        userDAO.updateUserName(userId, newUsername);
        userDAO.updateUserEmail(userId, newEmail);
        userDAO.updateUserPassword(userId, newPassword);
    }



    public void updateUserProfile(long userId, String newProfilePictureUrl, String newBio) throws ResourceNotFoundException, DBException {
        User user = userDAO.findById(userId);
        if (user != null) {
            userProfileDAO.changeProfilePicture(userId, newProfilePictureUrl);
            userProfileDAO.changeBio(userId, newBio);
        } else {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
    }

    public void addFollower(long userId, long followerId) throws ResourceNotFoundException, DBException, ConflictException {
        if (userId == followerId) {
            throw new ConflictException("No puedes seguirte a ti mismo");
        }

        User user = userDAO.findById(userId);
        User follower = userDAO.findById(followerId);

        if (user == null || follower == null) {
            throw new ResourceNotFoundException("Usuario o seguidor no encontrado");
        }

        //TODO: Implementar lógica para evitar que los usuarios sean seguidos por un usuario que han bloqueado

        userProfileDAO.incrementFollowersCount(userId);
    }

    public void removeFollower(long userId, long followerId) throws ResourceNotFoundException, DBException {
        if (userId == followerId) return;

        User user = userDAO.findById(userId);
        User follower = userDAO.findById(followerId);

        if (user == null || follower == null) {
            throw new ResourceNotFoundException("Usuario o seguidor no encontrado");
        }

        userProfileDAO.decrementFollowersCount(userId);
    }

    public void addFollowing(long userId, long followingId) throws ConflictException, ResourceNotFoundException, DBException {
        if (userId == followingId) {
            throw new ConflictException("No puedes seguirte a ti mismo");
        }

        User user = userDAO.findById(userId);
        User following = userDAO.findById(followingId);

        if (user == null || following == null) {
            throw new ResourceNotFoundException("Usuario o siguiendo no encontrado");
        }

        //TODO: Implementar lógica para evitar que los usuarios sigan a un usuario que han bloqueado

        userProfileDAO.incrementFollowingCount(userId);
    }

    public void removeFollowing(long userId, long followingId) throws  ResourceNotFoundException, DBException {
        if(userId == followingId) return;

        User user = userDAO.findById(userId);
        User following = userDAO.findById(followingId);

        if(user == null || following == null) {
            throw new ResourceNotFoundException("Usuario o siguiendo no encontrado");
        }

        userProfileDAO.decrementFollowingCount(userId);
    }

    public void blockUser(long userId, long blockedUserId) throws ConflictException, ResourceNotFoundException, DBException {
        if (userId == blockedUserId) throw new ConflictException("No puedes bloquearte a ti mismo");

        User user = userDAO.findById(userId);
        User blockedUser = userDAO.findById(blockedUserId);

        if(user == null || blockedUser == null) {
            throw new ResourceNotFoundException("Usuario o usuario bloqueado no encontrado");
        }

        userProfileDAO.blockUser(userId, blockedUserId);
    }

    public void unblockUser(long userId, long blockedUserId) throws ResourceNotFoundException, DBException {
        if (userId == blockedUserId) return;

        User user = userDAO.findById(userId);
        User blockedUser = userDAO.findById(blockedUserId);

        if (user == null || blockedUser == null) {
            throw new ResourceNotFoundException("Usuario o usuario bloqueado no encontrado");
        }

        userProfileDAO.unblockUser(userId, blockedUserId);
    }
}