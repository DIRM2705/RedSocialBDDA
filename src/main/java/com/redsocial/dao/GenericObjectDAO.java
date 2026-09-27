package com.redsocial.dao;

import com.redsocial.util.JPAUtil;

import javax.persistence.EntityManager;
import java.util.List;

public class GenericObjectDAO<T, ID> {
    private final Class<T> entityClass;
    protected final EntityManager em;

    public GenericObjectDAO(Class<T> entityClass)
    {
        this.entityClass = entityClass;
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
    }
    public T findById(ID id) {
        em.getTransaction().begin();
        T entity = em.find(entityClass, id);
        em.getTransaction().commit();

        return entity;
    }

    public List<T> findAll(EntityManager em) {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        em.getTransaction().begin();
        List<T> entities = em.createQuery(jpql, entityClass).getResultList();
        em.getTransaction().commit();
        return entities;
    }
}