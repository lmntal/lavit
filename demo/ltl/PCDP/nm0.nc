never { /* !([]((t1 ||ts1) && !t2)) */
T0_init:
	if
	:: (1) -> goto T0_init
	:: (!t1 && !ts1) || (t2) -> goto accept_all
	fi;
accept_all:
	skip
}

