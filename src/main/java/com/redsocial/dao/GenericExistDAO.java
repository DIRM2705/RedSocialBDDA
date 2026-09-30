package com.redsocial.dao;

import com.redsocial.db.ExistDB;
import com.redsocial.exception.DBException;
import com.redsocial.exception.InternalServerException;
import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.util.XMLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Component
public class GenericExistDAO {
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    @Value("${EXIST_BASE_SCHEMA_URI}")
    protected String BASE_SCHEMA_URI;
    @Value("${EXIST_BASE_NAMESPACE}")
    protected String BASE_NAMESPACE;

    protected final ExistDB db;

    protected GenericExistDAO(ExistDB db) {
        this.db = db;
    }

    protected String getResourceName(String res_type, long id) {
        return res_type + "_" + id + ".xml";
    }

    protected Document CreateBaseSchema(String xsdSchemaName) throws InternalServerException {
        try {
            String schemaUri = BASE_SCHEMA_URI + xsdSchemaName + ".xsd";
            String namespace = BASE_NAMESPACE + xsdSchemaName;
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.newDocument();

            Element root =
                    document.createElementNS(namespace, xsdSchemaName);

            // xmlns
            root.setAttributeNS(
                    "http://www.w3.org/2000/xmlns/",
                    "xmlns",
                    namespace
            );

            // xmlns:xsi
            root.setAttributeNS(
                    "http://www.w3.org/2000/xmlns/",
                    "xmlns:xsi",
                    XSI_NAMESPACE
            );

            // xsi:schemaLocation
            root.setAttributeNS(
                    XSI_NAMESPACE,
                    "xsi:schemaLocation",
                    namespace + " " + schemaUri
            );

            document.appendChild(root);

            return document;
        }
        catch (ParserConfigurationException e) {
            throw new InternalServerException("Error al crear el documento XML base: " + e.getMessage());
        }
    }

    protected void deleteById(Collection collection, String resourceName) throws ResourceNotFoundException, DBException {
        try {
            XMLResource res = (XMLResource) collection.getResource(resourceName);
            if (res == null) {
                throw new ResourceNotFoundException("El recurso con nombre " + resourceName + " no existe en la colección.");
            }

            collection.removeResource(res);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al eliminar el recurso: " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    protected void saveDocumentToCollection(Document doc, Collection collection, String resourceName) throws InternalServerException, DBException {
        String xmlContent = XMLUtil.documentToString(doc);

        try
        {
            XMLResource res = (XMLResource) collection.createResource(resourceName, "XMLResource");
            res.setContent(xmlContent);
            collection.storeResource(res);
        } catch (XMLDBException e) {
            throw new DBException("Error al guardar el documento en la colección: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }
}
