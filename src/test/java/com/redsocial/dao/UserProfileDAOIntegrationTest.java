package com.redsocial.dao;

import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserProfileDAO - Pruebas de integración con eXist-db")
class UserProfileDAOIntegrationTest {

    @Autowired private UserProfileDAO userProfileDAO;

    private User usuario;

    @BeforeEach
    void setUp() throws Exception {
        try {
            userProfileDAO.deleteUserProfile(1L);
        } catch (Exception ignored) { }
        usuario = new User("daniel", "d@test.com", "Password1");
        userProfileDAO.createUserProfile(usuario, "http://img.com/1.jpg", "Bio original");
    }

    @Test
    @DisplayName("createUserProfile: crea el perfil con valores iniciales")
    void createUserProfile_ok() throws Exception {
        String perfil = userProfileDAO.findUserProfileById(1L);

        assertThat(perfil).contains("<id>1</id>");
        assertThat(perfil).contains("http://img.com/1.jpg");
        assertThat(perfil).contains("<numeroSeguidores>0</numeroSeguidores>");
        assertThat(perfil).contains("<numeroSeguidos>0</numeroSeguidos>");
    }

    @Test
    @DisplayName("findUserProfileById: lanza excepción si no existe")
    void findUserProfileById_noExiste_lanzaNotFound() {
        assertThatThrownBy(() -> userProfileDAO.findUserProfileById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("changeProfilePicture: actualiza la foto")
    void changeProfilePicture_ok() throws Exception {
        userProfileDAO.changeProfilePicture(1L, "http://img.com/nueva.jpg");

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("http://img.com/nueva.jpg");
    }

    @Test
    @DisplayName("changeBio: actualiza la biografía")
    void changeBio_ok() throws Exception {
        userProfileDAO.changeBio(1L, "Bio nueva");

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("Bio nueva");
    }

    @Test
    @DisplayName("incrementFollowersCount: incrementa el contador")
    void incrementFollowersCount_ok() throws Exception {
        userProfileDAO.incrementFollowersCount(1L);
        userProfileDAO.incrementFollowersCount(1L);

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("<numeroSeguidores>2</numeroSeguidores>");
    }

    @Test
    @DisplayName("decrementFollowersCount: decrementa el contador")
    void decrementFollowersCount_ok() throws Exception {
        userProfileDAO.incrementFollowersCount(1L);
        userProfileDAO.incrementFollowersCount(1L);

        userProfileDAO.decrementFollowersCount(1L);

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("<numeroSeguidores>1</numeroSeguidores>");
    }

    @Test
    @DisplayName("incrementFollowingCount: incrementa el contador")
    void incrementFollowingCount_ok() throws Exception {
        userProfileDAO.incrementFollowingCount(1L);

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("<numeroSeguidos>1</numeroSeguidos>");
    }

    @Test
    @DisplayName("blockUser: agrega un usuario a la lista de bloqueados")
    void blockUser_ok() throws Exception {
        userProfileDAO.blockUser(1L, 42L);

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).contains("42");
    }

    @Test
    @DisplayName("unblockUser: quita el usuario de la lista de bloqueados")
    void unblockUser_ok() throws Exception {
        userProfileDAO.blockUser(1L, 42L);

        userProfileDAO.unblockUser(1L, 42L);

        String perfil = userProfileDAO.findUserProfileById(1L);
        assertThat(perfil).doesNotContain("<pu:idUsuario>42</pu:idUsuario>");
    }

    @Test
    @DisplayName("deleteUserProfile: elimina el perfil")
    void deleteUserProfile_ok() throws Exception {
        userProfileDAO.deleteUserProfile(1L);

        assertThatThrownBy(() -> userProfileDAO.findUserProfileById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}