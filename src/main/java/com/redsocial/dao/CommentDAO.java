package com.redsocial.dao;

import com.redsocial.db.ExistDB;
import com.redsocial.exception.DBException;
import com.redsocial.model.Comment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.XQueryService;

@Repository
public class CommentDAO extends GenericExistDAO {
    
    @Value("${EXIST_POST_PATH}")
    private String POST_COLLECTION_PATH;

    public CommentDAO(ExistDB db) {
        super(db);
    }

    public void createComment(long idPost, Comment comment) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(POST_COLLECTION_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    declare namespace p = "http://red-social.org/post";
                    
                    (: Inyectamos la ruta correcta dinámicamente :)
                    declare variable $collectionPath as xs:string external;
                    
                    let $post := collection($collectionPath)/p:post[p:id = $idPost]
                    
                    let $ultimoId :=
                        if (exists($post/p:comentarios/p:comentario/p:id))
                        then max(for $i in $post/p:comentarios/p:comentario/p:id return xs:integer($i))
                        else 0
                        
                    let $nuevoId := $ultimoId + 1
                    
                    return
                    if (exists($post/p:comentarios)) then
                        update insert
                            <p:comentario>
                                <p:id>{$nuevoId}</p:id>
                                <p:idAutor>{$authorID}</p:idAutor>
                                <p:contenido>{$contenido}</p:contenido>
                            </p:comentario>
                        into $post/p:comentarios
                    else
                        update insert
                            <p:comentarios>
                                <p:comentario>
                                    <p:id>{$nuevoId}</p:id>
                                    <p:idAutor>{$authorID}</p:idAutor>
                                    <p:contenido>{$contenido}</p:contenido>
                                </p:comentario>
                            </p:comentarios>
                        into $post
                    """;
            xqs.declareVariable("collectionPath", POST_COLLECTION_PATH);
            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("contenido", comment.getContent());
            xqs.declareVariable("authorID", comment.getAuthorId());
            xqs.query(query);
        } catch (Exception e) {
            throw new DBException("Error al crear comentario: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void updateComment(long idPost, long idComentario, String nuevoContenido) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(POST_COLLECTION_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    declare namespace p = "http://red-social.org/post";
                    declare variable $collectionPath as xs:string external;
                    
                    let $post := collection($collectionPath)/p:post[p:id = $idPost]
                    
                    return update replace
                        $post/p:comentarios/p:comentario[p:id = $idComentario]/p:contenido
                    with <p:contenido>{$nuevoContenido}</p:contenido>
                    """;
            xqs.declareVariable("collectionPath", POST_COLLECTION_PATH);
            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idComentario", idComentario);
            xqs.declareVariable("nuevoContenido", nuevoContenido);
            xqs.query(query);
        } catch (Exception e) {
            throw new DBException("Error al actualizar comentario: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void removeComment(long idPost, long idComentario) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(POST_COLLECTION_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    declare namespace p = "http://red-social.org/post";
                    declare variable $collectionPath as xs:string external;
                    
                    let $post := collection($collectionPath)/p:post[p:id = $idPost]
                    
                    return update delete
                        $post/p:comentarios/p:comentario[p:id = $idComentario]
                    """;
            xqs.declareVariable("collectionPath", POST_COLLECTION_PATH);
            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idComentario", idComentario);
            xqs.query(query);
        } catch (Exception e) {
            throw new DBException("Error al eliminar comentario: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void removeAllCommentsFromUser(long idUsuario) throws DBException {
        Collection collection = null;
        try {
            collection = db.getCollection(POST_COLLECTION_PATH);
            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    declare namespace p = "http://red-social.org/post";
                    declare variable $collectionPath as xs:string external;
                    
                    for $post in collection($collectionPath)/p:post
                    let $comentarios := $post/p:comentarios/p:comentario[p:idAutor = $idUsuario]
                    return
                        if (exists($comentarios)) then
                            update delete $comentarios
                        else ()
                    """;
            xqs.declareVariable("collectionPath", POST_COLLECTION_PATH);
            xqs.declareVariable("idUsuario", idUsuario);
            xqs.query(query);
        } catch (Exception e) {
            throw new DBException("Error al eliminar comentarios del usuario: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }
}