package com.redsocial.service;

import com.redsocial.dao.UserDAO;
import com.redsocial.dao.UserProfileDAO;
import com.redsocial.model.User;
import org.xmldb.api.base.XMLDBException;

public class UserService {
    private final UserDAO userDAO;
    private final UserProfileDAO userProfileDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.userProfileDAO = new UserProfileDAO();
    }

    public void registerUser(String name, String email, String password) {
        try {
            User newUser = new User(name, email, password);
            userDAO.createUser(newUser);
        } catch (IllegalArgumentException e) {
            //TODO: Manejar la excepción de manera adecuada, por ejemplo, registrando el error o mostrando un mensaje al usuario
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    public void deleteUser(long userId) {
        try {
            userDAO.deleteUser(userId);
        } catch (XMLDBException e) {
            //TODO: Manejar la excepción de manera adecuada, por ejemplo, registrando el error o mostrando un mensaje al usuario
            System.out.println("Error al eliminar usuario: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            //TODO: Manejar la excepción de manera adecuada, por ejemplo, registrando el error o mostrando un mensaje al usuario
            System.out.println("Error al eliminar usuario: " + e.getMessage());
        }
    }

    public void getProfile(long userId) {
        User user = userDAO.findById(userId);
        try {
            String profileInfo = (user != null) ? userProfileDAO.findUserProfileById(userId) : null;
            if (user != null) {
                System.out.println("Perfil del usuario:");
                System.out.println("Nombre: " + user.getUsername());
                System.out.println("Correo: " + user.getEmail());
                System.out.println("Información del perfil: " + profileInfo);
            } else {
                //TODO: Manejar el caso en que el usuario no exista, por ejemplo, mostrando un mensaje al usuario
                System.out.println("Usuario no encontrado.");
            }
        }catch (XMLDBException e)
        {
            //TODO: Manejar la excepción de manera adecuada, por ejemplo, registrando el error o mostrando un mensaje al usuario
            System.out.println("Error al obtener el perfil del usuario: " + e.getMessage());
        }
    }
}