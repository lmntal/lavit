/*
 *   Copyright (c) 2025, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.pmc;

import java.awt.BorderLayout;

import javax.swing.JPanel;

// TODO: 1. SLIM 実行
// slim -t --nd --hl --use-builtin-rule --show-transition <model_name>.lmn で実行し，
// 実行結果を <model_name>_slim.txt に保存する．

// TODO: 2. weights.json の作成
// パネル内で，各ルールに weight を設定できるようにする．
// 設定した weight を <model_name>_weights.json として保存する．

// TODO: 3. translator 実行
// <path_to_translator> <slim_output>.txt --model_type dtmc --weight <weights.json> --output <model_name>_dtmc.tra
// <model_name>_dtmc.tra を保存する．

// TODO: 4. predicates.pctl 等の作成
// パネル内で，PCTL 等を入力できるようにする．
// predicates.pctl に保存する．
// その他，ラベル付けファイルや，状態への報酬設定など．

// TODO: 5. PRISM 実行
// e.g. prism -importmodel '<model_name>_dtmc.tra,srew' predicates.pctl
// 実行結果を表示する．

// TODO: 6. State Viewer
// StateViewer 上で各遷移について実際に割り当てられる確率を表示できるようにする．

public class PmcPanel extends JPanel {
  public PmcPanel() {
    setLayout(new BorderLayout());
  }
}
