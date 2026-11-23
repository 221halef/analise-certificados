#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

VERSAO="1.0.0"
NOME_APP="AtividadesComplementares"
PASTA_SAIDA="target/instalador"

echo "Compilando o projeto..."
mvn -q clean package

rm -rf "$PASTA_SAIDA"
mkdir -p "$PASTA_SAIDA"

SISTEMA="$(uname -s)"

case "$SISTEMA" in
  Linux*)
    jpackage \
      --input target \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.App \
      --type deb \
      --linux-shortcut
    jpackage \
      --input target \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.App \
      --type rpm \
      --linux-shortcut
    ;;
  Darwin*)
    jpackage \
      --input target \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.App \
      --type dmg
    ;;
  MINGW*|MSYS*|CYGWIN*)
    jpackage \
      --input target \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.App \
      --type msi \
      --win-shortcut \
      --win-dir-chooser
    ;;
  *)
    echo "Sistema operacional não reconhecido: $SISTEMA"
    exit 1
    ;;
esac

echo "Instalador(es) gerado(s) em $PASTA_SAIDA"
