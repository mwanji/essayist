package com.moandjiezana.tent.essayist.config;

/**
 * User: pjesi
 * Date: 1/21/13
 * Time: 11:15 PM
 */
public class TentRequest {

    private String entity;
    private String post;
    private String action;

    private TentRequest(String entity, String post, String action) {
        this.entity = entity;
        this.post = post;
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public String getPost() {
        return post;
    }

    public String getAction() {
        return action;
    }

    static class Builder {

        private String entity;
        private String post;
        private String action;

        Builder entity(String entity){
            this.entity = entity;
            return this;
        }

        Builder post(String post){
            this.post = post;
            return this;
        }

        Builder action(String action){
            this.action = action;
            return this;
        }

        TentRequest build(){
            return new TentRequest(entity, post, action);
        }
    }

}
