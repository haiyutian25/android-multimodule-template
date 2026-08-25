#!/usr/bin/env python3
"""
One-click removal of Apache license header comments from template files.

Supported file types:
  .kt / .kts         leading /* ... */ block comment
  .xml               leading <!-- ... --> comment (a leading <?xml?> declaration is kept)
  .properties / .sh  leading '#' comment block (a '#!' shebang line is kept)

Safety rules:
  - Only a comment block at the very TOP of the file is considered.
  - The block is removed only if it contains license markers
    (Copyright / Apache License / Licensed under).
  - build / .git / .gradle / .idea / .kotlin directories are skipped.
  - Original line endings and UTF-8 (no BOM) encoding are preserved.

Usage:
  python remove_license_headers.py               # remove headers under the script's directory
  python remove_license_headers.py --list-only   # preview: list files only, change nothing
  python remove_license_headers.py --root <dir>  # process another directory
"""

import argparse
import re
import sys
from pathlib import Path

EXTENSIONS = {'.kt', '.kts', '.xml', '.properties', '.sh'}
EXCLUDE_DIRS = {'build', '.git', '.gradle', '.idea', '.kotlin'}
LICENSE_MARKERS = ('Copyright', 'Apache License', 'Licensed under')

BLOCK_COMMENT_RE = re.compile(r'^\s*/\*.*?\*/', re.DOTALL)
XML_COMMENT_RE = re.compile(r'^(<\?xml[^>]*\?>\s*)?<!--.*?-->', re.DOTALL)


def has_license(block: str) -> bool:
    return any(marker in block for marker in LICENSE_MARKERS)


def detect_newline(text: str) -> str:
    return '\r\n' if '\r\n' in text else '\n'


def strip_kt_header(text: str):
    m = BLOCK_COMMENT_RE.match(text)
    if m and has_license(m.group(0)):
        return text[m.end():].lstrip('\r\n')
    return None


def strip_xml_header(text: str):
    m = XML_COMMENT_RE.match(text)
    if m and has_license(m.group(0)):
        prolog = m.group(1)
        rest = text[m.end():].lstrip('\r\n')
        if prolog:
            return prolog.rstrip() + detect_newline(text) + rest
        return rest
    return None


def strip_hash_header(text: str, keep_shebang: bool):
    lines = text.splitlines()
    newline = detect_newline(text)
    start = 0
    shebang = None
    if keep_shebang and lines and lines[0].startswith('#!'):
        shebang = lines[0]
        start = 1
    i = start
    while i < len(lines) and lines[i].lstrip().startswith('#'):
        i += 1
    if i == start:
        return None
    block = '\n'.join(lines[start:i])
    if not has_license(block):
        return None
    rest = lines[i:]
    while rest and not rest[0].strip():
        rest.pop(0)
    body = newline.join(rest)
    if shebang is not None:
        return shebang + newline + newline + body
    return body


def process_file(text: str, ext: str):
    """Return the new content, or None if nothing should be changed."""
    if ext in ('.kt', '.kts'):
        return strip_kt_header(text)
    if ext == '.xml':
        return strip_xml_header(text)
    if ext == '.properties':
        return strip_hash_header(text, keep_shebang=False)
    if ext == '.sh':
        return strip_hash_header(text, keep_shebang=True)
    return None


def iter_files(root: Path):
    for path in sorted(root.rglob('*')):
        if not path.is_file() or path.suffix.lower() not in EXTENSIONS:
            continue
        if any(part in EXCLUDE_DIRS for part in path.relative_to(root).parts):
            continue
        yield path


def main() -> int:
    parser = argparse.ArgumentParser(
        description='Remove Apache license headers from template files.')
    parser.add_argument('--root', default=str(Path(__file__).resolve().parent),
                        help='directory to process (default: the script directory)')
    parser.add_argument('--list-only', action='store_true',
                        help='only list files that would be changed; change nothing')
    args = parser.parse_args()

    root = Path(args.root).resolve()
    if not root.is_dir():
        print(f'Not a directory: {root}', file=sys.stderr)
        return 2

    changed = 0
    for path in iter_files(root):
        try:
            with open(path, 'r', encoding='utf-8-sig', newline='') as fh:
                text = fh.read()
        except (UnicodeDecodeError, OSError) as exc:
            print(f'  [skipped] {path}: {exc}', file=sys.stderr)
            continue
        new_text = process_file(text, path.suffix.lower())
        if new_text is None or new_text == text:
            continue
        rel = path.relative_to(root)
        if args.list_only:
            print(f'  [would remove] {rel}')
        else:
            with open(path, 'w', encoding='utf-8', newline='') as fh:
                fh.write(new_text)
            print(f'  [removed] {rel}')
        changed += 1

    print()
    if args.list_only:
        print(f'Found {changed} file(s) with license headers. '
              f'Run without --list-only to remove them.')
    else:
        print(f'Removed license headers from {changed} file(s).')
    return 0


if __name__ == '__main__':
    sys.exit(main())
