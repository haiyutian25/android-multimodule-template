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

## ⚪ 差异但可不跟（方案不同 / 意义小）

| 项 | NiA 做法 | 本模板做法 | 结论 |
|----|---------|-----------|------|
| `ui-test-hilt-manifest` | 提供 `HiltComponentActivity` 供 UI 测试使用 | 用 `test-app` 模块实现同等目的 | 方案不同，无需改 |
| `core:data-test` 独立模块 | 数据层测试替身单独成模块 | 测试替身放在 `core:testing` | 功能等价，无需改 |
| 同步的 change-list 深度 | `Syncable` 有增量 change-list 版本机制 | "空库则拉全量"的简化版 | 与真实后端强相关，暂不跟 |

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
