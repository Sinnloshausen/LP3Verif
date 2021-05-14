# LP3Verif
LP3Verif is an open-source model checking tool for verification of location privacy properties of location privacy-preserving mechanisms (LPPM).

Location privacy-preserving mechanisms (LPPM) use different computational approaches to protect personal data in queries to location-based services.
These LPPMs give different privacy claims that are not per se comparable.
We propose the sigma-calculus, a process calculus to model LPPMs and their privacy guarantees.
LP3Verif is a model checking tool to verify location privacy properties of LPPMs using the sigma-calculus. LP3Verif is written in Java and uses the SMT solver CVC4 and can be executed from the command line to verify LPPM protocols specified in .lp3 syntax.

## Installation and Setup
* Download CVC4 (https://cvc4.github.io/downloads.html) and if necessary follow the installation instructions from the user manual (http://cvc4.cs.stanford.edu/wiki/User_Manual).
* Download the zip file from the latest release and unzip in target directory.
* In the unzipped LP3Verif folder edit the *tool.config* file with the editor of your choice.
* Edit the solver path to the absolute path where your CVC4 binaries are located. If necessary edit the name of the solver, e.g., "cvc4.exe" for windows.
* Then edit the smt2 path and name to a file of your choice. This file will be used by the smt solver and can contain additional tracing/debug information for LP3Verif.
* Depending on your operating system you can now create a shell/batch script to start the executable .jar file:
  * The command _java -jar "LP3Verif.jar"_ starts LP3Verif in interactive mode where a command line promt will ask for a path to an .lp3 file.
  * Calling LP3Verif with the additional argument *-lp3 path/file.lp3* directly starts the verification of the specified .lp3 file.
