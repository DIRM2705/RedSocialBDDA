package com.redsocial.db;

import com.redsocial.exception.DBException;
import com.redsocial.exception.ResourceNotFoundException;
import io.github.cdimascio.dotenv.Dotenv;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

public class ExistDB
{
    private static final Dotenv ENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    private static final ExistDB INSTANCE = new ExistDB();
    private static String BASE_URI;
    private static String USER;
    private static String PASSWORD;



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
        String baseUri = ENV.get("EXIST_BASE_URI");
        String user = ENV.get("EXIST_USER");
        String password = ENV.get("EXIST_PASSWORD");
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
    }

    public Collection getCollection(String collection_path) throws DBException, ResourceNotFoundException {

        if(collection_path == null || collection_path.isEmpty()) {
            throw new ResourceNotFoundException("El path de la colección no puede ser nulo o vacío.");
        }

        try {
            Collection collection =
                    DatabaseManager.getCollection(
                            BASE_URI + collection_path,
                            USER,
                            PASSWORD
                    );

            if (collection == null) {

                throw new ResourceNotFoundException("No se pudo acceder a la colección: " + collection_path);
            }

            return collection;
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la colección: " + e.getMessage());
        }
    }

    public void closeCollection(Collection collection) throws DBException {

        if (collection != null) {

            try
            {
                collection.close();
            }
            catch (XMLDBException e)
            {
                throw new DBException("Error al cerrar la colección: " + e.getMessage());
            }
        }
    }
}