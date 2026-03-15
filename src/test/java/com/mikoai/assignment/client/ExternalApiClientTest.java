package com.mikoai.assignment.client;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

//test approach for client class - 1 success and 1 failure case for each api

@ExtendWith(VertxExtension.class)
class ExternalApiClientTest {

    private MockWebServer mockServer;
    private WebClient webClient;
    private ExternalApiClient apiClient;

    @BeforeEach
    void setUp(Vertx vertx) throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        webClient = WebClient.create(vertx);

        apiClient = new ExternalApiClient(
                vertx, webClient, mockServer.getHostName(), mockServer.getPort(), false);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
        webClient.close();
    }

    @Test
    @DisplayName("fetchPostTitle() should extract title successfully")
    void testFetchPostTitleSuccess(VertxTestContext testContext) {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":1,\"title\":\"Sunt aut facere\",\"body\":\"...\"}"));

        apiClient.fetchPostTitle()
                .onComplete(testContext.succeeding(title -> testContext.verify(() -> {
                    assertEquals("Sunt aut facere", title);
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("fetchUserName() should extract name successfully")
    void testFetchUserNameSuccess(VertxTestContext testContext) {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":1,\"name\":\"Leanne Graham\",\"username\":\"Bret\"}"));

        apiClient.fetchUserName()
                .onComplete(testContext.succeeding(name -> testContext.verify(() -> {
                    assertEquals("Leanne Graham", name);
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("fetchPostTitle() should handle API failure")
    void testFetchPostTitleFailure(VertxTestContext testContext) {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        apiClient.fetchPostTitle()
                .onComplete(testContext.failing(err -> testContext.verify(() -> {
                    assertTrue(err.getMessage().contains("404"));
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("fetchUserName() should handle API failure")
    void testFetchUserNameFailure(VertxTestContext testContext) {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        apiClient.fetchUserName()
                .onComplete(testContext.failing(err -> testContext.verify(() -> {
                    assertTrue(err.getMessage().contains("404"));
                    testContext.completeNow();
                })));
    }
}
