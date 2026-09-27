/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

import com.redsocial.db.ExistDB;
import com.redsocial.model.Post;
import com.redsocial.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XQueryService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PostDAO extends GenericExistDAO {
    protected static final String POST_COLLECTION_PATH = ENV.get("EXIST_POST_PATH");


    public void createPost(Post post) throws IllegalStateException, XMLDBException, TransformerException, SAXException, IOException, ParserConfigurationException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);
        long id = post.getId();
        Post existingPost = findPostById(id);

        if (existingPost != null) {
            throw new IllegalStateException("El post con ID " + id + " ya existe.");
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
        creationDateElement.setTextContent(post.getCreationDate().toString());
        root.appendChild(creationDateElement);

        Element lastModifiedDateElement = doc.createElement("fechaUltimaModificacion");
        lastModifiedDateElement.setTextContent(post.getLastModifiedDate().toString());
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
            throw new IllegalStateException("El post debe tener al menos contenido o mediaUrls.");
        }

        Element mediaUrlsElement = doc.createElement("mediaUrls");
        for (String mediaUrl : post.getMediaUrls()) {
            Element mediaUrlElement = doc.createElement("mediaUrl");
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

    public void deletePost(long id) throws IllegalStateException, XMLDBException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);
        String resourceName = getResourceName("post", id);

        deleteById(collection, resourceName);
    }

    public void addLike(long idPost, long idUsuario) throws XMLDBException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);

        XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
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
        db.closeCollection(collection);
    }

    public void removeLike(long idPost, long idUsuario) throws XMLDBException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);

        XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
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
        db.closeCollection(collection);
    }

    public Post findPostById(long id) throws IllegalStateException, XMLDBException, ParserConfigurationException, SAXException, IOException {
        Collection collection = db.getCollection(POST_COLLECTION_PATH);

        XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                collection("/db/red-social/posts")/p:post[
                    p:id = $id
                ]
                """;
        xqs.declareVariable("id", id);

        ResourceSet result = xqs.query(query);

        if (!result.getIterator().hasMoreResources()) {
            return null;
        }

        Resource res = result.getIterator().nextResource();

        Document xml = XMLUtil.parseDocument(res.getContent().toString());
        db.closeCollection(collection);

        return new Post(xml);
    }

    public void addToCollection(long idPost, long idColeccion)
            throws XMLDBException {

        Collection collection = db.getCollection(POST_COLLECTION_PATH);


        XQueryService xqs =
                (XQueryService) collection.getService(
                        "XQueryService",
                        "1.0"
                );

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
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

        db.closeCollection(collection);
    }


    public void removeFromCollection(long idPost, long idColeccion)
            throws XMLDBException {

        Collection collection = db.getCollection(POST_COLLECTION_PATH);

        XQueryService xqs =
                (XQueryService) collection.getService(
                        "XQueryService",
                        "1.0"
                );

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
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

        db.closeCollection(collection);
    }


    /**
     * Actualiza el contenido de un post.
     */
    public void updateContents(int idPost, String contenido)
            throws XMLDBException {

        Collection collection = db.getCollection(POST_COLLECTION_PATH);


        XQueryService xqs =
                (XQueryService) collection.getService(
                        "XQueryService",
                        "1.0"
                );

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
                    p:id = $idPost
                ]
                
                return update value
                    $post/p:contenido
                with $contenido
                """;

        xqs.declareVariable("idPost", idPost);
        xqs.declareVariable("contenido", contenido);

        xqs.query(query);

        db.closeCollection(collection);
    }

    public void updateTags(int idPost, List<String> tags) throws XMLDBException {

        Collection collection = db.getCollection(POST_COLLECTION_PATH);


        XQueryService xqs =
                (XQueryService) collection.getService(
                        "XQueryService",
                        "1.0"
                );

        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                let $post := collection("/db/red-social/posts")/p:post[
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

        db.closeCollection(collection);
    }

    public ArrayList<Post> getPostsByCollection(long collectionId) throws XMLDBException, IOException, SAXException, ParserConfigurationException {
        ArrayList<Post> posts = new ArrayList<>();
        ExistDB db = ExistDB.getInstance();
        Collection collection = db.getCollection(POST_COLLECTION_PATH);

        XQueryService xqs = (XQueryService) collection.getService("XQueryService", "1.0");
        String query = """
                xquery version "3.1";
                
                declare namespace p = "http://red-social.org/post";
                
                for $post in collection("/db/red-social/posts")/p:post
                where $post/p:colecciones/p:idColeccion = $idColeccion
                return $post
                """;

        xqs.declareVariable("idColeccion", String.valueOf(collectionId));

        ResourceSet result = xqs.query(query);
        for(ResourceIterator iterator = result.getIterator(); iterator.hasMoreResources();)
        {
            Resource res = iterator.nextResource();
            String postXml = (String) res.getContent();
            Document postDoc = XMLUtil.parseDocument(postXml);

            Post post = new Post(postDoc);
            posts.add(post);
        }

        return posts;
    }
}
