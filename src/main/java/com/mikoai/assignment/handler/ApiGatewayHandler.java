package com.mikoai.assignment.handler;

import com.mikoai.assignment.service.AggregatorService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiGatewayHandler {

    private static final Logger LOGGER = Logger.getLogger(ApiGatewayHandler.class.getName());

    private final AggregatorService aggregatorService;

    public ApiGatewayHandler(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    public void handle(RoutingContext ctx) {
        LOGGER.info("Handling request to /aggregate");
        aggregatorService.aggregate() // here to have fail safe can simple replace aggregate() with
                                      // aggregateWithFallback()
                .onSuccess(response -> {
                    LOGGER.info("Successfully handled /aggregate request");
                    ctx.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/json")
                            .end(response.toJson().encodePrettily());
                })
                .onFailure(err -> {
                    LOGGER.log(Level.SEVERE, "Failed to handle /aggregate request", err);
                    JsonObject error = new JsonObject()
                            .put("error", "Failed to aggregate API responses")
                            .put("detail", err.getMessage());

                    ctx.response()
                            .setStatusCode(502)
                            .putHeader("Content-Type", "application/json")
                            .end(error.encodePrettily());
                });
    }
}
