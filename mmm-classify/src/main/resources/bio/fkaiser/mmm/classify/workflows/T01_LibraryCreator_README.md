**last run on %DATE% by %USER%**

This task creates a library of structural motif using MMM. Only the first _n_ most populated superfamilies are considered.

Required inputs are:

- `args[0]` the output path to which the results are written
- `args[1]` the desired _n_ most populated superfamilies to be considered
- `args[2]` the path to CATH domain database where all domain structures are deposited in separate directories based on their superfamily code (e.g. `3/4/50/300/*.pdb` for superfamily 3.4.50.300)
