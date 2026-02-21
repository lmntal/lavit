never { /* !([]!p && [](q -> <>c)) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!c && q) -> goto accept_S2
	:: (p) -> goto accept_all
	fi;
accept_S2:
	if
	:: (!c) -> goto accept_S2
	fi;
accept_all:
	skip
}

