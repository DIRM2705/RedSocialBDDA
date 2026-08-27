package com.redsocial.model;

import javax.persistence.*;

@Entity
public class Message extends Reactionable {
    @Id
    @GeneratedValue
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Message repliedTo;

    public Message() { super(); }

    public void forwardTo(Chat chat) {
        chat.addMessage(this);
    }

    public void reply(Message message) {
        this.repliedTo = message;
    }

    public long getId() { return id; }
    public Message getRepliedTo() { return repliedTo; }
}