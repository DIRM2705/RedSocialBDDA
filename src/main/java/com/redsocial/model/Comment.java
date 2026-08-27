package com.redsocial.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "comments") // Opcional: Define el nombre de la tabla en plural
public class Comment extends Editable implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Definimos la relación hacia el Post
    // LAZY es crucial aquí para el rendimiento: no queremos cargar el Post completo 
    // cada vez que consultemos un comentario.
    @ManyToOne(fetch = FetchType.LAZY)
    // 2. @JoinColumn le dice a JPA que cree una columna física llamada "post_id" 
    // en la tabla "comments" para guardar la llave foránea.
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

 

    // Constructor vacío requerido por JPA
    public Comment() {
        super();
    }

    public Comment(String content, User author, Post post) {
        super();
        this.content = content; // Heredado de Editable
        this.user = author;     // Heredado de Editable
        this.post = post;
    }

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
       this.id = id; // Cambia el return por esta asignación
   }

    public Post getPost() { 
        return post; 
    }

    public void setPost(Post post) { 
        this.post = post; 
    }
}