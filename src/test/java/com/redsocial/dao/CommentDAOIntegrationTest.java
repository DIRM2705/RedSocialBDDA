package com.redsocial.dao;

import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CommentDAO - Pruebas de integración con eXist-db")
class CommentDAOIntegrationTest {

    @Autowired private CommentDAO commentDAO;
    @Autowired private PostDAO postDAO;

    private Post post;

    @BeforeEach
    void setUp() throws Exception {
        // Limpia posts anteriores
        for (Post p : postDAO.findAllPosts()) {
            postDAO.deletePost(p.getId());
        }
        // Crea un post nuevo para cada test
        post = new Post(0, 1L, "Post para comentarios", List.of(), List.of());
        postDAO.createPost(post);
    }

    @Test
    @DisplayName("createComment: agrega comentario al post")
    void createComment_ok() throws Exception {
        Comment c = new Comment(42L, "Buen post");

        commentDAO.createComment(post.getId(), c);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getComments()).hasSize(1);
    }

    @Test
    @DisplayName("createComment: autoincrementa el ID del comentario")
    void createComment_autoincrementa() throws Exception {
        commentDAO.createComment(post.getId(), new Comment(1L, "Primero"));
        commentDAO.createComment(post.getId(), new Comment(2L, "Segundo"));

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getComments()).hasSize(2);
    }

    @Test
    @DisplayName("updateComment: modifica el contenido")
    void updateComment_ok() throws Exception {
        commentDAO.createComment(post.getId(), new Comment(1L, "Original"));

        commentDAO.updateComment(post.getId(), 1L, "Editado");

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getComments())
                .anyMatch(c -> c.getContent().equals("Editado"));
    }

    @Test
    @DisplayName("removeComment: elimina el comentario")
    void removeComment_ok() throws Exception {
        commentDAO.createComment(post.getId(), new Comment(1L, "A borrar"));

        commentDAO.removeComment(post.getId(), 1L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getComments()).isEmpty();
    }

    @Test
    @DisplayName("removeAllCommentsFromUser: elimina todos los comentarios de un usuario")
    void removeAllCommentsFromUser_ok() throws Exception {
        commentDAO.createComment(post.getId(), new Comment(42L, "Uno"));
        commentDAO.createComment(post.getId(), new Comment(42L, "Dos"));
        commentDAO.createComment(post.getId(), new Comment(99L, "Otro"));

        commentDAO.removeAllCommentsFromUser(42L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getComments()).hasSize(1);
        assertThat(actualizado.getComments().getFirst().getAuthorId()).isEqualTo(99L);
    }
}