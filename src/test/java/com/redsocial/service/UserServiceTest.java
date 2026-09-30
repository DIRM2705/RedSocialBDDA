package com.redsocial.service;

import com.redsocial.dao.CommentDAO;
import com.redsocial.dao.UserDAO;
import com.redsocial.dao.UserProfileDAO;
import com.redsocial.exception.ConflictException;
import com.redsocial.exception.ResourceNotFoundException;
import com.redsocial.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas unitarias")
class UserServiceTest {

    @Mock private UserDAO userDAO;
    @Mock private UserProfileDAO userProfileDAO;
    @Mock private CommentDAO commentDAO;

    @InjectMocks
    private UserService userService;

    private User usuarioExistente;

    @BeforeEach
    void setUp() {
        usuarioExistente = new User( "daniel", "daniel@example.com", "Secret123");
    }

    // ─────────────────────────────────────────────────────────────
    // authenticateUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("authenticateUser: devuelve true con credenciales correctas")
    void authenticateUser_credencialesCorrectas_true() {
        when(userDAO.findByName("daniel")).thenReturn(usuarioExistente);

        boolean ok = userService.authenticateUser("daniel", "Secret123");

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("authenticateUser: devuelve false si password no coincide")
    void authenticateUser_passwordIncorrecta_false() {
        when(userDAO.findByName("daniel")).thenReturn(usuarioExistente);

        boolean ok = userService.authenticateUser("daniel", "otra");

        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("authenticateUser: devuelve false si usuario no existe")
    void authenticateUser_usuarioInexistente_false() {
        when(userDAO.findByName("fantasma")).thenReturn(null);

        boolean ok = userService.authenticateUser("fantasma", "x");

        assertThat(ok).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // registerUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerUser: crea usuario y perfil")
    void registerUser_creaUsuarioYPerfil() throws Exception {
        userService.registerUser("daniel", "d@e.com", "Secret123",
                "http://img.com/d.jpg", "bio");

        verify(userDAO).createUser(any(User.class));
        verify(userProfileDAO).createUserProfile(any(User.class),
                eq("http://img.com/d.jpg"), eq("bio"));
    }

    @Test
    @DisplayName("registerUser: si el usuario ya existe, propaga ConflictException")
    void registerUser_duplicado_propagaConflict() throws Exception {
        doThrow(new ConflictException("ya existe"))
                .when(userDAO).createUser(any(User.class));

        assertThatThrownBy(() ->
                userService.registerUser("daniel", "d@e.com", "Secret123", null, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya existe");

        verifyNoInteractions(userProfileDAO);
    }

    // ─────────────────────────────────────────────────────────────
    // deleteUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser: elimina usuario y sus comentarios")
    void deleteUser_eliminaUsuarioYComentarios() throws Exception {
        userService.deleteUser(1L);

        verify(userDAO).deleteUser(1L);
        verify(commentDAO).removeAllCommentsFromUser(1L);
    }

    @Test
    @DisplayName("deleteUser: si el DAO falla, propaga excepción")
    void deleteUser_fallaEnDAO_propaga() throws Exception {
        doThrow(new ConflictException("no existe"))
                .when(userDAO).deleteUser(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(commentDAO);
    }

    // ─────────────────────────────────────────────────────────────
    // getProfile
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProfile: devuelve el perfil si el usuario existe")
    void getProfile_usuarioExiste_devuelvePerfil() throws Exception {
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userProfileDAO.findUserProfileById(1L)).thenReturn("<perfil/>");

        String perfil = userService.getProfile(1L);

        assertThat(perfil).isEqualTo("<perfil/>");
    }

    @Test
    @DisplayName("getProfile: lanza ResourceNotFoundException si usuario no existe")
    void getProfile_usuarioNoExiste_lanzaNotFound() throws Exception {
        when(userDAO.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verifyNoInteractions(userProfileDAO);
    }

    // ─────────────────────────────────────────────────────────────
    // updateUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser: actualiza username, email y password")
    void updateUser_actualizaTodosLosCampos() throws Exception {
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);

        userService.updateUser(1L, "nuevo", "n@e.com", "newPass1");

        verify(userDAO).updateUserName(1L, "nuevo");
        verify(userDAO).updateUserEmail(1L, "n@e.com");
        verify(userDAO).updateUserPassword(1L, "newPass1");
    }

    @Test
    @DisplayName("updateUser: lanza ResourceNotFoundException si no existe")
    void updateUser_usuarioNoExiste_lanzaNotFound() throws Exception {
        when(userDAO.findById(99L)).thenReturn(null);

        assertThatThrownBy(() ->
                userService.updateUser(99L, "x", "y", "z"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userDAO, never()).updateUserName(anyLong(), anyString());
    }

    // ─────────────────────────────────────────────────────────────
    // updateUserProfile
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserProfile: cambia foto y bio")
    void updateUserProfile_actualizaPerfil() throws Exception {
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);

        userService.updateUserProfile(1L, "http://n.jpg", "nueva bio");

        verify(userProfileDAO).changeProfilePicture(1L, "http://n.jpg");
        verify(userProfileDAO).changeBio(1L, "nueva bio");
    }

    @Test
    @DisplayName("updateUserProfile: lanza ResourceNotFoundException si no existe")
    void updateUserProfile_usuarioNoExiste_lanzaNotFound() throws Exception {
        when(userDAO.findById(99L)).thenReturn(null);

        assertThatThrownBy(() ->
                userService.updateUserProfile(99L, "x", "y"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(userProfileDAO);
    }

    // ─────────────────────────────────────────────────────────────
    // addFollower / removeFollower
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addFollower: incrementa contador")
    void addFollower_incrementaContador() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.addFollower(1L, 2L);

        verify(userProfileDAO).incrementFollowersCount(1L);
    }

    @Test
    @DisplayName("addFollower: lanza ConflictException si es el mismo usuario")
    void addFollower_mismoUsuario_lanzaConflict() {
        assertThatThrownBy(() -> userService.addFollower(1L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("seguirte a ti mismo");

        verifyNoInteractions(userDAO);
    }

    @Test
    @DisplayName("addFollower: lanza ResourceNotFoundException si alguno no existe")
    void addFollower_usuarioNoExiste_lanzaNotFound() throws Exception {
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(null);

        assertThatThrownBy(() -> userService.addFollower(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(userProfileDAO);
    }

    @Test
    @DisplayName("removeFollower: decrementa contador")
    void removeFollower_decrementaContador() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.removeFollower(1L, 2L);

        verify(userProfileDAO).decrementFollowersCount(1L);
    }

    @Test
    @DisplayName("removeFollower: si es el mismo usuario, no hace nada")
    void removeFollower_mismoUsuario_noHaceNada() throws Exception {
        userService.removeFollower(1L, 1L);

        verifyNoInteractions(userDAO, userProfileDAO);
    }

    // ─────────────────────────────────────────────────────────────
    // addFollowing / removeFollowing
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addFollowing: incrementa contador")
    void addFollowing_incrementaContador() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.addFollowing(1L, 2L);

        verify(userProfileDAO).incrementFollowingCount(1L);
    }

    @Test
    @DisplayName("addFollowing: lanza ConflictException si es el mismo usuario")
    void addFollowing_mismoUsuario_lanzaConflict() {
        assertThatThrownBy(() -> userService.addFollowing(1L, 1L))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(userDAO);
    }

    @Test
    @DisplayName("removeFollowing: decrementa contador")
    void removeFollowing_decrementaContador() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.removeFollowing(1L, 2L);

        verify(userProfileDAO).decrementFollowingCount(1L);
    }

    // ─────────────────────────────────────────────────────────────
    // blockUser / unblockUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("blockUser: bloquea correctamente")
    void blockUser_ok() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.blockUser(1L, 2L);

        verify(userProfileDAO).blockUser(1L, 2L);
    }

    @Test
    @DisplayName("blockUser: lanza ConflictException si es el mismo usuario")
    void blockUser_mismoUsuario_lanzaConflict() {
        assertThatThrownBy(() -> userService.blockUser(1L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("bloquearte a ti mismo");
    }

    @Test
    @DisplayName("unblockUser: desbloquea correctamente")
    void unblockUser_ok() throws Exception {
        User otro = new User("ana", "a@e.com", "Pass1234");
        when(userDAO.findById(1L)).thenReturn(usuarioExistente);
        when(userDAO.findById(2L)).thenReturn(otro);

        userService.unblockUser(1L, 2L);

        verify(userProfileDAO).unblockUser(1L, 2L);
    }

    @Test
    @DisplayName("unblockUser: si es el mismo usuario, no hace nada")
    void unblockUser_mismoUsuario_noHaceNada() throws Exception {
        userService.unblockUser(1L, 1L);

        verifyNoInteractions(userDAO, userProfileDAO);
    }
}