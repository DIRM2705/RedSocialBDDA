package com.redsocial.model;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Short_video extends Post {
    @Lob
    private byte[] videoContent; // Equivale a 'Video' en el diagrama

    public Short_video() { super(); }

    public byte[] getVideoContent() { return videoContent; }
    public void setVideoContent(byte[] videoContent) { this.videoContent = videoContent; }
}