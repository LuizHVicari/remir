# Examples by layer

These use a hypothetical `habit` feature. See the `profile` feature for real, complete code.

## core

```kotlin
// HabitDto.kt
@Serializable
data class HabitDto(val id: Uuid, val name: String, val createdAt: Instant)   // kotlin.uuid.Uuid, kotlin.time.Instant

// HabitRequests.kt
@Serializable
data class CreateHabitRequest(val name: String)

// HabitResources.kt
@Resource("/habits")
class HabitsResource(val page: Int = 0) {
    @Resource("{id}")
    class Id(val parent: HabitsResource = HabitsResource(), val id: String)
}

// HabitValidation.kt
object HabitValidation {
    val create = Validation<CreateHabitRequest> {
        CreateHabitRequest::name { notBlankTrimmed(); lengthBetween(2, 80) }
    }
}
```

## server

The table is the source of truth. After changing it, run `./gradlew :server:generateMigrations`.

```kotlin
// HabitTable.kt
object HabitsTable : Table("habits") {
    val id = uuid("id")                                          // kotlin.uuid.Uuid
    val userId = varchar("user_id", 64).references(UsersTable.id).index()
    val name = varchar("name", 80)
    val createdAt = timestamp("created_at")                      // org.jetbrains.exposed.v1.datetime.timestamp
    override val primaryKey = PrimaryKey(id)
}
```

The repository never opens a transaction. It always filters by user and returns the `core` DTO:

```kotlin
// HabitRepository.kt
class HabitRepository {
    fun list(userId: String): List<HabitDto> =
        HabitsTable.selectAll().where { HabitsTable.userId eq userId }.map { it.toDto() }

    fun existsWithName(userId: String, name: String): Boolean =
        !HabitsTable.selectAll()
            .where { (HabitsTable.userId eq userId) and (HabitsTable.name eq name) }
            .empty()

    private fun ResultRow.toDto() = HabitDto(this[HabitsTable.id], this[HabitsTable.name], this[HabitsTable.createdAt])
}
```

The service holds the DB-dependent rule and opens the transaction:

```kotlin
// HabitService.kt
class HabitService(private val repository: HabitRepository, private val clock: Clock = Clock.System) {
    suspend fun create(userId: String, request: CreateHabitRequest): HabitDto = dbQuery {
        if (repository.existsWithName(userId, request.name)) throw Conflict("A habit with this name already exists")
        repository.insert(userId, request.name, clock.now())
    }
}
```

Routes do HTTP only:

```kotlin
// HabitRoutes.kt
fun Route.habitRoutes(service: HabitService) {
    get<HabitsResource> { call.respond(service.list(call.userId())) }
    post<HabitsResource> {
        val request = HabitValidation.create.orThrow(call.receive<CreateHabitRequest>())
        call.respond(HttpStatusCode.Created, service.create(call.userId(), request))
    }
}
```

## app

```kotlin
// HabitApi.kt
class HabitApi(private val client: HttpClient) {
    suspend fun list(): List<HabitDto> = client.get(HabitsResource()).body()
}
```

In the ViewModel, actions are public functions and every change goes through `update { copy }`:

```kotlin
// HabitsViewModel.kt
data class HabitsState(val loading: Boolean = true, val items: List<HabitDto> = emptyList(), val error: String? = null)

class HabitsViewModel(private val api: HabitApi) : ViewModel() {
    private val _state = MutableStateFlow(HabitsState())
    val state: StateFlow<HabitsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            apiCall { api.list() }
                .onSuccess { items -> _state.update { it.copy(loading = false, items = items) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }
}
```

On the screen, the `Route` wires the ViewModel and the `Screen` stays stateless, with a `@Preview`:

```kotlin
// HabitsScreen.kt
@Composable
fun HabitsRoute(viewModel: HabitsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HabitsScreen(state, onRetry = viewModel::load)
}

@Composable
fun HabitsScreen(state: HabitsState, onRetry: () -> Unit) { /* ... */ }
```

## Server test

```kotlin
@Test
fun `user does not see another user's habits`() = remirTestApplication { client ->
    val tokenA = TestJwt.token("user_a_${Uuid.random()}")
    val tokenB = TestJwt.token("user_b_${Uuid.random()}")
    client.post("/habits") { bearerAuth(tokenA); contentType(ContentType.Application.Json); setBody(CreateHabitRequest("Read")) }

    val fromB = client.get("/habits") { bearerAuth(tokenB) }.body<List<HabitDto>>()

    assertTrue(fromB.isEmpty())
}
```
