package com.redsocial.dao;

import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.PostCollection;
import com.redsocial.util.JPAUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;


@Repository
public class PostCollectionDAO extends GenericObjectDAO<PostCollection, Long> {

    public PostCollectionDAO() {
        super(PostCollection.class);
    }

    public void createCollection(PostCollection collection) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.persist(collection);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteCollection(long collectionId) throws ResourceNotFoundException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            PostCollection collection = findById(collectionId);

            if (collection == null) {
                throw new ResourceNotFoundException("La colección no existe.");
            }

            em.remove(collection);
            em.getTransaction().commit();
        } catch (ResourceNotFoundException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateCollectionName(long collectionId, String newName) throws ResourceNotFoundException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            PostCollection collection = findById(collectionId);

            if (collection == null) {
                throw new ResourceNotFoundException("La colección no existe.");
            }

            collection.setName(newName);
            em.merge(collection);
            em.getTransaction().commit();
        } catch (ResourceNotFoundException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
