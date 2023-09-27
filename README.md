# BioResolve, a logical framework for Reaction Systems

*Reaction Systems (RSs) are a successful computational framework inspired by biological systems. 
A RS pairs a set of entities with a set of reactions over them. 
Entities can be used to enable or inhibit each reaction, and are produced by reactions.<sup>[1]</sup>*

This project is based on [1], and is a more usable and efficient reimplementation of the Prolog's version. 

The program's goal is, given a set of reactions, a context (or multiple parallel) and a possibly empty environment,
to generate a DOT file graph representing all the flow of computation of the results.

The user has thus to provide the mandatory sets of reactions and context(s) in order to start the computation.

Companion [slides](BioResolve.pdf) are included for a faster look at the program's functioning. 

# Structure

The project contains five main classes, four of which are examples based on those provided by 
[https://pages.di.unipi.it/bruni/LTSRS/](https://pages.di.unipi.it/bruni/LTSRS/).

Examples show a typical flow of executing the program programmatically, which is the original way of using the program.

The `BioResolve` main class is, instead, meant to ease the execution of the program by providing a basic (yet functional)
interface where to insert the three sets.

The graphical interface also provides an option to hide or show the intermediate results of the execution, mainly to be 
able to see what is the flow of the various components of the program.

The final result is a *DOT graph file* with a default name of *result.dot*.

# Building and executing

**This instructions require Maven to be installed**

To compile:
```
mvn compile
```

To execute:
```
java -cp target/classes bioresolve.BioResolve
```

A typical graphical transformation command for the DOT file:
```
dot -Tsvg result.dot > result.svg
```

# References

*[1] Linda Brodo, Roberto Bruni, Moreno Falaschi. A logical and graphical framework for reaction systems, Theoretical 
Computer Science 875 (2021) 1–27, [https://doi.org/10.1016/j.tcs.2021.03.024](https://doi.org/10.1016/j.tcs.2021.03.024)*.