package com.redsocial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
public class UserList implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String description;

    @ManyToMany
    private List<Post> posts = new ArrayList<>();

    public UserList(String name) {
        this.name = name;
    }

    public void addPost(Post post) {
        if (!this.posts.contains(post)) {
            this.posts.add(post);
        }
    }

    public void removePost(Post post) {
        this.posts.remove(post);
    }

    // Getters y Setters
    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Post> getPosts() { return posts; }
}