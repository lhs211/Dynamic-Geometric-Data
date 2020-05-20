# guardian-dominator-implementation
GuardianArchive is an implementation of the DynamicArchive interface which is for dynamic unbounded non-elitist archives to be used to keep track of solutions in a multi objective optimisation problem.

# Usage
The class experiment contains a main method where simulations against the archive can be ran. The default is to run one experiment which has an elite set of 512, a distribution of c=1, two dimensions, and guardian assignment method c1. This will average the results over 10 runs, for each simulation, storing the results in "results.dat". To instead test the list archive, the lines 265/266 should be modified in the experiment class.
