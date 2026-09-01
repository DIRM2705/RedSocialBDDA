package com.redsocial.controller;

import com.redsocial.dao.*;
import com.redsocial.model.*;
import com.redsocial.service.*;
import com.redsocial.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final UserService userService = new UserService();
    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();
    
    // Contenedor principal del feed extraído como variable global para auto-sincronización
    private JPanel feedContainer;

    public MainFrame() {
        setTitle("Red Social BDDA");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Usuarios", createUsuariosPanel());
        tabbedPane.addTab("Posts", createFeedPanel()); 

        // Listener de sincronización: Refresca el feed automáticamente al cambiar de pestaña
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                refreshFeed(); 
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // CLASE AUXILIAR Y MÉTODO PARA EL COMBOBOX
    // ==========================================
    private static class UserComboItem {
        private final long id;
        private final String name;

        public UserComboItem(long id, String name) {
            this.id = id;
            this.name = name;
        }
        public long getId() { return id; }
        @Override
        public String toString() { return name; } // Esto es lo que verá el usuario en pantalla
    }

    private JComboBox<UserComboItem> createUserComboBox() {
        JComboBox<UserComboItem> combo = new JComboBox<>();
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<User> users = userDAO.findAll(em);
            for (User u : users) {
                combo.addItem(new UserComboItem(u.getId(), u.getName()));
            }
        } finally {
            em.close();
        }
        return combo;
    }

    // ==========================================
    // FEED UNIFICADO (POSTS Y COMENTARIOS)
    // ==========================================
    private JPanel createFeedPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        feedContainer = new JPanel();
        feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));
        feedContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(feedContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        
        JButton btnCrearPost = new JButton("CREAR POST");
        btnCrearPost.setFont(new Font("Arial", Font.BOLD, 14));
        btnCrearPost.setPreferredSize(new Dimension(0, 50));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(btnCrearPost, BorderLayout.SOUTH);

        btnCrearPost.addActionListener(e -> {
            crearNuevoPost();
            refreshFeed();
        });

        refreshFeed();
        return mainPanel;
    }

    private void refreshFeed() {
        feedContainer.removeAll();
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<Post> posts = postDAO.findAll(em);
            for (int i = posts.size() - 1; i >= 0; i--) {
                feedContainer.add(createPostCard(posts.get(i)));
                feedContainer.add(Box.createRigidArea(new Dimension(0, 10))); 
            }
        } finally {
            em.close();
        }
        feedContainer.revalidate();
        feedContainer.repaint();
    }

    private JPanel createPostCard(Post post) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(245, 245, 250));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        String autorNombre = post.getUser() != null ? post.getUser().getName() : "Usuario Desconocido";
        JLabel lblAutor = new JLabel("Autor: " + autorNombre);
        lblAutor.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton btnEliminar = new JButton("X");
        btnEliminar.setForeground(Color.RED);
        btnEliminar.setToolTipText("Eliminar Post");
        
        headerPanel.add(lblAutor, BorderLayout.WEST);
        headerPanel.add(btnEliminar, BorderLayout.EAST);

        JTextArea txtContenido = new JTextArea(post.getContent());
        txtContenido.setWrapStyleWord(true);
        txtContenido.setLineWrap(true);
        txtContenido.setEditable(false);
        txtContenido.setOpaque(false);
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton btnComentarios = new JButton("Comentarios (" + post.getComment().size() + ")");
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(txtContenido, BorderLayout.CENTER);
        card.add(btnComentarios, BorderLayout.SOUTH);

        btnEliminar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar esta publicación?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    postService.deletePost(post.getId());
                    refreshFeed();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnComentarios.addActionListener(e -> openCommentsDialog(post));
        return card;
    }

    // ==========================================
    // VENTANAS EMERGENTES (DIALOGS)
    // ==========================================
    private void crearNuevoPost() {
        JComboBox<UserComboItem> comboUsers = createUserComboBox();
        if (comboUsers.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Debes registrar al menos un usuario primero.");
            return;
        }

        JTextArea txtContent = new JTextArea(5, 20);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        
        JLabel lblContador = new JLabel("0 / 280 caracteres");
        lblContador.setFont(new Font("Arial", Font.ITALIC, 11));
        
        // Listener de actualización en tiempo real del contador
        txtContent.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarContador(); }
            public void removeUpdate(DocumentEvent e) { actualizarContador(); }
            public void changedUpdate(DocumentEvent e) { actualizarContador(); }
            private void actualizarContador() {
                int length = txtContent.getText().length();
                lblContador.setText(length + " / 280 caracteres");
                lblContador.setForeground(length > 280 ? Color.RED : Color.BLACK);
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JScrollPane(txtContent), BorderLayout.CENTER);
        contentPanel.add(lblContador, BorderLayout.SOUTH);

        Object[] message = {
            "Selecciona al Autor:", comboUsers,
            "¿Qué estás pensando?", contentPanel
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Crear Nueva Publicación", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                UserComboItem seleccionado = (UserComboItem) comboUsers.getSelectedItem();
                postService.createPost(seleccionado.getId(), txtContent.getText());
                JOptionPane.showMessageDialog(this, "Publicado exitosamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCommentsDialog(Post post) {
        JDialog dialog = new JDialog(this, "Comentarios - Post " + post.getId(), true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Contenedor principal de comentarios (Muro vertical)
        JPanel commentsContainer = new JPanel();
        commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));
        commentsContainer.setBackground(Color.WHITE);
        commentsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(commentsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Cargar las tarjetas de comentarios
        if (post.getComment().isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay comentarios aún. ¡Sé el primero!");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            commentsContainer.add(emptyLabel);
        } else {
            for (Comment c : post.getComment()) {
                commentsContainer.add(createCommentCard(c, dialog, post));
                commentsContainer.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciado
            }
        }

        // Panel inferior de acciones (Solo crear nuevo)
        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Escribir Comentario");
        actionPanel.add(btnAdd);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.SOUTH);

        // Evento para agregar un nuevo comentario (Mantiene el contador de caracteres)
        btnAdd.addActionListener(e -> {
            JComboBox<UserComboItem> comboUsers = createUserComboBox();
            if (comboUsers.getItemCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "Debes registrar al menos un usuario primero.");
                return;
            }
            
            JTextArea txtComment = new JTextArea(4, 25);
            txtComment.setLineWrap(true);
            txtComment.setWrapStyleWord(true);
            
            JLabel lblContador = new JLabel("0 / 280 caracteres");
            lblContador.setFont(new Font("Arial", Font.ITALIC, 11));
            
            txtComment.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent ev) { actualizarContador(); }
                public void removeUpdate(DocumentEvent ev) { actualizarContador(); }
                public void changedUpdate(DocumentEvent ev) { actualizarContador(); }
                private void actualizarContador() {
                    int length = txtComment.getText().length();
                    lblContador.setText(length + " / 280 caracteres");
                    lblContador.setForeground(length > 280 ? Color.RED : Color.BLACK);
                }
            });

            JPanel commentInputPanel = new JPanel(new BorderLayout());
            commentInputPanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);
            commentInputPanel.add(lblContador, BorderLayout.SOUTH);

            Object[] msg = {"Selecciona al Autor:", comboUsers, "Comentario:", commentInputPanel};
            
            if (JOptionPane.showConfirmDialog(dialog, msg, "Nuevo Comentario", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    UserComboItem seleccionado = (UserComboItem) comboUsers.getSelectedItem();
                    commentService.addCommentToPost(post.getId(), seleccionado.getId(), txtComment.getText());
                    dialog.dispose(); 
                    refreshFeed(); // Refresca el feed principal por si el contador de comentarios cambió
                    openCommentsDialog(postDAO.findById(JPAUtil.getEntityManagerFactory().createEntityManager(), post.getId())); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            }
        });

        dialog.setVisible(true);
    }

    // Nuevo método para diseñar la tarjeta visual de cada comentario
    private JPanel createCommentCard(Comment comment, JDialog parentDialog, Post parentPost) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        card.setBackground(new Color(250, 250, 255)); // Fondo ligeramente azul/gris para distinguirlos
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Limita la altura

        // Cabecera del comentario
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        String autorNombre = comment.getUser() != null ? comment.getUser().getName() : "Desconocido";
        JLabel lblAutor = new JLabel(autorNombre);
        lblAutor.setFont(new Font("Arial", Font.BOLD, 12));
        lblAutor.setForeground(new Color(50, 50, 150));
        
        JButton btnEliminar = new JButton("X");
        btnEliminar.setForeground(Color.RED);
        btnEliminar.setFont(new Font("Arial", Font.BOLD, 10));
        btnEliminar.setMargin(new Insets(0, 4, 0, 4));
        btnEliminar.setToolTipText("Eliminar Comentario");
        
        headerPanel.add(lblAutor, BorderLayout.WEST);
        headerPanel.add(btnEliminar, BorderLayout.EAST);

        // Contenido del comentario
        JTextArea txtContenido = new JTextArea(comment.getContent());
        txtContenido.setWrapStyleWord(true);
        txtContenido.setLineWrap(true);
        txtContenido.setEditable(false);
        txtContenido.setOpaque(false);
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 13));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(txtContenido, BorderLayout.CENTER);

        // Evento para eliminar desde la propia tarjeta
        btnEliminar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parentDialog, "¿Seguro que deseas eliminar este comentario?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    commentService.deleteComment(comment.getId());
                    parentDialog.dispose();
                    refreshFeed(); 
                    openCommentsDialog(postDAO.findById(JPAUtil.getEntityManagerFactory().createEntityManager(), parentPost.getId()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialog, "Error: " + ex.getMessage());
                }
            }
        });

        return card;
    }

    // ==========================================
    // PESTAÑA 1: USUARIOS
    // ==========================================
    private JPanel createUsuariosPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Usuario"));
        
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JButton btnRegister = new JButton("Registrar");

        formPanel.add(new JLabel("Nombre:")); formPanel.add(txtName);
        formPanel.add(new JLabel("Correo:")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Contraseña:")); formPanel.add(txtPassword);
        formPanel.add(new JLabel()); formPanel.add(btnRegister);

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Email"}, 0);
        JTable table = new JTable(tableModel);

        table.removeColumn(table.getColumnModel().getColumn(0));
        
        JPanel bottomPanel = new JPanel();
        JButton btnDelete = new JButton("Eliminar Seleccionado");
     
       
        bottomPanel.add(btnDelete);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        
        btnRegister.addActionListener(e -> {
            try {
                userService.registerUser(txtName.getText(), txtEmail.getText(), new String(txtPassword.getPassword()));
                JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente.");
                txtName.setText(""); txtEmail.setText(""); txtPassword.setText("");
                loadUsers(tableModel);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                // CORRECCIÓN AQUÍ: Se usa getModel() para leer el ID oculto
                String valorCelda = table.getModel().getValueAt(row, 0).toString();
                Long userId = Long.parseLong(valorCelda);

                try {
                    userService.deleteUser(userId);
                    JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                    loadUsers(tableModel);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.");
            }
        });

        loadUsers(tableModel); 
        return panel;
    }

    private void loadUsers(DefaultTableModel model) {
        model.setRowCount(0);
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            for (User u : userDAO.findAll(em)) {
                // CORRECCIÓN AQUÍ: Se agregó u.getId() al inicio del arreglo
                model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail()});
            }
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}