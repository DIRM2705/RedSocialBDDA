/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.service;

import com.redsocial.dao.PostDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.model.Post;
import com.redsocial.model.User;
import com.redsocial.util.JPAUtil;
import javax.persistence.EntityManager;

public class PostService {

    private final PostDAO postDAO;
    private final UserDAO userDAO; //  validar quién publica

    public PostService() {
        this.postDAO = new PostDAO();
        this.userDAO = new UserDAO();
    }

    public void createPost(long authorId, String content) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            em.getTransaction().begin();

            // 1.usuario existe
            User author = userDAO.findById(em, authorId);
            if (author == null) {
                throw new IllegalArgumentException("El autor no existe.");
            }

            // 3. Crear el post y asociarlo
            Post newPost = new Post(content, author);
            
            // 4. Persistir
            postDAO.create(em, newPost);

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