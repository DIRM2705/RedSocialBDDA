package com.redsocial.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Reaction extends Editable {
    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    public Reaction() { super(); }

    public long getId() { return id; }
    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }
}