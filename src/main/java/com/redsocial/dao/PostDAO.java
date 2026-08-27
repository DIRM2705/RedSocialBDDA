/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

/**
 *
 * @author Alan
 */


import com.redsocial.model.Post;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class PostDao extends GenericDAO<Post, Long> {

    public PostDao() {
        super(Post.class);
    }

    // Método específico: Obtener los posts más recientes para el Feed
    public List<Post> findRecentPosts(EntityManager em, int limit) {
        String jpql = "SELECT p FROM Post p ORDER BY p.createdAt DESC";
        TypedQuery<Post> query = em.createQuery(jpql, Post.class);
        query.setMaxResults(limit); // Limita la cantidad de resultados
        return query.getResultList();
    }
}
