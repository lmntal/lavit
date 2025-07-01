#!/usr/bin/env python3

import sys
import re
import json
import argparse
from typing import Dict, List, Tuple

Transitions = Dict[int, List[Tuple[int, str, int]]]
Weights = Dict[str, float]
ProbTransitions = List[Tuple[int, int, float]]
MdpTransitions = list[Tuple[int, int, int, float]]

MdpTransitionGraph = Dict[int, List[Tuple[int, float]]]

def round_sig_4(num: float) -> str:
    from decimal import Decimal, ROUND_HALF_UP
    import math

    if num == 0:
        return "0"

    # Decimalに変換（精度を保つため文字列経由）
    dnum = Decimal(str(num))

    # 有効数字4桁に対応する指数を求めて丸める
    exponent = -int(math.floor(math.log10(abs(num)))) + 3
    rounded = dnum.quantize(Decimal("1e-" + str(exponent)), rounding=ROUND_HALF_UP)

    # 不要な0や小数点を除いて整形
    result = format(rounded.normalize(), "f").rstrip("0").rstrip(".")
    return result


def parse_transitions(lines: list[str]) -> Transitions:
    """
    遷移情報を含むテキスト行から遷移データを解析します。

    Args:
        lines (list[str]): 遷移情報を含むテキスト行のリスト

    Returns:
        Transitions: 遷移データの辞書。キーは開始状態、値は(終了状態, ルール名, マッチ数)のタプルのリスト
    """
    transitions: Transitions = {}
    state_pattern = r"(\d+)::(.+)"

    # Find the start of transitions section
    start_idx = lines.index("Transitions\n")

    # Skip 'init:1' line
    current_line = start_idx + 2

    while current_line < len(lines) and "::" in lines[current_line]:
        line = lines[current_line].strip()
        if not line:
            break

        match = re.match(state_pattern, line)
        if match:
            from_state = int(match.group(1))
            trans_str = match.group(2)

            # Parse transitions
            if trans_str:
                # Split multiple transitions
                trans_list = trans_str.split(",")
                transitions[from_state] = []

                for trans in trans_list:
                    # Extract destination state and match_counts
                    match = re.match(r"(\d+)\((.*)\)", trans)
                    if match:
                        to_state = int(match.group(1))
                        match_counts = match.group(2).split()
                        rule_name = match_counts[0] if match_counts else ""
                        transitions[from_state].append(
                            (to_state, rule_name, len(match_counts))
                        )

        current_line += 1

    return transitions


def calculate_probabilities(
    transitions: Transitions, weights: Weights
) -> ProbTransitions:
    """
    遷移データと重みから遷移確率を計算します。

    Args:
        transitions (Transitions): parse_transitions()で生成された遷移データ
        weights (Weights): 各ルール名に対する重みの辞書

    Returns:
        ProbTransitions: (開始状態, 終了状態, 確率)のタプルのリスト
    """
    prob_transitions: ProbTransitions = []

    for from_state, to_states in transitions.items():
        if not to_states:
            continue

        # Calculate rule match count
        rule_name_to_match_count = {}
        for to_state, rule_name, match_counts in to_states:
            if rule_name not in rule_name_to_match_count:
                rule_name_to_match_count[rule_name] = 0
            rule_name_to_match_count[rule_name] += match_counts

        # Calculate weight sum
        total_weight = 0
        for rule_name, match_counts in rule_name_to_match_count.items():
            total_weight += weights.get(rule_name, 1) * match_counts

        # Calculate probability for each transition
        for to_state, rule_name, match_counts in to_states:
            prob = (weights.get(rule_name, 1) * match_counts) / (total_weight)
            prob_transitions.append((from_state - 1, to_state - 1, prob))

    return prob_transitions

def transform_to_mdp_format(prob_transitions: ProbTransitions) -> ProbTransitions:
    # Count number of states and transitions
    num_states = max(
        max(t[0] for t in prob_transitions) + 1, max(t[1] for t in prob_transitions) + 1
    )
    
    # MDPの出力形式に変換する処理を追加
    transition_graph: MdpTransitionGraph = {}
    for from_state, to_state, prob in prob_transitions:
        if from_state not in transition_graph:
            transition_graph[from_state] = []
        transition_graph[from_state].append((to_state, prob))
        
    # MDP 
    mdp_transitions: MdpTransitions = []
    
    seen = [False] * num_states
    state_set = set()
    # state 0 から bfs で探索して、mdp を構築
    queue = [0]
    while queue:
        current_state = queue.pop(0)
        state_set.add(current_state)
        seen[current_state] = True
        for idx, (next_action_state, _) in enumerate(transition_graph.get(current_state, [])):
            for next_state, prob in transition_graph.get(next_action_state, []):
                mdp_transitions.append((current_state, idx, next_state, prob))
                if not seen[next_state]:
                    queue.append(next_state)

    # state_set が {0, 3, 4, 5} の場合、0->0, 3->1, 4->2, 5->3 のように、連続した整数に変換
    state_map = {state: idx for idx, state in enumerate(sorted(state_set))}
    for i in range(len(mdp_transitions)):
        from_state, action, to_state, prob = mdp_transitions[i]
        mdp_transitions[i] = (state_map[from_state], action, state_map[to_state], prob)

    return mdp_transitions
            

def write_output(prob_transitions: ProbTransitions, model_type: str, output_file: str = None) -> None:
    """
    遷移確率データを指定された形式で出力します。

    Args:
        prob_transitions (ProbTransitions): calculate_probabilities()で生成された遷移確率データ
        output_file (str, optional): 出力先ファイルパス。指定がない場合は標準出力に出力
        model_type (str): モデルの種類 ("dtmc"または"mdp")
    """
    if model_type == "dtmc":
        write_output_dtmc(prob_transitions, output_file)
    elif model_type == "mdp":
        write_output_mdp(prob_transitions, output_file)
    else:
        print(f"Error: Unsupported model type '{model_type}'")
        sys.exit(1)
    

def write_output_dtmc(prob_transitions: ProbTransitions, output_file: str = None) -> None:
    """
    遷移確率データを指定された形式で出力します。

    Args:
        prob_transitions (ProbTransitions): calculate_probabilities()で生成された遷移確率データ
        output_file (str, optional): 出力先ファイルパス。指定がない場合は標準出力に出力
    """
    # Count number of states and transitions
    num_states = max(
        max(t[0] for t in prob_transitions) + 1, max(t[1] for t in prob_transitions) + 1
    )
    num_transitions = len(prob_transitions)

    # Prepare output
    output_lines = []
    output_lines.append(f"{num_states} {num_transitions}")
    for from_state, to_state, prob in sorted(prob_transitions):
        prob_str = round_sig_4(prob)
        output_lines.append(f"{from_state} {to_state} {prob_str}")

    # Write output to file or stdout
    if output_file:
        try:
            with open(output_file, "w") as f:
                f.write("\n".join(output_lines) + "\n")
        except IOError:
            print(f"Error: Could not write to output file '{output_file}'")
            sys.exit(1)
    else:
        print("\n".join(output_lines))

def write_output_mdp(prob_transitions: MdpTransitions, output_file: str = None) -> None:
    """
    MDPの遷移確率データを指定された形式で出力します。

    Args:
        prob_transitions (MdpTransitions): calculate_probabilities()で生成された遷移確率データ
        output_file (str, optional): 出力先ファイルパス。指定がない場合は標準出力に出力
    """
    
    # Count number of states and transitions
    num_states = max(
        max(t[0] for t in prob_transitions) + 1, max(t[1] for t in prob_transitions) + 1
    )
    # prob_transitions の from_state, action の組のユニークな数
    choices_set = set()
    for from_state, action, _, _ in prob_transitions:
        choices_set.add((from_state, action))
    num_choices = len(choices_set)
    num_transitions = len(prob_transitions)
    
    # Prepare output
    output_lines = []
    output_lines.append(f"{num_states} {num_choices} {num_transitions}")
    for from_state, action, to_state, prob in sorted(prob_transitions):
        prob_str = round_sig_4(prob)
        output_lines.append(f"{from_state} {action} {to_state} {prob_str}")
    # Write output to file or stdout
    if output_file:
        try:
            with open(output_file, "w") as f:
                f.write("\n".join(output_lines) + "\n")
        except IOError:
            print(f"Error: Could not write to output file '{output_file}'")
            sys.exit(1)
    else:
        print("\n".join(output_lines))
    

def main():
    """
    コマンドライン引数を解析し、slim の実行結果(および、各ルールの重み情報)から
    確率付き状態遷移の生成を行います。

    usage:
        python3 translator.py <slim_output_file> [-t <model_type>] [-w <weight_file>] [-o <output_file>]

    options:
        -t, --model_type: モデルを指定(e.g. dtmc, mdp)
        -w, --weight: 各ルールの重みを定義したJSONファイル(オプション)
        -o, --output: 出力先ファイルパス(オプション、指定がない場合は標準出力)
    """
    # Parse command line arguments
    parser = argparse.ArgumentParser(
        description="Translate transitions to probabilities"
    )
    parser.add_argument("filename", help="Input file containing transitions")
    parser.add_argument("-t", "--model_type", choices=["dtmc", "mdp"], help="Model type")
    parser.add_argument("-w", "--weight", help="JSON file containing weights")
    parser.add_argument("-o", "--output", help="Output file (default: stdout)")
    args = parser.parse_args()

    # Read input from file
    try:
        with open(args.filename, "r") as f:
            lines = f.readlines()
    except FileNotFoundError:
        print(f"Error: File '{args.filename}' not found")
        sys.exit(1)

    # Read weights from JSON file if provided
    weights = {}
    if args.weight:
        try:
            with open(args.weight, "r") as f:
                weights = json.load(f)["weights"]
        except FileNotFoundError:
            print(f"Error: Weight file '{args.weight}' not found")
            sys.exit(1)
        except json.JSONDecodeError:
            print(f"Error: Invalid JSON format in '{args.weight}'")
            sys.exit(1)

    # Parse transitions
    transitions = parse_transitions(lines)

    # Calculate probabilities
    transitions = calculate_probabilities(transitions, weights)

    if args.model_type == "mdp":
        transitions: MdpTransitions = transform_to_mdp_format(transitions)
    
    # Write output    
    write_output(transitions, args.model_type, args.output)

if __name__ == "__main__":
    main()
