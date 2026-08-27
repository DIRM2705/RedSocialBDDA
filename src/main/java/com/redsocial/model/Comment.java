package com.redsocial.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Comment extends Editable {
    @Id
    @GeneratedValue
    private long id;

    public Comment() { super(); }

    public long getId() { return id; }
}