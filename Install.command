#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
echo '先にStart.command・Verify.commandで確認してください。'
echo 'Macの指定フォルダを修正版に置き換え、旧フォルダは丸ごとバックアップします。'
echo '/Users/k5/Documents/creve-web/workspace/creve/creve-web'
read -r -p '反映する場合だけ APPLY と入力してください: ' answer
[ "$answer" = APPLY ] || { echo '変更しませんでした。'; exit 0; }
exec python3 scripts/install.py --apply
