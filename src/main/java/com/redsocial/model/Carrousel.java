package com.redsocial.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Carrousel extends Post {
    @ElementCollection
    @Lob
    private List<byte[]> bitmaps = new ArrayList<>(); // Equivale a '[Bitmaps]' en el diagrama

    public Carrousel() { super(); }

    public List<byte[]> getBitmaps() { return bitmaps; }
    public void setBitmaps(List<byte[]> bitmaps) { this.bitmaps = bitmaps; }
}