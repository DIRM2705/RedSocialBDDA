package com.redsocial.service;

import com.redsocial.dao.UserDAO;
import com.redsocial.model.UserList;
import com.redsocial.model.User;
import com.redsocial.util.JPAUtil;
import javax.persistence.EntityManager;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Registra un nuevo usuario aplicando las reglas de negocio.
     */
    public void registerUser(String name, String email, String password) {
        // 1. Obtener el gestor de la base de datos a través de nuestro utilitario
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            // 2. Iniciar la transacción
            em.getTransaction().begin();

            // 3. Regla de negocio: El correo no debe estar repetido
            User existingUser = userDAO.findByEmail(em, email);
            if (existingUser != null) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado.");
            }

            // 4. Crear la entidad
            User newUser = new User(name, email, password);
            
            // 5. Regla de negocio: Todo usuario nace con una lista "liked" por defecto
            UserList likedList = new UserList("Favoritos");
            newUser.setLiked(likedList);

            // 6. Persistir usando el DAO
            userDAO.create(em, newUser);

            // 7. Confirmar la transacción
            em.getTransaction().commit();
            
        } catch (Exception e) {
            // Si algo falla, revertimos los cambios en la base de datos
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; // Relanzamos la excepción para que el Controller la maneje
        } finally {
            // Siempre liberar los recursos
            em.close();
        }
    }
    
    /**
     * Permite a un usuario seguir a otro.
     */
    public void followUser(long currentUserId, long targetUserId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            User currentUser = userDAO.findById(em, currentUserId);
            User targetUser = userDAO.findById(em, targetUserId);

            if (currentUser == null || targetUser == null) {
                throw new IllegalArgumentException("Uno o ambos usuarios no existen.");
            }

            // Regla de negocio: Verificar si hay bloqueos
            if (targetUser.getBlocked().contains(currentUser) || currentUser.getBlocked().contains(targetUser)) {
                throw new IllegalStateException("Acción denegada debido a un bloqueo.");
            }

            // Registrar el seguimiento bidireccional si no lo sigue ya
            targetUser.notifyFollow(currentUser);

            // En un flujo real de JPA, al estar dentro de una transacción, 
            // no es estrictamente necesario llamar a userDAO.update(), las entidades "managed" se actualizan solas al hacer commit.
            
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
}