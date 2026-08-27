/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.redsocial.dao;

/**
 *
 * @author Alan
 */

import com.redsocial.model.Chat;
import com.redsocial.model.Message;
import javax.persistence.EntityManager;

public class ChatDAO extends GenericDAO<Chat, Long> {

    public ChatDAO() {
        super(Chat.class);
    }

    // Método específico: Agregar un mensaje a un chat y actualizar
    public void addMessageToChat(EntityManager em, Long chatId, Message message) {
        Chat chat = findById(em, chatId);
        if (chat != null) {
            chat.getMessages().add(message); // Asumiendo que Chat tiene una List<Message>
            update(em, chat); // JPA se encarga de persistir el nuevo mensaje por alcance (Cascade)
        }
    }
}