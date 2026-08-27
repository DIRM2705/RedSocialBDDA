package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.PostDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import com.redsocial.model.User;
import com.redsocial.util.JPAUtil;
import javax.persistence.EntityManager;

public class CommentService {
    private final CommentDAO commentDao;
    private final PostDAO postDao;
    private final UserDAO userDao;

    public CommentService() {
        this.commentDao = new CommentDAO();
        this.postDao = new PostDAO();
        this.userDao = new UserDAO();
    }

    public void addCommentToPost(long postId, long userId, String content) {
        // Límite de 280 caracteres[cite: 2]
        if (content == null || content.trim().isEmpty() || content.length() > 280) {
            throw new IllegalArgumentException("El contenido del post debe tener entre 1 y 280 caracteres.");
        }
        
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            em.getTransaction().begin();

            Post post = postDao.findById(em, postId);
            if (post == null) {
                throw new IllegalArgumentException("La publicación no existe.");
            }

            User author = userDao.findById(em, userId);
            if (author == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }
            
            Comment newComment = new Comment(content, author, post);
            
            // Sincronizar la relación bidireccional en memoria[cite: 2]
            post.getComment().add(newComment);
            
            commentDao.create(em, newComment);
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

    public void deleteComment(long commentId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Comment comment = em.find(Comment.class, commentId);
            if (comment == null) {
                throw new IllegalArgumentException("El comentario no existe.");
            }
            
            // INTEGRIDAD: Lo desvinculamos de la lista del Post en memoria[cite: 2]
            comment.getPost().getComment().remove(comment);
            
            em.remove(comment);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}