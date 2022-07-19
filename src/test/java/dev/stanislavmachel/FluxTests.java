package dev.stanislavmachel;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

class FluxTests {

    private static final FruitsFactory FRUITS_FACTORY = new FruitsFactory();

    private static final List<String> EXPECT = List.of(
        "apple",
        "apricot",
        "avocado",
        "banana",
        "bell pepper",
        "bilberry",
        "blackberry",
        "blackcurrant",
        "blood orange",
        "blueberry"
    );

    @Test
    void testFluxSubscribe() {

        var publisher = FRUITS_FACTORY.fruitsFlux(3)
                                      .delayElements(Duration.ofMillis(1000))
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("apple",
                                "apricot",
                                "avocado")
                    .verifyComplete();

    }

    @Test
    void testFluxMap() {
        var publisher = FRUITS_FACTORY.fruitsFlux(3)
                                      .log()
                                      .map(String::toUpperCase)
                                      .log();
        StepVerifier.create(publisher)
                    .expectNext("APPLE",
                                "APRICOT",
                                "AVOCADO")
                    .verifyComplete();
    }

    @Test
    void testFluxFilter() {
        var publisher = FRUITS_FACTORY.fruitsFlux(100)
                                      .filter(fruit -> fruit.toLowerCase().startsWith("f"))
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("feijoa", "fig")
                    .verifyComplete();
    }

    @Test
    void testFluxFilterFood() {
        var publisher = FRUITS_FACTORY.foodFlux()
                                      .filter(food -> FoodType.FRUIT.equals(food.getType()))
                                      .log();

        StepVerifier.create(publisher)
                    .thenConsumeWhile(food -> FoodType.FRUIT.equals(food.getType()))
                    .verifyComplete();

        StepVerifier.create(publisher)
                    .expectNextCount(3)
                    .verifyComplete();

        StepVerifier.create(publisher)
                    .expectNextMatches(food -> FoodType.FRUIT.equals(food.getType()) && "apple".equals(food.getName()))
                    .expectNextMatches(food -> FoodType.FRUIT.equals(food.getType()) && "mango".equals(food.getName()))
                    .expectNextMatches(food -> FoodType.FRUIT.equals(food.getType()) && "banana".equals(food.getName()))
                    .verifyComplete();

        StepVerifier.create(publisher)
                    .consumeNextWith(fruit -> {
                                         Assertions.assertThat(fruit.getType()).isEqualTo(FoodType.FRUIT);
                                         Assertions.assertThat(fruit.getName()).isEqualTo("apple");
                                     }

                    )
                    .consumeNextWith(fruit ->
                                         Assertions.assertThat(fruit.getType()).isEqualTo(FoodType.FRUIT)
                    )
                    .consumeNextWith(fruit ->
                                         Assertions.assertThat(fruit.getType()).isEqualTo(FoodType.FRUIT)
                    )
                    .verifyComplete();
    }

    @Test
    void testFluxFilterMapFood() {
        var publisher = FRUITS_FACTORY.foodFlux()
                                      .filter(food -> FoodType.FRUIT.equals(food.getType()))
                                      .map(Food::getName)
                                      .map(String::toUpperCase)
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("APPLE", "MANGO", "BANANA")
                    .verifyComplete();

    }

    @Test
    void testFluxFilterFlatMapFood() {
        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .flatMap(fruit -> Flux.just(fruit.split(""))) // "apple" + "apricot" = 5 + 7 = 12 chars
                                      .log();

        StepVerifier.create(publisher)
                    .expectNextCount(12)
                    .verifyComplete();

    }

    @Test
    void testFluxFilterFlatMapAsync() {
        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .flatMap(fruit -> Flux.just(fruit.split(""))
                                                            .delayElements(Duration.ofMillis(
                                                                new Random().nextInt(2000)
                                                            ))) // "apple" + "apricot" = 5 + 7 = 12 chars

                                      .log();

        StepVerifier.create(publisher)
                    .expectNextCount(12)
                    .verifyComplete();

    }

    @Test
    void testFluxFilterConcatMapAsync() {
        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .concatMap(fruit -> Flux.just(fruit.split(""))
                                                              .delayElements(Duration.ofMillis(
                                                                  new Random().nextInt(2000)
                                                              ))) // "apple" + "apricot" = 5 + 7 = 12 chars

                                      .log();

        StepVerifier.create(publisher)
                    .expectNextCount(12)
                    .verifyComplete();

    }

    @Test
    void testFluxTransform() {

        Function<Flux<String>, Flux<String>> filter = data -> data.filter(s -> s.length() > 5);

        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .transform(filter)
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("apricot")
                    .verifyComplete();

    }

    @Test
    void testFluxTransformDefaultIfEmpty() {

        Function<Flux<String>, Flux<String>> filter = data -> data.filter(s -> s.length() > 10);

        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .transform(filter)
                                      .defaultIfEmpty("default fruit")
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("default fruit")
                    .verifyComplete();

    }

    @Test
    void testFluxTransformSwitchIfEmpty() {

        Function<Flux<String>, Flux<String>> filter = data -> data.filter(s -> s.length() > 7);

        var publisher = FRUITS_FACTORY.fruitsFlux(2)
                                      .transform(filter)
                                      .switchIfEmpty(Flux.just("Pineapple", "Jack Fruit"))
                                      .transform(filter)
                                      .log();

        StepVerifier.create(publisher)
                    .expectNext("pineapple", "jack fruit")
                    .verifyComplete();

    }

    @Test
    void testFluxConcat() {

        var fruits = Flux.just("apple", "mango");
        var vegetables = Flux.just("cucumber", "tomato");

        var publisher = Flux.concat(fruits, vegetables)
                            .log();

        StepVerifier.create(publisher)
                    .expectNext("apple", "mango", "cucumber", "tomato")
                    .verifyComplete();

    }

    @Test
    void testFluxConcatWith() {

        var fruits = Flux.just("apple", "mango");
        var vegetables = Flux.just("cucumber", "tomato");

        var publisher = fruits.concatWith(vegetables).log();

        StepVerifier.create(publisher)
                    .expectNext("apple", "mango", "cucumber", "tomato")
                    .verifyComplete();

    }

    @Test
    void testFluxMerge() {

        var fruits = Flux.just("apple", "mango")
                         .delayElements(Duration.ofMillis(50));
        var vegetables = Flux.just("cucumber", "tomato")
                             .delayElements(Duration.ofMillis(75));

        var publisher = Flux.merge(fruits, vegetables)
                            .log();

        StepVerifier.create(publisher)
                    .expectNext("apple", "cucumber", "mango", "tomato")
                    .verifyComplete();

    }

    @Test
    void testFluxMergeWith() {

        var fruits = Flux.just("apple", "mango")
                         .delayElements(Duration.ofMillis(50));
        var vegetables = Flux.just("cucumber", "tomato")
                             .delayElements(Duration.ofMillis(75));

        var publisher = fruits.mergeWith(vegetables)
                              .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "apple",
                        "cucumber",
                        "mango",
                        "tomato"
                    )
                    .verifyComplete();
    }

    @Test
    void testMergeSequential() {

        var fruits = Flux.just("apple", "mango")
                         .delayElements(Duration.ofMillis(50));
        var vegetables = Flux.just("cucumber", "tomato")
                             .delayElements(Duration.ofMillis(75));

        var publisher = Flux.mergeSequential(fruits, vegetables)
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "apple",
                        "mango",
                        "cucumber",
                        "tomato"
                    )
                    .verifyComplete();

    }

    @Test
    void testZip() {
        var fruits = Flux.just("apple", "mango");
        var vegetables = Flux.just("cucumber", "tomato");

        var publisher = Flux.zip(fruits, vegetables, (fruit, vegetable) -> fruit + vegetable)
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "applecucumber",
                        "mangotomato"
                    )
                    .verifyComplete();

    }

    @Test
    void testZip2() {
        var fruits = Flux.just("apple", "mango");

        var vegetables = Mono.just("cucumber");

        var publisher = Flux.zip(fruits, vegetables, (fruit, vegetable) -> fruit + vegetable)
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "applecucumber"
                    )
                    .verifyComplete();

    }

    @Test
    void testZipWith() {
        var fruits = Flux.just("apple", "mango");
        var vegetables = Flux.just("cucumber", "tomato");

        var publisher = fruits.zipWith(vegetables, (fruit, vegetable) -> fruit + vegetable)
                              .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "applecucumber",
                        "mangotomato"
                    )
                    .verifyComplete();

    }

    @Test
    void testZipTuple() {
        var fruits = Flux.just("apple", "mango");
        var vegetables = Flux.just("cucumber", "tomato");
        var berries = Flux.just("strawberry", "cherry");

        var publisher = Flux.zip(fruits, vegetables, berries)
                            .map(objects -> objects.getT1() + objects.getT2() + objects.getT3())
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "applecucumberstrawberry",
                        "mangotomatocherry"
                    )
                    .verifyComplete();

    }

    @Test
    void testOnNextSubscribeComplete() {

        var publisher = Flux.fromIterable(List.of("mango", "orange", "banana"))
                            .filter(s -> s.length() > 5)
                            .doOnNext(fruit -> System.out.println("fruit = " + fruit))
                            .doOnSubscribe(subscription -> System.out.println("subscription = " + subscription))
                            .doOnComplete(() -> System.out.println("completed"))
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "orange",
                        "banana"
                    )
                    .verifyComplete();
    }

    @Test
    void testOnErrorReturn() {

        var publisher = Flux.just("mango", "orange")
                            .concatWith(Flux.error(
                                new RuntimeException("Exception occurred")
                            ))
                            .onErrorReturn("banana")
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "mango",
                        "orange",
                        "banana"
                    )
                    .verifyComplete();

        var publisher2 = Flux.just("orange", "mango")
                             .map(s -> s + s.charAt(5))
                             .onErrorReturn("NPE")
                             .log();

        StepVerifier.create(publisher2)
                    .expectNext(
                        "orangee",
                        "NPE"
                    )
                    .verifyComplete();
    }

    @Test
    void testOnErrorContinue() {

        var publisher = Flux.just("mango", "orange", "banana")
                            .map(fruit -> {
                                if (fruit.equalsIgnoreCase("orange")) {
                                    throw new RuntimeException("This is orange!");
                                }
                                return fruit.toUpperCase();
                            })
                            .onErrorContinue((throwable, fruit) -> {
                                System.out.println("throwable = " + throwable);
                                System.out.println("fruit = " + fruit);
                            })
                            .log();

        StepVerifier.create(publisher)
                    .expectNext(
                        "MANGO",
                        "BANANA"
                    )
                    .verifyComplete();
    }

    @Test
    void testOnErrorMap() {

        var publisher = Flux.just("mango", "orange", "banana")
                            .map(fruit -> {
                                if (fruit.equalsIgnoreCase("orange")) {
                                    throw new RuntimeException("This is orange!");
                                }
                                return fruit.toUpperCase();
                            })
                            .onErrorMap(throwable -> {
                                System.out.println("throwable = " + throwable);
                                return new IllegalStateException("from onErrorMap");
                            })
                            .log();

        StepVerifier.create(publisher)
                    .expectNext("MANGO")
                    .expectError(IllegalStateException.class)
                    .verify();
    }

}
