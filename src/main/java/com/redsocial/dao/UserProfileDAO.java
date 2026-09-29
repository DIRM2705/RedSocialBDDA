package com.redsocial.dao;

import com.redsocial.exception.DBException;
import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.User;

import org.springframework.stereotype.Repository;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XQueryService;

@Repository
public class UserProfileDAO extends GenericExistDAO {
    private static final String USER_PROFILE_PATH = ENV.get("EXIST_USER_PROFILE_PATH");

    public String findUserProfileById(long id) throws ResourceNotFoundException, DBException {
        Collection collection = null;
        try
        {
            collection = db.getCollection(USER_PROFILE_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                xquery version "3.1";
                
                declare namespace pu =
                    "http://red-social.org/perfilUsuario";
                
                collection("/db/red-social/perfiles")
                /pu:perfilUsuario[
                    pu:id = $id
                ]
                """;

            xqs.declareVariable("id", id);
            ResourceSet result = xqs.query(query);
            ResourceIterator iterator = result.getIterator();

            if (!iterator.hasMoreResources()) {
                throw new ResourceNotFoundException("Perfil de usuario con ID " + id + " no encontrado.");
            }

            Resource resource = iterator.nextResource();

            return resource.getContent().toString();
        }
        catch (XMLDBException e)
        {
            throw new DBException("Error al buscar el perfil de usuario con ID " + id + ": " + e.getMessage());
        }
        finally
        {
            db.closeCollection(collection);
        }
    }

    public void createUserProfile(User user, String profilePictureURL, String bio) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        <pu:perfilUsuario>
                            <pu:id>{$id}</pu:id>
                            <pu:nombreUsuario>{$nombreUsuario}</pu:nombreUsuario>
                            <pu:fotoPerfil>{$fotoPerfil}</pu:fotoPerfil>
                            <pu:numeroSeguidores>0</pu:numeroSeguidores>
                            <pu:numeroSeguidos>0</pu:numeroSeguidos>
                            <pu:informacionUsuario>{$informacionUsuario}</pu:informacionUsuario>
                            <pu:usuariosBloqueados></pu:usuariosBloqueados>
                        </pu:perfilUsuario>
                    
                    return xmldb:store(
                        "/db/red-social/perfiles",
                        concat("perfil_", $id, ".xml"),
                        $perfil
                    )
                    """;

            xqs.declareVariable("id", user.getId());
            xqs.declareVariable("nombreUsuario", user.getUsername());
            xqs.declareVariable("fotoPerfil", profilePictureURL);
            xqs.declareVariable("informacionUsuario", bio);

            xqs.query(query);
        } catch (XMLDBException e) {
            throw new DBException("Error al crear el perfil de usuario: " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void deleteUserProfile(long id) throws DBException {
        Collection collection = null;

        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return
                        if (exists($perfil)) then
                            update delete $perfil
                        else
                            ()
                    """;

            xqs.declareVariable("id", id);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al eliminar el perfil de usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void changeProfilePicture(long id, String newProfilePictureURI) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:fotoPerfil
                    with $fotoPerfil
                    """;

            xqs.declareVariable("id", id);
            xqs.declareVariable("fotoPerfil", newProfilePictureURI);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al cambiar la foto de perfil del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void changeBio(long id, String newBio) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:informacionUsuario
                    with $informacionUsuario
                    """;

            xqs.declareVariable("id", id);
            xqs.declareVariable("informacionUsuario", newBio);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al cambiar la biografía del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void incrementFollowersCount(long id) throws DBException {
        Collection collection = null;
        try {


            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:numeroSeguidores
                    with ($perfil/pu:numeroSeguidores + 1)
                    """;

            xqs.declareVariable("id", id);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al incrementar el número de seguidores del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void incrementFollowingCount(long id) throws DBException {
        Collection collection = null;

        try {


            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:numeroSeguidos
                    with ($perfil/pu:numeroSeguidos + 1)
                    """;

            xqs.declareVariable("id", id);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al incrementar el número de seguidos del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void decrementFollowersCount(long id) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:numeroSeguidores
                    with ($perfil/pu:numeroSeguidores - 1)
                    """;

            xqs.declareVariable("id", id);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al decrementar el número de seguidores del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void decrementFollowingCount(long id) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs =
                    (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $id
                        ]
                    
                    return update value
                        $perfil/pu:numeroSeguidos
                    with ($perfil/pu:numeroSeguidos - 1)
                    """;

            xqs.declareVariable("id", id);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al decrementar el número de seguidos del usuario con ID " + id + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void blockUser(long userId, long blockedUserId) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $userId
                        ]
                    
                    let $usuarioBloqueado :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $blockedUserId
                        ]
                    
                    return
                    if (exists($perfil) and exists($usuarioBloqueado)) then
                    
                        if (exists($perfil/pu:usuariosBloqueados)) then
                    
                            update insert
                                <pu:idUsuario>{$blockedUserId}</pu:idUsuario>
                            into $perfil/pu:usuariosBloqueados
                    
                        else
                    
                            update insert
                                <pu:usuariosBloqueados>
                                    <pu:idUsuario>{$blockedUserId}</pu:idUsuario>
                                </pu:usuariosBloqueados>
                            into $perfil
                    
                    else
                        ()
                    """;

            xqs.declareVariable("userId", userId);
            xqs.declareVariable("blockedUserId", blockedUserId);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al bloquear al usuario con ID " + blockedUserId + " para el usuario con ID " + userId + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }

    public void unblockUser(long userId, long blockedUserId) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(USER_PROFILE_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace pu =
                        "http://red-social.org/perfilUsuario";
                    
                    let $perfil :=
                        collection("/db/red-social/perfiles")
                        /pu:perfilUsuario[
                            pu:id = $userId
                        ]
                    
                    return
                    if (exists($perfil)) then
                    
                        update delete
                            $perfil/pu:usuariosBloqueados/pu:idUsuario[
                                . = $blockedUserId
                            ]
                    
                    else
                        ()
                    """;

            xqs.declareVariable("userId", userId);
            xqs.declareVariable("blockedUserId", blockedUserId);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al desbloquear al usuario con ID " + blockedUserId + " para el usuario con ID " + userId + ": " + e.getMessage());
        }
        finally {
            db.closeCollection(collection);
        }
    }
}
