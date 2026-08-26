# 工作区结构（脚本自动导出）

> 由 `gen_structure.py` 从文件系统实时生成，排除构建产物与 `nowinandroid/` 参考克隆。
> 目录按字母序；`← Gradle 模块` 表示含 `build.gradle.kts` 的模块（不再展开其源码目录）。

```
architecture-templates-multimodule/
├── app/   ← Gradle 模块
├── benchmarks/   ← Gradle 模块
├── build-logic/
│   └── convention/   ← Gradle 模块
├── core/
│   ├── common/   ← Gradle 模块
│   ├── data/   ← Gradle 模块
│   ├── data-test/   ← Gradle 模块
│   ├── database/   ← Gradle 模块
│   ├── designsystem/   ← Gradle 模块
│   ├── domain/   ← Gradle 模块
│   ├── model/   ← Gradle 模块
│   ├── navigation/   ← Gradle 模块
│   ├── network/   ← Gradle 模块
│   ├── testing/   ← Gradle 模块
│   └── ui/   ← Gradle 模块
├── feature/
│   └── mymodel/
│       ├── api/   ← Gradle 模块
│       └── impl/   ← Gradle 模块
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml
├── sync/
│   ├── sync-test/   ← Gradle 模块
│   └── work/   ← Gradle 模块
├── ui-test-hilt-manifest/   ← Gradle 模块
├── build.gradle.kts
├── compose_compiler_config.conf
├── CONTRIBUTING.md
├── customizer.sh
├── DEVELOPMENT_MANUAL.md
├── gradle.properties
├── README.md
├── settings.gradle.kts
├── TEMPLATE_ALIGNMENT_GAPS.md
├── TEMPLATE_EVOLUTION_PLAN.md
├── TEMPLATE_REALIGNMENT_PLAN.md
└── WORKSPACE_STRUCTURE.md
```

## Gradle 模块清单（来自 settings.gradle.kts）

```
:app
:benchmarks
:core:common
:core:data
:core:data-test
:core:database
:core:designsystem
:core:domain
:core:model
:core:navigation
:core:network
:core:testing
:core:ui
:feature:mymodel:api
:feature:mymodel:impl
:sync:sync-test
:sync:work
:ui-test-hilt-manifest
```

共 18 个模块。
