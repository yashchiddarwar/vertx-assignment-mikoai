package com.mikoai.assignment;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

//test approach for main class - check if server starts and responds
//usually not necessary but wrote it for maximum code coverage

@ExtendWith(VertxExtension.class)
class MainVerticleTest {

    @Test
    @DisplayName("HTTP server should start and /aggregate should respond")
    void testHttpServerStartsAndResponds(Vertx vertx, VertxTestContext testContext) {
        int testPort = 8081;
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", testPort));

        vertx.deployVerticle(new MainVerticle(), options)
                .compose(id -> WebClient.create(vertx)
                        .get(testPort, "localhost", "/aggregate")
                        .send())
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertTrue(response.statusCode() == 200 || response.statusCode() == 502);
                    assertNotNull(response.bodyAsJsonObject());
                    testContext.completeNow();
                })));
    }
}
