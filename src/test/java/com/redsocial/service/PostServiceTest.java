package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.PostDAO;
import com.redsocial.exception.ConflictException;
import com.redsocial.exception.DBException;
import com.redsocial.model.Comment;
import com.redsocial.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService - Pruebas unitarias")
class PostServiceTest {

    @Mock
    private PostDAO postDAO;

    @Mock
    private CommentDAO commentDAO;

    @InjectMocks
    private PostService postService;

    private Post postValido;

    @BeforeEach
    void setUp() {
        postValido = new Post(0, 1L, "Contenido de prueba",
                List.of("http://img.com/1.jpg"),
                List.of("#test"));
    }

    // ─────────────────────────────────────────────────────────────
    // createPost
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPost: crea un post válido correctamente")
    void createPost_valido_llamaAlDAO() throws Exception {
        // Act
        postService.createPost(1L, "Contenido válido", List.of(), List.of("#tag"));

        // Assert
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postDAO, times(1)).createPost(captor.capture());

        Post postEnviado = captor.getValue();
        assertThat(postEnviado.getAuthorId()).isEqualTo(1L);
        assertThat(postEnviado.getContent()).isEqualTo("Contenido válido");
    }

    @Test
    @DisplayName("createPost: lanza ConflictException si content es null")
    void createPost_contentNull_lanzaConflict() {
        assertThatThrownBy(() ->
                postService.createPost(1L, null, List.of(), List.of()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("entre 1 y 280");

        verifyNoInteractions(postDAO);
    }

    @Test
    @DisplayName("createPost: lanza ConflictException si content está vacío")
    void createPost_contentVacio_lanzaConflict() {
        assertThatThrownBy(() ->
                postService.createPost(1L, "   ", List.of(), List.of()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("entre 1 y 280");

        verifyNoInteractions(postDAO);
    }

    @Test
    @DisplayName("createPost: lanza ConflictException si content excede 280 caracteres")
    void createPost_contentDemasiadoLargo_lanzaConflict() {
        String largo = "a".repeat(281);

        assertThatThrownBy(() ->
                postService.createPost(1L, largo, List.of(), List.of()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("entre 1 y 280");

        verifyNoInteractions(postDAO);
    }

    @Test
    @DisplayName("createPost: acepta exactamente 280 caracteres")
    void createPost_280Caracteres_esValido() throws Exception {
        String exacto = "a".repeat(280);

        postService.createPost(1L, exacto, List.of(), List.of());

        verify(postDAO).createPost(any(Post.class));
    }

    @Test
    @DisplayName("createPost: propaga DBException del DAO")
    void createPost_daoLanzaDBException_propaga() throws Exception {
        doThrow(new DBException("Error de BD"))
                .when(postDAO).createPost(any(Post.class));

        assertThatThrownBy(() ->
                postService.createPost(1L, "contenido", List.of(), List.of()))
                .isInstanceOf(DBException.class)
                .hasMessageContaining("Error de BD");
    }

    // ─────────────────────────────────────────────────────────────
    // deletePost
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletePost: delega al DAO")
    void deletePost_llamaAlDAO() throws Exception {
        postService.deletePost(10L);

        verify(postDAO).deletePost(10L);
    }

    @Test
    @DisplayName("deletePost: propaga DBException")
    void deletePost_daoFalla_propaga() throws Exception {
        doThrow(new DBException("no existe"))
                .when(postDAO).deletePost(10L);

        assertThatThrownBy(() -> postService.deletePost(10L))
                .isInstanceOf(DBException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // likePost / unlikePost
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("likePost: delega al DAO")
    void likePost_llamaAlDAO() throws Exception {
        postService.likePost(1L, 2L);

        verify(postDAO).addLike(1L, 2L);
    }

    @Test
    @DisplayName("unlikePost: delega al DAO")
    void unlikePost_llamaAlDAO() throws Exception {
        postService.unlikePost(1L, 2L);

        verify(postDAO).removeLike(1L, 2L);
    }

    // ─────────────────────────────────────────────────────────────
    // addToCollection / removeFromCollection
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addToCollection: delega al DAO")
    void addToCollection_llamaAlDAO() throws Exception {
        postService.addToCollection(1L, 100L);

        verify(postDAO).addToCollection(1L, 100L);
    }

    @Test
    @DisplayName("removeFromCollection: delega al DAO")
    void removeFromCollection_llamaAlDAO() throws Exception {
        postService.removeFromCollection(1L, 100L);

        verify(postDAO).removeFromCollection(1L, 100L);
    }

    // ─────────────────────────────────────────────────────────────
    // addComment
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addComment: construye el Comment y lo pasa al DAO")
    void addComment_creaCommentYDelega() throws Exception {
        postService.addComment(1L, 2L, "Buen post");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentDAO).createComment(eq(1L), captor.capture());

        Comment c = captor.getValue();
        assertThat(c.getAuthorId()).isEqualTo(2L);
        assertThat(c.getContent()).isEqualTo("Buen post");
    }

    @Test
    @DisplayName("addComment: propaga DBException")
    void addComment_daoFalla_propaga() throws Exception {
        doThrow(new DBException("fallo"))
                .when(commentDAO).createComment(anyLong(), any(Comment.class));

        assertThatThrownBy(() -> postService.addComment(1L, 2L, "x"))
                .isInstanceOf(DBException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // updateComment / removeComment
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateComment: delega al DAO")
    void updateComment_llamaAlDAO() throws Exception {
        postService.updateComment(1L, 5L, "nuevo contenido");

        verify(commentDAO).updateComment(1L, 5L, "nuevo contenido");
    }

    @Test
    @DisplayName("removeComment: delega al DAO")
    void removeComment_llamaAlDAO() throws Exception {
        postService.removeComment(1L, 5L);

        verify(commentDAO).removeComment(1L, 5L);
    }
}