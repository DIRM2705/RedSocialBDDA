package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.PostDAO;
import com.redsocial.exception.ConflictException;
import com.redsocial.exception.DBException;
import com.redsocial.exception.InternalServerException;
import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;

    public PostService(PostDAO postDAO, CommentDAO commentDAO) {
        this.postDAO = postDAO;
        this.commentDAO = commentDAO;
    }

    public void createPost(long authorId, String content, List<String> mediaURLs, List<String> hashtags) throws ConflictException, DBException, InternalServerException {
        // Límite de 280 caracteres
        if (content == null || content.trim().isEmpty() || content.length() > 280) {
            throw new ConflictException("El contenido del post debe tener entre 1 y 280 caracteres.");
        }

        Post post = new Post(authorId, content, mediaURLs, hashtags);
        
        postDAO.createPost(post);
    }

    public void deletePost(long postId) throws DBException, ConflictException {
        postDAO.deletePost(postId);
    }


    public void likePost(long postId, long userId) throws DBException {
        postDAO.addLike(postId, userId);

    }


    public void unlikePost(long postId, long userId) throws DBException {
        postDAO.removeLike(postId, userId);

    }

    public void addToCollection(long postId, long collectionId) throws DBException {
        postDAO.addToCollection(postId, collectionId);
    }

    public void removeFromCollection(long postId, long collectionId) throws DBException {
        postDAO.removeFromCollection(postId, collectionId);
    }

    public void addComment(long postId, long userId, String commentContent) throws DBException {
        Comment comment = new Comment(userId, commentContent);
        commentDAO.createComment(postId, comment);
    }

    public void updateComment(long postId, long commentId, String newContent) throws DBException{
        commentDAO.updateComment(postId, commentId, newContent);
    }

    public void removeComment(long postId, long commentId) throws DBException {
        commentDAO.removeComment(postId, commentId);
    }
}