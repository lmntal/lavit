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
		spec           [1, 21]
		commit         ["_init", 0]
		loadruleset    [0, @601]
		newatom      [1, 0, 'n'_1]
		newatom      [2, 0, 1_1]
		newatom      [3, 0, 'n'_1]
		newatom      [4, 0, 2_1]
		newatom      [5, 0, 'n'_1]
		newatom      [6, 0, 3_1]
		newatom      [7, 0, 'n'_1]
		newatom      [8, 0, 4_1]
		newatom      [9, 0, 'n'_1]
		newatom      [10, 0, 5_1]
		alloclink    [11, 1, 0]
		alloclink    [12, 2, 0]
		alloclink    [13, 3, 0]
		alloclink    [14, 4, 0]
		alloclink    [15, 5, 0]
		alloclink    [16, 6, 0]
		alloclink    [17, 7, 0]
		alloclink    [18, 8, 0]
		alloclink    [19, 9, 0]
		alloclink    [20, 10, 0]
		unifylinks     [11, 12, 0]
		unifylinks     [13, 14, 0]
		unifylinks     [15, 16, 0]
		unifylinks     [17, 18, 0]
		unifylinks     [19, 20, 0]
		proceed        []


Compiled Ruleset @601 
Compiled Rule 
	--atommatch:
		spec           [2, 5]
	--memmatch:
		spec           [1, 5]
		findatom     [1, 0, 'n'_1]
		findatom     [2, 0, 'n'_1]
		neqatom        [2, 1]
		commit         ["_nAnB", 0]
		removeatom     [1, 0, 'n'_1]
		removeatom     [2, 0, 'n'_1]
		newatom      [3, 0, 'n'_1]
		newatom      [4, 0, '*'_3]
		newlink        [3, 0, 4, 2, 0]
		relink         [4, 0, 1, 0, 0]
		relink         [4, 1, 2, 0, 0]
		freeatom       [1]
		freeatom       [2]
		proceed        []


Inline
