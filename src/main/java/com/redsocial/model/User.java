package com.redsocial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String email;
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany
    private List<User> followers = new ArrayList<>();

    @ManyToMany
    private List<User> followed_by = new ArrayList<>();

    @ManyToMany
    private List<User> blocked = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private UserList liked;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UserList> custom_lists = new ArrayList<>();

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void follow(User user) {
        if (!this.followers.contains(user)) {
            this.followers.add(user);
        }
    }

    public void notifyFollow(User user) {
        // Implementación de notificación
        if (!this.followed_by.contains(user)) {
        this.followed_by.add(user);
        }
    }

    public void block(User user) {
        if (!this.blocked.contains(user)) {
            this.blocked.add(user);
        }
    }

    public void unfollow(User user) {
        this.followers.remove(user);
    }

    public void notifyUnfollow(User user) {
        this.followed_by.remove(user);
    }

    public void unblock(User user) {
        this.blocked.remove(user);
    }

    public void addList(UserList list) {
        this.custom_lists.add(list);
    }

    public void removeList(UserList list) {
        this.custom_lists.remove(list);
    }

    public boolean authenticate(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    // Getters y Setters
    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Post> getPosts() { return posts; }
    public List<User> getFollowers() { return followers; }
    public List<User> getFollowed_by() { return followed_by; }
    public List<User> getBlocked() { return blocked; }
    public UserList getLiked() { return liked; }
    public void setLiked(UserList liked) { this.liked = liked; }
    public List<UserList> getCustom_lists() { return custom_lists; }
}