/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XQueryService;

import com.redsocial.db.ExistDB;
import com.redsocial.exception.ConflictException;
import com.redsocial.exception.DBException;
import com.redsocial.exception.InternalServerException;
import com.redsocial.model.Post;
import com.redsocial.util.XMLUtil;


@Repository
public class PostDAO extends GenericExistDAO {
    @Value("${EXIST_POST_PATH}")
    private String POST_COLLECTION_PATH; 

    public PostDAO(ExistDB db) {
        super(db);
    }

    public long getNextPostId() throws DBException, InternalServerException {
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $maxId := max(
                        for $post in collection("/db/apps/red-social/posts")/p:post
                        return xs:integer($post/p:id)
                    )
                    
                    return if (empty($maxId)) then 1 else $maxId + 1
                    """;

            ResourceSet result = xqs.query(query);
            Resource res = result.getIterator().nextResource();
            return Long.parseLong(res.getContent().toString());
        } catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void createPost(Post post) throws ConflictException, DBException, InternalServerException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);
        long id = post.getId();
        Post existingPost = findPostById(id);

        if (existingPost != null) {
            throw new ConflictException("El post con ID " + id + " ya existe.");
        }

        Document doc = CreateBaseSchema("post");
        Element root = doc.getDocumentElement();

        Element idElement = doc.createElement("id");
        idElement.setTextContent(String.valueOf(id));
        root.appendChild(idElement);

        Element authorIdElement = doc.createElement("idAutor");
        authorIdElement.setTextContent(String.valueOf(post.getAuthorId()));
        root.appendChild(authorIdElement);

        Element creationDateElement = doc.createElement("fechaCreacion");
        creationDateElement.setTextContent(post.getCreationDate());
        root.appendChild(creationDateElement);

        Element lastModifiedDateElement = doc.createElement("fechaModificacion");
        lastModifiedDateElement.setTextContent(post.getLastModifiedDate());
        root.appendChild(lastModifiedDateElement);

        Element collectionsElement = doc.createElement("colecciones");
        root.appendChild(collectionsElement);

        Element tagsElement = doc.createElement("tags");
        for (String tag : post.getTags()) {
            Element tagElement = doc.createElement("tag");
            tagElement.setTextContent(tag);
            tagsElement.appendChild(tagElement);
        }
        root.appendChild(tagsElement);

        List<String> mediaUrls = post.getMediaUrls();
        if (mediaUrls.isEmpty() && post.getContent().isEmpty()) {
            throw new ConflictException("El post debe tener al menos contenido o mediaUrls.");
        }

        Element mediaUrlsElement = doc.createElement("multimedia");
        for (String mediaUrl : post.getMediaUrls()) {
            Element mediaUrlElement = doc.createElement("uri");
            mediaUrlElement.setTextContent(mediaUrl);
            mediaUrlsElement.appendChild(mediaUrlElement);
        }

        Element contentElement = doc.createElement("contenido");
        contentElement.setTextContent(post.getContent());
        root.appendChild(contentElement);

        Element likesElement = doc.createElement("likes");
        root.appendChild(likesElement);

        Element commentsElement = doc.createElement("comentarios");
        root.appendChild(commentsElement);

        String resourceName = getResourceName("post", id);
        saveDocumentToCollection(doc, collection, resourceName);

        db.closeCollection(collection);

    }

    public void deletePost(long id) throws ConflictException, DBException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);
        String resourceName = getResourceName("post", id);

        deleteById(collection, resourceName);
    }

    public void addLike(long idPost, long idUsuario) throws DBException {
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    return
                    if (
                        not(
                            $post/p:likes/p:idUsuario
                            = $idUsuario
                        )
                    ) then
                    
                        update insert
                            <p:idUsuario>{$idUsuario}</p:idUsuario>
                        into $post/p:likes
                    
                    else
                        ()
                    """;

            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idUsuario", idUsuario);

            xqs.query(query);
        } catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void removeLike(long idPost, long idUsuario) throws DBException {
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    return update delete
                        $post/p:likes/p:idUsuario[
                            . = $idUsuario
                        ]
                    """;

            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idUsuario", idUsuario);

            xqs.query(query);
        } catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public Post findPostById(long id) throws ConflictException, DBException, InternalServerException {
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    collection("/db/apps/red-social/posts")/p:post[
                        p:id = $id
                    ]
                    """;
            xqs.declareVariable("id", id);

            ResourceSet result = xqs.query(query);

            if (!result.getIterator().hasMoreResources()) {
                return null;
            }

            Resource res = result.getIterator().nextResource();

            Document doc = XMLUtil.parseDocument(res.getContent().toString());
            return new Post(doc);
        } catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }

    }

    public void addToCollection(long idPost, long idColeccion) throws DBException {

        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);


            XQueryService xqs =
                    (XQueryService) collection.getService(
                            "XQueryService",
                            "1.0"
                    );

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    return
                    if (
                        not(
                            $post/p:colecciones/p:idColeccion
                            = $idColeccion
                        )
                    ) then
                    
                        update insert
                            <p:idColeccion>{$idColeccion}</p:idColeccion>
                        into $post/p:colecciones
                    
                    else
                        ()
                    """;

            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idColeccion", idColeccion);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }


    public void removeFromCollection(long idPost, long idColeccion) throws DBException {

        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs =
                    (XQueryService) collection.getService(
                            "XQueryService",
                            "1.0"
                    );

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    return update delete
                        $post/p:colecciones/p:idColeccion[
                            . = $idColeccion
                        ]
                    """;

            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("idColeccion", idColeccion);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }


    /**
     * Actualiza el contenido de un post.
     */
    public void updateContents(int idPost, String contenido) throws DBException {

        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);


            XQueryService xqs =
                    (XQueryService) collection.getService(
                            "XQueryService",
                            "1.0"
                    );

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    return update value
                        $post/p:contenido
                    with $contenido
                    """;

            xqs.declareVariable("idPost", idPost);
            xqs.declareVariable("contenido", contenido);

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void updateTags(long idPost, List<String> tags) throws DBException {

        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);


            XQueryService xqs =
                    (XQueryService) collection.getService(
                            "XQueryService",
                            "1.0"
                    );

            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    let $post := collection("/db/apps/red-social/posts")/p:post[
                        p:id = $idPost
                    ]
                    
                    let $nuevasTags :=
                        <p:tags>
                            {
                                for $tag in $tags
                                return <p:tag>{$tag}</p:tag>
                            }
                        </p:tags>
                    
                    return
                    if (exists($post/p:tags)) then
                    
                        update replace
                            $post/p:tags
                        with $nuevasTags
                    
                    else
                    
                        update insert
                            $nuevasTags
                        into $post
                    """;

            xqs.declareVariable("idPost", idPost);

            /*
             * XQuery recibe la lista como una secuencia.
             */
            xqs.declareVariable(
                    "tags",
                    tags.toArray(new String[0])
            );

            xqs.query(query);
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public ArrayList<Post> getPostsByCollection(long collectionId) throws DBException, InternalServerException {
        ArrayList<Post> posts = new ArrayList<>();
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    for $post in collection("/db/apps/red-social/posts")/p:post
                    where $post/p:colecciones/p:idColeccion = $idColeccion
                    return $post
                    """;

            xqs.declareVariable("idColeccion", String.valueOf(collectionId));

            ResourceSet result = xqs.query(query);
            for (ResourceIterator iterator = result.getIterator(); iterator.hasMoreResources(); ) {
                Resource res = iterator.nextResource();
                String postXml = (String) res.getContent();
                Document postDoc = XMLUtil.parseDocument(postXml);

                Post post = new Post(postDoc);
                posts.add(post);
            }

            return posts;
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public void removeAllPostsFromUser(long userId) throws DBException {
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    declare variable $idAutor as xs:string external;
                    
                    for $post in collection("/db/apps/red-social/posts")/p:post
                    where $post/p:idAutor = $idAutor
                    return $post/p:id/text()
                    """;

            xqs.declareVariable("idAutor", String.valueOf(userId));

            ResourceSet result = xqs.query(query);
            for (ResourceIterator iterator = result.getIterator(); iterator.hasMoreResources(); ) {
                Resource res = iterator.nextResource();
                String postIdStr = (String) res.getContent();
                long postId = Long.parseLong(postIdStr);

                deleteById(collection, getResourceName("post", postId));
            }
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }

    public ArrayList<Post> findAllPosts() throws DBException, InternalServerException {
        ArrayList<Post> posts = new ArrayList<>();
        Collection collection = null;

        try {
            collection = db.getCollection(POST_COLLECTION_PATH);

            XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
            String query = """
                    xquery version "3.1";
                    
                    declare namespace p = "http://red-social.org/post";
                    
                    for $post in collection("/db/apps/red-social/posts")/p:post
                    return $post
                    """;

            ResourceSet result = xqs.query(query);
            for (ResourceIterator iterator = result.getIterator(); iterator.hasMoreResources(); ) {
                Resource res = iterator.nextResource();
                String postXml = (String) res.getContent();
                Document postDoc = XMLUtil.parseDocument(postXml);

                Post post = new Post(postDoc);
                posts.add(post);
            }

            return posts;
        }
        catch (XMLDBException e) {
            throw new DBException("Error al acceder a la base de datos: " + e.getMessage());
        } finally {
            db.closeCollection(collection);
        }
    }
}
