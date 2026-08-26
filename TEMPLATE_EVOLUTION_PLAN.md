# 模板演进规划：参考 Now in Android 改进本模板

> 记录日期：2026-08-26
> 研究对象：`nowinandroid/`（Google 官方示例，克隆自 https://github.com/android/nowinandroid）
> 本模板：`architecture-templates-multimodule`（Android 官方多模块架构模板）

---

## 一、Now in Android（NiA）架构研究结论

### 1.1 基本信息

- 应用 ID：`com.google.samples.apps.nowinandroid`
- 工具链：Gradle 9.4 + AGP 9.0.0 + Kotlin 2.3.0 + JDK 17；compileSdk 36 / minSdk 23 / targetSdk 36
- 规模：37 个模块

### 1.2 架构模式

- **三层架构**：UI 层 → domain 层（UseCase）→ data 层（Repository + 数据源）
- **单向数据流（UDF）+ MVVM**：ViewModel 暴露 `StateFlow`，Compose 用 `collectAsStateWithLifecycle` 消费
- **离线优先（offline-first）**：Repository 只从本地 Room 读取，网络数据经同步机制写入本地
- **全响应式**：Kotlin Flow 贯穿 DAO → Repository → ViewModel

关键证据文件（相对 `nowinandroid/`）：

| 论断 | 证据 |
|------|------|
| 三层 + UDF | `docs/ArchitectureLearningJourney.md` |
| 模块化策略 | `docs/ModularizationLearningJourney.md` |
| offline-first | `core/data/src/main/kotlin/.../repository/OfflineFirstNewsRepository.kt` |
| MVVM + StateFlow | `feature/foryou/impl/src/main/kotlin/.../ForYouViewModel.kt` |

数据流示例（For You 首页）：

```
Retrofit(NiaNetworkDataSource) → SyncWorker(WorkManager) → Room(NiaDatabase)
→ OfflineFirstNewsRepository → ForYouViewModel(StateFlow) → ForYouScreen(Compose)
```

### 1.3 模块划分（37 个）

| 类别 | 模块 | 职责 |
|------|------|------|
| 应用 | `app` | 外壳：Application、MainActivity、导航宿主 `NiaApp` |
| | `app-nia-catalog` | 设计系统组件展示目录（独立 App） |
| core | `core:model` | 纯 Kotlin 领域模型（NewsResource、Topic、UserData 等） |
| | `core:network` | Retrofit 网络数据源 + demo 用静态 JSON 实现 |
| | `core:database` | Room 数据库/实体/DAO/迁移 |
| | `core:datastore` + `core:datastore-proto` | Proto DataStore 用户偏好 |
| | `core:data` | Repository 层（全部 OfflineFirst）+ `Synchronizer`/`Syncable` 同步抽象 + `NetworkMonitor` |
| | `core:domain` | UseCase 层 |
| | `core:designsystem` | Material 3 主题与品牌组件 |
| | `core:ui` | 跨 feature 共享业务组件 |
| | `core:navigation` | Navigation3 状态管理 |
| | `core:analytics` / `core:notifications` | 埋点/通知抽象（prod=真实实现，demo=Stub） |
| | `core:common` | 协程调度器限定符、Result 封装 |
| | `core:testing` 等 5 个测试模块 | 测试基础设施与 Hilt 测试替身 |
| feature | `foryou`/`interests`/`bookmarks`/`topic`/`search`（各分 api+impl）、`settings` | **api/impl 拆分**：api 只含路由键+资源，impl 含界面逻辑，只允许被 app 依赖 |
| 同步 | `sync:work` | WorkManager 同步：SyncWorker、WorkManagerSyncManager |
| 其他 | `benchmarks`、`lint`、`build-logic`、`ui-test-hilt-manifest` | 基准测试、自定义 Lint、约定插件、测试辅助 |

### 1.4 关键技术机制

- **Room**：`NiaDatabase`，实体含 `NewsResourceEntity`、`TopicEntity`、`NewsResourceTopicCrossRef`（多对多）、FTS 全文搜索实体、`RecentSearchQueryEntity`；自动迁移
- **Proto DataStore**：用户偏好（关注/书签/已读/主题/动态颜色/同步版本号）
- **网络**：Retrofit 2.11 + kotlinx-serialization 转换器 + OkHttp；demo 风味用本地 JSON assets
- **同步抽象（最值得学习）**：`Synchronizer` 接口 + `Syncable` 标记接口，`WorkManagerSyncManager` 调度 Hilt Worker，任何 Repository 声明"需要同步"即可接入
- **导航**：Navigation3（1.0.0），路由为 `@Serializable` NavKey，放在各 feature 的 api 模块
- **大屏适配**：material3-adaptive + windowSizeClass
- **DI**：Hilt 2.59 + KSP，`hilt-work` 支持 Worker 注入
- **构建**：`build-logic` 16 个约定插件统一所有模块配置；双风味 `demo`（无 Firebase 可匿名构建）/ `prod`；release 用 R8 + Baseline Profile

### 1.5 完整依赖清单（`nowinandroid/gradle/libs.versions.toml`）

| 分类 | 库 | 版本 |
|------|-----|------|
| 构建 | AGP / Kotlin / KSP | 9.0.0 / 2.3.0 / 2.3.4 |
| UI | Compose BOM / M3 Adaptive / Navigation3 / Activity / Lifecycle / Splashscreen | 2025.09.01(alpha) / 1.1.0-rc01 / 1.0.0 / 1.9.3 / 2.10.0 / 1.0.1 |
| 数据 | Room / DataStore / Retrofit / OkHttp / kotlinx-serialization / Protobuf | 2.8.3 / 1.2.0 / 2.11.0 / 4.12.0 / 1.8.0 / 4.29.2 |
| 后台 | WorkManager / Hilt / hilt-work | 2.10.0 / 2.59 / 1.2.0 |
| 异步 | Coroutines / kotlinx-datetime | 1.10.1 / 0.6.1 |
| 图片 | Coil（core/compose/svg） | 2.7.0 |
| Firebase | BOM（analytics/crashlytics/perf/messaging） | 33.7.0 |
| 性能 | Macrobenchmark / profileinstaller / metrics-performance / tracing | 1.5.0-alpha01 / 1.4.1 / 1.0.0-beta01 / 1.3.0-alpha02 |
| 测试 | JUnit4 / Truth / Turbine / Robolectric / Roborazzi / Espresso / UI Automator | 4.13.2 / 1.4.4 / 1.2.0 / 4.16 / 1.56.0 / 3.6.1 / 2.3.0 |
| 质量 | Spotless(ktlint) / dependency-guard / JaCoCo / androidx-lint-gradle | 8.3.0(1.4.0) / 0.5.0 / 0.8.12 / 1.0.0-alpha03 |
| 其他 | Accompanist-permissions / Browser / desugar_jdk_libs / OSS Licenses | 0.37.0 / 1.8.0 / 2.1.4 / 17.1.0 |

---

## 二、演进路线图（从 NiA 按需吸收）

### 第一阶段：低成本高价值

- [x] **拆分 `core-model` 模块**：把实体/领域模型从 `core-database` 独立出来，feature 与 data 层解耦（2026-08-26 完成：`core-model` 纯 Kotlin 模块放领域模型 `MyModel`，`core-database` 保留 `MyModelEntity` + `toModel()` 映射）
  - 参考：`nowinandroid/core/model/`
- [x] **feature 的 api/impl 拆分规范化**：现有 `feature-mymodel-navigation` 是雏形，对齐 NiA 的 `feature:*:api`（路由键+资源）/ `feature:*:impl`（界面+ViewModel）模式（2026-08-26 完成：模块改名 `feature-mymodel-api` / `feature-mymodel-impl`，包结构对齐 `.api.navigation` / `.impl`，customizer.sh 同步适配）
  - 参考：`nowinandroid/feature/foryou/api/` 与 `impl/`

### 第二阶段：架构升级

- [x] **离线优先 + 同步抽象**：引入 `Synchronizer` / `Syncable` 接口 + WorkManager `SyncWorker` 模式（2026-08-26 完成：`core-data` 新增 `SyncManager`/`Synchronizer`/`Syncable` 与 `MyModelNetworkDataSource`（假实现占位），新建 `sync-work` 模块：`SyncWorker`(HiltWorker) + `DelegatingWorker` + `WorkManagerSyncManager` + `Sync.initialize`，Application 启动入队同步，UI 展示同步进度）
  - 参考：`nowinandroid/core/data/src/main/kotlin/.../di/DataSyncModule.kt`、`nowinandroid/sync/work/`
- [x] **新增 `core-domain` UseCase 层**（2026-08-26 完成：`GetMyModelsUseCase`/`AddMyModelUseCase`，ViewModel 改为仅依赖 UseCase + SyncManager）
  - 参考：`nowinandroid/core/domain/`

### 第三阶段：工程化

- [x] **`build-logic` 约定插件**：消除各模块重复的 `build.gradle.kts` 配置（2026-08-26 完成：8 个约定插件 `template.android.application(.compose)`、`template.android.library(.compose)`、`template.android.test`、`template.android.room`、`template.android.hilt`、`template.jvm.library`，集中管理 compileSdk/minSdk/Java 17/Compose BOM/Room schema/Hilt 依赖）
  - 参考：`nowinandroid/build-logic/convention/`
- [ ] **NetworkMonitor / 网络状态感知**（可选）

### 第四阶段：补齐剩余差距

- [x] **新建 `core-common` 工具模块**：协程调度器限定符（`@Dispatcher` IO/Default、`@ApplicationScope`）+ `Result` 封装（Success/Error/Loading + `Flow.asResult()`）（2026-08-26 完成：纯 JVM 模块 + Hilt，Hilt 约定插件新增 JVM 分支注入 hilt-core，`DefaultMyModelRepository.syncWith` 接入 `@Dispatcher(IO)`）
  - 参考：`nowinandroid/core/common/`
- [ ] **低成本构建改进**：`settings.gradle.kts` 开启 `TYPESAFE_PROJECT_ACCESSORS` + JDK 17 兼容性检查
  - 参考：`nowinandroid/settings.gradle.kts`
- [ ] **新建 `core-network` 真实网络层**：Retrofit + OkHttp + kotlinx-serialization，替换 `MyModelNetworkDataSource` 假实现，让离线优先同步真正从网络拉取
  - 参考：`nowinandroid/core/network/`
- [ ] **测试基建**：测试替身（core-testing）、截图测试（Roborazzi）、sync-test
  - 参考：`nowinandroid/core/testing/`、`core/screenshot-testing/`、`sync/sync-test/`
- [ ] **Benchmark + Baseline Profile**：Macrobenchmark 模块，生成 Baseline Profile 优化启动
  - 参考：`nowinandroid/benchmarks/`
- [ ] **代码风格 Spotless + ktlint**（仅记录，暂不实施）
  - 参考：`nowinandroid/spotless/`、`build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/Spotless.kt`

### 明确不做（已确认）

- DataStore 用户偏好、设计系统（core-ui 保持现状）、CI、demo/prod 双风味、Firebase/分析/通知等业务能力、自定义 Lint 规则

### 注意事项

1. 每次引入一个模式后，同步更新 `customizer.sh` 保证新结构可被定制脚本处理
2. 保持依赖全部为稳定版，不跟随 NiA 的 alpha 通道
3. 引入前后都要跑 `gradlew assembleDebug` + `gradlew test` 验证
4. 本文件记录的 NiA 版本为 2026-08 克隆的 `main` 分支（提交 `7d45eae4`），后续参考时注意上游变化
