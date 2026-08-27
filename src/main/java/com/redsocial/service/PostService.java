package com.redsocial.service;

import com.redsocial.dao.PostDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.model.Post;
import com.redsocial.model.User;
import com.redsocial.model.UserList;
import com.redsocial.util.JPAUtil;
import java.util.List;
import javax.persistence.EntityManager;

public class PostService {
    private final PostDAO postDAO;
    private final UserDAO userDAO; 

    public PostService() {
        this.postDAO = new PostDAO();
        this.userDAO = new UserDAO();
    }

    public void createPost(long authorId, String content) {
        // Límite de 280 caracteres[cite: 2]
        if (content == null || content.trim().isEmpty() || content.length() > 280) {
            throw new IllegalArgumentException("El contenido del post debe tener entre 1 y 280 caracteres.");
        }
        
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            em.getTransaction().begin();

            User author = userDAO.findById(em, authorId);
            if (author == null) {
                throw new IllegalArgumentException("El autor no existe.");
            }

            Post newPost = new Post(content, author);
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

    public void deletePost(long postId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Post post = postDAO.findById(em, postId);
            
            if (post == null) {
                throw new IllegalArgumentException("El post no existe.");
            }

            // Integridad: Remover el post de la lista del autor[cite: 2]
            User author = post.getUser();
            if (author != null) {
                author.getPosts().remove(post);
            }

            // Integridad: Buscar y remover el post de cualquier UserList[cite: 2]
            String jpql = "SELECT l FROM UserList l WHERE :post MEMBER OF l.posts";
            List<UserList> listasQueContienenPost = em.createQuery(jpql, UserList.class)
                                                      .setParameter("post", post)
                                                      .getResultList();
                                                      
            for (UserList lista : listasQueContienenPost) {
                lista.getPosts().remove(post);
            }

            postDAO.delete(em, post);
            
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