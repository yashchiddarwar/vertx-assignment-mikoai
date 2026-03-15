package com.mikoai.assignment.client;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExternalApiClient {

    private static final Logger LOGGER = Logger.getLogger(ExternalApiClient.class.getName());

    static final String DEFAULT_HOST = "jsonplaceholder.typicode.com";
    static final String POST_PATH = "/posts/1";
    static final String USER_PATH = "/users/1";

    private final WebClient webClient; // webclient for calling api
    private final CircuitBreaker postBreaker; // handling failure and retries
    private final CircuitBreaker userBreaker; // handling failure and retries
    private final String host; // host name
    private final int port; // port number
    private final boolean ssl; // ssl enabled or not

    public ExternalApiClient(Vertx vertx, WebClient webClient) {
        this(vertx, webClient, DEFAULT_HOST, 443, true);
    }

    ExternalApiClient(Vertx vertx, WebClient webClient, String host, int port, boolean ssl) {
        this.webClient = webClient;
        this.host = host;
        this.port = port;
        this.ssl = ssl;

        CircuitBreakerOptions options = new CircuitBreakerOptions()
                .setMaxFailures(3)
                .setTimeout(5000)
                .setResetTimeout(10000);

        this.postBreaker = CircuitBreaker.create("post-api-breaker", vertx, options);
        this.userBreaker = CircuitBreaker.create("user-api-breaker", vertx, options);
    }

    public Future<String> fetchPostTitle() {
        LOGGER.info("Fetching post title...");
        return postBreaker.execute(promise -> webClient.get(port, host, POST_PATH)
                .ssl(ssl)
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        LOGGER.info("Successfully fetched post title");
                        promise.complete(response.bodyAsJsonObject().getString("title"));
                    } else {
                        LOGGER.warning("Posts API error: HTTP " + response.statusCode());
                        promise.fail("Posts API error: HTTP " + response.statusCode());
                    }
                })
                .onFailure(err -> {
                    LOGGER.log(Level.WARNING, "Posts API call failed", err);
                    promise.fail(err);
                }));
    }

    public Future<String> fetchUserName() {
        LOGGER.info("Fetching user name...");
        return userBreaker.execute(promise -> webClient.get(port, host, USER_PATH)
                .ssl(ssl)
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        LOGGER.info("Successfully fetched user name");
                        promise.complete(response.bodyAsJsonObject().getString("name"));
                    } else {
                        LOGGER.warning("Users API error: HTTP " + response.statusCode());
                        promise.fail("Users API error: HTTP " + response.statusCode());
                    }
                })
                .onFailure(err -> {
                    LOGGER.log(Level.WARNING, "Users API call failed", err);
                    promise.fail(err);
                }));
    }
}
