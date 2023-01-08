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
