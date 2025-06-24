# 🎓 코틀린 8주차 학습 과정 with Kordle-Clone 프로젝트

## 📚 목차

- [1주차: 코틀린 기초 문법](#-1주차-코틀린-기초-문법)
- [2주차: 객체지향과 함수형 프로그래밍](#-2주차-객체지향과-함수형-프로그래밍)
- [3주차: 컬렉션과 람다 처리](#-3주차-컬렉션과-람다-처리)
- [4주차: 비동기 처리와 코루틴](#-4주차-비동기-처리와-코루틴)
- [5주차: Spring--Kotlin-프로젝트-구성](#-5주차-spring--kotlin-프로젝트-구성)
- [6주차: API 테스트와 MockK 활용](#-6주차-api-테스트와-mockk-활용)
- [7주차: WebClient를 활용한 외부 API 연동](#-7주차-webclient를-활용한-외부-api-연동)
- [8주차: JPA 활용 API 구성과 통합 테스트](#-8주차-jpa-활용-api-구성과-통합-테스트)


## 📚 1주차: 코틀린 기초 문법


### 🔹 변수와 자료형

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/service/GameService.kt
class GameService {
    fun processGuess(sessionId: Long, request: GuessRequest): GuessResponse {
        // val: 불변 변수 (읽기 전용)
        // 타입 추론
        val session = gameSessionRepository.findById(sessionId)
        
        // 명시적 타입 지정
        val isCorrect: Boolean = feedback.all { it == LetterFeedback.CORRECT }
        
        // var: 가변 변수
        session.attemptsLeft--  // 남은 기회를 계산하기 위해 session.attemptsLeft는 var로 선언됨
    }
}
```

**개념 설명:**

* `val`은 읽기 전용 변수로, 초기화 이후 값을 변경할 수 없습니다.
* `var`는 값 변경이 가능한 변수입니다.
* Kotlin은 타입 추론이 가능하여, 명시적 타입 지정 없이도 컴파일이 가능합니다.
* 불변 변수 사용을 권장하며, Nullable 타입과 결합해 안정적인 코드 작성이 가능합니다.

---

### 🔹 함수 정의

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/util/HangulUtils.kt
object HangulUtils {
    // 반환 타입 명시
    fun validateJamos(jamos: List<Char>): Boolean {
        if (jamos.size != 6) return false
        return jamos.all { /* 조건 */ }
    }
    
    // 단일 표현식 함수 (반환 타입 추론)
    fun jamosToDisplayString(jamos: List<Char>) = jamos.joinToString("")
    
    // 기본 매개변수
    fun normalizeToSixChars(jamos: List<Char>): List<Char> = when {
        jamos.size == 6 -> jamos
        jamos.size < 6 -> jamos + List(6 - jamos.size) { ' ' }  // 기본값: 공백
        else -> jamos.take(6)
    }
}
```

**개념 설명:**

* 함수는 `fun` 키워드로 정의합니다.
* 단일 표현식 함수는 `=` 기호를 사용해 간결하게 작성할 수 있으며, 반환 타입을 생략할 수 있습니다.
* 기본 매개변수를 지정하면 오버로딩 없이 다양한 방식으로 함수를 호출할 수 있습니다.

---

### 🔹 Null-Safety

**프로젝트 예시 1:**

```kotlin
// src/main/kotlin/com/example/kordle/service/GameService.kt
fun createSession(request: StartGameRequest): StartGameResponse {
    val wordEntity = getWordForStage(nextStage)
    // ?. (Safe Call): null이면 null 반환
    val answerLength = wordEntity?.answer?.length

    // ?: (Elvis 연산자): null이면 우측 값 사용
    val safeWordEntity = wordEntity ?: throw IllegalStateException("단어를 찾을 수 없습니다.")
    
    // takeIf + Elvis 연산자 조합
    val safeAnswer = safeWordEntity.answer.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("단어 데이터가 비어있습니다")
}
```

**개념 설명:**

* Kotlin은 `NullPointerException`을 방지하기 위해 Null-Safety 기능을 제공합니다.
* `?.` (Safe Call): 객체가 null이면 전체 표현식이 null이 됩니다.
* `?:` (Elvis 연산자): null이면 우측의 기본값 또는 예외 처리 로직을 실행합니다.
* `!!` (Not-null Assertion): null이 아님을 확신할 때 사용하지만, 런타임 오류 위험이 있으므로 최소한으로 사용해야 합니다.
* `takeIf`: 조건을 만족하면 자기 자신을 반환하고, 그렇지 않으면 null을 반환합니다.

---

### 🔹 데이터 클래스 vs 일반 클래스

**프로젝트 예시:**
```kotlin
// src/main/kotlin/com/example/kordle/dto/GameDto.kt
// 데이터 클래스: equals, hashCode, toString, copy 자동 생성
data class GuessResponse(
    val feedback: List<LetterFeedback>,
    val attemptsLeft: Int,
    val isCleared: Boolean,
    val isGameOver: Boolean = false,
    val cumulativeFeedback: Map<Char, LetterFeedback> = emptyMap()
)

// src/main/kotlin/com/example/kordle/service/StatsService.kt
// 배치 처리용 데이터 클래스
data class StatsUpdate(
    val nickname: String,
    val isWin: Boolean,
    val attempts: Int,
    val currentStage: Int = 1
)
```

**개념 설명:**

* `data class`는 생성자, getter, `equals`, `hashCode`, `toString` 등을 자동으로 생성합니다.

---

## 📚 2주차: 객체지향과 함수형 프로그래밍

---

### 🔹 클래스와 생성자

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/controller/GameController.kt
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = ["*"])
class GameController(
    // 주 생성자를 통한 의존성 주입
    private val gameService: GameService,
    private val statsService: StatsService
) {
    // 코루틴 스코프 생성 - 클래스 프로퍼티
    private val controllerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("GameController")
    )
}
```

**개념 설명:**

* Kotlin 클래스는 `class` 키워드로 정의하며, 주 생성자(primary constructor)를 클래스 헤더에 정의할 수 있습니다.
* 생성자에 선언된 파라미터는 자동으로 클래스 프로퍼티로 사용될 수 있습니다.
* 스프링과 결합할 경우, 주 생성자를 통한 **의존성 주입(DI)** 이 간결하게 표현됩니다.

---

### 🔹 싱글톤 객체 (Object)

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/util/HangulUtils.kt
object HangulUtils {
    private val CHOSUNG = charArrayOf(/* 생략 */)
    private val COMPLEX_VOWEL_MAP = mapOf(/* 생략 */)
    
    fun decomposeWord(word: String): List<Char> {
        // 한글 분해 로직
    }
}
```

**개념 설명:**

* Kotlin에서는 `object` 키워드를 사용하여 **싱글톤 객체(singleton)** 를 간편하게 정의할 수 있습니다.
* 클래스와 달리 생성자가 없으며, 애플리케이션 전체에서 단 하나의 인스턴스로 존재합니다.
* 상수, 유틸성 메서드, 공통 기능 등을 담을 때 적합합니다.

---

### 🔹 Enum 클래스

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/entity/LetterFeedback.kt
enum class LetterFeedback {
    CORRECT, PRESENT, ABSENT
}

private fun getBetterFeedback(existing: LetterFeedback?, new: LetterFeedback): LetterFeedback {
    if (existing == null) return new
    return when {
        existing == LetterFeedback.CORRECT -> LetterFeedback.CORRECT
        new == LetterFeedback.CORRECT -> LetterFeedback.CORRECT
        existing == LetterFeedback.PRESENT -> LetterFeedback.PRESENT
        new == LetterFeedback.PRESENT -> LetterFeedback.PRESENT
        else -> LetterFeedback.ABSENT
    }
}
```

**개념 설명:**

* `enum class`는 열거형 상수를 정의할 때 사용하며, 각각은 클래스의 인스턴스입니다.
* switch나 when 표현식과 함께 사용하여 조건 분기 처리를 명확하게 할 수 있습니다.
* 각 enum 상수에 속성이나 메서드를 정의할 수도 있어, 단순한 상수를 넘는 기능을 갖출 수 있습니다.

---

### 🔹 고차 함수와 람다

**프로젝트 예시:**
```kotlin
// src/main/kotlin/com/example/kordle/service/WordService.kt
private fun loadDefaultWords() {
  val words = listOf("꼬들")

  // map: 고차 함수, 람다를 매개변수로 받음
  val wordEntities = words.map { word ->
    try {
      val jamos = HangulUtils.wordToSixJamos(word)
      val answerString = HangulUtils.jamosToCSVString(jamos)

      Word(text = word, stage = 1, answer = answerString)
    } catch (e: Exception) {
      null
    }
  }.filterNotNull()  // filterNotNull: 고차 함수
}
```

**개념 설명:**

* Kotlin에서 **고차 함수(Higher-Order Function)** 는 **함수를 매개변수로 받거나, 함수를 반환하는 함수**입니다.
* 함수도 값처럼 취급할 수 있기 때문에, 다른 함수에 전달하거나 결과로 반환할 수 있습니다.
* **`map`**: 컬렉션의 각 요소를 변환하여 **새로운 리스트**를 생성합니다.
* **`filter`**: 조건을 만족하는 요소만 걸러내어 **새로운 리스트**를 생성합니다.
* **`forEach`**: 컬렉션의 각 요소에 대해 주어진 동작을 **반복 실행**합니다.
* **`let`**: 객체가 **null이 아닐 때** 특정 동작을 수행하고 결과를 반환합니다.
* **`run`**: 객체에 대해 **연산을 수행하고 결과를 반환**합니다. (보통 초기화나 계산 시 사용)
* **`apply`**: 객체를 구성(configure)한 후 **그 객체 자체를 반환**합니다.
* **`also`**: 부가적인 작업을 수행한 후 **객체 자체를 반환**합니다. (디버깅이나 로깅 등)
* **`takeIf`**: 조건이 참이면 객체를 반환, 거짓이면 **null 반환**합니다.
* **`takeUnless`**: 조건이 거짓이면 객체를 반환, 참이면 **null 반환**합니다.
* **`fold`**: 초기값부터 시작해 각 요소를 누적해 **하나의 값으로 축약**합니다.

---

## 📚 3주차: 컬렉션과 람다 처리

---

### 🔹 컬렉션 API 마스터하기

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/util/HangulUtils.kt
fun parseJamosFromString(jamoString: String): List<Char> {
    val result = jamoString.trim()
        .removeSurrounding("[", "]")  // 문자열 처리
        .split(",")                   // List<String>으로 분할
        .map { it.trim() }           // 각 요소 trim (변환)
        .filter { it.isNotEmpty() }  // 빈 문자열 제거 (필터링)
        .map { element ->            // 문자로 변환
            if (element.length != 1) {
                throw IllegalArgumentException("잘못된 자모 형식: $element")
            }
            element[0] 
        }

    return result
}

// List 생성과 조작
fun normalizeToSixChars(jamos: List<Char>): List<Char> {
    return when {
        jamos.size == 6 -> jamos
        jamos.size < 6 -> jamos + List(6 - jamos.size) { ' ' }
        else -> jamos.take(6)
    }
}
```

**개념 설명:**

* Kotlin의 **컬렉션 API**는 리스트, 셋, 맵을 쉽게 조작할 수 있도록 다양한 확장 함수를 제공합니다.
* `map`, `filter`, `take`, `split` 등은 불변 컬렉션을 가공하는 데 자주 사용됩니다.

---

### 🔹 Map과 Set 활용

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/service/GameService.kt
private suspend fun calculateCumulativeFeedback(sessionId: Long): Map<Char, LetterFeedback> {
    val cumulativeFeedback = mutableMapOf<Char, LetterFeedback>()

    for (feedbackMap in feedbackMaps) {
        for ((jamo, feedback) in feedbackMap) {
            val existingFeedback = cumulativeFeedback[jamo]
            cumulativeFeedback[jamo] = getBetterFeedback(existingFeedback, feedback)
        }
    }

    return cumulativeFeedback
}

// src/main/kotlin/com/example/kordle/util/HangulUtils.kt
private val COMPLEX_JONGSUNG_MAP = mapOf(
    'ㄳ' to listOf('ㄱ', 'ㅅ'),
    'ㄵ' to listOf('ㄴ', 'ㅈ'),
    'ㄶ' to listOf('ㄴ', 'ㅎ')
)
```

**개념 설명:**

* `Map`은 키-값 쌍을 저장하는 자료구조이며, `mutableMapOf`, `mapOf`를 통해 생성할 수 있습니다.
* Kotlin의 `for ((key, value) in map)` 구문은 **구조 분해 할당**을 지원하여 가독성이 높습니다.
* `Set`은 중복을 허용하지 않는 컬렉션이며, 주로 집합 연산(교집합, 차집합 등)에 사용됩니다.
* `Map`과 `Set` 역시 고차 함수 (`filter`, `map`, `any` 등)를 함께 사용할 수 있습니다.

---

## 📚 4주차: 비동기 처리와 코루틴

---

### 🔹 Suspend 함수 이해

**프로젝트 예시:**

```kotlin
// src/main/kotlin/com/example/kordle/service/GameService.kt
class GameService {
    // suspend 함수: 일시 중단 가능한 함수
    suspend fun createSession(request: StartGameRequest): StartGameResponse {
        return try {
            val (nextStage, wordEntity) = coroutineScope {
                val nextStageDeferred = async { statsService.getNextStage(nickname) }
                val wordDeferred = async { getWordForStage(nextStage) }

                Pair(nextStageDeferred.await(), wordDeferred.await())
            }

            StartGameResponse(...)
        } catch (e: Exception) {
            throw IllegalStateException("게임 세션 생성 실패: ${e.message}", e)
        }
    }
}
```

**개념 설명:**

* `suspend` 함수는 일시 중단이 가능한 코루틴 함수로, **다른 suspend 함수 호출**이 가능합니다.
* 일반 함수에서 직접 호출할 수 없으며, 반드시 코루틴 또는 다른 suspend 함수 내에서 호출해야 합니다.
* suspend 함수는 중간에 일시 중단(suspension) 될 수 있기 때문에 	함수 실행 도중 다른 작업으로 전환 가능합니다. 
* Kotlin의 비동기 흐름 제어는 `suspend` 키워드를 기반으로 설계되어 있습니다.

---

### 🔹 Launch vs Async 차이점

**프로젝트 예시:**

```kotlin
suspend fun processGuess(sessionId: Long, request: GuessRequest): GuessResponse {
    val (feedback, cumulativeFeedback) = coroutineScope {
        val feedbackDeferred = async(Dispatchers.Default) { 
            generateJamoFeedback(request.jamos, answerJamos) 
        }
        val cumulativeDeferred = async(Dispatchers.IO) { 
            calculateCumulativeFeedback(sessionId) 
        }
        Pair(feedbackDeferred.await(), cumulativeDeferred.await())
    }

    if (isGameOver) {
        serviceScope.launch {  // 결과를 기다리지 않음
            try {
                delay(100)
                statsService.updateStats(...)
            } catch (e: Exception) {
                println("비동기 통계 업데이트 중 오류: ${e.message}")
            }
        }
    }
}
```

**개념 설명:**

* `launch`: **결과를 반환하지 않는** 비동기 작업에 사용. 주로 **단순 실행(Fire-and-Forget)** 방식으로 사용됩니다.
* `async`: **Deferred<T>를 반환**하며, `await()`를 호출하여 **결과를 가져올 수 있는** 비동기 작업에 적합합니다.
* `launch`는 예외가 자동 전파되지 않지만, `async`는 예외가 `await()` 시점에 전파됩니다.
* 두 방식 모두 `coroutineScope` 또는 `supervisorScope`와 함께 사용하여 **구조화된 동시성**을 구현할 수 있습니다.

---

### 🔹 Dispatcher 전략

**프로젝트 예시:**

```kotlin
class WordService {
    private suspend fun loadDefaultWords() = withContext(Dispatchers.IO) {
        val wordEntities = words.map { word ->
            async(Dispatchers.Default) {
                try {
                    val jamos = HangulUtils.wordToSixJamos(word)
                    // 복잡한 한글 처리 로직
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getRandomWord(): Word = withContext(Dispatchers.IO) {
        wordRepository.findRandomAllowedWord()
    }
}
```

**개념 설명:**

* Dispatchers는 코루틴이 어떤 스레드(또는 스레드 풀)에서 실행될지 결정하는 역할을 합니다.
* `Dispatchers.Default`: CPU 집약적 작업(연산, 데이터 처리)에 적합한 디스패처입니다.
* `Dispatchers.IO`: 파일, 네트워크, DB 접근 등 I/O 작업에 최적화된 디스패처입니다.
* `withContext(dispatcher) { ... }`를 사용하면 특정 블록에서 디스패처를 명시적으로 지정할 수 있습니다.
* Dispatcher 전략을 올바르게 사용하면 **스레드 리소스를 효율적으로 관리**할 수 있습니다.

---


### 🔹 구조화된 동시성

**프로젝트 예시:**

```kotlin
@RestController
class GameController {
    private val controllerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("GameController")
    )

    @PostMapping("/start")
    fun startGame(@RequestBody request: StartGameRequest): DeferredResult<ResponseEntity<StartGameResponse>> {
        val deferredResult = DeferredResult<ResponseEntity<StartGameResponse>>(10000L)

        controllerScope.launch {
            try {
                val response = gameService.createSession(request)
                deferredResult.setResult(ResponseEntity.ok(response))
            } catch (e: Exception) {
                deferredResult.setResult(ResponseEntity.internalServerError().build())
            }
        }

        return deferredResult
    }
}
```

**개념 설명:**

* **구조화된 동시성(Structured Concurrency)** 은 코루틴의 생명주기를 **컨텍스트(scope)** 와 함께 관리하는 방식입니다.
* 코루틴의 생명 주기를 명확한 범위(scope) 안에서 관리함으로써, 누수 없이 종료되도록 보장하는 것
* 부모 코루틴이 취소되면 **모든 자식 코루틴이 함께 취소**됩니다.
* `CoroutineScope`를 명시적으로 생성하면 생명주기를 명확히 제어할 수 있습니다.
* Spring MVC의 `DeferredResult`와 함께 사용하면 **논블로킹 HTTP 처리**가 가능합니다.

---

## 📚 5주차: Spring + Kotlin 프로젝트 구성

---

## 📚 6주차: API 테스트와 MockK 활용

---

### 🔹 MockK 기본 사용법

**프로젝트 예시:**

```kotlin
@ExtendWith(MockKExtension::class)
class GameServiceTest {

    @MockK
    private lateinit var gameSessionRepository: GameSessionRepository

    @MockK
    private lateinit var guessRepository: GuessRepository

    @MockK
    private lateinit var wordService: WordService

    @MockK
    private lateinit var statsService: StatsService

    @InjectMockKs
    private lateinit var gameService: GameService

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `게임 세션 생성 성공 테스트`() {
        val request = StartGameRequest("테스트유저")
        val word = Word(text = "코들", answer = "[ㄱ,ㄱ,ㅗ,ㄷ,ㅡ,ㄹ]", stage = 1)
        val gameSession = GameSession(
            id = 1L, nickname = "테스트유저", answer = word.answer, stage = 1
        )

        every { statsService.getNextStage("테스트유저") } returns 1
        every { wordService.getWordByStage(1) } returns word
        every { gameSessionRepository.save(any()) } returns gameSession

        val response = gameService.createSession(request)

        assertEquals(1L, response.sessionId)
        assertEquals(6, response.maxAttempts)
        assertEquals(1, response.currentStage)

        verify { gameSessionRepository.save(any()) }
        verify { statsService.getNextStage("테스트유저") }
        verify { wordService.getWordByStage(1) }
    }
}
```

**개념 설명:**

* `MockK`는 Kotlin에 특화된 **모킹(mocking) 프레임워크**로, 클래스와 인터페이스 모두 쉽게 모킹할 수 있습니다.
* `@MockK`: 의존성 객체를 가짜(mock)로 생성합니다.
* `@InjectMockKs`: 테스트 대상 클래스에 위의 Mock 객체들을 자동 주입합니다.
* `every { ... } returns ...`: 특정 조건의 메서드 호출에 대한 결과를 정의합니다.
* `verify { ... }`: 특정 메서드가 실제로 호출되었는지 검증합니다.
* `clearAllMocks()`: 테스트 간 Mock 상태를 초기화합니다. (every, verify, ... 등 초기화)

---

### 🔹 예외 처리 테스트

**프로젝트 예시:**

```kotlin
@Test
fun `빈 닉네임으로 게임 세션 생성 실패 테스트`() {
    val request = StartGameRequest("")

    assertThrows<IllegalArgumentException> {
        gameService.createSession(request)
    }
}

@Test
fun `존재하지 않는 세션으로 추측 처리 실패 테스트`() {
    val sessionId = 999L
    val request = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))

    every { gameSessionRepository.findById(sessionId) } returns Optional.empty()
    every { gameService.processGuess(sessionId, request) } throws IllegalArgumentException("")        
    }
}
```

**개념 설명:**

* `assertThrows<ExceptionType> { ... }`: 특정 예외가 발생하는지 검증합니다.
* MockK로 예외 상황을 시뮬레이션할 때는 `every { ... } returns ...` 또는 `throws` 등을 활용할 수 있습니다.

---

## 📚 7주차: WebClient를 활용한 외부 API 연동

---

### 🔹 외부 API 연동 예시 (WebClient)

**프로젝트 확장 예시:**

```kotlin
@Service
class ExternalWordService {
    
    private val webClient = WebClient.builder()
        .baseUrl("https://api.korean-dictionary.com")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()
    
    suspend fun fetchWordDefinition(word: String): WordDefinitionResponse? = 
        withContext(Dispatchers.IO) {
            try {
                webClient.get()
                    .uri("/word/{word}", word)
                    .retrieve()
                    .awaitBody<WordDefinitionResponse>()
            } catch (e: WebClientResponseException) {
                println("API 호출 실패: ${e.statusCode} - ${e.responseBodyAsString}")
                null
            } catch (e: Exception) {
                println("예상치 못한 오류: ${e.message}")
                null
            }
        }
}
```

**개념 설명:**

* `WebClient`는 Spring WebFlux에서 제공하는 **논블로킹 HTTP 클라이언트**입니다.
* `get().uri(...).retrieve().awaitBody<T>()`: HTTP GET 요청 후 JSON 응답을 `T` 타입으로 비동기 수신합니다.
* `withContext(Dispatchers.IO)`: I/O 연산을 별도 컨텍스트에서 수행하여 스레드 리소스를 효율적으로 관리합니다.

---

## 📚 8주차: JPA 활용 API 구성과 통합 테스트

---

### 🔹 JPA Entity 설계

**프로젝트 예시:**

```kotlin
@Entity
@Table(name = "game_sessions")
data class GameSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val nickname: String,
    
    @Column(nullable = false)
    val answer: String,
    
    @Column(nullable = false)
    val stage: Int = 1,
    
    @Column(nullable = false)
    var attemptsLeft: Int = 6,
    
    @Column(nullable = false)
    var isCleared: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "user_stats")
data class UserStats(
    @Id
    val nickname: String,
    
    @Column(nullable = false)
    var totalGames: Int = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "attempt_distribution", 
        joinColumns = [JoinColumn(name = "nickname")]
    )
    @MapKeyColumn(name = "attempts")
    @Column(name = "count")
    var attemptDistribution: MutableMap<Int, Int> = mutableMapOf()
)
```

**개념 설명:**

* `@Entity`: JPA가 관리하는 영속 객체임을 선언합니다.
* `@Id`, `@GeneratedValue`: 기본 키 및 자동 생성 전략을 지정합니다.
* `@ElementCollection`: 엔티티 내부에 별도 테이블로 컬렉션 데이터를 저장할 수 있도록 합니다.
* Map 구조는 `@CollectionTable`, `@MapKeyColumn`, `@Column`을 조합해 저장됩니다.

---

### 🔹 Repository 패턴

**프로젝트 예시:**

```kotlin
interface GameSessionRepository : JpaRepository<GameSession, Long> {
    fun countByIsCleared(isCleared: Boolean): Long
    fun countByAttemptsLeftGreaterThanAndIsCleared(attemptsLeft: Int, isCleared: Boolean): Long
}

interface WordRepository : JpaRepository<Word, String> {
    @Query("SELECT w FROM Word w ORDER BY RANDOM() LIMIT 1")
    fun findRandomAllowedWord(): Word?

    @Query("SELECT w FROM Word w WHERE w.stage = :stage")
    fun findByStage(@Param("stage") stage: Int): Word?

    @Query("SELECT MAX(w.stage) FROM Word w")
    fun findMaxStage(): Int?

    fun existsByText(text: String): Boolean
}
```

**개념 설명:**

* `JpaRepository`를 상속하면 기본적인 CRUD 메서드가 자동 제공됩니다.
* **쿼리 메서드**를 통해 메서드 이름만으로도 조건 기반 쿼리를 생성할 수 있습니다.
* `@Query`를 사용하면 JPQL 또는 네이티브 쿼리를 명시적으로 정의할 수 있습니다.
* `@Param`을 이용해 쿼리 파라미터를 바인딩할 수 있으며, 복잡한 검색 조건에도 활용 가능합니다.

---

### 🔹 통합 테스트 예시

**테스트 코드:**

```kotlin
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class GameControllerIntegrationTest {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    private lateinit var gameSessionRepository: GameSessionRepository

    @Test
    fun `전체 게임 플로우 통합 테스트`() {
        val startRequest = StartGameRequest("통합테스트유저")
        val startResponse = testRestTemplate.postForEntity(
            "/api/game/start",
            startRequest,
            StartGameResponse::class.java
        )

        assertThat(startResponse.statusCode).isEqualTo(HttpStatus.OK)
        val sessionId = startResponse.body!!.sessionId

        val guessRequest = GuessRequest(listOf('ㄱ', 'ㄱ', 'ㅗ', 'ㄷ', 'ㅡ', 'ㄹ'))
        val guessResponse = testRestTemplate.postForEntity(
            "/api/game/$sessionId/guess",
            guessRequest,
            GuessResponse::class.java
        )

        assertThat(guessResponse.statusCode).isEqualTo(HttpStatus.OK)

        val savedSession = gameSessionRepository.findById(sessionId).get()
        assertThat(savedSession.attemptsLeft).isEqualTo(5)
    }
}
```

**개념 설명:**

* `@SpringBootTest`: 전체 애플리케이션 컨텍스트를 로드하여 **통합 테스트 환경**을 구성합니다.
* `@TestPropertySource`: 테스트용 설정 파일(`application-test.yml`)을 적용합니다.
* `TestRestTemplate`: 실제 HTTP 요청을 통해 컨트롤러를 테스트할 수 있는 도구입니다.
* 단위 테스트와 달리 **HTTP 통신, DB 상태, 트랜잭션 전체 흐름을 검증**할 수 있습니다.
