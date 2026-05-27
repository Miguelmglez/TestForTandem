# Tandem Community

Feed paginado de la comunidad de Tandem con reacciones "like" persistentes entre sesiones.

---

## Cómo ejecutarlo

Requiere Android Studio Ladybug o superior y JDK 17.

```bash
git clone <repo>
cd TandemCommunity
./gradlew installDebug
```

---

## Stack

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Async | Coroutines + Flow |
| Red | Retrofit + OkHttp + kotlinx-serialization |
| Base de datos | Room |
| Paginación | Paging 3 |
| Imágenes | Coil |
| Tests | JUnit 4, MockK, Turbine, Truth |

Versiones centralizadas en `gradle/libs.versions.toml`.

---

## Arquitectura

```
presentation/   ← Compose UI, ViewModels, eventos de UI
      │
      ▼
domain/         ← Kotlin puro: entidades, interfaces de repositorio, use cases
      │
      ▼
data/           ← Retrofit, Room, implementaciones, mappers
```

La capa de dominio no tiene dependencias de Android salvo `PagingData` (ver decisiones más abajo). La presentación nunca ve DTOs ni entidades de Room.

### Flujo de datos reactivo

El reto principal fue combinar dos fuentes reactivas independientes: el feed remoto paginado y el estado local de likes. La solución usa `combine` + `PagingData.map`:

```kotlin
combine(pagerFlow, likedIdsFlow) { pagingData, likedIds ->
    pagingData.map { member ->
        LikedMember(member, isLiked = member.id in likedIds)
    }
}
```

Cuando el usuario da like → Room persiste → Room emite el nuevo set → `combine` re-emite el paging data con los flags actualizados → Compose recompone solo las tarjetas afectadas. Sin invalidación manual, sin nueva llamada a red.

---

## Decisiones de diseño

**¿Por qué `PagingSource` directo y no `RemoteMediator`?**
`RemoteMediator` tiene sentido para soporte offline, pero la app no lo necesita. Añadirlo implicaría una tabla de caché en Room, paging keys e invalidación: complejidad sin beneficio real. Solo persisto lo que pertenece al dispositivo: el estado de likes.

**¿Por qué `DataResult<T>` propio en vez de `Result<T>` o excepciones?**
`Result<T>` de Kotlin solo transporta `Throwable`, así que los errores quedan sin tipo. Con una sealed class (`NoConnection`, `Timeout`, `Server`, `Unknown`) el compilador fuerza el manejo exhaustivo en cada `when`.

**¿Por qué Room para los likes y no DataStore?**
DataStore es suficiente para un booleano, pero Room da queries reactivos via `Flow` de serie y escala si algún día los likes llevan metadatos. El modelo es "presencia = liked": si la fila existe, el miembro está likeado. Sin columna `isLiked: Boolean` que mantener sincronizada.

**¿Por qué use cases si son tan finos?**
Actualmente cada use case envuelve una llamada al repositorio, pero el ViewModel no importa el repositorio directamente, lo que mantiene la capa de presentación desacoplada del contrato de datos. Si en el futuro hay que añadir analytics, validación o combinar varios repositorios, el sitio natural ya existe sin tocar el ViewModel.

**¿Por qué `LikedMember` como proyección separada?**
El estado de like es información del usuario sobre un miembro, no una propiedad del miembro en sí. Separarlo significa que `CommunityMember` refleja fielmente los datos de la API y los mappers no saben nada de likes. Si aparecen otras proyecciones (bloqueado, favorito), componen igual de fácil.

**¿Por qué `cachedIn(viewModelScope)` en el ViewModel?**
Sin él, cada recomposición que recogiera el flow relanzaría la paginación desde la página 1. `cachedIn` materializa el `PagingData` en el scope del ViewModel y lo comparte entre recomposiciones y cambios de configuración.

**¿Por qué un `Channel` para eventos en vez de estado?**
El estado representa lo que la UI debe mostrar ahora. Los eventos puntuales (snackbars, errores de toggle) no son estado: deben dispararse exactamente una vez. Guardarlos en un `StateFlow` nullable obliga a limpiarlos manualmente y reaparecen en los cambios de configuración. `Channel.BUFFERED` + `receiveAsFlow` lo resuelve limpiamente.

---

## Supuestos

- **Fin de paginación**: se infiere por tamaño de respuesta (`< 20` miembros). La API no devuelve metadato explícito de última página. Una página final con exactamente 20 miembros haría falta un request extra para detectar el fin; lo acepto como caso improbable.
- **Idioma nativo**: se muestra solo el primero del array `natives`, siguiendo la captura de referencia. El array completo de `learns` sí aparece en la bio.
- **Miembros corruptos**: se descartan silenciosamente (ej: `firstName` vacío) para que un dato malo no envenene la página entera. En producción esto iría al sistema de logging.
- **Likes huérfanos**: si un miembro likeado desaparece de la API, su like se conserva. Limpiarlos requeriría conocer el catálogo completo, que la API paginada no expone.
- **Texto de la bio**: se genera en cliente desde el array `learns` porque el campo `topic` de la API no coincide con la captura de referencia.
- **Nombre de idioma**: usa `Locale.forLanguageTag(...).getDisplayLanguage(...)` con el locale del dispositivo, así "en" se muestra como "English" o "inglés" según el teléfono.

---

## Trade-offs y limitaciones conocidas

- **`PagingData` en la capa de dominio** — acopla el dominio a AndroidX. La alternativa sería una abstracción `PagedStream<T>` propia con mapeo en la capa de datos. Pragmático sobre puro: el coste no está justificado a este tamaño.
- **Sin tema oscuro** — la captura de referencia es solo light. La estructura del tema lo deja como extensión trivial.
- **Sin tests de UI instrumentados** — la lógica de ViewModel y componentes puros está cubierta por tests unitarios. Los tests de Compose añadirían bastante boilerplate para aserciones tipo snapshot.
- **Sin CI** — el siguiente paso natural sería un workflow de GitHub Actions con `./gradlew test` en cada push.
- **Módulo único** — una división en `:core`, `:data`, `:feature-community` aceleraría builds incrementales. La estructura de paquetes ya respeta los límites que haría esa separación.

### Con más tiempo añadiría

1. CI en GitHub Actions (tests + lint).
2. Detekt y ktlint con hook pre-push.
3. Tests de snapshot del `MemberCard` con Paparazzi.
4. Abstracción de logging en la capa de datos para los miembros descartados.
5. Pull-to-refresh en el listado.

---

## Tests

```bash
./gradlew test                   # Tests unitarios (JVM)
./gradlew connectedAndroidTest   # Tests instrumentados (DAO)
```

La estrategia prioriza ROI sobre cobertura como métrica. Los tests cubren:

- Invariantes de dominio (`Language`, `CommunityMember` rechazan datos inválidos)
- Lógica de mapping (DTO → dominio: trimming, filtrado, validación)
- Traducción de errores (`CommunityRemoteDataSource` convierte excepciones en `DataError`)
- Persistencia reactiva (`LikedMemberDao` emite en cada cambio)
- Comportamiento del ViewModel (`onLikeToggled` emite evento de fallo, silencioso en éxito)
- Funciones puras (`joinHumanReadable`)

No están cubiertos: el wiring de Hilt (verificado en compilación), la interfaz de Retrofit, la configuración del `Pager` ni el layout de Compose.

---

## Estructura del proyecto

```
app/src/main/java/com/mmg/testfortandem/
├── app/
│   └── TandemApplication.kt
├── MainActivity.kt
├── di/                       AppModule (red, base de datos), RepositoryModule
├── data/
│   ├── remote/               API Retrofit, DTOs, mappers, data source remoto
│   ├── local/                Room, DAOs, entidades, data source local
│   ├── paging/               Implementación de PagingSource
│   └── repository/           Implementación de CommunityRepository
├── domain/
│   ├── model/                Language, CommunityMember, LikedMember
│   ├── repository/           Interfaz CommunityRepository
│   └── usecase/              ObserveCommunity, ObserveLikedIds, ToggleMemberLike
└── presentation/
    ├── community/            CommunityScreen, ViewModel, eventos de UI
    ├── components/           MemberCard y subcomponentes
    └── theme/                Tema Material 3
```
