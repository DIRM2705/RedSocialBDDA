package com.redsocial.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {
    private long authorId;

    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    private String content;

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
