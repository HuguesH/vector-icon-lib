#!/bin/bash

echo '{'
echo '  "name": "vector-icon-lib Structurizr Theme",'
echo '  "description": "Thème Structurizr basé sur les icônes SVG du projet.",'

# Génération de la section elements
echo '  "elements": ['
first=1
find Divers OfficeSymbols_2014 -type f -name "*.svg" | sort | while read -r file; do
  name="${file%.svg}"
  tag=$(basename "$name" | sed 's/[^a-zA-Z0-9]/_/g')
  if [ $first -eq 0 ]; then
    echo ","
  fi
  echo -n "    { \"tag\": \"$tag\", \"sprite\": \"$name\" }"
  first=0
done
echo
echo '  ],'

# Génération de la section sprites
echo '  "sprites": ['
first=1
find Divers OfficeSymbols_2014 -type f -name "*.svg" | sort | while read -r file; do
  name="${file%.svg}"
  if [ $first -eq 0 ]; then
    echo ","
  fi
  echo -n "    { \"name\": \"$name\", \"file\": \"$file\" }"
  first=0
done
echo
echo '  ]'
echo '}'