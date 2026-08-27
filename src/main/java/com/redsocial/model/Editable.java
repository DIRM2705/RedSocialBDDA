package com.redsocial.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Editable implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    protected User user;

    protected String content;
    protected LocalDateTime creation_time;
    protected boolean edited;

    public Editable() {
        this.creation_time = LocalDateTime.now();
        this.edited = false;
    }

    public void edit(String new_content) {
        this.content = new_content;
        this.edited = true;
    }

    // Getters y Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreation_time() { return creation_time; }
    public void setCreation_time(LocalDateTime creation_time) { this.creation_time = creation_time; }
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
}