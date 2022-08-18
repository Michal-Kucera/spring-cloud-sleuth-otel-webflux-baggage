package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.SpanCustomizer;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.WebFluxSleuthOperators;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.Callable;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/")
    public String home() {
        logger.info("Handling home");
        return "Hello World";
    }
}

@Configuration
class UserIdWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserIdWebFilter.class);

    private static final String USER_ID = "userId";

    private final Tracer tracer;

    public UserIdWebFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return WebFluxSleuthOperators.withSpanInScope(
                tracer,
                tracer.currentTraceContext(),
                exchange,
                new Callable<>() {

                    @Override
                    public Mono<Void> call() {
                        return Mono.just(generateRandomUserId())
                                .doOnNext(this::addUserIdTag)
                                .doOnNext(this::addUserIdBaggage)
                                .then(chain.filter(exchange));
                    }

                    private String generateRandomUserId() {
                        return UUID.randomUUID().toString();
                    }

                    // TODO: this is not working for reactor's threads
                    private void addUserIdBaggage(String userId) {
                        tracer.createBaggage(USER_ID, userId);
                        logger.debug("User ID baggage added");
                    }

                    private void addUserIdTag(String userId) {
                        SpanCustomizer spanCustomizer = tracer.currentSpanCustomizer();
                        if (spanCustomizer != null) {
                            spanCustomizer.tag(USER_ID, userId);
                        }
                    }
                }
        );
    }
}
