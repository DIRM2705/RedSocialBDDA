package com.redsocial.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String profileURL;

    @ManyToMany
    private List<User> followers = new ArrayList<>();

    @ManyToMany
    private List<User> followed_by = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL)
    private List<PostCollection> custom_lists = new ArrayList<>();

    public User(String username, String email, String password) throws IllegalArgumentException {
        username = username.replace(" ", "_");

        // 1. Validar correo electrónico (evita vacíos alrededor del punto)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Formato de correo inválido. Debe contener un dominio válido (ej. usuario@dominio.com).");
        }

        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }

        // 2. Validar contraseña (Mínimo 8 caracteres, 1 número, 1 mayúscula)
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, un número y una mayúscula.");
        }

        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void addList(PostCollection list) {
        this.custom_lists.add(list);
    }

    public void removeList(long listId) {
        this.custom_lists.stream()
                .filter(l -> l.getId() == listId)
                .findFirst().ifPresent(list -> this.custom_lists.remove(list));
    }

    public boolean authenticate(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    // Getters y Setters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<User> getFollowers() { return followers; }
    public List<User> getFollowed_by() { return followed_by; }
}