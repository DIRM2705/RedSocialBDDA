package com.redsocial.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    // Instancia única (Singleton)
    private static final EntityManagerFactory emf;

    // Bloque estático que se ejecuta una sola vez al cargar la clase
    static {
        try {
            // "redsocial.odb" creará un archivo local en la raíz del proyecto
            emf = Persistence.createEntityManagerFactory("redsocial.odb");
        } catch (Throwable ex) {
            System.err.println("Fallo al inicializar EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}