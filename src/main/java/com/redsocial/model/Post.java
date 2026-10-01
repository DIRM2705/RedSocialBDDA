package com.redsocial.model;

import com.redsocial.dao.UserDAO;
import com.redsocial.exception.InternalServerException;
import com.redsocial.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private long id;
    private long authorId;
    private Date creationDate;
    private Date lastModifiedDate;
    private List<Long> collectionIds;
    private List<String> mediaUrls;
    private String content;
    private List<Comment> comments;
    private List<String> tags;
    private List<Long> likesUserID;

    // Utilidades seguras de parseo XML
    private String getXmlValue(Element root, String tag) {
        if (root.getElementsByTagName(tag).getLength() > 0) return root.getElementsByTagName(tag).item(0).getTextContent();
        if (root.getElementsByTagName("p:" + tag).getLength() > 0) return root.getElementsByTagName("p:" + tag).item(0).getTextContent();
        return "";
    }

    private List<Element> getXmlElements(Element root, String tag) {
        List<Element> elements = new ArrayList<>();
        NodeList list = root.getElementsByTagName(tag);
        for (int i = 0; i < list.getLength(); i++) elements.add((Element) list.item(i));
        NodeList listP = root.getElementsByTagName("p:" + tag);
        for (int i = 0; i < listP.getLength(); i++) elements.add((Element) listP.item(i));
        return elements;
    }

    // Constructor desde XML a prueba de fallos
    public Post(Document xmlDocument) {
        Element root = xmlDocument.getDocumentElement();

        String idStr = getXmlValue(root, "id");
        this.id = idStr.isEmpty() ? 0 : Long.parseLong(idStr);

        String authorIdStr = getXmlValue(root, "idAutor");
        this.authorId = authorIdStr.isEmpty() ? 0 : Long.parseLong(authorIdStr);

        try {
            String cDate = getXmlValue(root, "fechaCreacion");
            String mDate = getXmlValue(root, "fechaUltimaModificacion");
            this.creationDate = cDate.isEmpty() ? new Date() : DateFormat.getDateInstance().parse(cDate);
            this.lastModifiedDate = mDate.isEmpty() ? new Date() : DateFormat.getDateInstance().parse(mDate);
        } catch (Exception e) {
            this.creationDate = new Date();
            this.lastModifiedDate = new Date();
        }

        this.content = getXmlValue(root, "contenido");

        this.collectionIds = new ArrayList<>();
        this.mediaUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.likesUserID = new ArrayList<>();
        this.comments = new ArrayList<>();

        for (Element el : getXmlElements(root, "idColeccion")) {
            this.collectionIds.add(Long.parseLong(el.getTextContent()));
        }
        for (Element el : getXmlElements(root, "urlMedia")) {
            this.mediaUrls.add(el.getTextContent());
        }
        for (Element el : getXmlElements(root, "tag")) {
            this.tags.add(el.getTextContent());
        }
        for (Element el : getXmlElements(root, "idUsuario")) {
            String likeUserIdStr = el.getTextContent();
            if (!likeUserIdStr.isEmpty()) {
                this.likesUserID.add(Long.parseLong(likeUserIdStr));
            }
        }
        for (Element el : getXmlElements(root, "comentario")) {
            this.comments.add(new Comment(el));
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
        } catch (ParserConfigurationException e) {
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

        Element nombreAutorElement = doc.createElement("nombreAutor");
        User author = getUser();
        nombreAutorElement.appendChild(doc.createTextNode(author != null ? author.getUsername() : "Desconocido"));
        rootElement.appendChild(nombreAutorElement);

        Element creationDateElement = doc.createElement("fechaCreacion");
        creationDateElement.appendChild(doc.createTextNode(DateFormat.getDateInstance().format(creationDate != null ? creationDate : new Date())));
        rootElement.appendChild(creationDateElement);

        Element lastModifiedDateElement = doc.createElement("fechaUltimaModificacion");
        lastModifiedDateElement.appendChild(doc.createTextNode(DateFormat.getDateInstance().format(lastModifiedDate != null ? lastModifiedDate : new Date())));
        rootElement.appendChild(lastModifiedDateElement);

        Element contentElement = doc.createElement("contenido");
        contentElement.appendChild(doc.createTextNode(content != null ? content : ""));
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

        Element commentsElement = doc.createElement("comentarios");
        rootElement.appendChild(commentsElement);
        for (Comment comment : comments) {
            comment.toXML(doc, commentsElement);
        }

        return XMLUtil.documentToString(doc);
    }

    public void addComment(Comment newComment) { this.comments.add(newComment); }
    public void removeComment(Comment targetComment) { this.comments.remove(targetComment); }
    public long getId() { return id; }
    public long getAuthorId() { return authorId; }
    public Date getCreationDate() { return creationDate; }
    public Date getLastModifiedDate() { return lastModifiedDate; }
    public List<Long> getCollectionIds() { return collectionIds; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public String getContent() { return content; }
    public List<Comment> getComments() { return comments; }
    public List<String> getTags() { return tags; }
    public List<Long> getLikesUserID() { return likesUserID; }

    public User getUser() {
        try {
            UserDAO dao = new UserDAO();
            return dao.findById(authorId);
        } catch (Exception e) {
            return null; // Evita que un error de DB rompa el XML
        }
    }
}