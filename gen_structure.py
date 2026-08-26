#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
导出本模板工作区的真实目录 / 模块结构为 Markdown。

- 遍历目录树，排除构建产物与参考克隆（build/.git/.gradle/nowinandroid 等）。
- 含 build.gradle.kts 的目录标记为 Gradle 模块，且不再向下展开（避免 src/main/java 噪音）。
- 顶层与 gradle/ 目录同时列出关键文件（.md/.sh/.kts/.properties/.conf/.toml）。
- 额外从 settings.gradle.kts 解析 include(...) 得到权威模块清单。
输出写入 WORKSPACE_STRUCTURE.md。
"""
import os
import re

ROOT = os.path.dirname(os.path.abspath(__file__))
EXCLUDE_DIRS = {
    ".git", ".gradle", ".idea", ".kotlin", ".codebuddy",
    "build", "nowinandroid", "node_modules", ".cxx", "captures",
}
FILE_SUFFIX = (".md", ".sh", ".kts", ".properties", ".conf", ".toml")
# 除根目录外，还需要同时列出文件的目录
SHOW_FILES_IN = {"gradle"}


def is_gradle_module(d: str) -> bool:
    return os.path.isfile(os.path.join(d, "build.gradle.kts"))


def get_children(path: str, show_files: bool):
    """返回 (dirs, files)：dirs 为 (name, full)，files 为 (name, None)。"""
    try:
        entries = sorted(os.listdir(path), key=str.lower)
    except (PermissionError, FileNotFoundError):
        return [], []
    dirs, files = [], []
    for e in entries:
        if e.startswith("."):
            continue
        full = os.path.join(path, e)
        if os.path.isdir(full):
            if e not in EXCLUDE_DIRS:
                dirs.append((e, full))
        elif show_files and e.endswith(FILE_SUFFIX):
            files.append((e, None))
    return dirs, files


def draw(path: str, prefix: str, lines: list, show_files: bool):
    dirs, files = get_children(path, show_files)
    items = [(name + "/", full, True) for name, full in dirs]
    items += [(name, None, False) for name, _ in files]
    n = len(items)
    for i, (label, full, is_dir) in enumerate(items):
        last = i == n - 1
        connector = "└── " if last else "├── "
        disp = label
        if is_dir and is_gradle_module(full):
            disp += "   ← Gradle 模块"
        lines.append(prefix + connector + disp)
        if is_dir and not is_gradle_module(full):
            child_show = os.path.basename(full) in SHOW_FILES_IN
            draw(full, prefix + ("    " if last else "│   "), lines, child_show)


def modules_from_settings():
    settings = os.path.join(ROOT, "settings.gradle.kts")
    if not os.path.isfile(settings):
        return []
    text = open(settings, encoding="utf-8").read()
    return sorted(set(re.findall(r'include\(\s*"(:[^"]+)"\s*\)', text)))


def main():
    root_name = os.path.basename(ROOT)
    lines = ["```", root_name + "/"]
    draw(ROOT, "", lines, show_files=True)
    lines.append("```")

    mods = modules_from_settings()
    mod_lines = ["", "## Gradle 模块清单（来自 settings.gradle.kts）", "", "```"]
    mod_lines += mods
    mod_lines += ["```", "", f"共 {len(mods)} 个模块。"]

    out = [
        "# 工作区结构（脚本自动导出）", "",
        "> 由 `gen_structure.py` 从文件系统实时生成，排除构建产物与 `nowinandroid/` 参考克隆。",
        "> 目录按字母序；`← Gradle 模块` 表示含 `build.gradle.kts` 的模块（不再展开其源码目录）。", "",
    ]
    out += lines
    out += mod_lines

    target = os.path.join(ROOT, "WORKSPACE_STRUCTURE.md")
    with open(target, "w", encoding="utf-8") as fh:
        fh.write("\n".join(out) + "\n")
    print("written:", target)


if __name__ == "__main__":
    main()
