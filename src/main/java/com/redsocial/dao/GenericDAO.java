package com.redsocial.dao;

import javax.persistence.EntityManager;
import java.util.List;

public abstract class GenericDAO<T, ID> {
    
    private final Class<T> entityClass;

    // El constructor recibe la clase exacta para que JPA sepa qué mapear
    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void create(EntityManager em, T entity) {
        em.persist(entity);
    }

    public T findById(EntityManager em, ID id) {
        return em.find(entityClass, id);
    }

    public void update(EntityManager em, T entity) {
        em.merge(entity);
    }

    public void delete(EntityManager em, T entity) {
        // En JPA, la entidad debe estar administrada (managed) antes de eliminarse
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public List<T> findAll(EntityManager em) {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(jpql, entityClass).getResultList();
    }
}