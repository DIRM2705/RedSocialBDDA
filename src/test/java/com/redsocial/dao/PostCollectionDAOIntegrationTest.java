package com.redsocial.dao;

import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.PostCollection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("PostCollectionDAO - Pruebas de integración con ObjectDB")
class PostCollectionDAOIntegrationTest {

    @Autowired private PostCollectionDAO postCollectionDAO;

    @BeforeEach
    void limpiar() {
        for (PostCollection c : postCollectionDAO.findAll()) {
            try {
                postCollectionDAO.deleteCollection(c.getId());
            } catch (Exception ignored) { }
        }
    }

    @Test
    @DisplayName("createCollection: persiste la colección")
    void createCollection_ok() {
        PostCollection c = new PostCollection("Favoritos");

        postCollectionDAO.createCollection(c);

        PostCollection encontrada = postCollectionDAO.findById(1L);
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getName()).isEqualTo("Favoritos");
    }

    @Test
    @DisplayName("updateCollectionName: cambia el nombre")
    void updateCollectionName_ok() throws Exception {
        postCollectionDAO.createCollection(new PostCollection("Viejo"));

        postCollectionDAO.updateCollectionName(1L, "Nuevo");

        assertThat(postCollectionDAO.findById(1L).getName()).isEqualTo("Nuevo");
    }

    @Test
    @DisplayName("updateCollectionName: falla si no existe")
    void updateCollectionName_noExiste_lanzaNotFound() {
        assertThatThrownBy(() -> postCollectionDAO.updateCollectionName(99L, "X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteCollection: elimina la colección")
    void deleteCollection_ok() throws Exception {
        postCollectionDAO.createCollection(new PostCollection("A borrar"));

        postCollectionDAO.deleteCollection(1L);

        assertThat(postCollectionDAO.findById(1L)).isNull();
    }

    @Test
    @DisplayName("deleteCollection: falla si no existe")
    void deleteCollection_noExiste_lanzaNotFound() {
        assertThatThrownBy(() -> postCollectionDAO.deleteCollection(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}