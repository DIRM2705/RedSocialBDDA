/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

import com.redsocial.model.Comment;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class CommentDAO extends GenericDAO<Comment, Long> {

    public CommentDAO() {
        super(Comment.class);
    }

    // Método específico: Obtener comentarios paginados de un Post
    public List<Comment> findCommentsByPostPaginated(EntityManager em, Long postId, int pageNumber, int pageSize) {
        String jpql = "SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt ASC";
        TypedQuery<Comment> query = em.createQuery(jpql, Comment.class);
        query.setParameter("postId", postId);
        
        // Lógica de paginación
        query.setFirstResult((pageNumber - 1) * pageSize); // Offset (desde dónde empezar)
        query.setMaxResults(pageSize); // Limit (cuántos traer)
        
        return query.getResultList();
    }
    public List<Comment> findCommentsByUser(EntityManager em, Long userId) {
        
    String jpql = "SELECT c FROM Comment c JOIN FETCH c.post WHERE c.user.id = :userId ORDER BY c.creationTime DESC";
    return em.createQuery(jpql, Comment.class)
             .setParameter("userId", userId)
             .getResultList();
}
}