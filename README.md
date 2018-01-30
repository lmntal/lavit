
LaViT - LMNtal IDE
===============================

LaViTははプログラミング言語LMNtalのグラフィカルな統合開発環境です。  
バグ報告は lmntal@ueda.info.waseda.ac.jp までお願いします。  
LMNtal公開ページからダウンロード可能です。  

---

## Links
- [LaViT公開ページ](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/ "LaViT")
- [LMNtalチュートリアル](http://www.ueda.info.waseda.ac.jp/lmntal/lavit/index.php?LMNtal%E3%83%81%E3%83%A5%E3%83%BC%E3%83%88%E3%83%AA%E3%82%A2%E3%83%AB "LMNtalチュートリアル")

## Getting started

## Develop

### リリース方法
- version.txt に変更内容を追記

- Env.java のフィールドを書き換える

- この時点でちゃんと commit/tag/push をしておく  
YYYY 年 MM 月 DD 日のコミットでバージョン A.B.C に更新する場合，  
tag name: A.B.C  
tag message: version A.B.C (YYYY-MM-DD)  

-（SLIM なども更新する場合はバンドル版）バージョン X.Y.Z のリリースはLaViTX_Y_Z というディレクトリに入れ、アーカイブ化して公開する。  
今までtar.gz は用意していなかったが、zip ではパーミッションが保存されないために問題が生じることがあるので、tar.gzも用意したい。．  
（LaViT のみの更新LaViT-X.Y.Z.jarという名前で公開する。  

- 現状では /~shinobu/lavit/releases/ に置いている__
バージョンチェックとダウンロードもここを参照している。  
将来的には、admin (webadmin?) と連携して LaViT リリース用のディレクトリを作るか、（今までは google code でやっていたが）どこかのホスティングサービスを使っても良い。  



