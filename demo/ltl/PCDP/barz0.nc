never { /* !([]((c -> g) || !r)) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!g && c && r) -> goto accept_all
	fi;
accept_all:
	skip
}

