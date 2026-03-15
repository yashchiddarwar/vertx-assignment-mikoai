package com.mikoai.assignment.service;

import com.mikoai.assignment.client.ExternalApiClient;
import com.mikoai.assignment.model.AggregatedResponse;
import io.vertx.core.Future;

import java.util.logging.Logger;

public class AggregatorService {

    private static final Logger LOGGER = Logger.getLogger(AggregatorService.class.getName());

    private final ExternalApiClient apiClient;

    public AggregatorService(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Future<AggregatedResponse> aggregate() {
        LOGGER.info("Starting aggregation process...");
        Future<String> postTitleFuture = apiClient.fetchPostTitle();
        Future<String> userNameFuture = apiClient.fetchUserName();

        return Future.all(postTitleFuture, userNameFuture)
                .map(composite -> {
                    LOGGER.info("Aggregation completed successfully");
                    return new AggregatedResponse(
                            composite.resultAt(0),
                            composite.resultAt(1));
                });
    }

    // If one of the api fails, i wrote a failure safe logic.
    // Pretty siple, just recovers a fail response and replaces it with a fallback
    // value
    // not a good practice but works for this assignment
    public Future<AggregatedResponse> aggregateWithFallback() {
        Future<String> postTitleFuture = apiClient.fetchPostTitle()
                .recover(err -> Future.succeededFuture("Unknown Title"));

        Future<String> userNameFuture = apiClient.fetchUserName()
                .recover(err -> Future.succeededFuture("Unknown User"));

        return Future.all(postTitleFuture, userNameFuture)
                .map(composite -> new AggregatedResponse(
                        composite.resultAt(0),
                        composite.resultAt(1)));
    }

}
