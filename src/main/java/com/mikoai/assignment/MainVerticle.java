package com.mikoai.assignment;

import com.mikoai.assignment.client.ExternalApiClient;
import com.mikoai.assignment.handler.ApiGatewayHandler;
import com.mikoai.assignment.service.AggregatorService;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.logging.Logger;

public class MainVerticle extends VerticleBase {

    private static final Logger LOGGER = Logger.getLogger(MainVerticle.class.getName());

    static final int PORT = 8080;

    @Override
    public Future<Void> start() {
        int port = config().getInteger("http.port", PORT);

        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setConnectTimeout(5000) // setting timeout for connection
                .setIdleTimeout(10)); // setting timeout for idle connection
        // why - to prevent unreal wait times and preventing hanging of server

        ExternalApiClient apiClient = new ExternalApiClient(vertx, webClient);
        AggregatorService aggregatorService = new AggregatorService(apiClient);
        ApiGatewayHandler gatewayHandler = new ApiGatewayHandler(aggregatorService);

        Router router = Router.router(vertx);
        router.get("/aggregate").handler(gatewayHandler::handle);
        // i have created this endpoint such that even if one of api fails entire
        // request fails, can modify this to have a fallback for single api response but
        // problem statement says to return the response in combined format only.
        // wrote a failsafe logic aswell, but that function is not called anywhere
        // can be used if needed

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(server -> LOGGER.info("HTTP server started successfully on port " + server.actualPort()))
                .mapEmpty();
    }
}
