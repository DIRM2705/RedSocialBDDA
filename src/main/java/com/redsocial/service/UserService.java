package com.redsocial.service;

import com.redsocial.dao.UserDAO;
import com.redsocial.dao.CommentDAO;
import com.redsocial.model.Comment;
import com.redsocial.model.UserList;
import com.redsocial.model.User;
import com.redsocial.util.JPAUtil;
import java.util.List;
import javax.persistence.EntityManager;

public class UserService {
    private final UserDAO userDAO;
    private final CommentDAO commentDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
    }

    public void registerUser(String name, String email, String password) {
        // 1. Validar correo electrónico (evita vacíos alrededor del punto)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Formato de correo inválido. Debe contener un dominio válido (ej. usuario@dominio.com).");
        }

        // 2. Validar contraseña (Mínimo 8 caracteres, 1 número, 1 mayúscula)
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, un número y una mayúscula.");
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            em.getTransaction().begin();

            User existingUser = userDAO.findByEmail(em, email);
            if (existingUser != null) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado.");
            }

            User newUser = new User(name, email, password);
            
            UserList likedList = new UserList("Favoritos");
            newUser.setLiked(likedList);

            userDAO.create(em, newUser);
            em.getTransaction().commit();
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; 
        } finally {
            em.close();
        }
    }
    
    public void followUser(long currentUserId, long targetUserId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            User currentUser = userDAO.findById(em, currentUserId);
            User targetUser = userDAO.findById(em, targetUserId);

            if (currentUser == null || targetUser == null) {
                throw new IllegalArgumentException("Uno o ambos usuarios no existen.");
            }

            if (targetUser.getBlocked().contains(currentUser) || currentUser.getBlocked().contains(targetUser)) {
                throw new IllegalStateException("Acción denegada debido a un bloqueo.");
            }

            targetUser.notifyFollow(currentUser);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public void deleteUser(long userId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = userDAO.findById(em, userId);
            
            if (user == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }

            // 3. USO CORRECTO DEL PATRÓN DAO: Delegamos la consulta a CommentDAO
            List<Comment> userComments = commentDAO.findCommentsByUser(em, userId);
                                           
            for (Comment c : userComments) {
                // Desvincular de la memoria
                if (c.getPost() != null) {
                    c.getPost().getComment().remove(c);
                }
                // Delegamos la eliminación física al DAO genérico
                commentDAO.delete(em, c); 
            }

            // Integridad de seguidores/seguidos
            for (User followed : user.getFollowed_by()) {
                followed.getFollowers().remove(user);
            }
            
            for (User follower : user.getFollowers()) {
                follower.getFollowed_by().remove(user);
            }

            userDAO.delete(em, user);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}