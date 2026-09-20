#!/usr/bin/env python3
"""Generate local README.md and icons.json catalogues next to SVG files."""

from __future__ import annotations

import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import NotRequired, TypedDict
from urllib.parse import quote


COLLECTIONS = (
    ("Divers", "Divers", "Icônes issues de bibliothèques et d’outils divers du projet."),
    (
        "Microsoft-365",
        "ms-365_2024",
        "Icônes Microsoft 365 collectées depuis les bibliothèques Microsoft 365 2024.",
    ),
    (
        "OfficeSymbols",
        "OfficeSymbols_2014",
        "Icônes Office Symbols 2014 organisées par domaine fonctionnel.",
    ),
)


class IconEntry(TypedDict):
    file: str
    keywords: list[str]
    width: str | None
    height: str | None
    viewBox: NotRequired[str]


def svg_files(directory: Path) -> list[Path]:
    return sorted(
        (
            path
            for path in directory.iterdir()
            if path.is_file() and path.suffix.lower() == ".svg"
        ),
        key=lambda path: path.name.casefold(),
    )


def normalize_svg_filenames(source_root: Path) -> int:
    renamed = 0
    candidates = sorted(
        (
            path
            for path in source_root.rglob("*")
            if path.is_file()
            and path.suffix.lower() == ".svg"
            and " " in path.name
        ),
        key=lambda path: path.as_posix().casefold(),
    )
    for path in candidates:
        target = path.with_name(path.name.replace(" ", "-"))
        if target.exists():
            raise FileExistsError(f"Cannot rename {path}: target exists ({target})")
        path.rename(target)
        renamed += 1
    return renamed


def keywords(
    directory: Path, source_root: Path, collection_name: str, filename: str
) -> list[str]:
    relative = directory.relative_to(source_root)
    terms = re.findall(r"[A-Za-zÀ-ÿ0-9]+", Path(filename).stem)
    values = (collection_name, *relative.parts, *terms)
    return list(dict.fromkeys(value.casefold() for value in values))


def svg_dimensions(svg_file: Path) -> dict[str, str | None]:
    try:
        root = ET.parse(svg_file).getroot()
    except ET.ParseError as error:
        raise ValueError(f"Invalid SVG XML in {svg_file}: {error}") from error

    view_box = root.get("viewBox")
    view_box_values = view_box.split() if view_box else []
    width = root.get("width")
    height = root.get("height")
    if not width and len(view_box_values) == 4:
        width = view_box_values[2]
    if not height and len(view_box_values) == 4:
        height = view_box_values[3]

    dimensions: dict[str, str | None] = {
        "width": width,
        "height": height,
    }
    if view_box:
        dimensions["viewBox"] = view_box
    return dimensions


def markdown_path(filename: str) -> str:
    return quote(filename, safe="/:@-._~")


def markdown_text(value: str) -> str:
    return value.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]")


def icon_entries(
    directory: Path, source_root: Path, collection_name: str
) -> list[IconEntry]:
    entries: list[IconEntry] = []
    for svg_file in svg_files(directory):
        dimensions = svg_dimensions(svg_file)
        entry: IconEntry = {
            "file": svg_file.name,
            "keywords": keywords(
                directory, source_root, collection_name, svg_file.name
            ),
            "width": dimensions["width"],
            "height": dimensions["height"],
        }
        if dimensions.get("viewBox"):
            entry["viewBox"] = dimensions["viewBox"]
        entries.append(
            entry
        )
    return entries


def render_readme(
    directory: Path,
    source_root: Path,
    collection_name: str,
    origin: str,
    entries: list[IconEntry],
) -> str:
    relative = directory.relative_to(source_root)
    title = directory.name
    location = relative.as_posix() if relative != Path(".") else collection_name
    lines = [
        f"# {title}",
        "",
        f"{origin} Répertoire source : `{location}`.",
        "",
        f"Catalogue JSON : [icons.json](icons.json).",
        "",
        f"### Images SVG ({len(entries)})",
        "",
        "| Aperçu | Nom | Mots-clés | Taille | Fichier |",
        "| --- | --- | --- | --- | --- |",
    ]
    for entry in entries:
        file_name = str(entry["file"])
        name = Path(file_name).stem
        escaped_name = name.replace("|", "\\|")
        image_alt = markdown_text(name)
        image_path = markdown_path(file_name)
        keyword_text = ", ".join(str(value) for value in entry["keywords"]).replace(
            "|", "\\|"
        )
        size = f'{entry["width"] or "?"} × {entry["height"] or "?"}'
        lines.append(
            f"| ![{image_alt}]({image_path}) | {escaped_name} | {keyword_text} | "
            f"{size} | [{file_name}]({image_path}) |"
        )
    return "\n".join(lines) + "\n"


def generate_collection(
    source_root: Path, collection_name: str, origin: str
) -> int:
    generated = 0
    renamed = normalize_svg_filenames(source_root)
    if renamed:
        print(f"Renamed {renamed} SVG file(s) in {source_root}")
    for directory in sorted(
        (path for path in source_root.rglob("*") if path.is_dir()),
        key=lambda path: path.as_posix().casefold(),
    ):
        files = svg_files(directory)
        if not files:
            continue
        entries = icon_entries(directory, source_root, collection_name)
        (directory / "README.md").write_text(
            render_readme(directory, source_root, collection_name, origin, entries),
            encoding="utf-8",
        )
        (directory / "icons.json").write_text(
            json.dumps(entries, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        generated += 1
    return generated


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate local README.md and icons.json files for SVG directories."
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Project root (default: repository root).",
    )
    return parser.parse_args()


def main() -> None:
    root = parse_args().root.resolve()
    if not root.is_dir():
        raise SystemExit(f"Root directory does not exist: {root}")

    generated = 0
    for collection_name, source_name, origin in COLLECTIONS:
        source_root = root / source_name
        if not source_root.is_dir():
            print(f"Skipping missing collection: {source_name}")
            continue
        generated += generate_collection(source_root, collection_name, origin)
    print(f"Generated local catalogues in {generated} SVG directories")


if __name__ == "__main__":
    main()
