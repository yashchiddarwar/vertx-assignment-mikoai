package com.mikoai.assignment.service;

import com.mikoai.assignment.client.ExternalApiClient;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//test approach for service class - 1 success and 1 failure case aggregation logic

@ExtendWith({ VertxExtension.class, MockitoExtension.class })
class AggregatorServiceTest {

    @Mock
    private ExternalApiClient apiClient;

    private AggregatorService aggregatorService;

    @BeforeEach
    void setUp() {
        aggregatorService = new AggregatorService(apiClient);
    }

    @Test
    @DisplayName("aggregate() should return correct post_title and author_name")
    void testAggregateSuccess(VertxTestContext testContext) {
        when(apiClient.fetchPostTitle()).thenReturn(Future.succeededFuture("Test Post Title"));
        when(apiClient.fetchUserName()).thenReturn(Future.succeededFuture("Test Author Name"));

        aggregatorService.aggregate()
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals("Test Post Title", response.getPostTitle());
                    assertEquals("Test Author Name", response.getAuthorName());
                    assertEquals("Test Post Title", response.toJson().getString("post_title"));
                    assertEquals("Test Author Name", response.toJson().getString("author_name"));
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("aggregate() should fail when Posts API returns failure")
    void testAggregatePostsApiFailure(VertxTestContext testContext) {
        when(apiClient.fetchPostTitle()).thenReturn(Future.failedFuture("Posts API error: HTTP 500"));
        when(apiClient.fetchUserName()).thenReturn(Future.succeededFuture("Test Author Name"));

        aggregatorService.aggregate()
                .onComplete(testContext.failing(err -> testContext.verify(() -> {
                    assertNotNull(err.getMessage());
                    assertTrue(err.getMessage().contains("500") || err.getMessage().contains("Posts"));
                    testContext.completeNow();
                })));
    }
}
