# Project Reactor 

https://projectreactor.io/docs/core/release/reference/

***

## 3. Introduction to Reactive Programming

- Reactive Streams 는 push 방식이라는 것. 

## 3.3.4. Nothing Happens Until You subscribe()

- subscribe 하기 전까지는 아무 일도 안일어난다. 
- subscribe 를 하면 내부적으로 subscrbier 가 request 시그널을 보내서 데이터의 흐름이 시작된다. 

## A.2. Transforming an Existing Sequence

- I want to transform existing data:
  - 1-to-1 변경은 `map (Flux|Mono)`
    - 단순히 casting 이라면 `cast (Flux|Mono)`
    - 순서도 포함하고 싶다면 `index (Flux|Mono)`
  - 1-to-n 변경 (string to their characters) `flatMap (Flux|Mono)`
  - 1-to-n 변경인데 방출되는 요소를 보고 프로그래밍 적으로 제어를 하고 싶을 때 `handle (Flux|Mono)`
    - `sink` 를 통해 `next, error, complete` 제어를 해야함
  - asynchronous 한 작업을 하고 싶다면 `flatMap (Flux|Mono)` or async `Publihser`
    - 방출되는 데이터를 무시하고 결과만 주고 싶다면 `flatMap` 안에다가 `Mono.empty()` 를 리턴하라.
    - original sequence order 를 유지하고 싶다면 `Flux#flatMapSequential` 을 쓰자.
      - 비동기 작업 후 reorder 과정을 겪는다.
    - `Mono` 에서 async 한 작업 후 여러 개의 아이템을 방출한다면 `Mono#flatMapMany` 를 쓰자. 

- I want to add pre-set elements to an existing sequence
  - at the start
    - `Flux#startWith`
  - at the end 
    - `Flux#concatWithValues`

- I want aggregate a Flux 
  - into a List
    - `Flux#collectList`, `Flux#collectSortedList`
  - into a Map
    - `Flux#collectMap`, `Flux#collectMultiMap`
      - `MultiMap` 은 키 안에 values 가 여러개임. 그냥 `collectMap` 은 덮어씌운다. 
  - into a arbitrary container
    - `Flux#collect`
  - into the size of the sequence 
    - `Flux#count`
  - 방출되는 요소와 요소에 function 을 적용해서 하나의 Object 로 만드는 것. (e.g running sum) 
    - `Flux#reduce`
  - `reduce` 와 유사하지만, 축적되는 중간 과정의 요소들을 방출하고 싶다면 `Flux#scan`
  - into a boolean value from predicate
    - 모든 요소에 적용하고 싶다면 `Flux#all`
    - 최소 하나의 요소에 적용하고 싶다면 `Flux#any`
    - 최소 하나의 요소가 있는지 검사하고 싶다면 `Flux#hasElements()`
    - 주어진 요소가 있는지 검사하고 싶다면 `Flux#hasElement()`

- I want to combine publishers
  - in sequential order
    - `Flux#concat` or `concatWith (Flux|Mono)`
      - `concat` 은 시작할 때 static method 이고 `concatWith` 는 publihser 에서 호출하는 메소드 
    - 에러가 났을 떄 남은 source 들은 interrupt 받고 싶지 않다면 `Flux#concatDelayError` 를 쓰면 된다. 
      - `concat` 은 에러가 나면 남은 source 들은 Interrupt 받아서 멈춘다. 
    - eagerly subscribing 을 하고 싶다면 `Flux#mergeSequential` 을 해라.
      - `merge` 와 `concat`의 차이는 subscribe 시점의 차이인데 `merge` 는 만들 때부터 이미 `subscription` 객체를 만들어서 emit 된 요소들을 캐싱해두고 있다.
      - 그래서 `mergeSequential` 이 더 빠르긴 하지만 메모리가 계속 쓰이고 있기도 하다. 
      - 이 subscribe 되는 시점을 알아야 실수를 안하는데 예로 데이터베이스에 쓰고, 읽는 두 개의 publisher 가 있다고 하자. `concat` 으로 하면 다 쓰고 읽는다. `mergeSequential` 로 하면 써지기 전에 먼저 읽어올 수 있다.
  - in emission order (combined item emitted as they come = 들어온 순서대로 방출하고 싶다면)
    - `Flux#merge or mergeWith`
      - 이건 finite, async 원천 소스와 잘 어울린다. infinite 와는 어울리지 않는다고함. 왜냐하면 다른 publisher 를 subscribe 하기 전에 drain 시킬려는 경향이 있어서. 
    - 다른 타입과 merge 할려면 `Flux#zip or zipWith`
  - by pairing values
    - 2 개 Mono 를 하나로 `Mono#zipWith`
    - N 개 Mono 를 하나로 `Mono#zip`
  - by coordination their termination 
    - 1 개 Mono 와 any source 를 `Mono<Void>` 완료로. `Mono#and`
    - N 개의 소스로부터 완료될 때 `Mono#when`
    - arbitrary container type 으로 변경 
      - `Flux#zip`
      - `Flux#combineLast`
        - 소드를로 부터 마지막에 들어온 요소들만을 합침. 
  - 가장 먼저 오는 첫 번째 publisher 를 고르는 것.
    - `Flux#firstWithValue`
    - `Flux#firstWithSignal`
  - 들어온 요소로부터 새로운 publisher 를 만든다. 
    - `Flux#switchMap or switchOnNext`

- i want to repeat an existing sequence. 
  - `repeat (Flux|Mono)`
  - time interval 을 주고 싶다면 
    - `Flux.interval`

- i have an empty sequence but...
  - i want a value instead 
    - `defaultIfEmpty (Flux|Mono)`
  - i want another sequence instead 
    - `switchIfEmpty (Flux|Mono)`

- i have a sequences but i am not interested in values.
  - ignoring values
    - `Flux.ignoreElements()`
    - `Mono.ignoreElement()`
  - I want the completion represented as `Mono<Void>`
    - `then (Flux|Mono)`
  - I want to wait another task to complete at the end
    - `thenEmpty (Flux|Mono)`
  - I want to switch to another Mono at the end
    - `Mono#then(mono)`
  - I want to emit a single value at the end
    - `Mono#thenReturn((T)`
  - I want to switch to a Flux at the end.
    - `thenMany (Flux|Mono)`

- I have a Mono for which i want to defer completion
  - until another publisher, which is derived from this value, has completed
  - `Mono#delayUntil(function)`
  - 완료 신호를 늦추고 싶을 때 여기서는 값에 관심 없는게 아님. 

- i want to expand elements recursively into a graph of sequences and emit combinations
  - `expand(function) (Flux|Mono)`
    - bfs 방식으로 추가. 
  - `expandDeep(function) (Flux|Mono)`

## A.4. Filtering a Sequence

- I want to filter a sequence
  - 임의 조건을 기반으로 필터 `filter (Flux|Mono)`
    - filter 조건이 async 하게 결정된다면 `filterWhen (Flux|Mono)`
      - 조건이 true 면 replay 되는 식으로 요소가 들어온다. 
      - 조건이 false 나 empty 면 방출되는 요소는 drop 된다.
      - 중요한 건 여기서 filterWhen 으로 전달한 publisher 의 첫 번째 값만이 이용된다는 점이다. Mono 가 아니라면 첫 요소 이후에 취소될 것. 
  - 방출되는 요소의 type 을 보고 제한거는 것 `ofType (Flux|Mono)`
  - 요소들을 모두 무시하는 것 `Flux#ignoreElements`, `Mono#ignoreElement`
  - 중복되는 값을 무시하는 것 
    - 전체 sequence 중에서 중복 제거 `Flux#distinct`
    - 같은 요소가 연속으로 오는 걸 막는 것. `Flux#distinctUntilChanged`
      - distinct 가 모든 요소를 가지고 있어서 중복을 제거하는 데 여기서는 그러지 않는다. 
      - hashCode 충돌로 인한 구별을 못하는 것보다는 엄격한 구별을 한다. 
        - equals 비교와 hashcode 비교가 있는데, hashcode 는 힙 메모리에 있는 주소를 바탕으로 값을 매긴다. 일반적으로. 
        - 그래서 동일한 객체라면 equals 가 되면 hashcode 는 당연히 되야하는거지만, hashcode 는 안되는데 equals 가 되는 건 있을 수 없다. 
        - hashcode 는 이렇게 중복이 될 수 있으므로 hashtable 에서 설계할 때 알고있어야한다. 

- I want to keep a subset of the sequence
  - by taking N elements 
    - 시작부터 N 개 `Flux#take(long)`
      - unbounded 요청하고 N 개 `Flux#take(long, false)`
        - 옵션으로 true 를 주면 해당 N 개 만큼 cap 해서 upstream 으로 request 보낸다. upstream 은 N 개보다 더 생산해서 내보내지 않는거지. 
        - 실제로 이 값 true 고 0 개를 요청하면 subscribe 되지도 않는다.
        - false 로 옵션주면 unbounded 요청을 하니까 불필요한게 많이 생산될 수 있다. 웬만하면 true 를 주자.
      - duration 에 기반한 것. `Flux#take(Duration)`
      - Mono 와 같이 오로지 첫 번째 요소가 중요한 것이라면 `Flux#next`
    - 끝에서부터 N 개 `Flux#takeLast(long)`
    - 조건을 만나기 전까지 `Flux#takeUntil (predicate-based)` or `Flux#takeUntilOther (companion publihser-based)`
      - 멈춰야 할 때를 정해주는 것들.

  - by taking at most 1 element
    - 구체적인 위치를 통해서 가져오기 `Flux#elementAt`
    - 마지막 하나. `Flux#takeLast(1)`
      - 만약 empty 면 에러 `Flux#last`
      - 만약 empty 면 기본값. `Flux#last(t)`
  
  - by skipping elements
    - 시작부터 skip `Flux#skip(long)`
      - duration 에 기반한 skip `Flux#skip(duration)`
    - 마지막부터 skip `Flux#skipLast`
    - 조건을 만날 때까지 `Flux#skipUntil (predicate-base)` or `Flux#skipUntilOther(publisher) (companion publihser-based)`
    - 조건을 만나는 동안 skip `Flux#skipWhile`

  - by sampling items
    - duration 에 기반한 sample `Flux#sample(timespan)`
      - timespan 을 기반으로 요소를 방출한다. 요소는 해당시간동안 맨 마지막에 방출항 애. 
      - 완료전에 마지막으로 방출한 요소의 경우에는 onComplete signal 과 함께 나온다.
      - 마지막 대신 첫 번째 요소를 가지고 싶다면 `Flux#sampleFirst`
    - publisher 기반의 sample `Flux#sample(publihser)`
      - publisher 의 방출 타이밍에 맞춰서 window 가 결정됨.
    - publihser + timeout `Flux#sampleTimeout`

- i expect at most 1 element (error if more than one)
  - and i want error if the sequence is empty `Flux#single`
  - and i want default value if the sequence is empty `Flux#single(value)`
  - and i accept an empty sequence as well: `Flux#singleOrEmpty`


## A.3. Peeking into a Sequence

peeking 이란 뜻 자체는 훔쳐보다, 살짝 보다 이런 의미가 있다.

- without modifying the final sequence, i want to
  - get notified of / execute additional behavior
    - emission: `doOnNext (Flux|Mono)`
    - completion: `Flux#doOnComplete, Mono#doOnSuccess`
    - error termination: `doOnError (Flux|Mono)`
    - cancellation: `doOnCancel (Flux|Mono)`
    - "start" of the sequence: `doFirst (Flux|Mono)`
      - subscribe 되기 전에 먼저 실행됨. 
      - 여러개의 doFirst 가 실행되면 역순으로 실행된다. 
      - this is tied to `Publisher#subscribe(Subscriber)`
    - post-subscription : `doOnSubscribe (Flux|Mono)`
    - request: `doOnRequest (Flux|Mono)`
    - completion or error: `doOnTerminate (Flux|Mono)
      - but after it has been propagated downstream: `doAfterTerminate (Flux|Mono)`
    - any type of signal, represented as a Signal: `doOnEach (Flux|Mono)`
      - signal 로는 onError or onComplete, onNext, onSubscribe 가 있다. 
    - any terminating condition (complete, error, cancel): `doFinally (Flux|Mono)`

  - log what happens internally: `log (Flux|Mono)`

- I want to know of all events:
  - each represented as Signal object:
    - in a callback outside the sequence: `doOnEach (Flux|Mono)`
    - instead of the original onNext emissions: `materialize (Flux|Mono)`
      - onNext, onError, onComplete 와 같이 오는 것을 `Signal` instance 로 받을 수 있다고함.
      - error 도 signal 로 되기 때문에 에러가 온 이후에 completion 도 온다. 종료를 위해서.
      - and get back to the onNexts: `dematerialize (Flux|Mono)`
        - signal 다시 원래의 요소로 돌리는 것.
        - error signal 은 onError 를 일으키는 것과 같다.
  - as a line in a log: `log (Flux|Mono)`

## A.1. Creating a New Sequence

- that emits a T, and I already have: `just (Flux|Mono)`
  - from an `Optional<T>: Mono#justOrEmpty(Optional<T>)`
  - from a potentially null `T: Mono#justOrEmpty(T)`

- that emits a T returned by a method: `just (Flux|Mono)` as well
  - but lazily captured: use `Mono#fromSupplier` or wrap `just (Flux|Mono)` inside `defer (Flux|Mono)`

- that emits several T I can explicitly enumerate: `Flux#just`
  - 여러개를 열거해서 방출하고 싶을 때 쓰는 것. 

- that iterates over:
  - an array: `Flux#fromArray`
  - a collection or iterable: `Flux#fromIterable`
  - a range of integers: `Flux#range`
  - a Stream supplied for each Subscription: `Flux#fromStream(Supplier<Stream>)`
    - stream 은 재사용되지 않는다. 다 쓰면 자동으로 닫힌다. multiple subscribe 나 re-subscribe 되었을 때 replay 가 되지 않는다. 이 뜻. 

- that emits from various single-valued sources such as:
  - a Supplier<T>: `Mono#fromSupplier`
    - supplier 로 부터 Mono 를 생성해서 방출하는 것. null 이면 complete empty 가 나간다.
  - a task: `Mono#fromCallable`, `Mono#fromRunnable`
    - fromCallable 은 fromSupplier 와 같다.
  - a `CompletableFuture<T>: Mono#fromFuture`

- that completes: `empty (Flux|Mono)`
  - but lazily build the `Throwable: error(Supplier<Throwable>) (Flux|Mono)`

- that never does anything: `never (Flux|Mono)`
  - signal 을 안보내는 것. 

- that is decided at subscription: `defer (Flux|Mono)`
  - subscription 이 만들어 질때까지 publisher 생성을 미루는 것. 

- that depends on a disposable resource: `using (Flux|Mono)`
  - 데이터베이스 커넥션과 같은 Resource 를 subscriber 들을 위해서 확보하고 release 까지 해주는 것. 
  - 내부적으로 publisher 를 매개변수로 받는다. 
  - 비동기적으로 resource 를 cleanup 할려면 `usingWhen (Flux|Mono)`

- that generates events programmatically (can use state):
  - synchronously and one-by-one: `Flux#generate` 
    - 이벤트를 동기식으로 발행하는 것.  
    - 내부적으로 상태를 관리하는 것도 가지고 있다.
  - asynchronously (can also be sync), multiple emissions possible in one pass: `Flux#create (Mono#create as well, without the multiple emission aspect)`
    - 동기식, 비동기식으로 방출 가능 
    - 여러 스레드에서 방출하는 것도 가능.

#### Flux Generate Example 
```java
public class FluxGenerateExample {
    public static void main(String[] args) {
        Flux.generate(() -> 0, (state, sink) -> {
            sink.next("Value: " + state);
            if (state == 10) {
                sink.complete();
            }
            return state + 1;
        }).subscribe(System.out::println);
    }
}
```

#### Flux Generate Example 2 

````java
public class CharacterGenerator {
    
    public Flux<Character> generateCharacters() {
        
        return Flux.generate(() -> 97, (state, sink) -> {
            char value = (char) state.intValue();
            sink.next(value);
            if (value == 'z') {
                sink.complete();
            }
            return state + 1;
        });
    }
}
````

- application 의 상태에 영향을 받고, emit 할 요소를 calculation 후 방출하고 싶을 때 사용한다.
- generate 에서는 한번의 next() 밖에 못한다. 

#### Flux Create Example
```java
import reactor.core.publisher.Flux;

public class FluxCreateExample {
    public static void main(String[] args) {
      Flux.<String>create(emitter -> {

        ActionListener al = e -> {
          emitter.next(textField.getText());
        };
        // without cleanup support:

        button.addActionListener(al);

        // with cleanup support:

        button.addActionListener(al);
        emitter.onDispose(() -> {
          button.removeListener(al);
        });
      });
    }
}
```

- emitter (sinks) 언제 emit 해서 요소를 방출할 건지 조건을 정해놓으면 그거에 따른 flux 가 생성되고 처리할 수 있는건가.
- generate 와 다르게 application 의 영향을 받지 않고, multiple value or infinity values 를 계산해서 방출하고 싶을 때 쓴다.
- 기본적으로 Emit 된 아이템들이 downstream 에서 사용할 수 없다면 버퍼링된다. 많으면 drop 되겠지.

#### Flux Create Example 

````java
public class CharacterGenerator {
    
    public Flux<Character> generateCharacters() {
        
        return Flux.generate(() -> 97, (state, sink) -> {
            char value = (char) state.intValue();
            sink.next(value);
            if (value == 'z') {
                sink.complete();
            }
            return state + 1;
        });
    }
}
````

````java
public class CharacterCreator {
    public Consumer<List<Character>> consumer;

    public Flux<Character> createCharacterSequence() {
        return Flux.create(sink -> CharacterCreator.this.consumer = items -> items.forEach(sink::next));
    }
}
````

````java
@Test
public void whenCreatingCharactersWithMultipleThreads_thenSequenceIsProducedAsynchronously() throws InterruptedException {
    CharacterGenerator characterGenerator = new CharacterGenerator();
    List<Character> sequence1 = characterGenerator.generateCharacters().take(3).collectList().block();
    List<Character> sequence2 = characterGenerator.generateCharacters().take(2).collectList().block();
}
````

````java
CharacterCreator characterCreator = new CharacterCreator();
Thread producerThread1 = new Thread(() -> characterCreator.consumer.accept(sequence1));
Thread producerThread2 = new Thread(() -> characterCreator.consumer.accept(sequence2));
````

````java
List<Character> consolidated = new ArrayList<>();
characterCreator.createCharacterSequence().subscribe(consolidated::add);
````

- 정리해보자. 
- `Flux#create()` 에서 하는 일. 구독하면 다른 곳에서 생성해준 객체를 방출함. 
- `Flux#create().sinks` 가 하는 일. 어떤 이벤트가 올 때 값을 방출할건지 정의해야한다.
  - `next` 호출을 해줘야한다. 그래야 값이 생성됨. 
  - `next` 호출을 해줄 수 있는 조건을 `Flux#create` 에서 만들어줘야한다. 
- 외부에서 해당 이벤트를 발행시킨다. 

## 4.4. Programmatically creating a sequence

## 4.4.1. Synchronous generate

- one-by-one emission 가능 
- state 를 통한 complete, next, error 조절 가능.

#### Generate Example 추가 

```java
Flux<String> flux = Flux.generate(
    AtomicLong::new,
      (state, sink) -> { 
      long i = state.getAndIncrement(); 
      sink.next("3 x " + i + " = " + 3*i);
      if (i == 10) sink.complete();
      return state; 
    }, (state) -> System.out.println("state: " + state));
```

- mutate state 를 atomic 한 변수로 사용하는 것.

## 4.4.2. Asynchronous and Multi-threaded: create

- FluxSink 를 기반으로 동작
- blocking 하면 deadlock 걸림.
- `subscribeOn(schedulers, requestOnSeparateThread = false)` 를 언제하는지 명확하게 이해되지 않는다. 이 설정이 요청과 처리를 분리할 건지 현재의 스레드에서 요청과 처리 모두 담당할건지 를 말하는거 같은데 흠.
- `create` 는 존재하는 API 를 리액티브 세상으로 올 수 있도록 하는 유용함을 가지고 있다.

- 여기서 사용하는 `OverflowStrategy` 전략은 다음과 같다. 
  - `IGNORE`
    - downstream 의 backpressure 요청을 무시. 
    - queue 가 다차면 에러를 던진다.
  - `ERROR`
    - downstream 이 견디지 못하면 `IllegalStateException` 를 던진다.
  - `DROP`
    - downstream 이 받을 준비가 안되면 drop 한다. 
  - `LATEST`
    - 가장 최근의 시그널만 받도록 한다.
  - `BUFFER (default)`
    - 버퍼링한다. 못견딜때까지.

#### Flux Create Example 

```java
Flux<String> bridge = Flux.create(sink -> {
    myEventProcessor.register( 
      new MyEventListener<String>() { 

        public void onDataChunk(List<String> chunk) {
          for(String s : chunk) {
            sink.next(s); 
          }
        }

        public void processComplete() {
            sink.complete(); 
        }
    });
});
```

- `bridge` 를 먼저 만들어진 후에 이벤트를 받아야한다. 

## 4.4.3. Asynchronous but single-threaded: push

- `generate` 와 `create` 의 중간 단계이다.

```java
Flux<String> bridge = Flux.push(sink -> {
    myEventProcessor.register(
      new SingleThreadEventListener<String>() { 

        public void onDataChunk(List<String> chunk) {
          for(String s : chunk) {
            sink.next(s); 
          }
        }

        public void processComplete() {
            sink.complete(); 
        }

        public void processError(Throwable e) {
            sink.error(e); 
        }
    });
});
```

- `SingleThreadEventListener` 를 이용해서 Flux 를 내보내는 bridge 역할을 한다. 
- `Flux#create` 와 `Flux#push` 차이는 뭘까? 
  - 일단 `Flux#create` 와 `Flux#push` 모두 `sink.next()` 를 해줄 수 있는 consumer 를 만든다. 
    - 여기에 이벤트 리스너 같은 걸 붙혀놓기도 하지. `sink.next()` 를 호출할려고.
    - `sink.next()` 는 뭔데? 
      - `queue` 에 넣고 subscriber 에게 전달해주는 `onNext()` 를 호출해주는 역할을 담당한다. 
      - 그래서 sink 가 push 한 요소를 다른 publihser 에서 전달받을 수 있는거다.  
  - ~~그러면 `sink.next()` 이 부분이 하나의 스레드에서만 호출될 수 있는지, 여러 스레드에서 호출될 수 있는지 이 차인가.~~
  - `Flux#create` 와 `Flux#push` 는 내부적으로 `FluxCreate()` 를 생성하는 과정에서 차이가 있다. 
    - `FluxCreate.CreateMode` 가 다르다. 
      - `Flux.create`
        - `FluxCreate.CreateMode.PUSH_PULL`
      - `Flux.push()`
        - `FluxCreate.CreateMode.PUSH_ONLY`
      - 이 차이로 인해서 Flux.create 는 SerializedFluxSink 로 래핑된다.
  - `Flux#create` 는 멀티스레딩 소스를 지원한다고 하는데 어떻게 지원할 수 있을까? 
    -  내부적으로 `AtomicIntegerFieldUpdater` 를 이용해서 동시성 처리를 한다.
      - `sink.next()` 를 호출해서 subscribe 에 전달까지 해주는 애가 작동하고 있다면, 다른 스레드는 그냥 queue 에 넣어주기만 한다.
      - 이런 동시성 처리를 지원해준다. 
    - `Flux.push()` 는 싱글 스레드이므로 그냥 sink.next() 만 해주면 되는거니까. 동시성 처리를 위한 지원이 필요없다.


## 4.4.4. Handle

- handle 은 pulisher 에서 나온 녀석을 보고 다음 publisher 로 방출할건지, complete 때릴건지를 결정해준다. 

## 4.7 Sinks 

- Sinks 는 안전하게 트리거 될 수 있다. 그래서 특정 이벤트가 발생했을 때 신호를 생성해서 전달하는게 가능.
- Sinks 는 독립적으로 작동하며 다른 구성요소와 분리될 수 있다.
- Sinks 는 여러 Subscriber 와 상호작용할 수 있다. 단 unicast() 와 같은 특정 방식의 subscriber 는 제외.
  - unicast() 는 오로지 한 개의 publisher 가 한 개의 subscriber 에게만 데이터를 전달할 수 있는 방식이디.
  - unicast() 와 달리 multicast() 는 한 개의 발신자가 동시에 여러 수신자에게 전달할 수 있다.

## 4.7.1. Safely Produce from Multiple Threads by Using Sinks.One and Sinks.Many

- Sinks 를 이용하면 안전하게 데이터 생성 가능. 
- tryEmit 과 EmitAPI 를 이용해서 여러 스레드에서 병렬로 호출할 수 있고, 실패한다면 빠르게 실패해서 재시도를 하도록 할 수 있다.
- Sinks 는 Flux 와 같이 배압 제어 (onBackpressureBuffer) 와 같은 기능을 제공할 수 있다.
- Sinks.Many 는 DownStream Subscriber 에게 Flux 로 전달할 수 있고, Sinks.Empty 와 Sinks.One 은 Mono 로 제공가능.
- Sinks.many().multicast())
  - 새로 푸쉬된 데이터를 Subscriber 에게 전달하는 싱크
- Sinks.many().unicast()
  - 첫 번째 subscriber 가 등록되기 전에 푸쉬된 데이터만 버퍼링
- many().replay(): 지정된 기록 크기의 데이터를 새 구독자에게 다시 재생한 후 실시간으로 새 데이터를 푸시하는 싱크
- one(): 단일 요소를 구독자에게 재생하는 싱크
- empty(): 오직 종료 신호만 구독자에게 재생하는 싱크. Mono<T> 형태로 볼 수 있습니다.

- 이 내용은 리액터 프레임워크를 사용하여 여러 스레드에서 안전하게 데이터를 생산하고, 동시성 문제를 해결하는 방법에 대한 설명입니다.


## 4.7.2. Overview of Available Sinks

Sinks.many().unicast().onBackpressureBuffer(args?)

- Sinks.many 는 내부적으로 buffer 를 이용해서 backpressure 를 다룬다. 위의 메소드는 그리고 최대 하나의 subscriber 만 가질 수 있다.
- 사용 방법은 `Sinks.many().unicast().onBackpressureBuffer()` 를 하면 된다.
- 기본적으로 unbounded 로 배압을 조절하기 떄문에 커스텀한 구현을 하려면 Queue 를 구현해얗나다. `Sinks.many().unicast().onBackpressureBuffer(Queue)`
  - 만약 이런 상황에서 queue 가 bounded 하다면 버퍼가 가득 차고 downstream 으로부터의 요청이 수신되지 않았을 때는 Sink 는 요청을 거절한다. 

Sinks.many().multicast().onBackpressureBuffer(args?)
- 여러개의 subscrier 에게 데이터를 전달할 수 있다. backpressure 는 각각의 subscriber 를 존중하면서. 
- 사용 방법은 `Sinks.many().multicast().onBackpressureBuffer().` 이렇다.
- 모든 subscriber 가 구독을 취소한다면 내부 internal buffer 는 정리된다. 그리고 새로운 subscriber 를 받는 것도 거절한다. 이것과 관련된 설정은 `autoCancel` 파라미터로 할 수 있다. 이건 `multicast` 메소드에있음.

Sinks.many().multicast().directAllOrNothing()
- 여러 subscriber 에게 onNext 를 drop 할 수 있는 간단한 배압 시스템을 가진 Sinks 이다. 
- subscriber 중 하나만 느려도 drop 한다. 대신에 재요청을 해서 모든 subscriber 가 다시 처리할 수 있도록 함.
  - subscriber 가 느리다는 건 demand 요청이 없는 것이다. 맞네. 

Sinks.many().multicast().directBestEffort()
- subscriber 가 너무 느린 경우가 있는 경우 (수요가 없는 경우) 해당 subscriber 에게서만 onNext 를 드랍함.

Sinks.many().replay()
- 데이터를 캐싱하고, 늦게 subscribe 한 Subscrier 에게 replay 해줄 수 있는 Sinks.

Sinks.unsafe().many(
- Publisher 쪽에서 thread 안전성을 가지지 않아도 되는 경우에 사용하는 것. 오버헤드가 적음. thread-safe 한 sink 는 mutli-thread access 를 위해서 약간의 오버헤드가 든다.

Sinks.one()
- 단일 인스턴스의 Sinks.one<T> 을 생성한다. Mono 로.

Sinks.empty
- Sinks.one 과 유사하다. 다만 빈 Mono 또는 Mono.error 만 내뱉는게 가능.



## Question: Sinks 를 통해서 데이터를 생성해 전달하는 것과 Flux.create() 를 통해서 데이터를 생성하는 것의 차이는?

Sinks와 Flux.create()는 두 가지 다른 방법으로 리액터에서 데이터를 생성하고 전달하는 메커니즘입니다. 각 방법의 차이점은 다음과 같습니다.

Sinks:

- Sinks는 명시적으로 다양한 유형의 생성자를 제공합니다 (예: unicast, multicast, replay 등). 이를 통해 사용자는 원하는 생성자 유형에 따라 Sinks를 선택할 수 있습니다.
- Sinks는 스레드 안전을 보장하는 방식으로 설계되어 있습니다. 이로 인해 여러 스레드에서 동시에 데이터를 생성하고 처리할 수 있습니다.
- Sinks의 사용법이 좀 더 직관적이고 사용하기 쉽습니다. 예를 들어, Sinks.Many를 사용하면 여러 구독자에게 데이터를 전달할 수 있으며, 각 구독자에 대한 역압을 적용할 수 있습니다.


Flux.create():

- Flux.create()는 더 낮은 수준의 메커니즘으로, 데이터 생성 및 처리를 수동으로 제어할 수 있습니다. 이를 통해 사용자는 원하는대로 데이터를 생성하고 처리할 수 있습니다.
- Flux.create()는 사용자가 생산자의 동작을 직접 구현해야 하기 때문에 더 많은 유연성을 제공하지만, 동시에 구현이 더 복잡해질 수 있습니다.
- Flux.create()는 사용자가 직접 구현한 생산자가 Reactive Streams 사양을 준수하도록 해야 합니다. 이를 통해 사용자는 역압과 같은 중요한 개념을 직접 관리할 수 있지만, 이로 인해 구현이 더 복잡해질 수 있습니다.

결론적으로, Sinks는 사용자가 직접 스레드 안전성과 역압을 처리할 필요 없이 데이터를 생성하고 전달할 수 있는 더 높은 수준의 메커니즘을 제공하는 반면, Flux.create()는 사용자가 데이터를 생성하고 처리하는 방법을 더 세밀하게 제어할 수 있는 낮은 수준의 메커니즘을 제공합니다. 사용자의 요구와 상황에 따라 적절한 방법을 선택할 수 있습니다.




