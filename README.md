# vector-icon-lib
Vectoriel Icon Libraries

## Catalogue SVG

Chaque répertoire contenant des fichiers SVG possède son propre `README.md` et
son fichier `icons.json`, générés directement au milieu des logos. Les
catalogues sont donc disponibles dans les répertoires sources :

- `Divers/`
- `ms-365_2024/`
- `OfficeSymbols_2014/`

Les sous-répertoires qui contiennent des fichiers SVG disposent chacun de ces
deux fichiers :

- `README.md` : tableau Markdown avec miniature, nom, mots-clés issus de
  l'arborescence et de la composition du nom de fichier, dimensions du logo et
  lien local vers l'image. Les aperçus utilisent la syntaxe Markdown standard ;
- `icons.json` : mêmes informations dans un format exploitable par des scripts
  et des agents. Le champ `file` est la référence unique du logo ; le nom et
  l'aperçu sont dérivés de ce fichier, sans champs redondants.

Il n'y a plus de copie de ces catalogues dans `docs/`. La documentation est
maintenue au plus près des fichiers SVG afin de simplifier leur découverte et
leur utilisation.

Pour régénérer les catalogues depuis la racine du projet :

```bash
python3 scripts/generate-svg-docs.py
```

Pour régénérer le thème Structurizr :

```bash
python3 scripts/create-structurizr-theme.py
```
