never { /* ![]<>n5 */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!n5) -> goto accept_S2
	fi;
accept_S2:
	if
	:: (!n5) -> goto accept_S2
	fi;
}


