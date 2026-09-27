package com.redsocial.dao;

import com.redsocial.model.User;
import org.xmldb.api.base.XMLDBException;

import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class UserDAO extends GenericObjectDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    public void createUser(User user) throws IllegalArgumentException{
        String email = user.getEmail();
        String name = user.getUsername();

        try {
            em.getTransaction().begin();

            User existingUser = findByEmail(email);
            if (existingUser != null) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado.");
            }

            if (findByName(name) != null) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
            }

            em.persist(user);
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

    public void deleteUser(long userId)  throws XMLDBException, IllegalArgumentException {
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }

            CommentDAO commentDAO = new CommentDAO();
            commentDAO.removeAllCommentsFromUser(userId);

            // Integridad de seguidores/seguidos
            for (User followed : user.getFollowed_by()) {
                followed.getFollowers().remove(user);
            }

            for (User follower : user.getFollowers()) {
                follower.getFollowed_by().remove(user);
            }

            em.remove(em.contains(user) ? user : em.merge(user));

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateUserName(long userId, String newName) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }

            if (findByName(newName) != null) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
            }

            //Validar nombre (no vacío y sin espacios)
            newName = newName.replace(" ", "_");
            if (newName.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío.");
            }

            user.setUsername(newName);
            em.merge(user);
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

    public void updateUserEmail(long userId, String newEmail) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }

            if (findByEmail(newEmail) != null) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado.");
            }

            // Validar formato de correo electrónico
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")) {
                throw new IllegalArgumentException("Formato de correo inválido. Debe contener un dominio válido (ej. usuario.dominio.com).");
            }

            user.setEmail(newEmail);
            em.merge(user);
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

    public void updateUserPassword(long userId, String newPassword) throws IllegalArgumentException {
        try {
            em.getTransaction().begin();
            User user = findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }

            // Validar contraseña (Mínimo 8 caracteres, 1 número, 1 mayúscula)
            if (!newPassword.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, un número y una mayúscula.");
            }

            user.setPassword(newPassword);
            em.merge(user);
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

    /**
     * Busca un usuario por su correo electrónico.
     * Retorna null si no existe.
     *
     * @param email
     * @return
     */
    public User findByEmail(String email) {
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

    public User findByName(String name) {
        try {
            String jpql = "SELECT u FROM User u WHERE u.name = :name";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> searchUsers(String keyword) {
        // Usa LIKE para autocompletar nombres. Equivalente a un LIKE en T-SQL.
        String jpql = "SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(:keyword)";
        return em.createQuery(jpql, User.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    public List<User> findFollowers(Long userId) {
        // Navega por la colección followed_by usando un JOIN implícito
        String jpql = "SELECT f FROM User u JOIN u.followed_by f WHERE u.id = :userId";
        return em.createQuery(jpql, User.class)
                .setParameter("userId", userId)
                .getResultList();
    }


}