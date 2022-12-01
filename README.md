# CS410 - Automata Theory and Formal Languages

This repository contains projects that are assigned as a course work for CS410 at Özyeğin University, Istnabul. 

# Projects

There will be three projects in the repository. 

 - [Project 1: NFA to DFA Conversion](https://github.com/muhammadbilalraza/CS410/tree/main/Project1-Impl/)
 - Project 2: Converting a Context-Free Grammer (CFG) to Chowmsky Normal Form (CNF) (Will be uploaded after the deadline on LMS)
 - Project 3: (Yet to be assigned)

## [Project 1: NFA to DFA Conversion](https://github.com/muhammadbilalraza/CS410/tree/main/Project1-Impl/)

The project implements 3 main files: 

 1. [FileReader.java](https://github.com/muhammadbilalraza/CS410/tree/main/Project1-Impl/src/FileReader.java/)
 2. [SimulateNFAtoDFAConversion.java](https://github.com/muhammadbilalraza/CS410/tree/main/Project1-Impl/src/SimulateNFAtoDFAConversion.java/)
 3. [Main.java](https://github.com/muhammadbilalraza/CS410/tree/main/Project1-Impl/src/Main.java/)

**Approach**

Main class is used only to pass the file name to the SimulateNFAtoDFAConversion class which invokes the FileReader class. The FileReader class reads the given (in a specific format) txt file and loads information into relevant datatypes. 

The next step is to find similar transitions that are required to form NFA. For example, **A 1 A** and **A 1 B** should form a new transition as **A 1 AB**. This is done by using the function *findNewTransitions*. Here as the function finds a new state as the third element of the new transition. **AB**, it stores it in the `ArrayList<String>` of `newStates`. If the state already exists in the list the function, simply prevents rewriting. The next step is to form NFA for which I have created a *formNFA()* , the function first generates a transition table for the input transitions as well as the newly found in the previous step using a helper
function called *generateTuplesForTT*. This function takes the `ArrayList<String>` of transitions and mimics the transition table as we generally write it.

0 -> 0: A = (index) 1: 0, (index) 2: A

1 -> 1: B = (index) 1: 1, (index) 2: B

…..

This function forms NFA by comparing the input transition table with the transitions found using *findNewTransitions* function. It compares the input transitions with the third element in every tuple for newly found transitions. If the match is found, the function ignores the transitions from the input file and writes the newly formed to a temporary list. If there exists a transition in the input file that does not form any new transition, the function adds that to the temporary list as it. 

The next step is to form a DFA, for this I have written *formDFA()* method. The function iterates as many times as the size of the newly formed states. It takes a union of transitions to form a transition for the DFA. For example, **ABC** is formed as a new state from the transitions **A 1 B** and **A 1 AC** so the function unions these two transitions to form A 1 ABC and add these to the *newTransitionList*.

The next step is to write to the file and print to the console which is very straight forward since all the relevant information is stored in a proper format. For writing to the output file, I have used FileWriter and for printing tuples to the console I have written a extra helper function. The new final states are determined while writing to the file. It just checks if the final state symbol given as the input is found in the newly formed states and add those states to the new final states as well.
