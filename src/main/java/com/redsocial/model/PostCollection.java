package com.redsocial.model;

import com.redsocial.dao.PostDAO;
import org.xml.sax.SAXException;
import org.xmldb.api.base.XMLDBException;

import java.io.IOException;
import java.util.ArrayList;
import javax.persistence.*;
import javax.xml.parsers.ParserConfigurationException;

@Entity
public class PostCollection
{
    @Id
    @GeneratedValue
    private final long idColeccion;

    private String nombreColeccion;
    private final PostDAO postDAO;

    public PostCollection(int idColeccion, String nombreColeccion) {
        this.idColeccion = idColeccion;
        this.nombreColeccion = nombreColeccion;
        this.postDAO = new PostDAO();
    }

    public String getName() {
        return nombreColeccion;
    }

    public long getId() {
        return idColeccion;
    }

    public ArrayList<Post> getPosts() throws XMLDBException, SAXException, IOException, ParserConfigurationException {
        return postDAO.getPostsByCollection(idColeccion);
    }

    public void setName(String nombreColeccion) {
        this.nombreColeccion = nombreColeccion;
    }
}
