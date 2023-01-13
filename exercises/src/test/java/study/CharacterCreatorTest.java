package study;

import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class CharacterCreatorTest {

    @Test
    void test() throws InterruptedException {
        CharacterGenerator characterGenerator = new CharacterGenerator();
        List<Character> sequence1 = characterGenerator.generateCharacters().take(3).collectList().block();
        List<Character> sequence2 = characterGenerator.generateCharacters().take(2).collectList().block();

        CharacterCreator characterCreator = new CharacterCreator();
        Thread producerThread1 = new Thread(() -> characterCreator.consumer.accept(sequence1));
        Thread producerThread2 = new Thread(() -> characterCreator.consumer.accept(sequence2));

        List<Character> consolidated = new ArrayList<>();
        characterCreator.createCharacterSequence()
                .map(e -> {
                    System.out.println("doOnNext: " + Thread.currentThread().getName());
                    consolidated.add(e);
                    return e;
                })
                .subscribeOn(Schedulers.single(), false)
                .subscribe();

        characterCreator.consumer.accept(sequence1);
        producerThread1.start();
        producerThread2.start();

        producerThread1.join();
        producerThread2.join();

        consolidated.stream().forEach(System.out::println);
    }

    @Test
    void test2() throws InterruptedException {
        CharacterGenerator characterGenerator = new CharacterGenerator();
        List<Character> sequence1 = characterGenerator.generateCharacters().take(3).collectList().block();
        List<Character> sequence2 = characterGenerator.generateCharacters().take(2).collectList().block();

        CharacterCreator characterCreator = new CharacterCreator();


        Thread producerThread1 = new Thread(() -> characterCreator.consumer.accept(sequence1));
        Thread producerThread2 = new Thread(() -> characterCreator.consumer.accept(sequence2));

        List<Character> consolidated = new ArrayList<>();
        characterCreator.createCharacterSequence()
                .map(e -> {
                    System.out.println("doOnNext: " + Thread.currentThread().getName());
                    consolidated.add(e);
                    return e;
                })
                .subscribeOn(Schedulers.single(), false)
                .subscribe();

        characterCreator.consumer.accept(sequence1);
        producerThread1.start();
        producerThread2.start();

        producerThread1.join();
        producerThread2.join();

        consolidated.stream().forEach(System.out::println);
    }
}
