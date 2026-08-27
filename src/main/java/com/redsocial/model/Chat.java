package com.redsocial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
public class Chat implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @ManyToMany
    private List<User> participants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    public Chat() {}

    public void sendMsg(Message message) {
        this.messages.add(message);
    }

    public void deleteMsg(Message message) {
        this.messages.remove(message);
    }

    public void addUser(User user) {
        if (!this.participants.contains(user)) {
            this.participants.add(user);
        }
    }

    public void removeUser(User user) {
        this.participants.remove(user);
    }
    
    // Método auxiliar para ForwardTo
    protected void addMessage(Message msg) {
        this.messages.add(msg);
    }

    // Getters y Setters
    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<User> getParticipants() { return participants; }
    public List<Message> getMessages() { return messages; }
}