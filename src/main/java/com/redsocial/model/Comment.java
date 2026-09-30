package com.redsocial.model;

import com.redsocial.dao.UserDAO;
import com.redsocial.exception.InternalServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

    public void toXML(Document doc, Element rootElement) throws InternalServerException {

        Element idElement = doc.createElement("id");
        idElement.appendChild(doc.createTextNode(String.valueOf(id)));
        rootElement.appendChild(idElement);

        Element authorIdElement = doc.createElement("idAutor");
        authorIdElement.appendChild(doc.createTextNode(String.valueOf(authorId)));
        rootElement.appendChild(authorIdElement);

        Element contentElement = doc.createElement("contenido");
        contentElement.appendChild(doc.createTextNode(content));
        rootElement.appendChild(contentElement);

        for (Long userId : likesUserIds) {
            Element likeUserId = doc.createElement("like");
            likeUserId.appendChild(doc.createTextNode(String.valueOf(userId)));
            rootElement.appendChild(likeUserId);
        }

        for (Long userId : reportsUserIds) {
            Element reportUserId = doc.createElement("report");
            reportUserId.appendChild(doc.createTextNode(String.valueOf(userId)));
            rootElement.appendChild(reportUserId);
        }
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