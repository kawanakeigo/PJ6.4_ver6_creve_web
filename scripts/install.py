#!/usr/bin/env python3
"""Replace a project only on explicit --apply; preserve the whole original as a sibling backup."""
from pathlib import Path
import argparse, datetime, os, shutil, tempfile
root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('target', nargs='?', default='/Users/k5/Documents/creve-web/workspace/creve/creve-web')
parser.add_argument('--apply', action='store_true')
args = parser.parse_args()
target = Path(args.target).expanduser().absolute()
if target == root or root in target.parents or target in root.parents:
    parser.error('展開元と差し替え先が重なっています。ZIPは別の場所へ展開してください。')
if target.is_symlink() or not target.is_dir() or not (target/'pom.xml').is_file():
    parser.error('差し替え先に実在するプロジェクトフォルダとpom.xmlが必要です。')
backup = target.with_name(target.name+'_backup_'+datetime.datetime.now().strftime('%Y%m%d_%H%M%S'))
if backup.exists(): parser.error('同名バックアップがあります。時間をおいて再実行してください。')
print('差し替え先:', target)
print('旧版を丸ごと保存:', backup)
print('旧DB・旧.envは読み込みません。旧設定はバックアップ側に残します。')
if not args.apply:
    print('確認のみ。変更はしていません。反映するには --apply を追加してください。')
    raise SystemExit(0)
staging = Path(tempfile.mkdtemp(prefix='.creve-install-',dir=target.parent))
installed = False
moved = False
try:
    shutil.copytree(root,staging,dirs_exist_ok=True,ignore=shutil.ignore_patterns('.git','target','.env.local','private-backups','__pycache__','local-results'))
    local_config = root/'.env.local'
    if local_config.is_file():
        shutil.copy2(local_config,staging/'.env.local')
        (staging/'.env.local').chmod(0o600)
    # Preserve Git identity/history, never credentials or an old incompatible datasource.
    old_git = target/'.git'
    if old_git.is_dir(): shutil.copytree(old_git,staging/'.git')
    elif old_git.is_file(): shutil.copy2(old_git,staging/'.git')
    target.rename(backup); moved=True
    staging.rename(target); installed=True
    print('差し替え完了。旧版・旧設定・その他の元ファイルはバックアップに残っています。')
    print('復元時は修正版を別名へ移し、バックアップを元の名前へ戻してください。')
except Exception:
    if moved and not target.exists(): backup.rename(target)
    raise
finally:
    if not installed and staging.exists(): shutil.rmtree(staging)
