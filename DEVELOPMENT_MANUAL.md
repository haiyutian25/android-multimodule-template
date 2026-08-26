# 开发手册（Development Manual）

> 模板：`architecture-templates-multimodule`
> 架构参照：Now in Android（NiA）
> 更新日期：2026-08-27
> 本手册说明：模板的整体架构、各模块职责、核心模式（UDF + MVVM、离线优先），以及**后期如何基于本模板开发任意业务功能**。

---

## 一、模板是什么

这是一个**多模块 Android 架构模板**，以 Google 官方 Now in Android（NiA）为蓝本，沉淀了一套经过验证的现代 Android 架构：

- **单向数据流（UDF）+ MVVM**
- **离线优先（Offline-first）**：本地 Room 为唯一读取源，后台从网络同步
- **多模块分层**：UI / Domain / Data 清晰解耦
- **Jetpack Compose + Navigation3 + Hilt + Room + Retrofit + WorkManager**

模板内置一个最小可运行示例（`mymodel`），用于演示整套架构如何串起来。你可以用 `customizer.sh` 一键把它改成你的业务，或按"第六章"手动添加新业务模块。

### 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin（JDK 17） |
| UI | Jetpack Compose（Material 3） |
| 导航 | Navigation3（NavKey 模型） |
| 依赖注入 | Hilt |
| 本地存储 | Room |
| 网络 | Retrofit + OkHttp + kotlinx-serialization |
| 后台同步 | WorkManager |
| 异步 | Kotlin Coroutines + Flow |
| 构建 | Gradle（Kotlin DSL）+ build-logic 约定插件 |

---

## 二、架构总览

### 分层与依赖方向

```
┌─────────────────────────────────────────────────────┐
│  app（应用壳：单 Activity、导航组装、主题）              │
└───────────────┬─────────────────────────────────────┘
                │ 依赖
┌───────────────▼───────────────┐
│  feature:*:impl（UI + ViewModel）│ ──► feature:*:api（导航契约 NavKey）
└───────────────┬───────────────┘            │
                │ 依赖                        │
┌───────────────▼───────────────┐            │
│  core:domain（UseCase）         │            │
└───────────────┬───────────────┘            │
┌───────────────▼───────────────┐            │
│  core:data（Repository，离线优先）│           │
└─────┬───────────────┬─────────┘            │
┌─────▼──────┐  ┌─────▼──────┐               │
│ core:database│ │ core:network│              │
│  (Room)     │  │ (Retrofit) │              │
└─────────────┘  └────────────┘              │
        core:model（纯领域模型，最底层，被各层共享）
```

**依赖规则**：自上而下单向依赖，**禁止反向依赖、禁止循环依赖**。`core:model` 是最底层的纯领域模型，不依赖任何模块。

### 核心原则

1. **UDF + MVVM**：ViewModel 暴露 `StateFlow`，Compose 用 `collectAsStateWithLifecycle` 消费；状态下行、事件上行（详见第四章）。
2. **离线优先**：读取只走本地 Room，网络同步在后台进行（详见第五章）。
3. **feature 双模块拆分**：`feature:*:api`（导航契约）/ `feature:*:impl`（UI + ViewModel），feature 之间只通过 api 模块解耦通信。
4. **约定优于配置**：`build-logic` 约定插件统一管理各模块的构建配置，避免重复。

---

## 三、模块清单与职责

### 应用与工具模块

| 模块 | 职责 |
|---|---|
| `app` | 应用壳：`MyApplication`（Hilt 入口 + 启动同步）、`MainActivity`（单 Activity + 主题 + 导航组装 `MainNavigation`） |
| `benchmarks` | Macrobenchmark 启动基准测试 + Baseline Profile 生成 |
| `ui-test-hilt-manifest` | 提供 `HiltComponentActivity`（空 Hilt Activity），供 Compose UI 测试作宿主 |
| `build-logic` | Gradle 约定插件（统一各模块构建配置） |

### core 模块

| 模块 | 职责 |
|---|---|
| `core:model` | **纯领域模型**（如 `MyModel`），纯 JVM，无任何依赖，被各层共享 |
| `core:common` | 通用工具（纯 JVM）：协程调度器限定符 `@Dispatcher(IO/Default)`、应用级协程作用域 `@ApplicationScope` |
| `core:database` | **Room 本地存储**：`AppDatabase`、`MyModelEntity`、`MyModelDao`，Entity → 领域模型映射 |
| `core:network` | **网络层**：`MyModelNetworkDataSource` 接口 + `RetrofitMyModelNetwork`（Retrofit/OkHttp）+ 网络 DTO `NetworkMyModel` |
| `core:data` | **仓库层（离线优先核心）**：`MyModelRepository` + `DefaultMyModelRepository`；同步抽象 `SyncManager`/`Synchronizer`/`Syncable` |
| `core:data-test` | 数据层 Hilt 测试替身：`FakeMyModelRepository` + `TestDataModule`（`@TestInstallIn` 替换生产绑定） |
| `core:domain` | **UseCase 层**：`GetMyModelsUseCase`、`AddMyModelUseCase`（组合仓库的单一职责操作） |
| `core:navigation` | **Navigation3 基建**：`Navigator`、`NavigationState`（顶层栈 + 子栈管理） |
| `core:designsystem` | **设计系统**：`AppTheme`（Color/Type/Shape/Spacing）、可复用组件（`UiStateView`/`LoadingIndicator`/`ErrorView`/`EmptyView`）、`AppIcons` |
| `core:ui` | **跨 feature 共享业务 UI** 层（当前为占位，`api` 暴露 designsystem，随业务扩展填充） |
| `core:testing` | 测试基建：`HiltTestRunner`、`MainDispatcherRule`、`TestMyModelRepository`、`TestSyncManager`、`TestDispatchersModule` |

### feature 模块（双模块拆分）

| 模块 | 职责 |
|---|---|
| `feature:mymodel:api` | **导航契约**：`Main` NavKey（`@Serializable`），供其它模块导航到本 feature |
| `feature:mymodel:impl` | **UI + ViewModel**：`MyModelScreen`、`MyModelViewModel`、`EntryProvider`（导航注册） |

### sync 模块

| 模块 | 职责 |
|---|---|
| `sync:work` | **WorkManager 同步**：`SyncWorker`（`@HiltWorker`）、`WorkManagerSyncManager`、`Sync.initialize`、`DelegatingWorker` |
| `sync:sync-test` | 同步测试替身：`NeverSyncingSyncManager` + `TestSyncModule` |

---

## 四、单向数据流（UDF）+ MVVM

这是模板的**核心 UI 架构模式**，所有业务功能都必须遵循。

### 模式要点

- **ViewModel 暴露 `StateFlow`**：用 `stateIn(viewModelScope, WhileSubscribed(5000), 初始值)` 把 Flow 转成 StateFlow。
- **Compose 用 `collectAsStateWithLifecycle` 消费**：生命周期感知的状态收集。
- **状态下行、事件上行**：状态经 StateFlow 下发给 UI；UI 事件通过 lambda 回调传给 ViewModel。
- **有状态 / 无状态分层**：有状态 Composable 持 ViewModel、收集状态；无状态 Composable 只接收状态 + 事件回调，纯渲染（便于预览和测试）。
- **UiState 用 `sealed interface`**：`Loading / Error / Empty / Success`。

### 代码示例

**ViewModel（暴露 StateFlow）**：

```kotlin
@HiltViewModel
class MyModelViewModel @Inject constructor(
    private val getMyModels: GetMyModelsUseCase,
    private val addMyModel: AddMyModelUseCase,
    syncManager: SyncManager,
) : ViewModel() {

    val uiState: StateFlow<UiState<List<String>>> = getMyModels()
        .map { if (it.isEmpty()) UiState.Empty else UiState.Success(it) }
        .catch { emit(UiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addMyModel(name: String) = viewModelScope.launch { addMyModel(name) }
}
```

**Compose（collectAsStateWithLifecycle 消费 + 事件上行）**：

```kotlin
@Composable
fun MyModelScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyModelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    // 有状态层收集状态后，传给无状态层渲染
    // 事件通过 viewModel::addMyModel / viewModel::retry 上行
    UiStateView(
        uiState = uiState,
        onRetry = viewModel::retry,
        successContent = { items -> MyModelList(items = items) },
    )
}
```

### 约定清单（开发新功能必须遵守）

1. ViewModel 只依赖 **UseCase**，不直接依赖 Repository。
2. 状态一律 `StateFlow` + `stateIn(WhileSubscribed(5000))`。
3. Compose 一律 `collectAsStateWithLifecycle`（不要用 `collectAsState`）。
4. Compose **不直接**注入 Repository/UseCase/SyncManager，只通过 ViewModel。
5. 业务状态不放 Compose 本地（`mutableStateOf` 只用于瞬态 UI 态，如输入框文本）。
6. UiState 用 `sealed interface`，UI 必须处理全部状态（Loading/Error/Empty/Success），不得静默吞错。

---

## 五、离线优先（Offline-first）+ 同步机制

### 核心思想

**读取只走本地 Room（断网也能用），网络数据通过后台同步写入本地。** UI 永远只读本地库。

### 数据流

```
启动 → MyApplication.onCreate → Sync.initialize 入队 WorkManager（网络约束 CONNECTED）
     → SyncWorker（有网时）→ repository.syncWith → 网络拉取 → 写入 Room
UI   → ViewModel → repository.myModels → 只读 Room（离线也能显示缓存）
```

### 关键实现点

| 环节 | 位置 | 说明 |
|---|---|---|
| 本地读 | `DefaultMyModelRepository.myModels` | 只读 `myModelDao`，绝不直连网络 |
| 网络同步 | `DefaultMyModelRepository.syncWith` | 从 `networkDataSource` 拉取 → 写入 Room |
| 同步抽象 | `core:data` `Synchronizer`/`Syncable` | 仓库实现 `Syncable` 即可被同步 |
| 后台执行 | `sync:work` `SyncWorker` | `@HiltWorker`，调 `repository.sync()`，失败 `retry` |
| 触发 | `Sync.initialize` | 应用启动入队唯一同步任务（`ExistingWorkPolicy.KEEP`） |
| 同步状态 | `WorkManagerSyncManager` | 观察 `WorkInfo` 得 `isSyncing`，`requestSync()` 重新入队 |

> 当前同步策略为"空库则拉全量"的占位实现（`syncWith` 内有 `TODO`）；接入真实后端后可升级为 change-list 增量同步。

---

## 六、如何开发新业务功能（重点）

以新增一个 **`task`（任务）** 功能为例，演示完整流程。有两条路径：

### 路径 A：用 `customizer.sh` 一键生成（推荐用于新项目起步）

如果这是你的新项目起点，直接运行定制脚本，把模板的 `mymodel` 示例改成你的业务模型：

```bash
bash customizer.sh com.example.todo TodoItem TodoApp \
    --entity-field title --database-class TodoDatabase --main-activity TodoActivity \
    --query-method loadTodos --insert-method saveTodo --add-method addTodo \
    --fake-data "Buy milk,Walk dog"
```

- 位置参数：`包名` `数据模型名(PascalCase)` `[Application类名]`
- 可选参数：`--entity-field` / `--fake-data` / `--database-class` / `--main-activity` / `--query-method` / `--insert-method` / `--add-method`
- 脚本会重命名包、模型、模块目录、各层方法名，产出一个可直接开发的业务工程。

### 路径 B：在现有模板上手动添加新 feature

按"自底向上"顺序逐层添加：

**1. 领域模型 → `core:model`**
```kotlin
data class Task(val uid: Int, val title: String)
```

**2. 本地存储 → `core:database`**
- 新增 `TaskEntity`（`@Entity`）+ `TaskDao`（`@Dao`，`getTasks(): Flow<List<TaskEntity>>` + `insertTask`）。
- 在 `AppDatabase` 注册 Entity 与 DAO（`version` +1 并写迁移，或新库）。
- 提供 `TaskEntity.toModel(): Task` 映射。

**3. 网络层 → `core:network`**
- 新增网络 DTO `NetworkTask`（`@Serializable`，与领域模型分离）。
- 在网络数据源接口加 `fetchTasks()`，Retrofit 实现加对应 `@GET`。

**4. 仓库层 → `core:data`（离线优先）**
```kotlin
interface TaskRepository : Syncable {
    val tasks: Flow<List<Task>>
}
class DefaultTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val networkDataSource: ...,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : TaskRepository {
    override val tasks = taskDao.getTasks().map { list -> list.map { it.toModel() } } // 只读本地
    override suspend fun syncWith(synchronizer: Synchronizer) = suspendRunCatching { /* 拉网络入库 */ }.isSuccess
}
```
- 在 `DataModule` 用 `@Binds` 绑定 `DefaultTaskRepository → TaskRepository`。
- 实现 `Syncable` 后，`SyncWorker` 会自动把它纳入同步。

**5. UseCase → `core:domain`**
```kotlin
class GetTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repo.tasks
}
```

**6. feature api → 新建 `feature:task:api`**
- 应用 `template.android.feature.api` 约定插件。
- 定义 `@Serializable data object TaskList : NavKey`（导航契约）。

**7. feature impl → 新建 `feature:task:impl`**
- 应用 `template.android.feature.impl` + `template.android.library.compose` 约定插件。
- `TaskViewModel`（依赖 UseCase + SyncManager，暴露 StateFlow）。
- `TaskScreen`（`collectAsStateWithLifecycle` + `UiStateView` 处理四态）。
- `EntryProvider`（`entry<TaskList> { TaskScreen(...) }` 导航注册）。

**8. 组装 → `app`**
- `app` 依赖 `feature:task:api` 与 `feature:task:impl`。
- 在 `MainNavigation` 的 `entryProvider { ... }` 里加入 `TaskEntryProvider(navigator)`；如需作为顶层目的地，加入 `rememberNavigationState` 的 `topLevelKeys`。

**9. 注册模块 → `settings.gradle.kts`**
```kotlin
include(":feature:task:api")
include(":feature:task:impl")
```

> 约定插件（`template.android.feature.api/impl`）已自动注入 feature 公共依赖（`core:ui`、lifecycle、navigation3 等），无需在每个 feature 里手写。

---

## 七、构建与测试

| 命令 | 用途 |
|---|---|
| `gradlew assembleDebug` | 构建 Debug |
| `gradlew testDebugUnitTest` | 单元测试 |
| `gradlew :app:assembleDebugAndroidTest` | 构建仪器测试 APK |
| `gradlew :core:data:createDebugCombinedCoverageReport` | 生成 Jacoco 覆盖率报告（需先有执行数据） |
| `gradlew :benchmarks:pixel6Api33...Benchmark` | 运行基准测试（需 GMD/设备） |

> 要求 **JDK 17+**；`settings.gradle.kts` 启用了 `TYPESAFE_PROJECT_ACCESSORS`（用 `projects.xxx` 引用模块）。

---

## 八、目录结构速览

```
.
├── app/                        # 应用壳
├── benchmarks/                 # 基准测试 + Baseline Profile
├── build-logic/                # Gradle 约定插件
├── core/
│   ├── common/                 # 调度器/作用域（纯 JVM）
│   ├── data/                   # 仓库层（离线优先）+ 同步抽象
│   ├── data-test/              # 数据层 Hilt Fake
│   ├── database/               # Room
│   ├── designsystem/           # 主题 + 组件 + 图标
│   ├── domain/                 # UseCase
│   ├── model/                  # 领域模型（纯 JVM）
│   ├── navigation/             # Navigation3（Navigator/NavigationState）
│   ├── network/                # Retrofit/OkHttp
│   ├── testing/                # 测试基建
│   └── ui/                     # 共享业务 UI（占位）
├── feature/
│   └── mymodel/
│       ├── api/                # 导航契约（NavKey）
│       └── impl/               # UI + ViewModel
├── sync/
│   ├── work/                   # WorkManager 同步
│   └── sync-test/              # 同步测试替身
├── ui-test-hilt-manifest/      # UI 测试 Hilt 宿主 Activity
├── customizer.sh               # 一键定制脚本
└── gradle/libs.versions.toml   # 版本目录
```

---

## 九、相关文档

- `TEMPLATE_EVOLUTION_PLAN.md`：模板从 NiA 吸收各模式的演进记录。
- `TEMPLATE_ALIGNMENT_GAPS.md`：与 NiA 的对齐差距清单与落地记录。
- `TEMPLATE_REALIGNMENT_PLAN.md`：架构/结构/组件的重新对齐方案与实施记录。
