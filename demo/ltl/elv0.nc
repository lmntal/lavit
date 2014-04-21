never { /* !(!<>(([]!ep)&&<>m)) */
T1_init:
	if
	:: (1) -> goto T1_init
	:: (!ep) -> goto T0_S2
	:: (m && !ep) -> goto accept_S3
	fi;
T0_S2:
	if
	:: (!ep) -> goto T0_S2
	:: (m && !ep) -> goto accept_S3
	fi;
accept_S3:
	if
	:: (!ep) -> goto accept_S3
	fi;
}

