package com.mikoai.assignment.model;

import io.vertx.core.json.JsonObject;

public class AggregatedResponse {

    private final String postTitle;
    private final String authorName;

    public AggregatedResponse(String postTitle, String authorName) {
        this.postTitle = postTitle;
        this.authorName = authorName;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getAuthorName() {
        return authorName;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("post_title", postTitle)
                .put("author_name", authorName);
    }
}
