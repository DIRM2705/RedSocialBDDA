package com.redsocial.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Post extends Reactionable {

    @Id
    @GeneratedValue
    private long id;
    
    private String title;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    // CORRECCIÓN CLAVE: mappedBy evita la tabla intermedia innecesaria[cite: 2]
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comment = new ArrayList<>();

    public Post() { super(); }

    public Post(String content, User author) {
        super();
        this.content = content;
        this.user = author;
    }

    public void share() {
        // Lógica de compartición
    }

    public void report() {
        // Lógica de reporte
    }

    public void addComment(Comment newComment) {
        this.comment.add(newComment);
    }

    public void removeComment(Comment targetComment) {
        this.comment.remove(targetComment);
    }

    // Getters y Setters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<Comment> getComment() { return comment; }
}