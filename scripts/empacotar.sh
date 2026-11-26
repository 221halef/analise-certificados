#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

VERSAO="1.1.1"
NOME_APP="AtividadesComplementares"
PASTA_SAIDA="target/instalador"

echo "Compilando o projeto..."
mvn -q clean package

rm -rf "$PASTA_SAIDA"
mkdir -p "$PASTA_SAIDA"

rm -rf target/entrada
mkdir -p target/entrada
cp target/atividades-complementares.jar target/entrada/
cp -r target/libs target/entrada/libs

SISTEMA="$(uname -s)"

case "$SISTEMA" in
  Linux*)
    jpackage \
      --input target/entrada \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.Launcher \
      --type deb \
      --linux-shortcut
    jpackage \
      --input target/entrada \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.Launcher \
      --type rpm \
      --linux-shortcut
    ;;
  Darwin*)
    jpackage \
      --input target/entrada \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.Launcher \
      --type dmg
    ;;
  MINGW*|MSYS*|CYGWIN*)
    jpackage \
      --input target/entrada \
      --dest "$PASTA_SAIDA" \
      --name "$NOME_APP" \
      --app-version "$VERSAO" \
      --main-jar atividades-complementares.jar \
      --main-class br.com.atividadescomplementares.ui.Launcher \
      --type msi \
      --win-shortcut \
      --win-dir-chooser \
      --win-console
    ;;
  *)
    echo "Sistema operacional não reconhecido: $SISTEMA"
    exit 1
    ;;
esac

echo "Instalador(es) gerado(s) em $PASTA_SAIDA"
