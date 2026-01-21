never { /* !([](ts1 || t1)) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!t1 && !ts1) -> goto accept_all
	fi;
accept_all:
	skip
}

