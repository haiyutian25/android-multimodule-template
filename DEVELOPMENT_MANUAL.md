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

> 上图为主数据流。此外：`feature:*:impl` 还直接依赖 `core:data`（注入 `SyncManager`）、并经约定插件依赖 `core:ui`（→ `core:designsystem`）；`feature:*:api` 依赖 `core:navigation`（NavKey）；`sync:work` 依赖 `core:data` 承担后台同步。

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
| `core:testing` | 测试基建：`HiltTestRunner`、`MainDispatcherRule`、`TestMyModelRepository`、`TestSyncManager`、`TestDispatcherModule`、`TestDispatchersModule` |

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

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<String>>> = retryTrigger
        .flatMapLatest {
            getMyModels()
                .map { data -> if (data.isEmpty()) UiState.Empty else UiState.Success(data) }
                .catch { emit(UiState.Error(it)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addMyModel(name: String) = viewModelScope.launch { addMyModel(name) }

    fun retry() { retryTrigger.value++ }   // 重试：重新触发数据加载
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
    Column(modifier.safeDrawingPadding().fillMaxSize()) {
        if (isSyncing) LinearProgressIndicator(Modifier.fillMaxWidth())   // 同步中指示
        MyModelInput(onSave = viewModel::addMyModel)                      // 事件上行
        UiStateView(
            uiState = uiState,
            onRetry = viewModel::retry,                                   // 事件上行
            modifier = Modifier.fillMaxWidth().weight(1f),
            successContent = { items -> MyModelList(items = items) },     // 无状态渲染
        )
    }
}
```

### 约定清单（开发新功能必须遵守）

1. ViewModel 依赖 **UseCase** 获取业务数据（可注入 `SyncManager` 等架构组件），**不直接依赖 Repository**。
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
| 同步抽象 | `core:data` `Synchronizer`/`Syncable` | 仓库实现 `Syncable` 获得同步能力；**还需**注入 `SyncWorker` 并调 `sync()` 才真正参与同步（非自动，见 6.2） |
| 后台执行 | `sync:work` `SyncWorker` | `@HiltWorker`，调 `repository.sync()`，失败 `retry` |
| 触发 | `Sync.initialize` | 应用启动入队唯一同步任务（`ExistingWorkPolicy.KEEP`） |
| 同步状态 | `WorkManagerSyncManager` | 观察 `WorkInfo` 得 `isSyncing`，`requestSync()` 重新入队 |

> 当前同步策略为"空库则拉全量"的占位实现（`syncWith` 内有 `TODO`）；接入真实后端后可升级为 change-list 增量同步。
> **升级前提**：NiA 的 change-list 增量同步依赖版本号存储（`ChangeListVersions`，NiA 存于 DataStore，由 `Synchronizer.getChangeListVersions/updateChangeListVersions` 读写）。本模板已决策"不做 DataStore"，升级前需先确定版本存储方案（如用 Room 表存同步版本并为 `Synchronizer` 扩展版本读写），否则增量同步会卡在版本管理环节。

---

## 六、如何开发（流程 + 模块归属）

本章回答三个问题：**按什么流程开发**、**什么代码放哪个模块**、**易混淆时怎么决策**；最后给出两条落地路径（`customizer.sh` 一键生成 / 手动逐层添加，以新增 `task` 功能为例）。

### 6.1 总体开发流程（自底向上）

**原则：自底向上逐层构建**——先写被依赖的下层，再写依赖它的上层，保证每一步其依赖都已就绪。依赖方向自上而下单向（见第二章）。

```
新增一个业务功能，自底向上依次经过：

① core:model         → 领域模型（纯数据类，无 Android 依赖）
② core:database      → Entity + DAO + 注册 Database（+ toModel 映射 + DatabaseModule 提供 DAO）
③ core:network       → 网络 DTO + Retrofit 数据源
④ core:data          → Repository（离线优先）+ DataModule @Binds 绑定
⑤ core:domain        → UseCase（组合 Repository 的单一操作）
⑥ settings.gradle.kts → include 新模块（feature:xxx:api / feature:xxx:impl）
⑦ feature:xxx:api    → NavKey 导航契约（@Serializable）
⑧ feature:xxx:impl   → ViewModel + Screen + EntryProvider
⑨ app                → MainNavigation 组装 + 添加模块依赖
⑩ 测试联动           → core:data-test（TestDataModule 补新仓库 Fake 绑定）+ core:testing（按需补测试替身）
```

> - ①~⑤ 与 ⑩ 在模板已有模块内追加代码即可，无需改 `settings.gradle.kts`；只有**新建 feature 模块**才需要 ⑥ 的 include。
> - **⑩ 不是可选项**：`TestDataModule` 以 `@TestInstallIn` **整体替换** `DataModule`，往 `DataModule` 新增仓库绑定后，若不同步在 `TestDataModule` 补 Fake 绑定，所有现存 Hilt 仪器测试都会因缺少绑定而构建失败。
> - **网络配置**：网络层从根目录 `local.properties` 读取 `BACKEND_URL` 注入 BuildConfig（见 `core:network/build.gradle.kts`）；未配置时回退占位地址，同步会始终失败、列表为空，属接入真实后端前的预期行为。
> - **include 必须先于引用**：`projects.feature.xxx.api` 等 typesafe 访问器由 `settings.gradle.kts` 的 include 生成，不先 include 并 Gradle Sync，步骤⑦⑧的 `build.gradle.kts` 依赖声明无法编译。
> - **领域模型贯穿各层**：Repository 向上暴露 `Flow<List<领域模型>>`（如 `Flow<List<Task>>`），由 database 层 `toModel()` 映射后经 UseCase 传到 ViewModel；不要在 Repository 把模型降级成 `List<String>` 等基本类型。
> - 建议每完成一层就跑 `gradlew assembleDebug` + `testDebugUnitTest`，尽早暴露问题。若仓库需同步（实现 `Syncable`），还要把它注入 `SyncWorker`（`sync:work`）并在 `doWork` 调 `sync()` 才纳入同步（非自动）。

### 6.2 模块归属速查表（什么代码放哪个模块）

| 你要写的东西 | 放哪个模块 | 说明 |
|---|---|---|
| 领域模型（纯数据类，无 Android 依赖） | `core:model` | 最底层，被各层共享 |
| Room Entity / DAO / Database / `toModel` 映射 | `core:database` | 本地持久化；新增 DAO 还要在 `DatabaseModule` 补 `@Provides` |
| 网络 DTO / Retrofit 接口 / 网络数据源实现 | `core:network` | 只负责网络，不碰本地 |
| Repository 接口 + 离线优先实现 | `core:data` | UI 的唯一数据入口 |
| 同步能力（仓库需同步） | `core:data` + `sync:work` | 仓库实现 `Syncable.syncWith`；**还需**注入 `SyncWorker` 并调 `sync()`（非自动；多仓库可 `awaitAll` 并行）；升级 change-list 增量同步前先读第五章"升级前提" |
| UseCase（组合 Repository 的聚焦操作） | `core:domain` | ViewModel 经它取数（架构组件如 `SyncManager` 另行注入） |
| ViewModel | `feature:*:impl` | 依赖 UseCase + SyncManager |
| Compose Screen / 本 feature 私有组件 | `feature:*:impl` | 业务界面 |
| NavKey 导航契约 | `feature:*:api` | 供其它模块导航进来 |
| 可复用设计组件 / 主题 / 图标 | `core:designsystem` | 零业务、零领域模型 |
| 跨 feature 共享的业务 UI | `core:ui` | 消费 `core:model` 的组件 |
| 数据层 Hilt Fake（替换生产绑定） | `core:data-test` | `@TestInstallIn` 整体替换 `DataModule`；**每往 `DataModule` 新增一个仓库，必须同步在此补 Fake 绑定**，否则仪器测试构建失败 |
| 测试规则 / 测试替身 / 测试数据 | `core:testing` | 通用测试基建 |
| WorkManager 同步 Worker | `sync:work` | 后台同步执行 |

### 6.3 归属决策要点（易混淆场景）

- **`core:designsystem` vs `core:ui`**：前者纯设计（主题/通用组件/图标，**零业务、不依赖领域模型**）；后者是**跨 feature 共享的业务 UI**（消费 `core:model`）。只在一个 feature 内用的 UI 放 `feature:*:impl`，不上提。
- **什么时候写 UseCase**：组合一个或多个 Repository 成一个聚焦操作时。模板约定 ViewModel 一律经 UseCase 取数，不直连 Repository。注意：这是**比 NiA 更严的约定**（NiA 的 ViewModel 如 `ForYouViewModel` 实际同时直注 Repository 与 UseCase），模板有意收紧以强制分层。UseCase 的真正价值在"组合多仓库 + 派生新领域模型"（如 NiA `GetFollowableTopicsUseCase` 用 `combine` 组合两个仓库），仅透传单仓库只是形式上的满足。
- **Repository 返回什么**：一律返回领域模型流（`Flow<List<领域模型>>`），让领域模型从 database（`toModel()`）→ data → domain → ViewModel 贯穿传递。内置 `mymodel` 示例为极简演示返回了 `Flow<List<String>>`，业务开发请勿模仿，按领域模型贯穿处理。
- **业务状态放哪**：放 ViewModel 的 `StateFlow`；**瞬态 UI 态**（如输入框文本）才用 Compose 本地 `mutableStateOf`。
- **导航目的地放哪**：`feature:*:api` 的 NavKey；**不要**放 impl，否则别的模块导航进来就得依赖 impl。
- **测试替身放哪**：替换生产 Hilt 绑定的 Fake 放 `core:data-test`；通用测试规则/工具放 `core:testing`；同步替身放 `sync:sync-test`。

### 6.4 路径 A：用 `customizer.sh` 一键生成（推荐用于新项目起步）

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

### 6.5 路径 B：在现有模板上手动添加新 feature（完整示例）

按 6.1 的"自底向上"顺序逐层添加：

**1. 领域模型 → `core:model`**
```kotlin
data class Task(val uid: Int, val title: String)
```

**2. 本地存储 → `core:database`**
- 新增 `TaskEntity`（`@Entity`，主键写法可参照 `MyModelEntity`：`@PrimaryKey(autoGenerate = true) var uid: Int = 0`）+ `TaskDao`（`@Dao`，`getTasks(): Flow<List<TaskEntity>>` + `insertTask`）。
- 在 `AppDatabase` 注册 Entity 与 DAO（`version` +1 并写迁移，或新库）。
- **在 `DatabaseModule` 为新 DAO 补 `@Provides`**（模板逐个 `@Provides` 暴露 DAO，漏掉这步 Hilt 注入会失败）。
- 提供 `TaskEntity.toModel(): Task` 映射。
- 注意：模板示例 `MyModelDao.getMyModels()` 带 `ORDER BY uid DESC LIMIT 10`（演示用限制），新业务 DAO 按需决定排序与条数。

**3. 网络层 → `core:network`**
- 新增网络 DTO `NetworkTask`（`@Serializable`，与领域模型分离）。
- 在网络数据源接口加 `fetchTasks()`，Retrofit 实现加对应 `@GET`。
- 网络层的 `BASE_URL` 来自 `local.properties` 的 `BACKEND_URL`（由 `core:network/build.gradle.kts` 注入 BuildConfig）；接入真实后端前需在 `local.properties` 添加 `BACKEND_URL=https://your.backend/`（须以 `/` 结尾），否则同步始终失败，属预期行为。
- 若新建独立的网络数据源接口（而非扩展现有接口），记得在 `NetworkModule` 补对应 `@Binds`。

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
- **勿忘测试联动**：新增仓库绑定后必须在 `TestDataModule` 补对应 Fake 绑定（详见步骤 10），否则仪器测试构建失败。
- 实现 `Syncable` 后，**还需**把该仓库注入 `SyncWorker`（`sync:work`）并在 `doWork` 调 `sync()` 才会参与同步（非自动；多仓库可用 `awaitAll` 并行）。

**5. UseCase → `core:domain`**
```kotlin
class GetTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repo.tasks
}
```

> 本例为单仓库透传（与内置 `mymodel` 示例相同）。实际业务请优先"组合多仓库 / 派生新模型"（见 6.3 UseCase 条目）。

**6. 注册模块 → `settings.gradle.kts`**（先于步骤 7/8：不 include 则 `projects.feature.task.*` 访问器不存在，后续 `build.gradle.kts` 无法编译）
```kotlin
include(":feature:task:api")
include(":feature:task:impl")
```
include 后执行一次 Gradle Sync，即可使用 `projects.feature.task.api` / `projects.feature.task.impl` 访问器。

**7. feature api → 新建 `feature:task:api`**
- 应用 `template.android.feature.api` 约定插件。
- 定义 `@Serializable data object TaskList : NavKey`（导航契约）。

**8. feature impl → 新建 `feature:task:impl`**
- 应用 `template.android.feature.impl` + `template.android.library.compose` + `template.android.library.jacoco` 约定插件。
- 约定插件已注入公共依赖（`core:ui`、lifecycle、navigation3）；**还需在 `build.gradle.kts` 显式声明本 feature 特有依赖**：`implementation(projects.core.domain)`（UseCase）、`implementation(projects.core.data)`（注入 `SyncManager`）、`implementation(projects.feature.task.api)`，以及 compose/material3 等。
- `TaskViewModel`（依赖 UseCase + SyncManager，暴露 StateFlow）。
- `TaskScreen`（`collectAsStateWithLifecycle` + `UiStateView` 处理四态）。
- `EntryProvider`（`entry<TaskList> { TaskScreen(...) }` 导航注册）。

**9. 组装 → `app`**
- `app` 依赖 `feature:task:api` 与 `feature:task:impl`。
- 在 `MainNavigation` 的 `entryProvider { ... }` 里加入 `TaskEntryProvider(navigator)`；如需作为顶层目的地，加入 `rememberNavigationState` 的 `topLevelKeys`。
- 新 feature 如有界面文案，在 `feature:task:impl/src/main/res/values/` 添加 `strings.xml`（参照 `feature:mymodel:impl`）。
- 新增顶层目的地后，记得跟进 `app` 的 `NavigationTest`（仪器测试）。

**10. 测试联动 → `core:data-test` / `core:testing`（必做，非可选）**
- 新增 `FakeTaskRepository`（实现 `TaskRepository` 接口，参照 `FakeMyModelRepository`），并在 `TestDataModule` 补对应 `@Binds`。原因：`TestDataModule` 以 `@TestInstallIn` **整体替换** `DataModule`，步骤 4 新增的 `TaskRepository` 绑定不会自动进入测试图，漏掉这步所有现存仪器测试都会构建失败。
- 单元测试如需仓库替身，参照 `TestMyModelRepository` 在 `core:testing` 添加 `TestTaskRepository`。

> 约定插件（`template.android.feature.api/impl`）已自动注入 feature 公共依赖（`core:ui`、lifecycle、navigation3 等），无需在每个 feature 里手写。
> 开发完成后运行 `python gen_structure.py` 更新 `WORKSPACE_STRUCTURE.md`，保持结构文档与新模块一致。

---

## 七、构建与测试

| 命令 | 用途 |
|---|---|
| `gradlew assembleDebug` | 构建 Debug |
| `gradlew testDebugUnitTest` | 单元测试 |
| `gradlew :app:assembleDebugAndroidTest` | 构建仪器测试 APK |
| `gradlew :core:data:createDebugCombinedCoverageReport` | 生成 Jacoco 覆盖率报告（需先有执行数据） |
| `gradlew :benchmarks:pixel6Api33DebugAndroidTest` | 在 GMD（`pixel6Api33`）上运行基准测试 |
| `gradlew :app:generateBaselineProfile` | 用 benchmarks 规则生成 Baseline Profile（需 GMD） |

> 要求 **JDK 17+**；`settings.gradle.kts` 启用了 `TYPESAFE_PROJECT_ACCESSORS`（用 `projects.xxx` 引用模块）。

---

## 八、目录结构速览

> 结构由 `gen_structure.py` 从文件系统自动导出（完整机器可读版见 `WORKSPACE_STRUCTURE.md`），此处为标注了职责的同一份结构；模块清单与 `settings.gradle.kts` 完全一致。

```
.
├── app/                        # 应用壳（单 Activity + 导航组装 + 主题）
├── benchmarks/                 # Macrobenchmark + Baseline Profile
├── build-logic/
│   └── convention/             # Gradle 约定插件
├── core/
│   ├── common/                 # 调度器/作用域（纯 JVM）
│   ├── data/                   # 仓库层（离线优先）+ 同步抽象
│   ├── data-test/              # 数据层 Hilt Fake（TestDataModule）
│   ├── database/               # Room
│   ├── designsystem/           # 主题 + 状态组件 + 图标
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
├── gradle/
│   └── libs.versions.toml      # 版本目录
├── sync/
│   ├── work/                   # WorkManager 同步
│   └── sync-test/              # 同步测试替身
├── ui-test-hilt-manifest/      # UI 测试 Hilt 宿主 Activity
└── customizer.sh               # 一键定制脚本
```

> 结构变化后可随时运行 `python gen_structure.py` 重新生成 `WORKSPACE_STRUCTURE.md`（含顶层文件与 18 个模块的权威清单）。

---

## 九、相关文档

- `TEMPLATE_EVOLUTION_PLAN.md`：模板从 NiA 吸收各模式的演进记录。
- `TEMPLATE_ALIGNMENT_GAPS.md`：与 NiA 的对齐差距清单与落地记录。
- `TEMPLATE_REALIGNMENT_PLAN.md`：架构/结构/组件的重新对齐方案与实施记录。
- `TEMPLATE_GENERATOR_PLAN.md`：「干净 hello world 骨架 + 生成器脚本」改造方案（待实施）。
- `WORKSPACE_STRUCTURE.md`：脚本自动导出的目录/模块结构（`python gen_structure.py` 重新生成）。
