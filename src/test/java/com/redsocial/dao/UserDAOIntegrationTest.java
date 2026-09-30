package com.redsocial.dao;

import com.redsocial.exception.ConflictException;
import com.redsocial.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserDAO - Pruebas de integración con ObjectDB")
class UserDAOIntegrationTest {

    @Autowired
    private UserDAO userDAO;

    private long contador = 1;

    private User crearUsuario(String username) throws ConflictException {
        User u = new User(
                username,
                username + "@test.com",
                "Password1"
        );
        userDAO.createUser(u);
        return u;
    }

    @BeforeEach
    void limpiar() {
        // Limpia todos los usuarios antes de cada test
        List<User> todos = userDAO.findAll();
        for (User u : todos) {
            try {
                userDAO.deleteUser(u.getId());
            } catch (Exception ignored) { }
        }
        contador = 1;
    }

    // ─────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser: persiste un usuario nuevo")
    void createUser_persiste() throws Exception {
        User u = new User("daniel", "d@test.com", "Password1");

        userDAO.createUser(u);

        User encontrado = userDAO.findByName("daniel");
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getEmail()).isEqualTo("d@test.com");
    }

    @Test
    @DisplayName("createUser: falla si el email ya existe")
    void createUser_emailDuplicado_lanzaConflict() throws Exception {
        crearUsuario("daniel");

        User duplicado = new User("otro", "daniel@test.com", "Password1");

        assertThatThrownBy(() -> userDAO.createUser(duplicado))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("correo electrónico");
    }

    @Test
    @DisplayName("createUser: falla si el username ya existe")
    void createUser_usernameDuplicado_lanzaConflict() throws Exception {
        crearUsuario("daniel");

        User duplicado = new User("daniel", "otro@test.com", "Password1");

        assertThatThrownBy(() -> userDAO.createUser(duplicado))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("nombre de usuario");
    }

    // ─────────────────────────────────────────────────────────────
    // findByName / findByEmail
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByName: devuelve el usuario si existe")
    void findByName_existe_devuelveUsuario() throws Exception {
        crearUsuario("daniel");

        User u = userDAO.findByName("daniel");

        assertThat(u).isNotNull();
        assertThat(u.getUsername()).isEqualTo("daniel");
    }

    @Test
    @DisplayName("findByName: devuelve null si no existe")
    void findByName_noExiste_devuelveNull() {
        User u = userDAO.findByName("fantasma");

        assertThat(u).isNull();
    }

    @Test
    @DisplayName("findByEmail: devuelve el usuario si existe")
    void findByEmail_existe_devuelveUsuario() throws Exception {
        crearUsuario("daniel");

        User u = userDAO.findByEmail("daniel@test.com");

        assertThat(u).isNotNull();
        assertThat(u.getUsername()).isEqualTo("daniel");
    }

    @Test
    @DisplayName("findByEmail: devuelve null si no existe")
    void findByEmail_noExiste_devuelveNull() {
        assertThat(userDAO.findByEmail("nadie@test.com")).isNull();
    }

    // ─────────────────────────────────────────────────────────────
    // updateUserName
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserName: cambia el nombre correctamente")
    void updateUserName_ok() throws Exception {
        User u = crearUsuario("daniel");

        userDAO.updateUserName(u.getId(), "daniel_nuevo");

        User actualizado = userDAO.findById(u.getId());
        assertThat(actualizado.getUsername()).isEqualTo("daniel_nuevo");
    }

    @Test
    @DisplayName("updateUserName: falla si el nuevo nombre ya existe")
    void updateUserName_duplicado_lanzaConflict() throws Exception {
        User u1 = crearUsuario("daniel");
        User u2 = crearUsuario("ana");

        assertThatThrownBy(() -> userDAO.updateUserName(u2.getId(), "daniel"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("updateUserName: falla si el usuario no existe")
    void updateUserName_usuarioNoExiste_lanzaConflict() {
        assertThatThrownBy(() -> userDAO.updateUserName(999L, "nuevo"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no existe");
    }

    // ─────────────────────────────────────────────────────────────
    // updateUserEmail
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserEmail: cambia el email correctamente")
    void updateUserEmail_ok() throws Exception {
        User u = crearUsuario("daniel");

        userDAO.updateUserEmail(u.getId(), "nuevo@test.com");

        User actualizado = userDAO.findById(u.getId());
        assertThat(actualizado.getEmail()).isEqualTo("nuevo@test.com");
    }

    @Test
    @DisplayName("updateUserEmail: falla si el formato es inválido")
    void updateUserEmail_formatoInvalido_lanzaConflict() throws Exception {
        User u = crearUsuario("daniel");

        assertThatThrownBy(() -> userDAO.updateUserEmail(u.getId(), "no-es-email"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Formato de correo");
    }

    @Test
    @DisplayName("updateUserEmail: falla si el email ya existe")
    void updateUserEmail_duplicado_lanzaConflict() throws Exception {
        User u1 = crearUsuario("daniel");
        User u2 = crearUsuario("ana");

        assertThatThrownBy(() ->
                userDAO.updateUserEmail(u2.getId(), u1.getEmail()))
                .isInstanceOf(ConflictException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // updateUserPassword
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserPassword: cambia la contraseña correctamente")
    void updateUserPassword_ok() throws Exception {
        User u = crearUsuario("daniel");

        userDAO.updateUserPassword(u.getId(), "NuevaPass1");

        User actualizado = userDAO.findById(u.getId());
        assertThat(actualizado.getPassword()).isEqualTo("NuevaPass1");
    }

    @Test
    @DisplayName("updateUserPassword: falla si no tiene mayúscula")
    void updateUserPassword_sinMayuscula_lanzaConflict() throws Exception {
        User u = crearUsuario("daniel");

        assertThatThrownBy(() -> userDAO.updateUserPassword(u.getId(), "password1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("mayúscula");
    }

    @Test
    @DisplayName("updateUserPassword: falla si no tiene número")
    void updateUserPassword_sinNumero_lanzaConflict() throws Exception {
        User u = crearUsuario("daniel");

        assertThatThrownBy(() -> userDAO.updateUserPassword(u.getId(), "Password"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("updateUserPassword: falla si tiene menos de 8 caracteres")
    void updateUserPassword_corta_lanzaConflict() throws Exception {
        User u = crearUsuario("daniel");

        assertThatThrownBy(() -> userDAO.updateUserPassword(u.getId(), "Pa1"))
                .isInstanceOf(ConflictException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // searchUsers
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchUsers: encuentra usuarios por coincidencia parcial")
    void searchUsers_coincidenciaParcial() throws Exception {
        crearUsuario("daniel");
        crearUsuario("daniela");
        crearUsuario("ana");

        List<User> resultados = userDAO.searchUsers("dani");

        assertThat(resultados).hasSize(2);
        assertThat(resultados).extracting(User::getUsername)
                .containsExactlyInAnyOrder("daniel", "daniela");
    }

    @Test
    @DisplayName("searchUsers: devuelve lista vacía si no hay coincidencias")
    void searchUsers_sinCoincidencias() throws Exception {
        crearUsuario("daniel");

        List<User> resultados = userDAO.searchUsers("zzz");

        assertThat(resultados).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // deleteUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser: elimina el usuario")
    void deleteUser_ok() throws Exception {
        User u = crearUsuario("daniel");

        userDAO.deleteUser(u.getId());

        assertThat(userDAO.findById(u.getId())).isNull();
    }

    @Test
    @DisplayName("deleteUser: falla si el usuario no existe")
    void deleteUser_noExiste_lanzaConflict() {
        assertThatThrownBy(() -> userDAO.deleteUser(999L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no existe");
    }
}