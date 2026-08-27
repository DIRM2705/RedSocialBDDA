package com.redsocial.dao;

import com.redsocial.model.User;
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
}