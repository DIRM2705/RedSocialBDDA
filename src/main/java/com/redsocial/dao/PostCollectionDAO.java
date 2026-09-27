package com.redsocial.dao;
import com.redsocial.model.PostCollection;



public class PostCollectionDAO extends GenericObjectDAO<PostCollection, Long> {

    public PostCollectionDAO() {
        super(PostCollection.class);
    }

    public void createCollection(PostCollection collection) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            em.persist(collection);
            em.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteCollection(long collectionId) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            PostCollection collection = findById(collectionId);

            if (collection == null) {
                throw new IllegalArgumentException("La colección no existe.");
            }

            em.remove(collection);
            em.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateCollectionName(long collectionId, String newName) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            PostCollection collection = findById(collectionId);

            if (collection == null) {
                throw new IllegalArgumentException("La colección no existe.");
            }

            collection.setName(newName);
            em.merge(collection);
            em.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
