package com.vkash.simplesoap;

import android.util.Base64;

public class SoapCredentials {

    private final String server;
    private final String base;
    private final String wsdl;
    private final int timeout;
    private final String auth;

    public SoapCredentials(Builder builder) {
        server = builder.server;
        base = builder.base;
        wsdl = builder.wsdl;
        timeout = builder.timeout;
        auth = builder.auth;
    }

    public String getServer() {
        return server;
    }

    public String getBase() {
        return base;
    }

    public String getWsdl() {
        return wsdl;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getAuth() {
        return auth;
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final String server;
        private String base = "xyz";
        private String wsdl = "ws1.1cws";
        private int timeout = 30000;
        private String auth = "";

        public Builder(String server) {
            this.server = server;
        }

        public Builder base(String base) {
            this.base = base;
            return this;
        }

        public Builder wsdl(String wsdl) {
            this.wsdl = wsdl;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder auth(String login, String pass) {
            this.auth = "Basic " + Base64.encodeToString((login + ":" + pass).getBytes(), Base64.DEFAULT);
            return this;
        }

        public SoapCredentials build() {
            return new SoapCredentials(this);
        }
    }
}
