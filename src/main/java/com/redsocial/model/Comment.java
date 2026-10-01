package com.redsocial.model;

import com.redsocial.dao.UserDAO;
import com.redsocial.exception.InternalServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private Long id;
    private Long authorId;
    private String content;
    private List<Long> likesUserIds;
    private List<Long> reportsUserIds;

    // Utilidad segura para extraer texto del XML (con o sin namespace "p:")
    private String getXmlValue(Element root, String tag) {
        if (root.getElementsByTagName(tag).getLength() > 0) return root.getElementsByTagName(tag).item(0).getTextContent();
        if (root.getElementsByTagName("p:" + tag).getLength() > 0) return root.getElementsByTagName("p:" + tag).item(0).getTextContent();
        return "";
    }

    // Utilidad segura para extraer listas de elementos
    private List<Element> getXmlElements(Element root, String tag) {
        List<Element> elements = new ArrayList<>();
        NodeList list = root.getElementsByTagName(tag);
        for (int i = 0; i < list.getLength(); i++) elements.add((Element) list.item(i));
        NodeList listP = root.getElementsByTagName("p:" + tag);
        for (int i = 0; i < listP.getLength(); i++) elements.add((Element) listP.item(i));
        return elements;
    }

    // Constructor desde XML a prueba de fallos
    public Comment(Element commentElement) {
        String idStr = getXmlValue(commentElement, "id");
        this.id = idStr.isEmpty() ? 0 : Long.parseLong(idStr);

        String authorIdStr = getXmlValue(commentElement, "idAutor");
        this.authorId = authorIdStr.isEmpty() ? 0 : Long.parseLong(authorIdStr);

        this.content = getXmlValue(commentElement, "contenido");

        this.likesUserIds = new ArrayList<>();
        this.reportsUserIds = new ArrayList<>();

        for (Element el : getXmlElements(commentElement, "like")) {
            this.likesUserIds.add(Long.parseLong(el.getTextContent()));
        }
        for (Element el : getXmlElements(commentElement, "report")) {
            this.reportsUserIds.add(Long.parseLong(el.getTextContent()));
        }
    }

    public Comment(long authorID, String contenido) {
        this.authorId = authorID;
        this.content = contenido;
        this.likesUserIds = new ArrayList<>();
        this.reportsUserIds = new ArrayList<>();
    }

    public void toXML(Document doc, Element parentElement) throws InternalServerException {
        Element comentarioElement = doc.createElement("comentario");

        Element idElement = doc.createElement("id");
        idElement.appendChild(doc.createTextNode(id != null ? String.valueOf(id) : "0"));
        comentarioElement.appendChild(idElement);

        Element authorIdElement = doc.createElement("idAutor");
        authorIdElement.appendChild(doc.createTextNode(authorId != null ? String.valueOf(authorId) : "0"));
        comentarioElement.appendChild(authorIdElement);

        Element nombreAutorElement = doc.createElement("nombreAutor");
        User user = getUser();
        nombreAutorElement.appendChild(doc.createTextNode(user != null ? user.getUsername() : "Desconocido"));
        comentarioElement.appendChild(nombreAutorElement);

        Element contentElement = doc.createElement("contenido");
        contentElement.appendChild(doc.createTextNode(content != null ? content : ""));
        comentarioElement.appendChild(contentElement);

        for (Long userId : likesUserIds) {
            Element likeUserId = doc.createElement("like");
            likeUserId.appendChild(doc.createTextNode(String.valueOf(userId)));
            comentarioElement.appendChild(likeUserId);
        }

        for (Long userId : reportsUserIds) {
            Element reportUserId = doc.createElement("report");
            reportUserId.appendChild(doc.createTextNode(String.valueOf(userId)));
            comentarioElement.appendChild(reportUserId);
        }

        parentElement.appendChild(comentarioElement);
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public List<Long> getLikesUserIds() { return likesUserIds; }
    public List<Long> getReportsUserIds() { return reportsUserIds; }

    public User getUser() {
        try {
            UserDAO dao = new UserDAO();
            return dao.findById(authorId);
        } catch (Exception e) {
            return null; // Evita que un error de DB rompa el XML
        }
    }
}