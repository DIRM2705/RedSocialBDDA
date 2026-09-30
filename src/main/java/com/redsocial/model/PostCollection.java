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
    private long idColeccion;

    private String nombreColeccion;
    public PostCollection(String nombreColeccion) {
        this.nombreColeccion = nombreColeccion;
    }

    public String getName() {
        return nombreColeccion;
    }

    public long getId() {
        return idColeccion;
    }

    public void setName(String nombreColeccion) {
        this.nombreColeccion = nombreColeccion;
    }
}
