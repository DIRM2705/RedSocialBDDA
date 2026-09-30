package com.redsocial.controller;

import com.redsocial.dto.CommentRequest;
import com.redsocial.dto.PostRequest;
import com.redsocial.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/posts")
public class PostController
{
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<?> createPost(@Valid  @RequestBody PostRequest req)
    {
        postService.createPost(
                req.getAuthorId(),
                req.getContent(),
                req.getMediaURLs() != null ? java.util.Arrays.asList(req.getMediaURLs()) : java.util.Collections.emptyList(),
                req.getHashtags() != null ? java.util.Arrays.asList(req.getHashtags()) : java.util.Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Post creado exitosamente"));
    }

    // DELETE /api/posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable long postId)
    {
        postService.deletePost(postId);
        return ResponseEntity.ok(Map.of("message", "Post eliminado exitosamente"));
    }

    // POST /api/posts/{postId}/likes?userId=1
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable long postId, @RequestParam long userId) {
        postService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Like agregado"));
    }

    // DELETE /api/posts/{postId}/likes?userId=1
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> unlikePost(@PathVariable long postId, @RequestParam long userId) {
        postService.unlikePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Like eliminado"));
    }

    // POST /api/posts/{postId}/collections/{collectionId}
    @PostMapping("/{postId}/collections/{collectionId}")
    public ResponseEntity<?> addToCollection(@PathVariable long postId, @PathVariable long collectionId) {
        postService.addToCollection(postId, collectionId);
        return ResponseEntity.ok(Map.of("message", "Agregado a la colección"));
    }

    // DELETE /api/posts/{postId}/collections/{collectionId}
    @DeleteMapping("/{postId}/collections/{collectionId}")
    public ResponseEntity<?> removeFromCollection(@PathVariable long postId, @PathVariable long collectionId) {
        postService.removeFromCollection(postId, collectionId);
        return ResponseEntity.ok(Map.of("message", "Eliminado de la colección"));
    }

    // POST /api/posts/{postId}/comments
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable long postId, @Valid @RequestBody CommentRequest req) {
        postService.addComment(postId, req.getAuthorId(), req.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Comentario agregado"));
    }

    // PUT /api/posts/{postId}/comments/{commentId}
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable long postId, @PathVariable long commentId, @Valid @RequestBody CommentRequest req) {
        postService.updateComment(postId, commentId, req.getContent());
        return ResponseEntity.ok(Map.of("message", "Comentario actualizado"));
    }

    // DELETE /api/posts/{postId}/comments/{commentId}
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> removeComment(@PathVariable long postId, @PathVariable long commentId) {
        postService.removeComment(postId, commentId);
        return ResponseEntity.ok(Map.of("message", "Comentario eliminado"));
    }

    // GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable long postId) {
        String postXML = postService.getPost(postId);
        return ResponseEntity.ok(Map.of("post", postXML));
    }

    // GET /api/posts
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        var postsXML = postService.getAllPosts();
        return ResponseEntity.ok(Map.of("posts", postsXML));
    }

}
