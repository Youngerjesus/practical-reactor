package study;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class MonoTest {

    @Test
    void mono_future_test() throws InterruptedException, ExecutionException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return "hello";
        });

        Mono<String> mono = Mono.fromFuture(completableFuture)
                .map(e -> {
                    System.out.println(Thread.currentThread().getName() + " e");
                    return e;
                });

        StepVerifier.create(mono)
                .expectNext("hello")
                .verifyComplete();
    }
    @Test
    void mono_just_test_1() {
        Mono<Integer> just = Mono.just(createMono(5));

        just.subscribe();

        just.subscribe();
    }

    private Integer createMono(Integer num) {
        System.out.println(Thread.currentThread().getName() + " called createMono " + num);
        return num * 2;
    }

    private Integer createMono2() {
        System.out.println(Thread.currentThread().getName() + " called createMono2 ");
        return 5;
    }

    @Test
    void mono_just_test_2() {
        Mono<Integer> just = Flux.just(1,1,1)
                        .flatMap(e -> Mono.just(createMono2()))
                        .collectList()
                        .thenReturn(5);

        just.subscribe();

        just.subscribe();
    }

    @Test
    void mono_deferContextual_test() {
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono.deferContextual(ctx ->
                        Mono.just(s + " " + ctx.get(key))))
                .contextWrite(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
                .expectNext("Hello World")
                .verifyComplete();
    }

    @Test
    void mono_defer() {
        Flux.just(1,2,3)
            .flatMap(e -> Mono.defer(() -> Mono.just(e)))
            .map(e -> {
                System.out.println(Thread.currentThread().getName() + " " + e);
                return e;
            })
            .subscribe();

        Sinks.Many<Object> objectMany = Sinks.many().unicast().onBackpressureBuffer();
    }
}
