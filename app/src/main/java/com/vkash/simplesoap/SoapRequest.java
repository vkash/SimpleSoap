package com.vkash.simplesoap;

import java.util.HashMap;
import java.util.Map;

public class SoapRequest {

    private final String method;
    private final String namespace;
    private final String wsname;
    private final HashMap<String, String> params;

    public SoapRequest(Builder builder) {
        method = builder.method;
        namespace = builder.namespace;
        wsname = builder.wsname;
        params = builder.params;
    }

    public String getMethod() {
        return method;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getWSName() {
        return wsname;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final String method;
        private final String namespace;
        private final String wsname;
        private final HashMap<String, String> params = new HashMap<>();

        public Builder(String namespace, String wsname, String method) {
            this.namespace = namespace;
            this.wsname = wsname;
            this.method = method;
        }

        public Builder param(String name, String value) {
            this.params.put(name, value);
            return this;
        }

        public SoapRequest build() {
            return new SoapRequest(this);
        }
    }
}
