# 模板完整重新对齐方案：架构 / 结构 / 组件

> 记录日期：2026-08-26
> 参照对象：`nowinandroid/`（Google 官方示例）
> 本模板：`architecture-templates-multimodule`
> 状态：**方案已产出，待用户勾选确认后逐项实施**

---

## 0. 范围与约束

- **本次目标**：只对齐**架构分层、模块结构、组件组织**，让模板获得更好的架构、结构与组件。
- **明确不动**之前排除的业务特性：DataStore、分析（Analytics）、Firebase、通知、截图测试（Roborazzi）、需真实后端的 change-list 增量同步等。
- **明确不引入**任何 NiA 业务名：News / Topics / Interests / Bookmarks / ForYou，以及 `Nia*` 前缀。所有新增组件一律使用**泛化命名**，保持本模板的 `MyModel` 体系。

---

## 1. 现状对比

### 1.1 本模板模块依赖图

依据各模块 `build.gradle.kts` 的 dependencies，以及 build-logic 约定插件隐式注入的依赖整理：

| 模块 | 项目内依赖 | 备注 |
|---|---|---|
| `app` | `core:ui`、`core:navigation`、`feature:mymodel:api`、`feature:mymodel:impl`、`sync:work` | 测试依赖 `core:testing`、`core:data-test` |
| `core:ui` | 无 | 仅 compose-ui + material3 + core-ktx，**叶子模块** |
| `core:domain` | `core:data` | 未直接依赖 core:model（经 data 传递） |
| `core:data` | `core:common`、`core:database`、`core:model`、`core:network` | 数据层汇聚点 |
| `core:database` | `core:model`（api 暴露） | Room |
| `core:network` | 无 | Retrofit/OkHttp/serialization，自带 `NetworkMyModel` DTO |
| `core:model` | 无 | 纯 JVM library，最底层 |
| `core:common` | 无 | 纯 JVM + Hilt，协程调度器 + `Result` 工具 |
| `core:navigation` | 无 | api 暴露 navigation3-runtime，叶子模块 |
| `feature:mymodel:api` | `core:navigation`（api，约定插件注入） | 仅导航契约（`Main` NavKey） |
| `feature:mymodel:impl` | 显式 `core:data`、`core:domain`、`feature:mymodel:api`；隐式（约定插件）`core:ui`、lifecycle-compose、hilt-viewmodel-compose、navigation3-runtime | UI 所在模块 |
| `sync:work` | `core:data`；api 暴露 `androidx.hilt.work` | WorkManager 同步 |

依赖方向（自上而下，无循环）：

```
app ──→ core:ui（叶子）
app ──→ core:navigation（叶子）
app ──→ feature:mymodel:impl ──→ feature:mymodel:api ──→ core:navigation
     │                        ──→ core:domain ──→ core:data
     │                        ──→ core:data
     │                        ──→ core:ui（约定插件隐式注入）
     └─→ sync:work ──→ core:data ──→ core:common（叶子）
                                 ──→ core:database ──→ core:model（叶子，api 暴露）
                                 ──→ core:model
                                 ──→ core:network（叶子）
```

### 1.2 本模板 `core:ui` 内容盘点

`core/ui/src/main` 下**仅 3 个 .kt 文件，无资源文件**：

| 文件 | 作用 |
|---|---|
| `core/ui/.../Color.kt` | 6 个颜色常量（Purple80/Pink80 等），Android Studio 默认紫色系 |
| `core/ui/.../Theme.kt` | MaterialTheme 定义：`DarkColorScheme`/`LightColorScheme` + `MyApplicationTheme`，支持动态取色 |
| `core/ui/.../Type.kt` | Material3 `Typography`，仅定制 `bodyLarge` |

结论：
- 有 MaterialTheme 定义（`MyApplicationTheme`）。
- **没有任何自定义 Composable 组件**。
- design token 极不完整：只有颜色 + 字体，缺 Shapes、间距、图标。
- `Theme.kt` 残留未使用 import（`SideEffect`、`toArgb`、`LocalView`、`ViewCompat`、`Activity`），是 IDE 模板痕迹。

### 1.3 本模板可复用 UI 组件盘点

全模板（排除 nowinandroid 与测试源码）含 `@Composable` 的文件仅 5 个：

| 文件 | Composable | 性质 |
|---|---|---|
| `core/ui/.../Theme.kt` | `MyApplicationTheme` | 主题，非组件 |
| `core/navigation/.../NavigationState.kt` | `rememberNavigationState` | 导航基础设施 |
| `app/.../ui/Navigation.kt` | `MainNavigation` | app 组装 |
| `feature/mymodel/impl/.../EntryProvider.kt` | `MyModelEntryProvider` | 导航注册 |
| `feature/mymodel/impl/.../MyModelScreen.kt` | `MyModelScreen` ×2 | 业务屏幕 |

即：**无**加载指示器封装、**无**错误态/重试视图、**无**空态视图、**无**通用卡片/列表项、**无**图标模块。

相关"状态抽象"存在但无配套 UI：
- `core/common/.../result/Result.kt`：通用 `Result<T>`（Success/Error/Loading）+ `Flow.asResult()`。
- `MyModelViewModel` 的 `MyModelUiState`（Loading/Error/Success）——与 `Result` 语义重复，且 Error/Loading 无 UI 落地。

### 1.4 `MyModelScreen` 现状问题

- 外层有状态层 `hiltViewModel()` + `collectAsStateWithLifecycle()` 收集 `uiState` 与 `isSyncing`；同步中显示 `LinearProgressIndicator`；**仅在 Success 时渲染内容**。
- 内层无状态层：`Column` + `Row`，`TextField`（输入）、`Button`+`Text`（"Save"）、`forEach { Text(...) }` 平铺列表项。
- 直接使用裸 material3 组件（`Button`/`TextField`/`Text`/`LinearProgressIndicator`），对 `core:ui` 的复用仅限主题（且只在 Preview 里）。
- 问题：
  1. `MyModelUiState` 有 Loading/Error/Success 三态，但 **Screen 只处理 Success**，Loading/Error 无 UI，错误被静默吞掉。
  2. 列表用 `forEach` 而非 `LazyColumn`，不可扩展。
  3. 硬编码字符串 `"Save"`、`"Saved item: $it"`，未走 string resources。
  4. `onItemClick: (NavKey) -> Unit` 参数声明了但未使用。

### 1.5 NiA 的 UI 分层（参照）

NiA 把 UI 层拆成两个职责明确的 core 模块，单向依赖：

```
feature:xxx:impl ──► core:ui ──► core:designsystem ──► Compose/Material3/Coil
        │               │                │
        └───────────────┴──► core:model（纯数据模型，无 UI）
```

- **`core:designsystem`**：纯设计系统。主题（颜色/字体/渐变/背景/着色）、图标表、对 Material3 的"品牌化包装"组件。**零业务、零领域模型**，不依赖任何其他 core 模块。
- **`core:ui`**：跨 feature 共享的"业务 UI"，直接消费 `core:model` 领域对象的组件，外加埋点/JankStats/预览数据等"UI 层公共设施"。依赖 designsystem（api 传递）。

**NiA `core:designsystem` 结构（23 个 .kt 文件）：**

- `theme/`（6 文件）：`Theme.kt`（`NiaTheme`，4 套 ColorScheme + 动态取色 + 经 CompositionLocal 下发 Gradient/Background/Tint）、`Color.kt`、`Type.kt`（`NiaTypography`）、`Background.kt`、`Gradient.kt`、`Tint.kt`。
- `icon/`（1 文件）：`NiaIcons.kt`（全 App 图标集中入口）。
- `component/`（11 文件）：`Background`、`Button`、`Chip`、`DynamicAsyncImage`（Coil 封装）、`IconButton`、`LoadingWheel`（自绘加载指示器）、`Navigation`（底栏/侧栏/NavigationSuiteScaffold）、`Tabs`、`Tag`、`TopAppBar`、`ViewToggle`。
- `component/scrollbar/`（5 文件）：自研滚动条子系统（`Scrollbar`、`AppScrollbars`、`ScrollbarExt` 等）。

---

## 2. 差距诊断（关键问题）

| # | 差距 | 影响 |
|---|---|---|
| 1 | `core:ui` 名不副实：只有主题（3 文件、0 组件、0 资源） | 无设计系统可言 |
| 2 | **无任何可复用 UI 组件**：无加载/错误/空态封装 | `MyModelUiState` 的 Loading/Error 态无 UI，**错误被静默吞掉** |
| 3 | design token 不完整：只有 Color/Typography，缺 Shapes/间距/图标；主题是 IDE 模板残留（默认紫、`MyApplicationTheme`、未使用 import） | 设计系统不成立 |
| 4 | **两套状态模型重复**：`core:common:Result` vs feature `MyModelUiState` | 架构冗余 |
| 5 | `MyModelScreen` 只处理 Success；`forEach` 非 `LazyColumn`；硬编码字符串 | 组件质量差、不可扩展 |
| 6 | **无 `core:designsystem` 分层**（NiA 是 designsystem→ui 两层） | 结构未对齐 NiA |

---

## 3. 改进点清单（按优先级）

### 🔴 P0 —— 核心结构与真实痛点（强烈建议）

**A. 新建 `core:designsystem` 模块，建立 NiA 式分层**
- 将主题（Color/Theme/Type）+ design tokens 从 `core:ui` 迁入 `core:designsystem`；`core:ui` 保留为"跨 feature 共享业务 UI"层。
- 依赖方向对齐 NiA：`feature:impl → core:ui → core:designsystem`。
- 涉及排除项：无。

**B. 补齐可复用状态组件（放入 `core:designsystem`）**
- `LoadingIndicator`、`ErrorView`（带重试）、`EmptyView`，以及一个统一的"按 UiState 分发"呈现组件。
- 直接解决"错误被静默吞掉"的真实痛点。
- 涉及排除项：无。

**C. `MyModelScreen` 完整处理三态**
- 用 B 的组件呈现 Loading/Error/Success；错误不再静默。
- 涉及排除项：无。

### 🟡 P1 —— 组件质量与架构整洁（建议）

**D. 完善 design tokens + 清理主题残留**
- 补 Shapes/间距 token；重命名 `MyApplicationTheme`→泛化名；去除未使用 import；替换 IDE 默认紫色为中性配色。
- 涉及排除项：无。

**E. `MyModelScreen` 组件升级**
- `forEach` → `LazyColumn`；硬编码字符串 → string resources。
- 涉及排除项：无。

**F. 统一状态模型**
- 梳理并去重 `core:common:Result` 与 feature `MyModelUiState`，对齐 NiA 的单一 UiState 模式。
- 涉及排除项：无。（改动稍深，涉及 ViewModel/UseCase）

### 🟢 P2 —— 可选增强

**G. 图标集中管理**：建泛化的 `TemplateIcons` object（若后续需要图标）。
**H. `core:ui` 结构预留**：当前内容少，作为"共享业务 UI"层保留占位，随模板扩展。

---

## 4. 明确**不做**（NiA 有，但不适合单 feature 模板 / 属排除项）

- ❌ 自研滚动条系统（Scrollbar）、`DynamicAsyncImage`/Coil 图片组件（模板无图片加载需求）。
- ❌ `NavigationSuiteScaffold`/Tabs/Chip/Tag/ViewToggle（单顶层目的地用不上）。
- ❌ 截图测试、JankStats、分析（属之前排除项）。
- ❌ 任何 NiA 业务名与 `Nia*` 前缀。

---

## 5. 实施方式（确认后执行）

1. 用户勾选要实施的改进点（如"全部 P0+P1"或指定字母）。
2. 按勾选项**逐项完整实施**，全程保持 `MyModel` 泛化命名、不引入 NiA 业务名。
3. 每步：构建验证（`assembleDebug` + `testDebugUnitTest`）→ customizer 兼容性验证（临时副本跑 `customizer.sh`）→ 更新文档 → 提交推送。

---

## 6. 确认状态

- [ ] A. 新建 `core:designsystem` 分层
- [ ] B. 可复用状态组件（Loading/Error/Empty）
- [ ] C. `MyModelScreen` 三态处理
- [ ] D. design tokens 完善 + 主题清理
- [ ] E. `MyModelScreen` 组件升级（LazyColumn + strings）
- [ ] F. 统一状态模型
- [ ] G. 图标集中管理（P2）
- [ ] H. `core:ui` 结构预留（P2）

> 待用户勾选后开始实施。
