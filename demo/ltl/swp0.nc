never { /* ![]( s -> <>a ) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!a && s) -> goto accept_S2
	fi;
accept_S2:
	if
	:: (!a) -> goto accept_S2
	fi;
}

