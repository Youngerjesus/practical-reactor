# Chapter 5 Creating Sequence Quiz 

<details>
<summary> Q.1 Mono 나 Flux 에서 null 을 내뱉을 수 있는가? 없다면 null 을 다루는 방법은 뭔가? </summary>

기본적으로 null 을 내뱉을 수 없다. `Mono.just(null)` 을 하면 nullPointerException 이 난다. 

null 을 내뱉을 수 있으려면 `Mono.justOrEmpty()` 을 하면 된다. 

</details> 

<details> 
<summary> `Mono.just` 와 `Mono.defer` 의 차이는 뭘까? 어느 상황에 각각 이것들을 사용할 수 있을까? </summary>

- just 는 객체 생성 타임에 이미 만들어지고 defer 는 target mono 를 subscribe 할 떄 만들어서 downstream 에 전달한다.
  - 즉 구독할 때 만드냐, 미리 만드냐의 차이다.
  - **defer 와 just 의 차이는 lazy 하냐 eager 하냐의 차이다.**
- ~~Mono.defer 의 한 에시는 Mono.defer() 에서 Mono 를 만들 때 Scheduler 를 갈아끼워서 다른 스레드에서 Mono 를 생산하도록 해서 blocking 당하지 않도록 하는 기법을 쓴다.~~

```java
Mono.defer(() -> this.reactiveRandomNumbers.monoFrecuency
        .subscribeOn(Schedulers.boundedElastic()));
```
  - `Mono.fromFuture` 가 있는데 그럼 이걸 왜쓰냐? 이 방식은 아닌 거 같다. 

- subscribe 할 때마다 생성하니까 조건에 따라서 subscribe 할 떄가 있고, 안할 떄가 있다면 defer 를 쓸 수 있을듯.
  - 여기서는 `switchIfEmpty()` 같은 곳에서 Mono.defer 를 사용하라고 한다. 
- Mono.defer 를 사용할 수 있는 또 다른 케이스 
  - When each subscribed execution could produce a different result
    - 하나의 결과만을 계속해서 내뱉는다면 `just` 를 쓰고 다른 결과를 만들어내야한다면 `defer` 를 통해서 계산을 해야겠지.

```java
Flux.just(1,2,3)
    .flatMap(e -> Mono.defer(() -> Mono.just(e)))
    .map(e -> {
        System.out.println(Thread.currentThread().getName() + " " + e);
        return e;
    })
    .subscribe();
```

  - `deferContextual` can be used for the current context-based evaluation of publisher
    - `deferContextual` 에서 사용하는 것도 가능하다고 함.
    - 맥락에 따라서 사용이 다른 경우에는 lazy 한 연산이 eager 한 연산과 차이를 낼 수 있다. 

#### `deferContextual` 의 사용은 뭔데? 

- downstream 에서 context 에 값을 써놓고, upstream 에서 context 의 값을 조회하는 방법
- 시용 예제를 보면 이해하기 쉽다. 

```java
String key = "message";
Mono<String> r = Mono.just("Hello")
        .flatMap(s -> Mono.deferContextual(ctx ->
                Mono.just(s + " " + ctx.get(key))))
        .contextWrite(ctx -> ctx.put(key, "World"));

StepVerifier.create(r)
        .expectNext("Hello World")
        .verifyComplete();
```

</details>

<details> 
<summary> `Mono.never()` 과 `Mono.empty()` 를 비교해보고 적용성도 비교하면 어떻게 다른가? </summary>

- Mono.empty() 는 성공적으로 완료 신호는 보내지만 Mono.never() 는 어떠한 신호도 보내지 않는다. 
- Mono.never() 는 테스트 하는 경우에 쓰이기도 한다. 타임 아웃을 내보내고 싶을 떄. 
- 또 다른 경우는 다른 publisher 를 조건에 따라서 트리거링 하고 싶지 않을 때 쓰기도 한다. 어떠한 신호를 내보내지도 않으니까. 

</details>

<details> 
<summary> `Flux.create()` 와 `Flux.push()` 그리고 `Flux.generate()` 에 대한 차이와 사용 가이드에 대해서 말해보자. </summary>

- 모두 Sink 를 이용해서 요소를 방출한다는 점은 같다. 
- Flux.generate() 는 동기식으로 one-by-one 씩 내보내고 싶을 때 쓴다. 
- Flux.create() 와 Flux.push() 는 비동기식으로 방출하지만 Flux.create() 는 multi-source 에서 요소들을 방출할 수 있기 때문에 동시성 처리를 지원한다. 
다만 Flux.push() 는 비동기식 + 싱글 스레드 기반으로 요소들을 방출한다. 
  - 이렇게 방출할 때 downstream 에서 요소를 받을 수 없을 때 사용하는 여러가지 전략들이 있다. 

</details>
