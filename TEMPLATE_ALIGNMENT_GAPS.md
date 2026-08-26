# 模板对齐差距清单：相对 Now in Android 的未对齐项

> 记录日期：2026-08-26
> 参照对象：`nowinandroid/`（Google 官方示例）
> 本模板：`architecture-templates-multimodule`
> 说明：本文档记录**除 `TEMPLATE_EVOLUTION_PLAN.md` 已列项之外**的其余未对齐项。
> 已在演进规划中明确"不做/暂缓"的项（DataStore、设计系统、CI、双风味、Firebase/分析/通知、自定义 Lint、Spotless、Roborazzi 截图测试）**不在此重复**。

---

## 🔴 建议对齐（低成本、真实收益）✅ 已完成（2026-08-26）

> 本节两项均已对齐落地，验证方式见文末"落地记录"。

### 1. `gradle.properties` 配置落后 ✅

当前模板的 `gradle.properties` 配置明显落后于 NiA，属于最容易、最值得优先处理的一项。

| 配置项 | 本模板现状 | NiA 做法 | 说明 |
|--------|-----------|---------|------|
| 配置缓存开关 | `org.gradle.unsafe.configuration-cache=true` | `org.gradle.configuration-cache=true` | `unsafe.` 前缀是旧写法，应改用稳定属性名 |
| Kotlin 守护进程内存 | 缺失 | `kotlin.daemon.jvmargs=-Xmx...` | Kotlin 编译守护进程内存调优 |
| KSP 工程隔离 | 缺失 | `ksp.project.isolation.enabled=true` | 配合配置缓存/隔离工程 |
| 配置缓存并行 | 缺失 | `org.gradle.configuration-cache.parallel=true` | 并行重建配置 |
| 默认构建特性 | 缺失 | `android.defaults.buildfeatures.resvalues=false`、`android.defaults.buildfeatures.shaders=false` | 关闭默认构建特性以提速 |
| 测试 APK 保留 | 缺失 | `android.injected.androidTest.leaveApksInstalledAfterRun=true` | 仪器测试后保留 APK，便于调试 |
| JVM 参数 | 仅 `-Xmx2048m` | G1GC + CodeCache 等完整调优 | NiA 有 `-XX:+UseG1GC`、`-XX:SoftRefLRUPolicyMSPerMB`、`-XX:ReservedCodeCacheSize`、`-XX:+HeapDumpOnOutOfMemoryError` 等 |

参考：`nowinandroid/gradle.properties`

### 2. `compose_compiler_config.conf`（Compose 稳定性配置）✅

- 本模板**完全没有**该文件。
- NiA 用它把 model 类（`core.model.data.*`）及 `java.time.ZoneId`/`ZoneOffset` 标记为 **stable**，避免 Compose 编译器把这些不可变数据类误判为不稳定类型，从而减少不必要的重组（recomposition）。
- 对齐方式：新建 `compose_compiler_config.conf`，将本模板的 model 包（`android.template.core.model.*`）声明为 stable，并在 Compose 编译器配置中引用该文件。

参考：`nowinandroid/compose_compiler_config.conf`

---

## 🟡 可选对齐（结构性、改动中等）✅ 已完成（2026-08-26）

> 本节两项均已对齐落地，验证方式见文末"落地记录（可选对齐部分）"。

### 3. `core:navigation` 模块 ✅

- NiA 有独立的 `core:navigation` 模块，包含 `Navigator` 与 `NavigationState`，专门管理 Navigation3 的回退栈、顶层目的地切换（`navigate`/`goBack`/顶层栈与子栈分离）。
- 本模板的 `app` 目前是**内联**管理回退栈，没有抽出独立的导航状态管理模块。
- 评估：单 feature 模板未必需要完整的顶层导航管理，但这是 NiA 的标准做法。若后续模板要支持多顶层目的地/更复杂的导航，建议对齐。

参考：`nowinandroid/core/navigation/src/main/kotlin/.../Navigator.kt`、`NavigationState.kt`

### 4. build-logic 缺失的约定插件（排除项之外）✅

- **feature 专用约定插件**：NiA 有 `AndroidFeatureApiConventionPlugin` / `AndroidFeatureImplConventionPlugin`，把 feature 模块的公共依赖（`core:ui`、`core:designsystem`、lifecycle、navigation3 等）集中到插件里统一注入；本模板是在每个 feature 的 `build.gradle.kts` 里手写这些依赖。
  - 评估：feature 数量多时才体现价值；当前单个 feature 收益小。
- **Jacoco 约定插件**：NiA 有 `AndroidApplicationJacocoConventionPlugin` / `AndroidLibraryJacocoConventionPlugin`（代码覆盖率基建）。演进规划文档未提及，本模板也没有。

参考：`nowinandroid/build-logic/convention/src/main/kotlin/AndroidFeatureApiConventionPlugin.kt` 等

---

## ⚪ 差异项深度对比（原"可不跟"，已逐项评估）

> 2026-08-26 对三项差异做了两侧实现的深度对比，结论如下。

### 5. `ui-test-hilt-manifest`（UI 测试的 Hilt 方案）

| | NiA `ui-test-hilt-manifest` | 本模板 `test-app` |
|---|---|---|
| 模块类型 | 普通 library，仅 1 个 Activity + 1 个 Manifest | `com.android.test` 独立测试 APK（`targetProjectPath=":app"`） |
| 核心 | `HiltComponentActivity`（`@AndroidEntryPoint` 空 `ComponentActivity`） | `AppTest` 用 `createAndroidComposeRule<MainActivity>()` |
| 测试对象 | 各模块**自己的 Composable**（isolated，测试里自行 setContent） | **真实 MainActivity**（端到端） |

- 两者**目的不同**：NiA 的 `HiltComponentActivity` 是为 [dagger#3394](https://github.com/google/dagger/issues/3394) 提供的 Hilt 宿主 Activity，让每个 feature 能 isolated 跑 Hilt UI 测试；我们的 `test-app` 直接测真实 `MainActivity`（本身已 `@AndroidEntryPoint`），绕开了该 issue。
- NiA 的端到端导航测试（`app/src/androidTest/NavigationTest.kt`）同样用 `createAndroidComposeRule<MainActivity>()`，与我们的 `test-app` 本质一致，只是放在 app 的 androidTest 而非独立模块。
- **结论**：无运行时性能差（都是测试态）。NiA 方案胜在**可扩展**（feature 多了各自 isolated 测试、启动快）；`test-app` 胜在**真实**但更重，且 NiA 并无此模块。
- **处理**：**已完整对齐 NiA**——补齐 `ui-test-hilt-manifest` 模块，**删除 `test-app`**（NiA 无此模块）；app 的 UI 测试保留在 `app/src/androidTest/NavigationTest.kt`（对应 NiA 的同名测试），app 以 `debugImplementation` 接入 `ui-test-hilt-manifest`、`androidTestImplementation` 接入 `core:data-test`（见落地记录）。

### 6. `core:data-test` 独立模块（测试替身组织）

- NiA 有两个分工明确的模块：
  - `core:data-test`：**可被 Hilt 替换的数据层 Fake**（`FakeNewsRepository` 等）+ `TestDataModule`（`@TestInstallIn` 替换 `DataModule`）。
  - `core:testing`：**通用测试基建**（`NiaTestRunner`、测试规则、`TestDispatcher` DI、测试数据、单测用 `Test*Repository`、`TestSyncManager` 等）。
- 本模板只有 `core:testing`（`HiltTestRunner`、`TestDispatcherModule`、`MainDispatcherRule`、`TestSyncManager`、`TestMyModelRepository`），但 **Hilt Fake（`FakeMyModelRepository` + `fakeMyModels`）被写在了生产模块 `core:data` 里**（`core/data/.../di/DataModule.kt`）。
- **结论**：**NiA 架构上更干净**——生产模块不掺测试代码、依赖方向清晰（测试依赖生产，绝不反向）、Fake 可跨测试模块复用。我们曾把 Fake 塞进生产 `core:data`，导致依赖它的模块都会带上测试假实现，是**设计短板**。运行时性能无差异，差别在架构卫生。
- **处理**：**已完整对齐 NiA**——新建 `core:data-test` 模块，将 `FakeMyModelRepository` + `fakeMyModels` 迁出生产 `core:data`，并新增 `TestDataModule`（`@TestInstallIn` 替换 `DataModule`）；`core:data` 的单测改用内联数据、不再依赖 `core:data-test`（与 NiA 一致，避免循环依赖）。
- **绑定对照（1:1 替换完整性佐证）**：NiA 原则是 `TestDataModule` 把生产 `DataModule` 的**每个绑定**都换成 Fake。

  | 生产 `DataModule` 绑定 | NiA 的 Fake | 本模板 |
  |---|---|---|
  | `TopicsRepository` | `FakeTopicsRepository` | 无此仓库 |
  | `NewsRepository` | `FakeNewsRepository` | 无此仓库 |
  | `UserDataRepository` | `FakeUserDataRepository` | 无此仓库 |
  | `RecentSearchRepository` | `FakeRecentSearchRepository` | 无此仓库 |
  | `SearchContentsRepository` | `FakeSearchContentsRepository` | 无此仓库 |
  | `NetworkMonitor` | `AlwaysOnlineNetworkMonitor` | 无此接口（全仓 0 匹配） |
  | `TimeZoneMonitor` | `DefaultZoneIdTimeZoneMonitor` | 无此接口（0 匹配） |
  | —— | —— | `MyModelRepository` → `FakeMyModelRepository` ✅ |

  本模板生产 `DataModule` 仅绑定 `MyModelRepository`，`core:data-test` 的 `TestDataModule` 已 1:1 替换；`SyncManager` 替身在 `sync:sync-test`（`NeverSyncingSyncManager`），与 NiA 分工一致。app 的 androidTest 无需 `sync:sync-test`（`DelegatingWorker` 走 WorkManager 默认自动初始化，`HiltTestApplication` 下 `WorkManager.getInstance()` 不崩），与 NiA 的 app androidTest 行为一致。

### 7. 同步的 change-list 深度

| 维度 | NiA change-list 增量 | 本模板"空库拉全量" |
|---|---|---|
| 首次同步 | 拉全量 | 拉全量 |
| 后续同步 | **只传 change-list（id+版本+删除标记，极小）+ 变化项**（分批 `SYNC_BATCH_SIZE=40`） | 不再同步 |
| 数据新鲜度/正确性 | 持续同步，**更新+删除都会传播** | **首次种子后永不更新**，本地逐渐失真 |
| 网络/服务端压力 | 增量，大数据量+高频同步优势明显 | 一次性最省，但代价是从不更新 |
| 依赖 | **需后端提供"按版本返回变更"接口** | 无特殊要求 |

- NiA 的 `changeListSync`（`SyncUtilities.kt`）：读版本 → 拉该版本后的 change-list → 空则结束 → 按 `isDelete` 拆分，删已删项、只对变化 id 拉完整数据 upsert → 推进版本。
- 本模板 `DefaultMyModelRepository.syncWith` 仅在本地库为空时拉全量插入，之后不再同步（代码中已有 `TODO` 标注）。
- **结论**：**NiA 明显更好**——既正确（传播更新/删除）又在重复同步下性能更优（只传差异）。本模板是占位实现，唯一"优点"是简单、一次性流量最省，但以数据永不更新为代价，生产不可用。**前提**：change-list 需后端支持，与真实后端强相关。
- **处理**：暂不跟（等真实后端提供 change-list 接口后再对齐）。

---

## 处理建议

1. **优先**：第 1 项（`gradle.properties`）+ 第 2 项（`compose_compiler_config.conf`）——低成本、无风险、立刻见效的配置对齐。**（已完成，见落地记录）**
2. **按需**：第 3 项（`core:navigation`）、第 4 项（feature 约定插件 / Jacoco）——结构性改动，视模板定位决定是否引入。**（已完成，见落地记录）**
3. **不跟**：⚪ 部分——方案不同或意义小，保持现状。

---

## 落地记录（建议对齐部分）

**改动文件：**

1. `gradle.properties` —— 对齐 NiA：
   - `org.gradle.unsafe.configuration-cache=true` → `org.gradle.configuration-cache=true`（稳定写法）
   - 新增 `org.gradle.configuration-cache.parallel=true`、`org.gradle.configuration-cache.problems=warn`
   - 新增 `kotlin.daemon.jvmargs`（G1GC + CodeCache 调优）
   - `org.gradle.jvmargs` 由 `-Xmx2048m` 升级为 G1GC + SoftRef + CodeCache + `-Xmx4g -Xms4g`
   - 新增 `android.defaults.buildfeatures.resvalues=false`、`android.defaults.buildfeatures.shaders=false`
   - 新增 `android.injected.androidTest.leaveApksInstalledAfterRun=true`
   - 新增 `ksp.project.isolation.enabled=true`、`org.gradle.configureondemand=false`
2. `compose_compiler_config.conf` —— 新建于根目录，将 `android.template.core.model.*` 及 `java.time.ZoneId`/`ZoneOffset` 标记为 stable。
3. `build-logic/convention/src/main/kotlin/AndroidCompose.kt` —— 通过 `ComposeCompilerGradlePluginExtension.stabilityConfigurationFiles` 接入上述配置文件。
4. `customizer.sh` —— 新增对 `compose_compiler_config.conf` 的包名重写（原脚本只处理 `.kt`/`.kts`，定制后会导致稳定性标记静默失效）。

**验证：**

- 主工程 `assembleDebug` 构建成功，配置缓存正常存储（`Configuration cache entry stored`）。
- `:core:ui:compileDebugKotlin` 禁用缓存强制真实编译通过，确认 Compose 编译器消费了稳定性配置文件。
- 单元测试 `testDebugUnitTest` 全部通过。
- 临时副本运行 `customizer.sh com.example.todo TodoItem TodoApp …`：conf 文件正确重写为 `com.example.todo.core.model.*`，定制副本 `assembleDebug` 构建成功。

---

## 落地记录（可选对齐部分）

**改动文件：**

1. **`core:navigation` 新模块**（对齐 NiA 的 Navigation3 导航抽象）：
   - `core/navigation/build.gradle.kts`、`Navigator.kt`、`NavigationState.kt`（`rememberNavigationState` / `NavigationState` / `Navigator` / `toEntries`）。
   - `settings.gradle.kts` 注册 `:core:navigation`。
   - `app/.../ui/Navigation.kt` 由"内联 `rememberNavBackStack` + `NavDisplay`"重构为"`rememberNavigationState` + `Navigator` + `toEntries`"，`onBack` 走 `navigator.goBack()`。
   - `feature/mymodel/impl/.../EntryProvider.kt` 由接收 `NavBackStack` 改为接收 `Navigator`（`onItemClick` 走 `navigator.navigate`）。
2. **feature 约定插件**：
   - 新增 `AndroidFeatureApiConventionPlugin`（library + serialization + `api(:core:navigation)` + serialization-core）。
   - 新增 `AndroidFeatureImplConventionPlugin`（library + hilt + `core:ui` + lifecycle/viewModel/hilt-viewModel Compose + navigation3-runtime）。
   - `feature/mymodel/api`、`feature/mymodel/impl` 的 `build.gradle.kts` 改用约定插件，公共依赖下沉。
3. **Jacoco 约定插件**：
   - 新增 `Jacoco.kt`（`configureJacoco`，对齐 NiA 的 combined coverage report）、`AndroidApplicationJacocoConventionPlugin`、`AndroidLibraryJacocoConventionPlugin`。
   - `libs.versions.toml` 新增 `jacoco = "0.8.12"` 及 4 个插件 id。
   - 应用到 `app` + `core:data/database/domain/navigation/ui` + `feature:mymodel:impl` + `sync:work`（跳过 JVM 模块 `core:common`/`core:model` 及测试/基准模块）。
4. `build-logic/convention/build.gradle.kts` 注册上述 4 个新插件。

**验证：**

- 主工程 `assembleDebug` 构建成功，Jacoco 任务（`:app:jacocoDebug` 等）正常生效。
- `:core:data:createDebugCombinedCoverageReport` 任务已注册（dry-run 可解析任务图）。
- 单元测试 `testDebugUnitTest` 在 Jacoco 插桩下全部通过。
- 临时副本运行 `customizer.sh`：`core:navigation` 源码与包名正确迁移为 `com.example.todo.core.navigation`，无 `android.template` 残留，定制副本（`feature:todoitem`）`assembleDebug` 构建成功。

---

## 落地记录（差异项对齐部分 · 第 5/6 项：完整对齐 NiA 的测试模块结构）

**改动文件：**

1. **新建 `ui-test-hilt-manifest` 模块**（对齐 NiA，根目录）：
   - `build.gradle.kts`：`template.android.library` + `template.android.hilt`。
   - `HiltComponentActivity.kt`：`@AndroidEntryPoint` 的空 `ComponentActivity`（dagger#3394 的 Hilt 宿主 Activity）。
   - `AndroidManifest.xml`：以 NoActionBar 主题声明 `HiltComponentActivity`。
2. **新建 `core:data-test` 模块**（对齐 NiA）：
   - `build.gradle.kts`：`template.android.library` + `template.android.hilt`，`api(projects.core.data)` + `implementation(libs.hilt.android.testing)`。
   - `FakeMyModelRepository.kt`：数据层 Hilt Fake + 测试数据 `fakeMyModels`（从生产 `core:data` 迁出）。
   - `TestDataModule.kt`：`@TestInstallIn` 替换 `DataModule`，绑定 `FakeMyModelRepository`。
3. **生产 `core:data` 去除测试代码**：`di/DataModule.kt` 只保留 `DataModule`；`DefaultMyModelRepositoryTest` 改用内联测试数据，不再依赖 `core:data-test`（与 NiA 一致，避免循环依赖）。
4. **删除 `test-app` 模块**（NiA 无此模块）：UI 测试由 `app/src/androidTest/NavigationTest.kt` 承担（对应 NiA 的同名测试）。
5. **app 对齐 NiA 测试接线**：`debugImplementation(projects.uiTestHiltManifest)` + `debugImplementation(compose ui test manifest)` + `androidTestImplementation(projects.core.dataTest)`。
6. `settings.gradle.kts`：新增 `:ui-test-hilt-manifest`、`:core:data-test`，移除 `:test-app`。

**验证：**

- 主工程 `assembleDebug`、`testDebugUnitTest`、`:app:assembleDebugAndroidTest` 全部构建/通过（`core:data-test` 的 Hilt 代码生成正常）。
- 临时副本运行 `customizer.sh`：`FakeMyModelRepository`→`FakeTodoItemRepository`、`HiltComponentActivity` 包名重写为 `com.example.todo.uitesthiltmanifest`，无 `android.template` 残留，`test-app` 不再出现，定制副本 `assembleDebug` + `testDebugUnitTest` 通过。

---

## 已确认对齐的架构模式：单向数据流（UDF）+ MVVM ✅（2026-08-26 核查通过）

> 本节为**已对齐确认项**（非差距项）：经彻底核查，本模板与 NiA 一致，采用完整、闭合的 UDF + MVVM，无违反点。

**模式特征（与 NiA 一致）：**

- **ViewModel 暴露 StateFlow**：`MyModelViewModel` 的 `uiState`/`isSyncing` 均为 `StateFlow`，经 `stateIn(viewModelScope, WhileSubscribed(5000), …)` 暴露。
- **Compose 用 `collectAsStateWithLifecycle` 消费**：`MyModelScreen` 收集 `uiState` 与 `isSyncing`。
- **状态下行、事件上行**：状态经 StateFlow 下发；事件经 lambda 回调（`onSave = viewModel::addMyModel`、`onRetry = viewModel::retry`）回到 ViewModel。
- **有状态/无状态分层**：`MyModelScreen`(有状态，持 ViewModel) → `MyModelInput`/`MyModelList`(无状态，纯渲染)。

**反向排查（均通过，无违反）：**

| 检查项 | 结果 |
|---|---|
| Compose 是否直连数据层（注入 Repository/UseCase/SyncManager） | ❌ 无 |
| 业务状态是否误放 Compose 本地 | ❌ 无（唯一 `mutableStateOf` 是输入框瞬态文本，属正常 UI 态） |
| Compose 是否绕过 ViewModel 直接 `collect` Flow | ❌ 无 |
| `isSyncing` 是否走 ViewModel | ✅ 走 `viewModel.isSyncing`，未直连 `SyncManager` |
| 是否唯一 ViewModel / 唯一消费点 | ✅ 仅 `MyModelViewModel` + `MyModelScreen` |

**与 NiA 的唯一差异（非差距）**：NiA 用 `core:common:Result.asResult()` 包装 Flow 再映射 UiState；本模板在 P1-F 移除了 `Result`，由 ViewModel 直接 `map`/`catch` 映射到 `UiState`（更简洁，效果等价）。

**结论**：数据层 → ViewModel（StateFlow）→ Compose（collectAsStateWithLifecycle）→ 事件回调回 ViewModel，是一条干净、闭合的单向数据流，且为唯一 MVVM 通路，无旁路、无违反，无需改动。
