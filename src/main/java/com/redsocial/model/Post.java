package com.redsocial.model;

import com.redsocial.dao.UserDAO;
import com.redsocial.exception.InternalServerException;
import com.redsocial.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private long id;

    private final long authorId;
    private Date creationDate;
    private Date lastModifiedDate;

    private final List<Long> collectionIds;
    private final List<String> mediaUrls;
    private final String content;
    private final List<Comment> comments;
    private final List<String> tags;
    private final List<Long> likesUserID;

    public Post(Document xmlDocument)
    {
        Element root = xmlDocument.getDocumentElement();
        this.id = Long.parseLong(root.getElementsByTagName("id").item(0).getTextContent());
        this.authorId = Long.parseLong(root.getElementsByTagName("idAutor").item(0).getTextContent());
        try
        {
            this.creationDate = DateFormat.getDateInstance().parse(root.getElementsByTagName("fechaCreacion").item(0).getTextContent());
            this.lastModifiedDate = DateFormat.getDateInstance().parse(root.getElementsByTagName("fechaUltimaModificacion").item(0).getTextContent());
        }
        catch (ParseException e)
        {
            this.creationDate = new Date();
            this.lastModifiedDate = new Date();
        }
        this.content = root.getElementsByTagName("contenido").item(0).getTextContent();
        this.comments = new ArrayList<>();
        this.collectionIds = new ArrayList<>();
        this.mediaUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.likesUserID = new ArrayList<>();

        for (int i = 0; i < root.getElementsByTagName("idColeccion").getLength(); i++) {
            this.collectionIds.add(Long.parseLong(root.getElementsByTagName("idColeccion").item(i).getTextContent()));
        }

        for (int i = 0; i < root.getElementsByTagName("urlMedia").getLength(); i++) {
            this.mediaUrls.add(root.getElementsByTagName("urlMedia").item(i).getTextContent());
        }

        for (int i = 0; i < root.getElementsByTagName("tag").getLength(); i++) {
            this.tags.add(root.getElementsByTagName("tag").item(i).getTextContent());
        }

        for (int i = 0; i < root.getElementsByTagName("like").getLength(); i++) {
            Element likeElement = (Element) root.getElementsByTagName("like").item(i);
            long userId = Long.parseLong(likeElement.getElementsByTagName("idUsuario").item(0).getTextContent());
            this.likesUserID.add(userId);
        }

        for (int i = 0; i < root.getElementsByTagName("comentario").getLength(); i++) {
            Element commentElement = (Element) root.getElementsByTagName("comentario").item(i);
            Comment comment = new Comment(commentElement);
            this.comments.add(comment);
        }
    }

    public Post(long id, long authorId, String content, List<String> mediaUrls, List<String> tags) {
        this.id = id;
        this.authorId = authorId;
        this.creationDate = new Date();
        this.lastModifiedDate = new Date();
        this.content = content;
        this.collectionIds = new ArrayList<>();
        this.mediaUrls = mediaUrls;
        this.comments = new ArrayList<>();
        this.tags = tags;
        this.likesUserID = new ArrayList<>();
    }

    public String toXML() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        Document doc;
        try {
            doc = factory.newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new InternalServerException("Error al crear el documento XML: " + e.getMessage());
        }

        Element rootElement = doc.createElement("post");
        doc.appendChild(rootElement);

        Element idElement = doc.createElement("id");
        idElement.appendChild(doc.createTextNode(String.valueOf(id)));
        rootElement.appendChild(idElement);

        Element authorIdElement = doc.createElement("idAutor");
        authorIdElement.appendChild(doc.createTextNode(String.valueOf(authorId)));
        rootElement.appendChild(authorIdElement);

        Element creationDateElement = doc.createElement("fechaCreacion");
        creationDateElement.appendChild(doc.createTextNode(DateFormat.getDateInstance().format(creationDate)));
        rootElement.appendChild(creationDateElement);

        Element lastModifiedDateElement = doc.createElement("fechaUltimaModificacion");
        lastModifiedDateElement.appendChild(doc.createTextNode(DateFormat.getDateInstance().format(lastModifiedDate)));
        rootElement.appendChild(lastModifiedDateElement);

        Element contentElement = doc.createElement("contenido");
        contentElement.appendChild(doc.createTextNode(content));
        rootElement.appendChild(contentElement);

        for (Long collectionId : collectionIds) {
            Element collectionIdElement = doc.createElement("idColeccion");
            collectionIdElement.appendChild(doc.createTextNode(String.valueOf(collectionId)));
            rootElement.appendChild(collectionIdElement);
        }

        for (String mediaUrl : mediaUrls) {
            Element mediaUrlElement = doc.createElement("urlMedia");
            mediaUrlElement.appendChild(doc.createTextNode(mediaUrl));
            rootElement.appendChild(mediaUrlElement);
        }

        for (String tag : tags) {
            Element tagElement = doc.createElement("tag");
            tagElement.appendChild(doc.createTextNode(tag));
            rootElement.appendChild(tagElement);
        }

        for (Long userId : likesUserID) {
            Element likeUserId = doc.createElement("like");
            likeUserId.appendChild(doc.createTextNode(String.valueOf(userId)));
            rootElement.appendChild(likeUserId);
        }

        for (Comment comment : comments) {
            comment.toXML(doc, rootElement);
        }

        return XMLUtil.documentToString(doc);
    }

    public void share() {
        // Lógica de compartición
    }

    public void report() {
        // Lógica de reporte
    }

    public void addComment(Comment newComment) {
        this.comments.add(newComment);
    }

    public void removeComment(Comment targetComment) {
        this.comments.remove(targetComment);
    }

    public long getId() {
        return id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public User getUser() {
        UserDAO dao = new UserDAO();
        return dao.findById(authorId);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public List<Long> getCollectionIds() {
        return collectionIds;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getContent() {
        return content;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Long> getLikesUserID() {
        return likesUserID;
    }
}