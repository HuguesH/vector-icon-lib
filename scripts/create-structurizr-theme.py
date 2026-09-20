#!/usr/bin/env python3
"""Generate the Structurizr theme from the project's SVG icons."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


THEME_NAME = "vector-icon-lib Structurizr Theme"
THEME_DESCRIPTION = "Thème Structurizr basé sur les icônes SVG du projet."
SOURCE_DIRECTORIES = ("Divers", "OfficeSymbols_2014")


def svg_files(root: Path) -> list[Path]:
    return sorted(
        (
            path
            for source_name in SOURCE_DIRECTORIES
            for path in (root / source_name).rglob("*.svg")
        ),
        key=lambda path: path.as_posix(),
    )


def element_tag(svg_file: Path) -> str:
    return re.sub(r"[^a-zA-Z0-9]", "_", svg_file.stem)


def create_theme(root: Path) -> dict[str, object]:
    sprites = [
        {
            "name": svg_file.with_suffix("").relative_to(root).as_posix(),
            "file": svg_file.relative_to(root).as_posix(),
        }
        for svg_file in svg_files(root)
    ]
    elements = [
        {
            "tag": element_tag(svg_file),
            "sprite": svg_file.with_suffix("").relative_to(root).as_posix(),
        }
        for svg_file in svg_files(root)
    ]
    return {
        "name": THEME_NAME,
        "description": THEME_DESCRIPTION,
        "elements": elements,
        "sprites": sprites,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate the Structurizr theme JSON file."
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Project root (default: repository root).",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Output JSON file (default: <root>/theme.json).",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    root = args.root.resolve()
    output = (args.output or root / "theme.json").resolve()
    if not root.is_dir():
        raise SystemExit(f"Root directory does not exist: {root}")

    missing = [name for name in SOURCE_DIRECTORIES if not (root / name).is_dir()]
    if missing:
        raise SystemExit(f"Missing source directory(ies): {', '.join(missing)}")

    theme = create_theme(root)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(theme, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Generated {len(theme['sprites'])} sprites in {output}")


if __name__ == "__main__":
    main()
