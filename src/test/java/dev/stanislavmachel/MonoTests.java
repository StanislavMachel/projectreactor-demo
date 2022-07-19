package dev.stanislavmachel;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class MonoTests {

    private static final FruitsFactory FRUITS_FACTORY = new FruitsFactory();

    @Test
    void testMonoSubscribe() {

        var publisher = FRUITS_FACTORY.fruitMono().log();

        StepVerifier.create(publisher)
                    .expectNext("apple")
                    .verifyComplete();
    }

    @Test
    void testMap() {
        var publisher = FRUITS_FACTORY.fruitMono()
                                      .log()
                                      .map(String::toUpperCase)
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("APPLE")
                    .verifyComplete();
    }

    @Test
    void testFlatMap() {
        var publisher = FRUITS_FACTORY.fruitMono()
                                      .flatMap(fruit -> Mono.just(List.of(fruit.split(""))))
                                      .log();

        StepVerifier.create(publisher)
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @Test
    void testFlatMapMany() {
        var publisher = FRUITS_FACTORY.fruitMono()
                                      .flatMapMany(fruit -> Flux.just(fruit.split("")))
                                      .log();

        StepVerifier.create(publisher)
                    .expectNextCount(5)
                    .verifyComplete();
    }

    @Test
    void testMonoConcatWith() {
        var appleMono = Mono.just(Fruits.APPLE);
        var mangoMono = Mono.just(Fruits.BANANA);

        var publisher = appleMono.concatWith(mangoMono).log();

        StepVerifier.create(publisher)
                    .expectNext(Fruits.APPLE, Fruits.BANANA)
                    .verifyComplete();
    }

    @Test
    void testZipWith() {
        var apple = Mono.just("apple");
        var cucumber = Mono.just("cucumber");

        var publisher = apple.zipWith(cucumber)
                             .map(objects -> objects.getT1() + objects.getT2())
                             .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "applecucumber"
                    )
                    .verifyComplete();

    }

}
