package com.redsocial.db;

import com.redsocial.exception.DBException;
import com.redsocial.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

@Component
public class ExistDB {

    @Value("${EXIST_BASE_URI}")
    private String baseUri;

    @Value("${EXIST_USER}")
    private String user;

    @Value("${EXIST_PASSWORD}")
    private String password;

    @PostConstruct
    public void init() throws Exception {
        // 1. Validar que las variables se inyectaron
        validateVariables();

        // 2. Registrar el driver de eXist-db
        Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
        Database database = (Database) cl.getDeclaredConstructor().newInstance();
        DatabaseManager.registerDatabase(database);

        System.out.println(">>> ExistDB inicializado con URI: " + baseUri);
    }

    private void validateVariables() {
        if (baseUri == null || baseUri.isEmpty()) {
            throw new IllegalStateException(
                    "La variable EXIST_BASE_URI no está definida");
        }
        if (user == null || user.isEmpty()) {
            throw new IllegalStateException(
                    "La variable EXIST_USER no está definida");
        }
        if (password == null) {
            throw new IllegalStateException(
                    "La variable EXIST_PASSWORD no está definida (puede estar vacía)");
        }
    }

    public Collection getCollection(String collectionPath)
            throws DBException, ResourceNotFoundException {

        if (collectionPath == null || collectionPath.isEmpty()) {
            throw new ResourceNotFoundException(
                    "El path de la colección no puede ser nulo o vacío.");
        }

        try {
            Collection collection = DatabaseManager.getCollection(
                    baseUri + collectionPath, user, password);

            if (collection == null) {
                throw new ResourceNotFoundException(
                        "No se pudo acceder a la colección: " + collectionPath);
            }
            return collection;

        } catch (XMLDBException e) {
            throw new DBException(
                    "Error al acceder a la colección: " + e.getMessage());
        }
    }

    public void closeCollection(Collection collection) throws DBException {
        if (collection != null) {
            try {
                collection.close();
            } catch (XMLDBException e) {
                throw new DBException(
                        "Error al cerrar la colección: " + e.getMessage());
            }
        }
    }
}