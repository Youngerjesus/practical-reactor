package study;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;

public class FluxTest {

    @Test
    void handle_test() {
        Flux.just(1, 2, 3, 4)
            .handle((number, sink) -> {
                if (number % 2 == 0) {
                    sink.next(number);
                } else {
                    sink.next(number);
                }
            })
            .cast(Integer.class)
            .map(i -> i * i)
            .log()
            .subscribe(System.out::println, System.err::println);
    }

    @Test
    void reduce_test() {
        Flux.just(1, 2, 3, 4)
            .reduce((acc, val) -> {
                System.out.println("acc: " + acc);
                System.out.println("val: " + val);
                return acc + val + val;
            })
            .log()
            .subscribe();
        // 1 + 4 + 6 + 8
    }

    @Test
    void scan_test() {
        Flux.just(1, 2, 3, 4)
            .scan((acc, val) -> {
                System.out.println("acc: " + acc);
                System.out.println("val: " + val);
                return acc + val + val;
            })
            .log()
            .subscribe();
    }

    @Test
    void concatDelayError_test() {
        Flux<String> flux1 = Flux.just("A", "B", "C")
                .map((i) -> {
                    if (Objects.equals(i, "B")) {
                        throw new RuntimeException("error");
                    }
                    return i.toLowerCase();
                });

        Flux<String> flux2 = Flux.just("D", "E", "F");

        Flux<String> resultFlux = Flux.concatDelayError(flux1, flux2);

        resultFlux.subscribe(
                System.out::println,
                System.out::println,
                () -> System.out.println("Done")
        );
    }

    @Test
    void mergeSequential_vs_concat_test() {
        Flux<Integer> a = Flux.range(0, 3).delayElements(Duration.ofMillis(100));
        Flux<Integer> b = Flux.range(0, 3).delayElements(Duration.ofMillis(100));

        Flux.concat(a, b)
            .timed()
            .doOnNext(x -> System.out.println(x.get() + ": " + x.elapsed().toMillis()))
            .blockLast();
    }

    @Test
    void mergeSequential_vs_concat_test2() {
        Flux<Integer> a = Flux.range(0, 3).delayElements(Duration.ofMillis(100));
        Flux<Integer> b = Flux.range(0, 3).delayElements(Duration.ofMillis(100));

        Flux.merge(a, b)
            .timed()
            .doOnNext(x -> System.out.println(x.get() + ": " + x.elapsed().toMillis()))
            .log()
            .blockLast();
    }
}
