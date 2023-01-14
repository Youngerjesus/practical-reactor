package study;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

public class CharacterCreator {
    public Consumer<List<Character>> consumer;

    public Flux<Character> createCharacterSequence() {
        return Flux.push(sink -> {
            CharacterCreator.this.consumer = items -> items.forEach(e -> {
                System.out.println("sinkNext: " + Thread.currentThread().getName());
                sink.next(e);
            });
        });
    }
}
