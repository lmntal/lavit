Compiled Ruleset @602 
Compiled Rule 
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 1]
		jump           [L160, [0], [], []]
	--guard:L160:
		spec           [1, 1]
		jump           [L153, [0], [], []]
	--body:L153:
		spec           [1, 4]
		commit         ["_init", 0]
		loadruleset    [0, @601]
		newatom      [1, 0, 'a'_0]
		newatom      [2, 0, 'b'_0]
		newatom      [3, 0, 'c'_0]
		proceed        []


Compiled Ruleset @601  
Compiled Rule 
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 3]
		findatom     [1, 0, 'a'_0]
		commit         ["_ab", 0]
		removeatom     [1, 0, 'a'_0]
		newatom      [2, 0, 'b'_0]
		freeatom       [1]
		proceed        []

Compiled Rule 
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 3]
		findatom     [1, 0, 'b'_0]
		commit         ["_bd", 0]
		removeatom     [1, 0, 'b'_0]
		newatom      [2, 0, 'd'_0]
		freeatom       [1]
		proceed        []


Inline
