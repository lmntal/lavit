
LaViT - LMNtal IDE
===============================

LaViTはプログラミング言語LMNtalのグラフィカルな統合開発環境です。  
バグ報告は lmntal@ueda.info.waseda.ac.jp までお願いします。  
[LaViT](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/ "LaViT")からダウンロード可能です。  

---

## Links
- [LaViT紹介ページ](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/ "LaViT")
- [LMNtalチュートリアル](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/index.php?LMNtal%E3%83%81%E3%83%A5%E3%83%BC%E3%83%88%E3%83%AA%E3%82%A2%E3%83%AB "LMNtalチュートリアル")

## Getting started

LMNtalコンパイラ・処理系SLIM・可視化ツールUNYO・Grapheneをすべて同梱したバンドル版を
[LaViTダウンロードページ](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/ "LaViT")から入手できます。  
ダウンロードして展開し、`run.sh`（Linux/macOS）または `run.bat`（Windows）を実行してください。

## Develop

以下では、LaViT本体とLMNtalコンパイラ・SLIM・Grapheneを組み合わせた**ローカルバンドルmyLaViTを個人環境で構築する手順**を説明します。
バージョン番号の決定やGitHubへのリリースはLMNtalグループが行います。ここではあくまで手元で動作確認できる一式を組み立てることを目的としています。

---

### 1. LaViT 本体のビルド

```bash
git clone git@github.com:lmntal/lavit.git
cd lavit
git checkout x.x.x        # 使用したい安定版タグ
ant jar                    # LaViT.jar を生成
```

出力ディレクトリを作成し、必要ファイルをまとめます。

```bash
mkdir myLaViT
cp LaViT.jar run.sh run.bat version.txt myLaViT/
cp -r demo myLaViT/
```

---

### 2. lmntal ディレクトリの準備

既存の公式バンドル版から `lmntal/` ディレクトリを流用します。

```bash
cp -r 旧バンドル版LaViT/lmntal myLaViT/
```

続いて、新版に差し替えるため古いファイルを削除します。

```bash
# 旧バイナリを削除
rm -rf myLaViT/lmntal/lib
rm -rf myLaViT/lmntal/slim-*
rm -rf myLaViT/lmntal/graphene

# lmntal/bin/ は set_cp.sh と lmntal だけを残す
cd myLaViT/lmntal/bin
ls | grep -v -E '^(set_cp\.sh|lmntal)$' | xargs rm -f
cd -
```

---

### 3. lmntal-compiler の組み込み

```bash
git clone git@github.com:lmntal/lmntal-compiler.git
cd lmntal-compiler
git checkout vz.z.z        # 使用したい安定版タグ
ant jar                    # bin/lmntal.jar を生成
```

生成物を `lmntal/bin/` にコピーします。

```bash
cp bin/lmntal bin/lmntal.jar /path/to/myLaViT/lmntal/bin/
cd ..
```

LaViT は `lmntal/bin/lmntal.jar` を参照してLMNtalコンパイラを起動します。

---

### 4. SLIM の組み込み

余分なファイルを除外するため `git archive` を使います。

```bash
git clone git@github.com:lmntal/slim.git
cd slim
# --prefix の末尾スラッシュを忘れないこと（忘れるとディレクトリ構造が壊れる）
git archive --format=tar --prefix=slim-y.y.y/ vy.y.y > ../slim-y.y.y.tar
cd ..

tar -xvf slim-y.y.y.tar
cd slim-y.y.y
rm .gitignore              # Git管理用ファイルは不要
./autogen.sh               # configure スクリプトを生成
cd ..
```

展開・生成したディレクトリを所定の場所に移動します。

```bash
mv slim-y.y.y /path/to/myLaViT/lmntal/
```

ユーザーはバンドルを入手後、`lmntal/slim-y.y.y/` で `./configure && make` を実行してSLIMのバイナリをビルドします。

---

### 5. Graphene の組み込み

```bash
mkdir -p myLaViT/lmntal/graphene

git clone git@github.com:lmntal/Graphene.git
cd Graphene
git checkout vz.z.z        # 使用したい安定版タグ
sbt assembly               # fat jar を生成
```

生成されたJARをコピーし、LaViTが期待するファイル名にリネームします。

```bash
cp target/scala-2.10/Graphene-assembly-z.z.z.jar \
   /path/to/myLaViT/lmntal/graphene/graphene.jar
cd ..
```

LaViT は `lmntal/graphene/graphene.jar` を固定で参照するため、ファイル名は必ず `graphene.jar` にしてください。

---

### 完成後のディレクトリ構成

```
myLaViT/
├── LaViT.jar
├── run.sh / run.bat
├── version.txt
├── demo/
└── lmntal/
    ├── bin/
    │   ├── set_cp.sh
    │   ├── lmntal
    │   └── lmntal.jar
    ├── slim-y.y.y/        ← ユーザーが ./configure && make でビルド
    ├── graphene/
    │   └── graphene.jar
    └── unyo.../           ← 旧バンドルから流用
```

---

### リリース方法

> 以下はLMNtalグループが公式リリースを行う際の手順です。

- `version.txt` に変更内容を追記する

- `Env.java` の以下フィールドを書き換える
  ```java
  public static final String APP_VERSION = "x.x.x";
  public static final String APP_DATE    = "YYYY/MM/DD";
  ```

- この時点でちゃんと commit/tag/push をしておく  
  YYYY 年 MM 月 DD 日のコミットでバージョン A.B.C に更新する場合：
  ```
  tag name: A.B.C
  tag message: version A.B.C (YYYY-MM-DD)
  ```

- （SLIM なども更新する場合はバンドル版）バージョン X.Y.Z のリリースは `LaViTX_Y_Z` というディレクトリに入れ、アーカイブ化して公開する。  
  zip ではパーミッションが保存されないため、tar.gz で配布することが望ましい。  
  （LaViT のみの更新の場合は `LaViT-X.Y.Z.jar` という名前で公開する。）

- 現状では `/~shinobu/lavit/releases/` に置いている。  
  バージョンチェックとダウンロードもここを参照している。  
  将来的には、admin (webadmin?) と連携して LaViT リリース用のディレクトリを作るか、どこかのホスティングサービスを使っても良い。



