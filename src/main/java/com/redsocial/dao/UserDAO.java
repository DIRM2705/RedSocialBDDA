package com.redsocial.dao;

import com.redsocial.exception.ConflictException;
import com.redsocial.model.PostCollection;
import com.redsocial.model.User;
import com.redsocial.util.JPAUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Repository
public class UserDAO extends GenericObjectDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    public void createUser(User user) throws ConflictException {
        String email = user.getEmail();
        String name = user.getUsername();
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            em.getTransaction().begin();

            User existingUser = findByEmail(email);
            if (existingUser != null) {
                throw new ConflictException("El correo electrónico ya está registrado.");
            }

            if (findByName(name) != null) {
                throw new ConflictException("El nombre de usuario ya está en uso.");
            }

            em.persist(user);
            em.getTransaction().commit();

        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteUser(long userId)  throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            // Integridad de seguidores/seguidos
            for (User followed : user.getFollowed_by()) {
                followed.getFollowers().remove(user);
            }

            for (User follower : user.getFollowers()) {
                follower.getFollowed_by().remove(user);
            }

            em.remove(em.contains(user) ? user : em.merge(user));

            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateUserName(long userId, String newName) throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            if (findByName(newName) != null) {
                throw new ConflictException("El nombre de usuario ya está en uso.");
            }

            //Validar nombre (no vacío y sin espacios)
            newName = newName.replace(" ", "_");

            if (newName.equals(user.getUsername())) {
                throw new ConflictException("El nuevo nombre de usuario es igual al actual.");
            }

            if (newName.trim().isEmpty()) {
                throw new ConflictException("El nombre no puede estar vacío.");
            }

            user.setUsername(newName);
            em.merge(user);
            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateUserEmail(long userId, String newEmail) throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            if (findByEmail(newEmail) != null) {
                throw new ConflictException("El correo electrónico ya está registrado.");
            }

            // Validar formato de correo electrónico
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")) {
                throw new ConflictException("Formato de correo inválido. Debe contener un dominio válido (ej. usuario.dominio.com).");
            }

            user.setEmail(newEmail);
            em.merge(user);
            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateUserPassword(long userId, String newPassword) throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            // Validar contraseña (Mínimo 8 caracteres, 1 número, 1 mayúscula)
            if (!newPassword.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                throw new ConflictException("La contraseña debe tener al menos 8 caracteres, un número y una mayúscula.");
            }

            user.setPassword(newPassword);
            em.merge(user);
            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void createCollection(long userId, String collectionName) throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            // Validar nombre de la colección (no vacío)
            if (collectionName.trim().isEmpty()) {
                throw new ConflictException("El nombre de la colección no puede estar vacío.");
            }

            // Crear y agregar la nueva colección
            PostCollection newCollection = new PostCollection(collectionName);
            user.addList(newCollection);
            em.merge(user);
            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteCollection(long userId, long collectionID) throws ConflictException {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new ConflictException("El usuario no existe.");
            }

            user.removeList(collectionID);
            em.merge(user);
            em.getTransaction().commit();
        } catch (ConflictException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Busca un usuario por su correo electrónico.
     * Retorna null si no existe.
     *
     * @param email
     * @return
     */
    public User findByEmail(String email) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT u FROM User u WHERE u.email = :email";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            // ObjectDB lanza esta excepción si la consulta no devuelve ningún resultado
            return null;
        }
        finally {
            em.close();
        }
    }

    public User findByName(String name) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT u FROM User u WHERE u.username = :name";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        finally {
            em.close();
        }
    }

    public List<User> searchUsers(String keyword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        // Usa LIKE para autocompletar nombres. Equivalente a un LIKE en T-SQL.
        String jpql = "SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(:keyword)";
        List<User> users = em.createQuery(jpql, User.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
        em.close();
        return users;
    }
}