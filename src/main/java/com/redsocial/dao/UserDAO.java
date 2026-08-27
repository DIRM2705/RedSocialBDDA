package com.redsocial.dao;

import com.redsocial.model.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class UserDAO extends GenericDAO<User, Long> {

    public UserDAO() {
        // Le indicamos a la superclase que trabajará con la entidad User
        super(User.class);
    }

    /**
     * Busca un usuario por su correo electrónico.
     * Retorna null si no existe.
     * @param em
     * @param email
     * @return 
     */
    public User findByEmail(EntityManager em, String email) {
        try {
            String jpql = "SELECT u FROM User u WHERE u.email = :email";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            // ObjectDB lanza esta excepción si la consulta no devuelve ningún resultado
            return null; 
        }
    }
    public List<User> searchUsers(EntityManager em, String keyword) {
    // Usa LIKE para autocompletar nombres. Equivalente a un LIKE en T-SQL.
    String jpql = "SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(:keyword)";
    return em.createQuery(jpql, User.class)
             .setParameter("keyword", "%" + keyword + "%")
             .getResultList();
}
    public List<User> findFollowers(EntityManager em, Long userId) {
    // Navega por la colección followed_by usando un JOIN implícito
    String jpql = "SELECT f FROM User u JOIN u.followed_by f WHERE u.id = :userId";
    return em.createQuery(jpql, User.class)
             .setParameter("userId", userId)
             .getResultList();
}
}