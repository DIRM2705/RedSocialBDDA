package com.redsocial.dao;

import com.redsocial.exception.ConflictException;
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
@DisplayName("PostDAO - Pruebas de integración con eXist-db")
class PostDAOIntegrationTest {

    @Autowired
    private PostDAO postDAO;

    private long contador = 1;

    private Post crearPost() throws Exception {
        long id = contador++;
        Post post = new Post(
                0, 1L,                                   // authorId
                "Contenido " + id,
                List.of("#test"),
                List.of()                             // mediaUrls
        );
        postDAO.createPost(post);
        return post;
    }

    @BeforeEach
    void limpiar() throws Exception {
        // Borra todos los posts creados por tests anteriores
        List<Post> todos = postDAO.findAllPosts();
        for (Post p : todos) {
            try {
                postDAO.deletePost(p.getId());
            } catch (Exception ignored) { }
        }
        contador = 1;
    }

    // ─────────────────────────────────────────────────────────────
    // createPost / findPostById
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPost: persiste un post correctamente")
    void createPost_persiste() throws Exception {
        Post post = crearPost();

        Post encontrado = postDAO.findPostById(post.getId());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getContent()).isEqualTo(post.getContent());
        assertThat(encontrado.getAuthorId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createPost: falla si el ID ya existe")
    void createPost_idDuplicado_lanzaConflict() throws Exception {
        crearPost();

        Post duplicado = new Post(0, 1L, "otro", List.of(), List.of());

        assertThatThrownBy(() -> postDAO.createPost(duplicado))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya existe");
    }

    @Test
    @DisplayName("createPost: falla si no hay contenido ni media")
    void createPost_sinContenidoNiMedia_lanzaConflict() {
        Post vacio = new Post(0, 99L, "otro", List.of(), List.of());

        assertThatThrownBy(() -> postDAO.createPost(vacio))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("al menos contenido o mediaUrls");
    }

    @Test
    @DisplayName("findPostById: devuelve null si no existe")
    void findPostById_noExiste_devuelveNull() throws Exception {
        assertThat(postDAO.findPostById(999L)).isNull();
    }

    // ─────────────────────────────────────────────────────────────
    // deletePost
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletePost: elimina el post")
    void deletePost_ok() throws Exception {
        Post post = crearPost();

        postDAO.deletePost(post.getId());

        assertThat(postDAO.findPostById(post.getId())).isNull();
    }

    // ─────────────────────────────────────────────────────────────
    // addLike / removeLike
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addLike: agrega un like al post")
    void addLike_ok() throws Exception {
        Post post = crearPost();

        postDAO.addLike(post.getId(), 42L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getLikesUserID()).contains(42L);
    }

    @Test
    @DisplayName("addLike: no duplica el like si ya existe")
    void addLike_duplicado_noAgrega() throws Exception {
        Post post = crearPost();

        postDAO.addLike(post.getId(), 42L);
        postDAO.addLike(post.getId(), 42L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getLikesUserID()).hasSize(1);
    }

    @Test
    @DisplayName("removeLike: elimina el like del post")
    void removeLike_ok() throws Exception {
        Post post = crearPost();
        postDAO.addLike(post.getId(), 42L);

        postDAO.removeLike(post.getId(), 42L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getLikesUserID()).doesNotContain(42L);
    }

    // ─────────────────────────────────────────────────────────────
    // addToCollection / removeFromCollection
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addToCollection: agrega el post a una colección")
    void addToCollection_ok() throws Exception {
        Post post = crearPost();

        postDAO.addToCollection(post.getId(), 100L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getCollectionIds()).contains(100L);
    }

    @Test
    @DisplayName("removeFromCollection: quita el post de la colección")
    void removeFromCollection_ok() throws Exception {
        Post post = crearPost();
        postDAO.addToCollection(post.getId(), 100L);

        postDAO.removeFromCollection(post.getId(), 100L);

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getCollectionIds()).doesNotContain(100L);
    }

    @Test
    @DisplayName("getPostsByCollection: devuelve los posts de la colección")
    void getPostsByCollection_ok() throws Exception {
        Post p1 = crearPost();
        Post p2 = crearPost();
        Post p3 = crearPost();
        postDAO.addToCollection(p1.getId(), 200L);
        postDAO.addToCollection(p2.getId(), 200L);

        List<Post> posts = postDAO.getPostsByCollection(200L);

        assertThat(posts).hasSize(2);
        assertThat(posts).extracting(Post::getId)
                .containsExactlyInAnyOrder(p1.getId(), p2.getId());
    }

    // ─────────────────────────────────────────────────────────────
    // updateTags
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTags: reemplaza las tags del post")
    void updateTags_ok() throws Exception {
        Post post = crearPost();

        postDAO.updateTags(post.getId(), List.of("#nuevo", "#actualizado"));

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getTags()).containsExactlyInAnyOrder("#nuevo", "#actualizado");
    }

    // ─────────────────────────────────────────────────────────────
    // updateContents
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateContents: cambia el contenido")
    void updateContents_ok() throws Exception {
        Post post = crearPost();

        postDAO.updateContents((int) post.getId(), "Contenido nuevo");

        Post actualizado = postDAO.findPostById(post.getId());
        assertThat(actualizado.getContent()).isEqualTo("Contenido nuevo");
    }
}