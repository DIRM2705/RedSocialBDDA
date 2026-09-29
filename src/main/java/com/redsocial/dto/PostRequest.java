package com.redsocial.dto;

public class PostRequest {
    private long authorId;
    private String content;
    private String[] mediaURLs;
    private String[] hashtags;

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

    public String[] getMediaURLs() {
        return mediaURLs;
    }

    public void setMediaURLs(String[] mediaURLs) {
        this.mediaURLs = mediaURLs;
    }

    public String[] getHashtags() {
        return hashtags;
    }

    public void setHashtags(String[] hashtags) {
        this.hashtags = hashtags;
    }


}
