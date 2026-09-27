package com.redsocial.model;

import com.redsocial.dao.UserDAO;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class Comment {

    private Long id;
    private Long authorId;
    private String content;
    private List<Long> likesUserIds;
    private List<Long> reportsUserIds;

 

    // Constructor vacío requerido por JPA
    public Comment(Element commentElement)
    {
        this.id = Long.parseLong(commentElement.getElementsByTagName("id").item(0).getTextContent());
        this.authorId = Long.parseLong(commentElement.getElementsByTagName("idAutor").item(0).getTextContent());
        this.content = commentElement.getElementsByTagName("contenido").item(0).getTextContent();
        this.likesUserIds = new ArrayList<>(); // Inicializar según sea necesario
        this.reportsUserIds = new ArrayList<>(); // Inicializar según sea necesario

        for (int i = 0; i < commentElement.getElementsByTagName("like").getLength(); i++) {
            Long userId = Long.parseLong(commentElement.getElementsByTagName("like").item(i).getTextContent());
            this.likesUserIds.add(userId);
        }

        for (int i = 0; i < commentElement.getElementsByTagName("report").getLength(); i++) {
            Long userId = Long.parseLong(commentElement.getElementsByTagName("report").item(i).getTextContent());
            this.reportsUserIds.add(userId);
        }
    }

    public Comment(long authorID, String contenido)
    {
        this.authorId = authorID;
        this.content = contenido;
        this.likesUserIds = new ArrayList<>();
        this.reportsUserIds = new ArrayList<>();
    }


    public Long getId() { 
        return id; 
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        UserDAO dao = new UserDAO();
        return dao.findById(authorId);
    }

    public List<Long> getLikesUserIds() {
        return likesUserIds;
    }

    public List<Long> getReportsUserIds() {
        return reportsUserIds;
    }
}