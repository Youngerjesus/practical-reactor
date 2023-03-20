# Chapter 6 Combining Publishers Quiz

<details>
<summary> `Flux.flatMap` 은 어떤 역할을 하는 걸까? </summary>

- 내부 요소를 비동기적으로 transforming 한다 그리고 publisher 로 다시 만든다.
- 각 요소마다 publisher 가 생기므로 이 내뱉는 요소들을 다시 하나로 merge 해서 하나의 flux 로 만드는 과정을 이룬다.
  - 하나의 flux 로 만들 때 interleave 과정도 가능하다.

</details>

<details>
<summary> flatMap, flatMapSequential, concatMap 의 차이는 뭔가? </summary>

3가지 차원에서 비교하는게 가능. 
- Generation of inners and subscription 
- Ordering of the flattened values 
- Interleaving 

flatMap 
- Generation of inners and subscription: eagerly subscribing 한다. 
- Ordering of the flattened values: original 순서를 보장하지 않는다. 도착한 순서대로. 
- Interleaving: interleaving 을 보장한다. 

flatMapSequential 
- Generation of inners and subscription: flatMap 과 동일
- Ordering of the flattened values: original source 순서를 보장한다. 늦게 만들어진 Publisher 의 요소들은 queue 에 쌓이는 형태로. 
- Interleaving: interleaving 을 보장하지 않는다.

concatMap 
- Generation of inners and subscription: next inner 를 만들기 전에 이전 inner publisher 가 완료될 때까지 기다린다.
- Ordering of the flattened values: source element 순서와 동일하다. 
- Interleaving: interleaving 을 보장하지 않는다.

</details>

<details>
<summary> map vs flatMap 의 차이는 뭔가? </summary>

map
- parameter 로 `Function` 을 받는다. 그리고 이걸 통해서 emit 한 item 을 변환한다.
- 예) 소문자 알파벳 -> 대문자 알파벳으로. 

flatMap 
- map 과 같이 parameter 로 `Function` 을 받지만 좀 다르다. input item 을 Publisher 로 반환하는 것을 받는다. 

</details>

<details> 
<summary> Flux.merge 와 Flux.concat 과의 차이점은? </summary>

merge
- concat 과 달리 source 들을 즉시 subscribe 한다. 
- interleaving 가능.
- merge 는 finite or asynchronous source 와 같다.
- infinite source 를 subscribe 할 때 scheduler 를 바꾸지 않으면 다음 subscribe 는 호출되지 않는다. drain 할려는 거 때문에. 

concat
- all source 에서 나온 item 들은 Concatenation 한다. 
- 하나의 source 를 subscribe 하고 방출이 다 되면 다음 source 를 subscribe 한다. 
- 에러가 나면 interrupt 가 발생해서 즉시 downstream 에 전달된다.  

</details>
