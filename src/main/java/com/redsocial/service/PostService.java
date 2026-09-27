package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.PostDAO;
import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import java.util.List;

public class PostService {
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;

    public PostService() {
        this.postDAO = new PostDAO();
        this.commentDAO = new CommentDAO();
    }

    public void createPost(long authorId, String content, List<String> mediaURLs, List<String> hashtags) {
        // Límite de 280 caracteres
        if (content == null || content.trim().isEmpty() || content.length() > 280) {
            throw new IllegalArgumentException("El contenido del post debe tener entre 1 y 280 caracteres.");
        }

        Post post = new Post(authorId, content, mediaURLs, hashtags);
        
        try {
            postDAO.createPost(post);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el post: " + e.getMessage(), e);
        }
    }

    public void deletePost(long postId) {
        try {
            postDAO.deletePost(postId);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el post: " + e.getMessage(), e);
        }
    }

    public void likePost(long postId, long userId) {
        try {
            postDAO.addLike(postId, userId);
        } catch (Exception e) {
            throw new RuntimeException("Error al dar like al post: " + e.getMessage(), e);
        }
    }

    public void unlikePost(long postId, long userId) {
        try {
            postDAO.removeLike(postId, userId);
        } catch (Exception e) {
            throw new RuntimeException("Error al quitar like del post: " + e.getMessage(), e);
        }
    }

    public void addToCollection(long postId, long collectionId) {
        try {
            postDAO.addToCollection(postId, collectionId);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar el post a la colección: " + e.getMessage(), e);
        }
    }

    public void removeFromCollection(long postId, long collectionId) {
        try {
            postDAO.removeFromCollection(postId, collectionId);
        } catch (Exception e) {
            throw new RuntimeException("Error al quitar el post de la colección: " + e.getMessage(), e);
        }
    }

    public void addComment(long postId, long userId, String commentContent) {
        Comment comment = new Comment(userId, commentContent);
        try {
            commentDAO.createComment(postId, comment);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar comentario al post: " + e.getMessage(), e);
        }
    }

    public void updateComment(long postId, long commentId, String newContent) {
        try {
            commentDAO.updateComment(postId, commentId, newContent);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar comentario del post: " + e.getMessage(), e);
        }
    }

    public void removeComment(long postId, long commentId) {
        try {
            commentDAO.removeComment(postId, commentId);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar comentario del post: " + e.getMessage(), e);
        }
    }
}