never { /* !([]!z) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (z) -> goto accept_all
	fi;
accept_all:
	skip
}

