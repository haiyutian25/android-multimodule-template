# 模板"干净骨架 + 生成器"改造方案

> 记录日期：2026-08-27
> 本模板：`architecture-templates-multimodule`
> 架构参照：Now in Android（NiA）
> 状态：**方案文档（待实施）**
> 关联文档：`DEVELOPMENT_MANUAL.md`（生成器产出的代码必须遵循手册规范）

---

## 一、背景与目标

### 1.1 现状

模板内置一套**完整可运行**的 `mymodel` CRUD 示例，贯穿全部分层：

| 层 | mymodel 内容 |
|---|---|
| `core:model` | `MyModel` |
| `core:database` | `MyModelEntity`(+`MyModelDao`) / `AppDatabase` / `di/DatabaseModule` / Room schema |
| `core:network` | `NetworkMyModel` / `MyModelNetworkDataSource` / `RetrofitMyModelNetwork` / `di/NetworkModule` |
| `core:data` | `MyModelRepository` / `DefaultMyModelRepository` / `di/DataModule` |
| `core:domain` | `GetMyModelsUseCase` / `AddMyModelUseCase` |
| `feature:mymodel:api/impl` | `Main` NavKey / `MyModelScreen` / `MyModelViewModel` / `EntryProvider` |
| `sync:work` | `SyncWorker`（注入 `MyModelRepository`） |
| 测试 | `FakeMyModelRepository`+`TestDataModule`、`TestMyModelRepository`、ViewModel/Repository 单测、Screen 仪器测试 |

`customizer.sh` 是一个**重命名器**：把上述**已存在**的 mymodel 内容（包名、类名、模块目录、方法名、假数据）重命名成用户的业务。

### 1.2 核心矛盾

- 诉求：**去掉模板的功能和 UI，变成干净的 hello world 骨架**。
- 但重命名器**必须有"已存在的 mymodel"**才能工作；删掉 mymodel，脚本所有参数就失去作用对象。
- 结论：在"重命名器"架构下，「干净模板」与「脚本全参数可用」**不可兼得**。

### 1.3 目标方案

把脚本从**重命名器**改造成**生成器**：

- **模板** = 干净的 hello world 骨架（所有模块保留，但没有 CRUD 内容）。
- **脚本** = 运行时根据参数**生成**整套 CRUD 分层（model→database→network→data→domain→feature）+ 测试，跑完直接得到一个可运行的业务工程。
- 效果：模板是干净的；脚本的**每个参数都有实际作用**（它们决定生成什么）。

---

## 二、总体设计

两部分改造：

- **Part A**：把模板清空为 hello world 骨架。
- **Part B**：把 `customizer.sh` 重写为生成器。

设计原则：

1. 生成器产出的代码**完全遵循 `DEVELOPMENT_MANUAL.md`**（分层、离线优先、UDF+MVVM、测试联动）。
2. **现有脚本参数全部保留**，逐一映射到生成行为（见 4.1）。
3. **生成产物 ≡ 旧模板跑旧脚本的结果**：即「新模板 + 生成器」与「旧模板 + 重命名器」产出**等价**的业务工程，保证行为一致、可回归对比。

---

## 三、Part A：模板清空为 hello world 骨架

### 3.1 各模块清空后的目标状态

| 模块 | 清空后**保留** | **移除**（mymodel 内容） |
|---|---|---|
| `app` | `MyApplication`(Hilt 入口 + `Sync.initialize`)、`MainActivity`、`MainNavigation`(渲染 hello world) | mymodel 接线（`MyModelEntryProvider`） |
| `core:model` | 空壳（模块 + 构建配置） | `MyModel` |
| `core:common` | **原样**（调度器限定符 / `@ApplicationScope`，基础设施） | — |
| `core:database` | 空壳（Room 约定插件） | `MyModelEntity`/`MyModelDao`/`AppDatabase`/`DatabaseModule`/schema |
| `core:network` | 通用网络基建（`NetworkModule` 的 Json / OkHttp `@Provides`） | `NetworkMyModel`/数据源接口/`RetrofitMyModelNetwork`/mymodel `@Binds` |
| `core:data` | 同步抽象 `SyncUtilities`(`Synchronizer`/`Syncable`/`suspendRunCatching`) + `util/SyncManager` + 空 `DataModule` | `MyModelRepository`/`DefaultMyModelRepository` |
| `core:domain` | 空壳 | `GetMyModelsUseCase`/`AddMyModelUseCase` |
| `core:navigation` | **原样**（`Navigator`/`NavigationState`） | — |
| `core:designsystem` | **原样**（`AppTheme`/`UiStateView`/状态组件/`AppIcons`） | — |
| `core:ui` | **原样**（占位，`api` 暴露 designsystem） | — |
| `core:testing` | `HiltTestRunner`/`MainDispatcherRule`/`TestDispatcher(s)Module`/`TestSyncManager` | `TestMyModelRepository` |
| `core:data-test` | 空 `TestDataModule`（保留 `@TestInstallIn` 骨架） | `FakeMyModelRepository` |
| `feature`(hello world) | 一个最小 UDF+MVVM 的 hello world feature | mymodel CRUD UI |
| `sync:work` | 同步骨架：`WorkManagerSyncManager`/`Sync`/`DelegatingWorker`/`SyncModule` + 占位 `SyncWorker` | `SyncWorker` 对 mymodel 仓库的注入 |
| `sync:sync-test` | **原样**（`NeverSyncingSyncManager`/`TestSyncModule`） | — |
| `benchmarks` / `ui-test-hilt-manifest` | **原样** | — |

### 3.2 hello world feature 设计（最小 UDF+MVVM）

- 保留 `ViewModel + StateFlow + UiState + collectAsStateWithLifecycle`，但**不接**数据库 / 网络 / 同步。
- 作为模板的默认首页，演示核心 UI 模式（对应手冊第四章）。
- 生成器运行时，以用户业务 feature 取代该 hello world（见决策点 D2）。

### 3.3 同步处理（沿用决策 B：保留同步骨架）

- `core:data` 保留 `SyncManager`/`Synchronizer`/`Syncable`；`sync:work` 保留骨架，`SyncWorker` 暂为"无仓库可同步"的占位（`doWork` 直接返回成功）。
- `MyApplication` 仍调用 `Sync.initialize`。
- 生成器生成仓库后，会把仓库注入 `SyncWorker` 并纳入同步（见 4.2 / 4.3）。

---

## 四、Part B：脚本改造为生成器

### 4.1 参数 → 生成映射（现有参数全部保留）

| 参数 | 现状（重命名器） | 生成器中（生成） |
|---|---|---|
| 包名（位置 1） | 重命名 `android.template` | 生成代码的基础包名 |
| 模型名（位置 2） | 重命名 `MyModel`/`myModel`/`mymodel` | 模型类名 + 派生各类名 + feature 模块名 |
| App 名（位置 3） | 重命名 `MyApplication` | Application 类名 |
| `--entity-field` | 重命名实体字段 `name` | 生成的 Entity 字段名 |
| `--fake-data` | 替换假数据 | 生成的测试假数据 |
| `--database-class` | 重命名 `AppDatabase` | 生成的 Room 数据库类名 |
| `--main-activity` | 重命名 `MainActivity` | 生成的 MainActivity 类名 |
| `--query-method` | 重命名 `getMyModels` | 生成的 DAO 查询方法名 |
| `--insert-method` | 重命名 `insertMyModel` | 生成的 DAO 插入方法名 |
| `--add-method` | 重命名 `addMyModel` | 生成的 ViewModel 增方法名 |

### 4.2 生成产物清单（按模块）

以模型名 `TodoItem`、包名 `com.example.todo`、`--database-class TodoDatabase` 为例：

| 模块 | 生成的文件 |
|---|---|
| `core:model` | `TodoItem.kt` |
| `core:database` | `TodoItemEntity.kt`(Entity + DAO + `toModel`)、`TodoDatabase.kt`、`di/DatabaseModule.kt`(`@Provides` DAO) |
| `core:network` | `model/NetworkTodoItem.kt`、`TodoItemNetworkDataSource.kt`、`retrofit/RetrofitTodoItemNetwork.kt`、`di/NetworkModule.kt`(补 `@Binds`) |
| `core:data` | `TodoItemRepository.kt`(接口 + `Default` 离线优先实现)、`di/DataModule.kt`(补 `@Binds`) |
| `core:domain` | `GetTodoItemsUseCase.kt`、`AddTodoItemUseCase.kt` |
| `feature:todoitem:api` | `build.gradle.kts`、`navigation/NavigationKeys.kt`(NavKey) |
| `feature:todoitem:impl` | `build.gradle.kts`、`TodoItemViewModel.kt`、`TodoItemScreen.kt`、`navigation/EntryProvider.kt` |
| `app` | `MainNavigation` 接入 `TodoItemEntryProvider`、`build.gradle.kts` 加依赖 |
| `settings.gradle.kts` | `include(":feature:todoitem:api")` / `include(":feature:todoitem:impl")` |
| `core:data-test` | `FakeTodoItemRepository.kt`、`TestDataModule.kt`(补 `@Binds`) |
| `core:testing` | `TestTodoItemRepository.kt` |
| `sync:work` | `SyncWorker` 注入并同步 `TodoItemRepository` |
| 测试 | ViewModel 单测、Repository 单测、Screen 仪器测试、`app` `NavigationTest` |

### 4.3 生成流程

1. 解析并校验参数（沿用现有校验规则）。
2. 生成 `core:model`。
3. 生成 `core:database`（Entity/DAO/Database/`DatabaseModule`）。
4. 生成 `core:network`（DTO/数据源/Retrofit 实现/`NetworkModule` 绑定）。
5. 生成 `core:data`（Repository/`DataModule` 绑定）。
6. 生成 `core:domain`（UseCases）。
7. 生成 feature api/impl 模块 + `settings.gradle.kts` include。
8. 接线 `app`（`MainNavigation` + `build.gradle.kts` 依赖）。
9. 生成测试替身（`core:data-test`/`core:testing`）并补 `TestDataModule` 绑定（测试联动，手册 6.5 步骤 10）。
10. 接线同步（`SyncWorker` 注入仓库并 `sync()`）。
11. 生成各层测试。
12. （可选）移除 hello world feature（见 D2）。
13. 清理（`.google`/`.github`/`CONTRIBUTING`/`LICENSE`/`README`/`.git`，沿用现脚本末尾逻辑）。

### 4.4 实现方式

- **方式 A（推荐）：种子模板**。把一套"参照实现"（即当前 mymodel 代码，参数化为占位符）存放在独立目录（如 `generator/seed/`）。生成器读取种子模板 → 替换占位符（包名/模型名/方法名等）→ 写入目标位置。
  - 优点：模板代码与生成逻辑分离、易维护、生成产物天然与手册一致。
  - 本质：mymodel 代码从"主源码树"**搬到**"种子模板目录"，主树变干净。
- **方式 B：内联生成**（bash heredoc 直接输出代码）。脚本自包含但庞大、难维护。
- **建议方式 A**。

---

## 五、实施路线图

| 阶段 | 内容 | 产出 |
|---|---|---|
| 1 | 搭建种子模板目录：把 mymodel 全套代码参数化（占位符化） | `generator/seed/` |
| 2 | Part A：清空模板为 hello world 骨架 | 干净模板（可构建） |
| 3 | Part B：实现生成器（读种子→替换→写入→接线） | 新 `customizer.sh` |
| 4 | 验证：生成→构建→测试；与旧产物等价性对比 | 通过验证 |
| 5 | 更新文档：`DEVELOPMENT_MANUAL.md`、`WORKSPACE_STRUCTURE.md` 等 | 文档同步 |

---

## 六、决策点与注意事项

| # | 决策点 | 建议 |
|---|---|---|
| D1 | 同步去留 | **已定 B**：保留同步骨架，`SyncWorker` 占位，生成器填仓库 |
| D2 | hello world feature 与生成 feature 的关系 | 建议**替换**：生成器生成业务 feature 后移除 hello world，避免双首页 |
| D3 | `DataModule`/`TestDataModule` 保留空壳还是生成器创建 | 建议**保留空壳**（含 `@Module`/`@TestInstallIn` 注解），生成器只补 `@Binds` |
| D4 | `core:network` 通用基建 | Json/OkHttp `@Provides` **保留在模板**，mymodel `@Binds` 由生成器补 |
| D5 | 种子模板目录位置与是否随模板分发 | 建议 `generator/seed/`，随模板分发（生成器依赖它） |
| D6 | 生成器是否支持重复运行/增量 | 首期**一次性生成**即可，重复运行语义后续再定 |

注意事项：

- 生成器替换占位符时要覆盖 `.kt`/`.kts`/`AndroidManifest.xml`/`strings.xml` 等所有载体。
- feature 模块目录名、`settings.gradle.kts` include、typesafe 访问器三处需一致。
- 生成后必须能 `assembleDebug` + `testDebugUnitTest` 通过，否则生成器视为失败。

---

## 七、验证方式

1. **可构建性**：生成后 `gradlew assembleDebug`、`testDebugUnitTest`、`:app:assembleDebugAndroidTest` 全通过。
2. **等价性**：用同一组参数，「新模板 + 生成器」与「旧模板 + 重命名器」产出的工程在关键文件上等价（包名/类名/方法名一致）。
3. **多参数组合**：不同模型名 / 实体字段 / 数据库类 / 方法名 / 假数据组合，均能正确生成。
4. **干净度**：清空后的模板 `gradlew assembleDebug` 能通过（hello world 可运行），且无 mymodel 残留。

---

## 八、与既有文档的关系

- 本方案**取代**此前"把 mymodel 清空成 hello world 空壳（脚本缩水为重命名空壳）"的讨论结论——那条路会让脚本参数失效，已被否定。
- 实施完成后，`DEVELOPMENT_MANUAL.md` 第六章的"路径 A：customizer 一键生成"需改写为"生成器"语义；`WORKSPACE_STRUCTURE.md` 需重新导出。
