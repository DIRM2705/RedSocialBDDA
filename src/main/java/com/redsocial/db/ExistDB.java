package com.redsocial.db;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

public class ExistDB
{
    private static final ExistDB INSTANCE = new ExistDB();
    private static String BASE_URI;
    private static String USER;
    private static String PASSWORD;
    private static String BASE_SCHEMA_URI;
    private ExistDB()
    {
        try
        {
            validateVariables();
            Class cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            org.xmldb.api.base.Database database = (org.xmldb.api.base.Database) cl.getDeclaredConstructor().newInstance();
            org.xmldb.api.DatabaseManager.registerDatabase(database);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static ExistDB getInstance()
    {
        return INSTANCE;
    }

    private void validateVariables() throws IllegalStateException
    {
        String baseUri = System.getenv("EXIST_BASE_URI");
        String user = System.getenv("EXIST_USER");
        String password = System.getenv("EXIST_PASSWORD");
        String baseSchemaUri = System.getenv("EXIST_BASE_SCHEMA_URI");
        if (baseUri == null || baseUri.isEmpty()) {
            throw new IllegalStateException("La variable de entorno EXIST_BASE_URI no está definida.");
        }

        BASE_URI = baseUri;

        if (user == null || user.isEmpty()) {
            throw new IllegalStateException("La variable de entorno EXIST_USER no está definida.");
        }

        USER = user;

        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("La variable de entorno EXIST_PASSWORD no está definida.");
        }

        PASSWORD = password;

        if (baseSchemaUri == null || baseSchemaUri.isEmpty()) {
            throw new IllegalStateException("La variable de entorno EXIST_COLLECTION_PATH no está definida.");
        }

        BASE_SCHEMA_URI = baseSchemaUri;
    }

    public Collection getCollection(String collection_path) throws IllegalStateException, XMLDBException {

        if(collection_path == null || collection_path.isEmpty()) {
            throw new IllegalArgumentException("El path de la colección no puede ser nulo o vacío.");
        }

        Collection collection =
                DatabaseManager.getCollection(
                        BASE_URI + collection_path,
                        USER,
                        PASSWORD
                );

        if (collection == null) {

            throw new XMLDBException(XMLDBException.INVALID_COLLECTION, "No se pudo acceder a: " + collection_path);
        }

        return collection;
    }

    public void closeCollection(Collection collection) throws XMLDBException {

        if (collection != null) {
            collection.close();
        }
    }

    public XMLResource getResource(Collection collection, String resourceName) throws IllegalStateException, XMLDBException {
        Resource resource = collection.getResource(resourceName);

        if (resource == null) {

            throw new IllegalStateException("El recurso no existe: " + resourceName);
        }

        return (XMLResource) resource;
    }
}