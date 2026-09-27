package com.redsocial.dao;

import io.github.cdimascio.dotenv.Dotenv;

import com.redsocial.util.XMLUtil;
import com.redsocial.db.ExistDB;

import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class ColectionsDAO {
    private static final Dotenv ENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String NAMESPACE = "http://red-social.org/coleccionPosts";
    private static final String COLLECTION_PATH =
            ENV.get("EXIST_COLLECTION_PATH");

    private static final ExistDB db = ExistDB.getInstance();


    private String getResourceName(int id) {
        return "collection_" + id + ".xml";
    }

    public void createCollection(int id, String name) throws IllegalStateException, ParserConfigurationException, XMLDBException, TransformerException {
        Collection collection = db.getCollection(COLLECTION_PATH);
        String resourceName = getResourceName(id);

        if (collection.getResource(resourceName) != null) {
            throw new IllegalStateException("La colección con ID " + id + " ya existe.");
        }

        Document doc = XMLUtil.CreateBaseSchema("coleccionPosts");
        Element root = doc.getDocumentElement();

        Element idElement = doc.createElementNS(NAMESPACE, "idColeccion");
        idElement.setTextContent(String.valueOf(id));
        root.appendChild(idElement);

        Element nombreElement = doc.createElementNS(NAMESPACE, "nombreColeccion");
        nombreElement.setTextContent(name);
        root.appendChild(nombreElement);

        Element postsElement = doc.createElementNS(NAMESPACE, "posts");
        root.appendChild(postsElement);

        String xmlContent = XMLUtil.documentToString(doc);

        XMLResource res = (XMLResource) collection.createResource(resourceName, "XML");
        res.setContent(xmlContent);
        collection.storeResource(res);

        db.closeCollection(collection);
    }

    public String getCollection(int id) throws  XMLDBException {
        Collection collection = db.getCollection(COLLECTION_PATH);
        String resourceName = getResourceName(id);

        XMLResource res = (XMLResource) collection.getResource(resourceName);
        String result = null;
        if (res != null) {
            result = res.getContent().toString();
        }
        db.closeCollection(collection);
        return result;
    }

    public void deleteCollection(int id) throws IllegalStateException, XMLDBException {
        Collection collection = db.getCollection(COLLECTION_PATH);
        String resourceName = getResourceName(id);

        XMLResource res = (XMLResource) collection.getResource(resourceName);
        if(res == null) {
            throw new IllegalStateException("La colección con ID " + id + " no existe.");
        }

        collection.removeResource(res);
        db.closeCollection(collection);
    }

    public void updateCollectionName(int id, String newName) throws IllegalStateException, XMLDBException, TransformerException, ParserConfigurationException, SAXException, IOException {
        Collection collection = db.getCollection(COLLECTION_PATH);
        String resourceName = getResourceName(id);

        XMLResource res = db.getResource(collection, resourceName);
        Document doc = XMLUtil.parseDocument(res.getContent().toString());

        Element root = doc.getDocumentElement();

        NodeList nombresNodes = root.getElementsByTagNameNS(NAMESPACE, "nombreColeccion");
        if (nombresNodes.getLength() == 0) {
            throw new IllegalStateException("No se encontró el elemento nombreColeccion en la colección con ID " + id);
        }

        Element nombreElement = (Element) nombresNodes.item(0);
        nombreElement.setTextContent(newName);

        res.setContent(XMLUtil.documentToString(doc));
        collection.storeResource(res);
        db.closeCollection(collection);
    }
}
