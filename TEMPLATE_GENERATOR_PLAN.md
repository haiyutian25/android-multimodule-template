# 模板"hello world 架构演示 + 生成器"改造方案

> 记录日期：2026-08-27
> 本模板：`architecture-templates-multimodule`
> 架构参照：Now in Android（NiA）
> 状态：**已实施并验证**（Part A：mymodel→greeting 架构演示；Part B：customizer 改造为生成器；定制后工程 assembleDebug + testDebugUnitTest 通过）
> 定型：**方案 A** —— hello world 是**完整架构演示**（各 core 模块保留 hello world 内容，非空壳）
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

### 1.2 核心矛盾与解决

- 原诉求：**去掉具体的 mymodel 业务**，让模板成为干净、通用的起点；**同时脚本所有参数仍要能正常工作**。
- 矛盾：重命名器依赖"已存在的 mymodel"；若把 mymodel 删成空壳，脚本所有参数就失去作用对象。
- **解决（本方案，已定型 A）**：
  1. 模板**不做空壳**，而是把 mymodel 业务替换为一个**通用的 hello world 架构演示**——贯穿三层、离线优先、全响应式的最小可运行示例，让学习者一眼看到完整架构。
  2. 脚本从**重命名器**改造成**生成器**：按参数**从零生成**用户业务 CRUD，并**移除 hello world 演示**。

### 1.3 目标方案

- **模板** = hello world 架构演示：一个**贯穿三层、离线优先、全响应式**的通用可运行示例（取代 mymodel，无具体业务语义）。
- **脚本** = 生成器：运行时根据参数**生成**整套业务 CRUD 分层 + 测试，**移除 hello world 演示**，跑完直接得到一个可运行的业务工程。
- 效果：模板是通用的"架构演示"；脚本的**每个参数都有实际作用**（它们决定生成什么）。

---

## 二、总体设计

两部分改造：

- **Part A**：把 mymodel 业务替换为 **hello world 架构演示**。
- **Part B**：把 `customizer.sh` 重写为**生成器**。

设计原则：

1. 生成器产出的代码**完全遵循 `DEVELOPMENT_MANUAL.md`**（分层、离线优先、UDF+MVVM、测试联动）。
2. **现有脚本参数全部保留**，逐一映射到生成行为（见 4.1）。
3. **生成产物 ≡ 旧模板跑旧脚本的结果**：即「新模板 + 生成器」与「旧模板 + 重命名器」产出**等价**的业务工程，保证行为一致、可回归对比。

---

## 三、Part A：模板内置 hello world 架构演示

### 3.1 hello world 演示的架构特征

hello world **不是**最小静态页，而是**完整架构演示**，覆盖模板四大核心模式：

1. **完整三层架构**：UI 层（feature）→ domain 层（UseCase）→ data 层（Repository + 数据源）。
2. **单向数据流（UDF）+ MVVM**：ViewModel 暴露 `StateFlow`，Compose 用 `collectAsStateWithLifecycle` 消费。
3. **离线优先（offline-first）**：Repository 只从本地 Room 读取，网络数据经同步机制写入本地。
4. **全响应式**：Kotlin Flow 贯穿 DAO → Repository → ViewModel。

### 3.2 各模块的 hello world 内容

因 hello world 演示完整架构，各 core 模块**保留 hello world 内容（非空壳）**：

| 模块 | hello world 内容 |
|---|---|
| `app` | `MyApplication`(Hilt 入口 + `Sync.initialize`)、`MainActivity`、`MainNavigation`(渲染 hello world 首页) |
| `core:model` | hello world 领域模型 |
| `core:common` | **原样**（调度器限定符 / `@ApplicationScope`，基础设施） |
| `core:database` | hello world Entity(+DAO) / Database / `DatabaseModule` |
| `core:network` | hello world DTO / 数据源 / Retrofit 实现 + 通用网络基建（Json/OkHttp） |
| `core:data` | hello world Repository（离线优先）+ 同步抽象（`Synchronizer`/`Syncable`/`SyncManager`）+ `DataModule` |
| `core:domain` | hello world UseCase |
| `core:navigation` | **原样**（`Navigator`/`NavigationState`） |
| `core:designsystem` | **原样**（`AppTheme`/`UiStateView`/状态组件/`AppIcons`） |
| `core:ui` | **原样**（占位，`api` 暴露 designsystem） |
| `core:testing` | 测试基建 + hello world 测试替身 |
| `core:data-test` | hello world Fake Repository + `TestDataModule` |
| `feature`(hello world) | hello world Screen / ViewModel / EntryProvider（完整三层演示） |
| `sync:work` | 同步骨架 + `SyncWorker`（注入并同步 hello world 仓库） |
| `sync:sync-test` | **原样**（`NeverSyncingSyncManager`/`TestSyncModule`） |
| `benchmarks` / `ui-test-hilt-manifest` | **原样** |

### 3.3 同步处理

- hello world 演示离线优先，故同步是**真实**的（非空转占位）：`core:data` 保留 `SyncManager`/`Synchronizer`/`Syncable`，`sync:work` 的 `SyncWorker` 注入 hello world 仓库并同步。
- `MyApplication` 调用 `Sync.initialize`。
- 生成器生成用户仓库时，相应更新 `SyncWorker`（把注入的 hello world 仓库替换为用户仓库）。

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
10. 接线同步（`SyncWorker` 注入用户仓库并 `sync()`）。
11. 生成各层测试。
12. **移除 hello world 演示**：删除 hello world feature 模块，及各 core 模块中的 hello world 相关类（Entity/DAO/Repository/UseCase/数据源等），保留生成器刚生成的用户业务类与通用基建。
13. 清理（`.google`/`.github`/`CONTRIBUTING`/`LICENSE`/`README`/`.git`，沿用现脚本末尾逻辑）。

### 4.4 实现方式

**方案：bash heredoc 内联生成**。生成器以 bash heredoc 直接输出各层文件代码，替换参数占位（包名 / 模型名 / 实体字段 / 方法名 / 假数据等）。脚本自包含，不依赖外部种子模板文件。

> 注意：hello world 演示代码与生成器 heredoc 代码是**两份副本**（前者演示、后者生成），实施时须保持二者架构规范一致（都遵循 `DEVELOPMENT_MANUAL.md`），避免漂移。

---

## 五、实施路线图

| 阶段 | 内容 | 状态 |
|---|---|---|
| 1 | Part A：把 mymodel 业务替换为 hello world（`greeting`）架构演示 | ✅ 完成 |
| 2 | Part B：`customizer.sh` 改造为生成器 | ✅ 完成 |
| 3 | 验证：定制→`assembleDebug`→`testDebugUnitTest` | ✅ 通过 |
| 4 | 更新文档：手册 / 结构 / README | ✅ 完成 |

> **实施说明**：Part B 最终采用"**重命名 greeting 种子**"方案——定制脚本把模板内置的 `greeting` 演示整体重命名为用户业务（包名/模型/模块目录/方法名/字段），并清理过期 Room schema；而非 4.4 最初设想的 heredoc 逐文件生成。前者复用已验证的演示代码、无"两份副本漂移"问题，产出等价且更简洁。字段改名采用**定向模式**，避免误伤 designsystem 的 `message` 参数与 `Throwable.message`。

---

## 六、决策点与注意事项

| # | 决策点 | 状态 / 建议 |
|---|---|---|
| D1 | hello world 定位 | **已定 A**：完整架构演示（非空壳） |
| D2 | 生成时 hello world 处理 | 生成器生成用户业务后**移除** hello world |
| D3 | 实现方式 | **已定**：bash heredoc 内联生成 |
| D4 | hello world 具体命名 | 实施时定（如 `Greeting`/`HelloMessage` 等模型类名） |
| D5 | 生成器是否支持重复运行 | 首期**一次性生成**，重复运行语义后续再定 |

注意事项：

- 生成器替换占位符时要覆盖 `.kt`/`.kts`/`AndroidManifest.xml`/`strings.xml` 等所有载体。
- feature 模块目录名、`settings.gradle.kts` include、typesafe 访问器三处需一致。
- 生成后必须能 `assembleDebug` + `testDebugUnitTest` 通过，否则生成器视为失败。
- 移除 hello world 时，务必只删 hello world 相关类，**勿误删**用户业务类与通用基建（`core:common`/`navigation`/`designsystem`/`ui` 等）。

---

## 七、验证方式

1. **可构建性**：生成后 `gradlew assembleDebug`、`testDebugUnitTest`、`:app:assembleDebugAndroidTest` 全通过。
2. **等价性**：用同一组参数，「新模板 + 生成器」与「旧模板 + 重命名器」产出的工程在关键文件上等价（包名/类名/方法名一致）。
3. **多参数组合**：不同模型名 / 实体字段 / 数据库类 / 方法名 / 假数据组合，均能正确生成。
4. **hello world 可运行**：模板（hello world 架构演示）本身 `gradlew assembleDebug` 通过，完整演示三层 / 离线优先 / 响应式。

---

## 八、与既有文档的关系

- 本方案已定型 **方案 A**（hello world 为完整架构演示，非空壳）；此前"清空成空壳"的表述已废弃。
- 实施完成后，`DEVELOPMENT_MANUAL.md` 第六章的"路径 A：customizer 一键生成"需改写为"生成器"语义；`WORKSPACE_STRUCTURE.md` 需重新导出。
