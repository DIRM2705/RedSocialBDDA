/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    /**
     * Agregar un nuevo comentario a un Post existente.
     * 
     * @param postId  ID de la publicación.
     * @param userId  ID del usuario que comenta.
     * @param content Contenido del comentario.
     */
    public void addCommentToPost(long postId, long userId, String content) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            em.getTransaction().begin();

            // 1. Validar y obtener el Post
            Post post = postDao.findById(em, postId);
            if (post == null) {
                throw new IllegalArgumentException("La publicación no existe.");
            }

            // 2. Validar y obtener al Usuario (Autor del comentario)
            User author = userDao.findById(em, userId);
            if (author == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }
            
            // 3. Crear el nuevo comentario
            Comment newComment = new Comment();
            newComment.setContent(content);
            newComment.setUser(author); // Heredado de Editable
            newComment.setPost(post);

            // 4. Sincronizar la relación bidireccional en memoria
            post.getComment().add(newComment);

            // 5. Persistir el comentario en la base de datos
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
}