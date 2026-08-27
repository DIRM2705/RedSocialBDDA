package com.redsocial.controller;

import com.redsocial.dao.PostDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import com.redsocial.model.User;
import com.redsocial.service.CommentService;
import com.redsocial.service.PostService;
import com.redsocial.service.UserService;
import com.redsocial.util.JPAUtil;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Scanner;

public class MainPrueba {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Servicios para escritura (INSERT / UPDATE)
        UserService userService = new UserService();
        PostService postService = new PostService();
        CommentService commentService = new CommentService();
        
        // DAOs para lectura (SELECT)
        UserDAO userDAO = new UserDAO();
        PostDAO postDAO = new PostDAO();
        
        boolean salir = false;

        System.out.println("=================================================");
        System.out.println("   BIENVENIDO A TU RED SOCIAL (Consola)   ");
        System.out.println("=================================================");

        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Registrar nuevo Usuario");
            System.out.println("2. Crear un Post");
            System.out.println("3. Agregar un Comentario a un Post");
            System.out.println("4. Ver todos los Usuarios");
            System.out.println("5. Ver todos los Posts");
            System.out.println("6. Ver un Post y sus Comentarios");
            System.out.println("7. Salir y cerrar base de datos");
            System.out.println("7. Eliminar un Comentario");
            System.out.println("8. Eliminar un Post");
            System.out.println("9. Eliminar un Usuario");
            System.out.println("10. Salir y cerrar base de datos");
            System.out.print("Elige una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("\n-- REGISTRAR USUARIO --");
                    System.out.print("Ingresa el nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Ingresa el correo: ");
                    String correo = scanner.nextLine();
                    System.out.print("Ingresa la contraseña: ");
                    String password = scanner.nextLine();

                    try {
                        userService.registerUser(nombre, correo, password);
                        System.out.println("✅ [ÉXITO] Usuario registrado correctamente.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    }
                    break;

case "2":
                    System.out.println("\n-- CREAR UN POST --");
                    EntityManager emUsersForPost = JPAUtil.getEntityManagerFactory().createEntityManager();
                    try {
                        List<User> usuariosDisponibles = userDAO.findAll(emUsersForPost);
                        if (usuariosDisponibles.isEmpty()) {
                            System.out.println("⚠️ No hay usuarios registrados. Registra uno primero (Opción 1).");
                            break; // Salimos de la opción 2
                        }
                        
                        System.out.println("Usuarios disponibles para publicar:");
                        for (User u : usuariosDisponibles) {
                            System.out.println("  -> ID: " + u.getId() + " | Nombre: " + u.getName());
                        }
                        System.out.println("---------------------------------");
                        
                        System.out.print("Ingresa el ID del autor: ");
                        long autorId = Long.parseLong(scanner.nextLine());
                        
                        System.out.print("Escribe el contenido del post: ");
                        String contenidoPost = scanner.nextLine();

                        postService.createPost(autorId, contenidoPost);
                        System.out.println("✅ [ÉXITO] Post creado y guardado.");
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] El ID debe ser un número válido.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    } finally {
                        emUsersForPost.close();
                    }
                    break;

                case "3":
                    System.out.println("\n-- AGREGAR COMENTARIO --");
                    EntityManager emInfo = JPAUtil.getEntityManagerFactory().createEntityManager();
                    try {
                        List<Post> postsDisponibles = postDAO.findAll(emInfo);
                        if (postsDisponibles.isEmpty()) {
                            System.out.println("⚠️ No hay posts disponibles. Crea uno primero (Opción 2).");
                            break; 
                        }
                        
                        System.out.println("Posts disponibles para comentar:");
                        for (Post p : postsDisponibles) {
                            System.out.println("  -> Post ID: " + p.getId() + " | De: " + p.getUser().getName() + " | \"" + p.getContent() + "\"");
                        }
                        System.out.println("---------------------------------");
                        
                        System.out.print("Ingresa el ID del Post que vas a comentar: ");
                        long postId = Long.parseLong(scanner.nextLine());
                        
                        System.out.print("Ingresa tu ID de Usuario (quien comenta): ");
                        long comentaristaId = Long.parseLong(scanner.nextLine());
                        
                        System.out.print("Escribe tu comentario: ");
                        String contenidoComentario = scanner.nextLine();

                        commentService.addCommentToPost(postId, comentaristaId, contenidoComentario);
                        System.out.println("✅ [ÉXITO] Comentario publicado.");
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] Los IDs deben ser números válidos.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    } finally {
                        emInfo.close();
                    }
                    break;

                case "4":
                    System.out.println("\n-- LISTA DE USUARIOS --");
                    EntityManager emUsuarios = JPAUtil.getEntityManagerFactory().createEntityManager();
                    try {
                        List<User> usuarios = userDAO.findAll(emUsuarios);
                        if (usuarios.isEmpty()) {
                            System.out.println("No hay usuarios registrados aún.");
                        } else {
                            for (User u : usuarios) {
                                System.out.println("ID: " + u.getId() + " | Nombre: " + u.getName() + " | Correo: " + u.getEmail());
                            }
                        }
                    } finally {
                        emUsuarios.close(); // Siempre cerramos la conexión al terminar la lectura
                    }
                    break;

                case "5":
                    System.out.println("\n-- LISTA DE POSTS --");
                    EntityManager emPosts = JPAUtil.getEntityManagerFactory().createEntityManager();
                    try {
                        List<Post> posts = postDAO.findAll(emPosts);
                        if (posts.isEmpty()) {
                            System.out.println("No hay publicaciones aún.");
                        } else {
                            for (Post p : posts) {
                                // Gracias a JPA, podemos navegar del Post al Usuario directamente
                                System.out.println("Post ID: " + p.getId() + " | Autor: " + p.getUser().getName() + " | Contenido: " + p.getContent());
                            }
                        }
                    } finally {
                        emPosts.close();
                    }
                    break;

                case "6":
                    System.out.println("\n-- VER POST Y SUS COMENTARIOS --");
                    System.out.print("Ingresa el ID del Post a consultar: ");
                    try {
                        long postIdConsulta = Long.parseLong(scanner.nextLine());
                        EntityManager emConsulta = JPAUtil.getEntityManagerFactory().createEntityManager();
                        try {
                            Post post = postDAO.findById(emConsulta, postIdConsulta);
                            if (post == null) {
                                System.out.println("❌ No se encontró ningún post con el ID " + postIdConsulta);
                            } else {
                                // Imprimimos los datos del Post
                                System.out.println("\n? POST [" + post.getId() + "] escrito por " + post.getUser().getName() + ":");
                                System.out.println("   \"" + post.getContent() + "\"");
                                
                                // Navegamos hacia la lista de comentarios automáticamente
                                System.out.println("\n? COMENTARIOS (" + post.getComment().size() + "):");
                                if (post.getComment().isEmpty()) {
                                    System.out.println("   No hay comentarios en esta publicación.");
                                } else {
                                    for (Comment c : post.getComment()) {
                                        System.out.println("   - [" + c.getUser().getName() + "] dice: " + c.getContent());
                                    }
                                }
                            }
                        } finally {
                            emConsulta.close();
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] El ID debe ser un número válido.");
                    }
                    break;

             case "7":
                    System.out.println("\n-- ELIMINAR COMENTARIO --");
                    System.out.print("Ingresa el ID del comentario a borrar: ");
                    try {
                        long commentId = Long.parseLong(scanner.nextLine());
                        commentService.deleteComment(commentId);
                        System.out.println("✅ [ÉXITO] Comentario eliminado. La integridad del Post se mantiene.");
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] El ID debe ser un número.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    }
                    break;

                case "8":
                    System.out.println("\n-- ELIMINAR POST --");
                    System.out.print("Ingresa el ID del Post a borrar (¡Cuidado, se borrarán sus comentarios!): ");
                    try {
                        long deletePostId = Long.parseLong(scanner.nextLine());
                        postService.deletePost(deletePostId);
                        System.out.println("✅ [ÉXITO] Post y sus comentarios eliminados correctamente.");
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] El ID debe ser un número.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    }
                    break;

                case "9":
                    System.out.println("\n-- ELIMINAR USUARIO --");
                    System.out.print("Ingresa el ID del Usuario a borrar: ");
                    try {
                        long deleteUserId = Long.parseLong(scanner.nextLine());
                        userService.deleteUser(deleteUserId);
                        System.out.println("✅ [ÉXITO] Usuario eliminado del sistema.");
                    } catch (NumberFormatException e) {
                        System.err.println("❌ [ERROR] El ID debe ser un número.");
                    } catch (Exception e) {
                        System.err.println("❌ [ERROR] " + e.getMessage());
                    }
                    break;

                case "10":
                    salir = true;
                    System.out.println("\nCerrando el sistema...");
                    break;
        }
        }
        scanner.close();
        if (JPAUtil.getEntityManagerFactory() != null && JPAUtil.getEntityManagerFactory().isOpen()) {
            JPAUtil.getEntityManagerFactory().close();
        }
        System.out.println("=== SISTEMA APAGADO ===");
    }
}