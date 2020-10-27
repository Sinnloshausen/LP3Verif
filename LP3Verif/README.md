This repository contains a work-in-progress implementation of a temporal-epistemic
logic for an applied pi calculus.
The current state is based on the calculus and the logic descibed in a paper by
Chadra, Delaune, and Kremer (Epistemic Logic for the Applied Pi Calculus) from
2009.

The calculus and the logic will be transformed to another one (to be described
in an upcoming paper).

The Verifier.java file in the model checking package contains the method "verify"
which is the main function of the verfication and calls all the necessary methods.
It follows a model checking algorithm as described in the model_checking.pdf file.

The processCalculus package contains all classes and methods for the language of
the applied pi calculus described in above mentioned paper, while the properties
package contains all classes and methods needed for the propoerty formulas also
described in the paper.

The Test.java file in the main package contains the main function that executes
some verification of hard-coded calculi and print some results into the console.

DISCLAIMER:
- the current functionalities of the verifier are not maintained
- some known bugs exist and will not be fixed
    -> the project will be changed to model a different type of process calculus
       and different types of property formulas