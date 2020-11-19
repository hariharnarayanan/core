package net.securustech.ews.data.elasticsearch.rest.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.elasticsearch.rest")
public class ElasticsearchRestProperties {

    private static final Integer DEFAULT_SOCKET_TIMEOUT=30000;
    private static final Integer DEFAULT_CONNECT_TIMEOUT=10000;

    private String uri;
    private String username = "";
    private String password;

    private int maxTotalConnection = 50;
    private int defaultMaxTotalConnectionPerRoute = 50;
    private int readTimeout = 5000;
    private Boolean multiThreaded = true;
    private Integer sockettimeout;
    private Integer connecttimeout;

    /**
     * Proxy settings.
     */
    private final Proxy proxy = new Proxy();

    public Proxy getProxy() {
        return this.proxy;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxTotalConnection() {
        return maxTotalConnection;
    }

    public void setMaxTotalConnection(int maxTotalConnection) {
        this.maxTotalConnection = maxTotalConnection;
    }

    public int getDefaultMaxTotalConnectionPerRoute() {
        return defaultMaxTotalConnectionPerRoute;
    }

    public void setDefaultMaxTotalConnectionPerRoute(int defaultMaxTotalConnectionPerRoute) {
        this.defaultMaxTotalConnectionPerRoute = defaultMaxTotalConnectionPerRoute;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Boolean getMultiThreaded() {
        return multiThreaded;
    }

    public void setMultiThreaded(Boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }

    public Integer getConnectTimeout() {
        return connecttimeout == null?DEFAULT_CONNECT_TIMEOUT:connecttimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connecttimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return sockettimeout == null?DEFAULT_SOCKET_TIMEOUT:sockettimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.sockettimeout = socketTimeout;
    }

    public static class Proxy {

        /**
         * Proxy host the HTTP client should use.
         */
        private String host;

        /**
         * Proxy port the HTTP client should use.
         */
        private Integer port;

        public String getHost() {
            return this.host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return this.port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

    }
}


