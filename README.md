# Tandem Community

Paginated Tandem community feed with persistent per-member like reactions.

---

## Running

Requires Android Studio Ladybug or newer and JDK 17.

```bash
git clone <repo>
cd TandemCommunity
./gradlew installDebug
```

---

## Stack

| Area | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Async | Coroutines + Flow |
| Networking | Retrofit + OkHttp + kotlinx-serialization |
| Database | Room |
| Pagination | Paging 3 |
| Images | Coil |
| Testing | JUnit 4, MockK, Turbine, Truth |

Versions are centralised in `gradle/libs.versions.toml`.

---

## Architecture

```
presentation/   ← Compose UI, ViewModels, UI events
      │
      ▼
domain/         ← Pure Kotlin: entities, repository interfaces, use cases
      │
      ▼
data/           ← Retrofit, Room, implementations, mappers
```

The domain layer has no Android dependencies except `PagingData` (see trade-offs below). The presentation layer never sees DTOs or Room entities.

### Reactive data flow

The main challenge was merging two independent reactive sources: the paginated remote feed and the local like state. The solution uses `combine` + `PagingData.map`:

```kotlin
combine(pagerFlow, likedIdsFlow) { pagingData, likedIds ->
    pagingData.map { member ->
        LikedMember(member, isLiked = member.id in likedIds)
    }
}
```

When the user toggles a like → Room persists it → Room emits the new set → `combine` re-emits the paging data with updated flags → Compose recomposes only the affected cards. No manual invalidation, no network re-fetch.

---

## Design decisions

**Why `PagingSource` directly instead of `RemoteMediator`?**
`RemoteMediator` makes sense for offline support, but the app doesn't need it. Adding it would mean a Room cache table, paging keys, and invalidation logic — complexity with no matching benefit. The only thing that truly belongs on the device is the user's like state.

**Why a custom `DataResult<T>` instead of `Result<T>` or exceptions?**
Kotlin's `Result<T>` carries only `Throwable`, leaving errors untyped. A custom sealed class (`NoConnection`, `Timeout`, `Server`, `Unknown`) gives the compiler exhaustiveness checks at every `when`.

**Why Room for likes instead of DataStore?**
DataStore would be sufficient for a simple boolean, but Room provides reactive queries via `Flow` out of the box and scales naturally if likes ever need metadata. The model is "presence equals liked": the row exists if the member is liked, absent otherwise — no `isLiked: Boolean` column to keep in sync.

**Why keep use cases if they're so thin?**
Each use case currently wraps a single repository call, but the ViewModel never imports the repository directly, keeping the presentation layer decoupled from the data contract. If analytics, validation, or multi-repository orchestration is needed in the future, the right place already exists without touching the ViewModel.

**Why `LikedMember` as a separate projection?**
Like state is the user's opinion about a member, not a property of the member itself. Keeping it separate means `CommunityMember` faithfully reflects remote data and mappers know nothing about likes. Other projections (blocked, favourited) compose the same way.

**Why `cachedIn(viewModelScope)` in the ViewModel?**
Without it, every recomposition that re-collects the flow would restart pagination from page 1. `cachedIn` materialises the `PagingData` in the ViewModel scope, sharing it across recompositions and configuration changes.

**Why a `Channel` for events instead of state?**
State represents what the UI should show right now. One-shot events (snackbars, toggle errors) are not state — they should fire exactly once. Storing them as nullable `StateFlow` requires manual clearing and causes them to reappear on configuration change. `Channel.BUFFERED` + `receiveAsFlow` solves this cleanly.

---

## Assumptions

- **End of pagination**: inferred from response size (`< 20` members). The API provides no explicit end-of-list metadata. A last page with exactly 20 members would require an extra request to detect the end; accepted as an unlikely edge case.
- **Native language badge**: shows only the first entry from the `natives` array, matching the reference screenshot.
- **Bio text**: generated client-side from the full `natives` array — "I can help you learn [native languages]". The API's `topic` field doesn't match the reference screenshot content.
- **Corrupt members**: silently discarded (e.g. blank `firstName`) so a single bad entry doesn't poison the whole page. In production this would go to a structured logger.
- **Orphaned likes**: if a liked member disappears from the API, their like is preserved. Cleaning them up would require knowing the full member catalogue, which the paginated API doesn't expose.
- **Language display names**: always rendered in English via `Locale.ENGLISH`, regardless of the device locale.

---

## Trade-offs and known limitations

- **`PagingData` in the domain layer** — couples the domain to AndroidX. The alternative would be a custom `PagedStream<T>` abstraction with mapping in the data layer. Pragmatic over pure: the cost isn't justified at this scale.
- **No dark theme** — the reference screenshot is light-only. The theme structure leaves dark mode as a trivial extension.
- **No instrumented UI tests** — ViewModel and pure component logic is covered by unit tests. Compose UI tests would add significant boilerplate for what would amount to snapshot-style assertions.
- **No CI** — the natural next step would be a GitHub Actions workflow running `./gradlew test` on every push.
- **Single module** — a split into `:core`, `:data`, `:feature-community` would speed up incremental builds. The package structure already respects the boundaries that split would draw.

### With more time I would add

1. CI on GitHub Actions (tests + lint).
2. Detekt and ktlint with a pre-push hook.
3. Snapshot tests for `MemberCard` with Paparazzi.
4. A logging abstraction in the data layer for discarded corrupt members.

---

## Testing

```bash
./gradlew test                   # Unit tests (JVM)
./gradlew connectedAndroidTest   # Instrumented tests (DAO)
```

The strategy prioritises return-on-investment over coverage as a metric. Tests cover:

- Domain invariants (`Language`, `CommunityMember` reject invalid input)
- Mapping logic (DTO → domain: trimming, filtering, validation)
- Error translation (`CommunityRemoteDataSource` converts exceptions into typed `DataError`s)
- Reactive persistence (`LikedMemberDao` emits on every change)
- ViewModel behaviour (`onLikeToggled` emits a failure event on error, stays silent on success)
- Pure functions (`joinHumanReadable`)

Explicitly not covered: Hilt module wiring (compile-time verified), the Retrofit interface, `Pager` configuration, and Compose screen layout.

---

## Project structure

```
app/src/main/java/com/mmg/testfortandem/
├── app/
│   └── TandemApplication.kt
├── MainActivity.kt
├── di/                       AppModule (network, database), RepositoryModule
├── data/
│   ├── remote/               Retrofit API, DTOs, mappers, remote data source
│   ├── local/                Room, DAOs, entities, local data source
│   ├── paging/               PagingSource implementation
│   └── repository/           CommunityRepository implementation
├── domain/
│   ├── model/                Language, CommunityMember, LikedMember
│   ├── repository/           CommunityRepository interface
│   └── usecase/              ObserveCommunity, ObserveLikedIds, ToggleMemberLike
└── presentation/
    ├── community/            CommunityScreen, ViewModel, UI events
    ├── components/           MemberCard and sub-components
    └── theme/                Material 3 theme
```
