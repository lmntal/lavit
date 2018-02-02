Compiled Ruleset @601 
Compiled Rule 
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 1]
		jump           [L110, [0], [], []]
	--guard:L110:
		spec           [1, 1]
		jump           [L103, [0], [], []]
	--body:L103:
		spec           [1, 19]
		commit         ["_init", 0]
		newatom      [1, 0, 'a'.'b'_1]
		newatom      [2, 0, 'c'_0]
		newatom      [3, 0, 'b'.'b'_1]
		newatom      [4, 0, 'c'_0]
		newatom      [5, 0, 'list'_1]
		newatom      [6, 0, '.'_3]
		newatom      [7, 0, '.'_3]
		newatom      [8, 0, '[]'_1]
		alloclink    [9, 1, 0]
		alloclink    [10, 3, 0]
		alloclink    [11, 5, 0]
		alloclink    [12, 6, 0]
		alloclink    [13, 6, 1]
		alloclink    [14, 6, 2]
		alloclink    [15, 7, 0]
		alloclink    [16, 7, 1]
		alloclink    [17, 7, 2]
		alloclink    [18, 8, 0]
		unifylinks     [9, 12, 0]
		unifylinks     [10, 15, 0]
		unifylinks     [11, 14, 0]
		unifylinks     [13, 17, 0]
		unifylinks     [16, 18, 0]
		loadmodule     [0, "a"]
		loadmodule     [0, "b"]
		proceed        []


Inline
