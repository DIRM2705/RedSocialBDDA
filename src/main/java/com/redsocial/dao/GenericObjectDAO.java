package com.redsocial.dao;

import com.redsocial.util.JPAUtil;

import javax.persistence.EntityManager;
import java.util.List;

public class GenericObjectDAO<T, ID> {
    private final Class<T> entityClass;

    public GenericObjectDAO(Class<T> entityClass)
    {
        this.entityClass = entityClass;
    }
    public T findById(ID id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        T entity = em.find(entityClass, id);
        em.getTransaction().commit();
        em.close();
        return entity;
    }

    public List<T> findAll()
    {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        em.getTransaction().begin();
        List<T> entities = em.createQuery(jpql, entityClass).getResultList();
        em.getTransaction().commit();
        em.close();
        return entities;
    }
}