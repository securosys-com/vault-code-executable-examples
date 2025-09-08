/**
 * Copyright (c)2025 Securosys SA, authors: Tomasz Madej
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 **/
package com.securosys.tee.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseConnection implements Cloneable {
    @JsonIgnore
    private String host;
    @JsonIgnore
    private int port;
    @JsonIgnore
    private String database;
    @JsonIgnore
    private Map<String, String> parameters;
    private String username;
    private String password;
    private String table;

    // New field to track DB type (postgresql or mariadb)
    @JsonIgnore
    private String dbType;

    @JsonProperty("url")
    public void setUrl(String url) throws URISyntaxException {
        if (url.startsWith("jdbc:postgresql://")) {
            dbType = "postgresql";
            parseUrl(url, "jdbc:postgresql://", 5432);
        } else if (url.startsWith("jdbc:mariadb://")) {
            dbType = "mariadb";
            parseUrl(url, "jdbc:mariadb://", 3306);
        } else if (url.startsWith("jdbc:h2:")) {
            dbType = "h2";
            String h2UrlPart = url.substring("jdbc:h2:".length());

            // Example parsing for mem or file mode:
            if (h2UrlPart.startsWith("mem:")) {
                host = null;
                port = -1;
                dbType = "h2:mem";
                database = h2UrlPart.substring("mem:".length());  // store mem db name
            } else if (h2UrlPart.startsWith("file:")) {
                host = null;
                port = -1;
                dbType = "h2:file";
                database = h2UrlPart.substring("file:".length()); // file path
            } else if (h2UrlPart.startsWith("tcp://")) {
                // tcp://host[:port]/path
                URI uri = new URI("http://" + h2UrlPart.substring("tcp://".length()));
                host = uri.getHost();
                port = uri.getPort() == -1 ? 9092 : uri.getPort(); // default H2 port
                database = uri.getPath().substring(1); // remove leading '/'
            } else {
                // fallback for other types or embedded
                host = null;
                port = -1;
                database = h2UrlPart;
            }

            parameters = new HashMap<>();            // Parse H2 URL differently (or just store it raw)
            // Always add or override DB_CLOSE_DELAY to -1 for H2 URLs:
        } else {
            throw new IllegalArgumentException("Unsupported JDBC URL: " + url);
        }
    }

    private void parseUrl(String url, String prefix, int defaultPort) throws URISyntaxException {
        URI uri = new URI("http://"+url.substring(prefix.length()));
        this.host = uri.getHost();
        this.port = uri.getPort();
        if(this.host == null || this.port==-1){
            String withoutPrefix = url.substring(prefix.length());
            String[] hostAndDb = withoutPrefix.split("/", 2);
            String[] hostPort = hostAndDb[0].split(":", 2);

            this.host = hostPort[0];
            this.port = (hostPort.length > 1) ? Integer.parseInt(hostPort[1]) : defaultPort;
        }

        this.database = (uri.getPath() != null && uri.getPath().length() > 1) ? uri.getPath().substring(1) : null;
        this.parameters = new HashMap<>();

        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    parameters.put(kv[0], kv[1]);
                }
            }
        }
    }

    @JsonProperty("url")
    public String getUrl() {

        if (dbType == null) {
            // fallback to postgresql if not set
            dbType = "postgresql";
        }
        if (dbType.startsWith("h2")) {
            StringBuilder urlBuilder = new StringBuilder();

            if (host == null && port == -1) {
                // mem or file or embedded mode
                if (dbType.startsWith("h2:mem") || database.startsWith("h2:file")) {
                    urlBuilder.append("jdbc:").append(dbType).append(":").append(database);
                } else {
                    urlBuilder.append("jdbc:h2:").append(database); // fallback generic
                }
            } else {
                // tcp mode
                String portPart = (port == -1) ? "" : ":" + port;
                urlBuilder.append("jdbc:h2:tcp://").append(host).append(portPart).append("/").append(database);
            }

            if (parameters != null && !parameters.isEmpty()) {
                urlBuilder.append("?");
                parameters.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
                urlBuilder.setLength(urlBuilder.length() - 1); // remove trailing &
            } else {
                // if parameters is null or empty (should not happen because we add DB_CLOSE_DELAY),
                // add DB_CLOSE_DELAY anyway
                urlBuilder.append(";DB_CLOSE_DELAY=-1");
                urlBuilder.append(";DB_CLOSE_ON_EXIT=FALSE");
            }

            return urlBuilder.toString();
        }
        StringBuilder rebuiltUrl = new StringBuilder();
        rebuiltUrl.append("jdbc:").append(dbType).append("://").append(host).append(":").append(port).append("/").append(database);

        if (parameters != null && !parameters.isEmpty()) {
            rebuiltUrl.append("?");
            parameters.forEach((key, value) -> rebuiltUrl.append(key).append("=").append(value).append("&"));
            rebuiltUrl.setLength(rebuiltUrl.length() - 1); // remove trailing &
        }
        return rebuiltUrl.toString();
    }

    public DatabaseConnection(String url, String username, String password) throws URISyntaxException {
        this.username = username;
        this.password = password;
        setUrl(url);
    }

    @Override
    public DatabaseConnection clone() {
        try {
            DatabaseConnection clone = (DatabaseConnection) super.clone();
            // Deep copy mutable fields
            if (this.parameters != null) {
                clone.parameters = new HashMap<>(this.parameters);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    // Default constructor for frameworks/serialization
    public DatabaseConnection() {}
}
