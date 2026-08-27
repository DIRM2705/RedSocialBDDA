package com.redsocial.test;

import com.redsocial.dao.PostDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.model.Post;
import com.redsocial.model.User;
import com.redsocial.service.CommentService;
import com.redsocial.service.PostService;
import com.redsocial.service.UserService;
import com.redsocial.util.JPAUtil;

import javax.persistence.EntityManager;
import java.util.List;

public class MainPrueba {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DEL SISTEMA ===\n");

        UserService userService = new UserService();
        PostService postService = new PostService();
        CommentService commentService = new CommentService();

        // PRUEBA 1
        System.out.println("Prueba 1: Registrando un usuario nuevo...");
        try {
            userService.registerUser("Juan Perez", "juan@ejemplo.com", "password123");
            System.out.println("[ÉXITO] Transacción de registro completada.");
        } catch (Exception e) {
            System.out.println("[OMITIDO/INFO] " + e.getMessage());
        }
        System.out.println("---------------------------------------------------");

        // ---------------------------------------------------------
        // ABRIR CONEXIÓN DE LECTURA PARA OBTENER LOS IDs REALES
        // ---------------------------------------------------------
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        UserDAO userDAO = new UserDAO();
        PostDAO postDAO = new PostDAO();
        
        long autorIdReal = -1;
        long postIdReal = -1;

        try {
            List<User> usuarios = userDAO.findAll(em);
            if (!usuarios.isEmpty()) {
                autorIdReal = usuarios.get(0).getId();
                System.out.println("-> [SISTEMA] ID real del usuario obtenido: " + autorIdReal);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar usuarios.");
        }

        // ---------------------------------------------------------
        // PRUEBA 3: Crear Post
        // ---------------------------------------------------------
        System.out.println("\nPrueba 3: Creando un Post para el usuario con ID " + autorIdReal + "...");
        try {
            if (autorIdReal != -1) {
                postService.createPost(autorIdReal, "¡Hola a todos! Probando IDs dinámicos.");
                System.out.println("[ÉXITO] Post creado y guardado en la base de datos.");
            }
        } catch (Exception e) {
            System.err.println("[FALLO] " + e.getMessage());
        }
        System.out.println("---------------------------------------------------");

        // PRUEBA 4: 
        try {
            List<Post> posts = postDAO.findAll(em);
            if (!posts.isEmpty()) {
                postIdReal = posts.get(0).getId();
                System.out.println("-> [SISTEMA] ID real del post obtenido: " + postIdReal);
                
                System.out.println("\nPrueba 4: Agregando un comentario al Post con ID " + postIdReal + "...");
                commentService.addCommentToPost(postIdReal, autorIdReal, "¡Excelente sistema! Ahora sí funciona.");
                System.out.println("[ÉXITO] Comentario guardado y relacionado exitosamente al Post.");
            }
        } catch (Exception e) {
            System.err.println("[FALLO] No se pudo agregar el comentario: " + e.getMessage());
        }
        
        em.close(); // Cerramos la conexión de lectura
        System.out.println("---------------------------------------------------");

        // ---------------------------------------------------------
        // CIERRE DE RECURSOS
        // ---------------------------------------------------------
        System.out.println("\nCerrando conexiones a la base de datos...");
        if (JPAUtil.getEntityManagerFactory() != null && JPAUtil.getEntityManagerFactory().isOpen()) {
            JPAUtil.getEntityManagerFactory().close();
        }
        System.out.println("=== PRUEBAS FINALIZADAS ===");
    }
}
