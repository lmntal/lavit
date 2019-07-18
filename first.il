Compiled Ruleset @602 
Compiled Rule 
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 1]
		jump           [L143, [0], [], []]
	--guard:L143:
		spec           [1, 1]
		jump           [L136, [0], [], []]
	--body:L136:
		spec           [1, 11]
		commit         ["_init", 0]
		loadruleset    [0, @601]
		newatom      [1, 0, 'n'_1]
		newatom      [2, 0, 'integer'.'set'_3]
		newatom      [3, 0, 2_1]
		newatom      [4, 0, 50_1]
		alloclink    [5, 1, 0]
		alloclink    [6, 2, 0]
		alloclink    [7, 2, 1]
		alloclink    [8, 2, 2]
		alloclink    [9, 3, 0]
		alloclink    [10, 4, 0]
		unifylinks     [5, 8, 0]
		unifylinks     [6, 9, 0]
		unifylinks     [7, 10, 0]
		loadmodule     [0, "integer"]
		proceed        []


Compiled Ruleset @601 
Compiled Rule 
	--atommatch:
		spec           [2, 5]
	--memmatch:
		spec           [1, 9]
		findatom     [1, 0, 'n'_1]
		findatom     [2, 0, 'n'_1]
		neqatom        [2, 1]
		derefatom    [4, 2, 0]
		isint          [4]
		derefatom    [3, 1, 0]
		isint          [3]
		imod         [5, 3, 4]
		allocatom    [6, 0_1]
		ieq            [5, 6]
		commit         ["_nAnB", 0]
		removeatom     [1, 0, 'n'_1]
		removeatom     [2, 0, 'n'_1]
		removeatom     [3, 0]
		removeatom     [4, 0]
		copyatom     [7, 0, 4]
		newatom      [8, 0, 'n'_1]
		newlink        [8, 0, 7, 0, 0]
		freeatom       [1]
		freeatom       [2]
		freeatom       [5]
		freeatom       [6]
		freeatom       [3]
		freeatom       [4]
		proceed        []


Inline
