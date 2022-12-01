import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SimulateNFAtoDFAConversion {

    private FileReader fr;
    private ArrayList<String> newTransitionList; //transition list for DFA
    private List<ArrayList<String>> DFAtTable; //transition table for DFA
    private List<ArrayList<String>> NFAtTable; //transition table for NFA
    private ArrayList<String> newStates; //list of new states introduced
    private ArrayList<String> newFinalStates; //list of final states including newly introduced states

    public SimulateNFAtoDFAConversion (File file) {

        newTransitionList = new ArrayList<>();
        newStates = new ArrayList<>();
        DFAtTable = new ArrayList<>();
        NFAtTable = new ArrayList<>();
        newFinalStates = new ArrayList<>();

        fr = new FileReader(file);

        //getting first final state (as in input file)
        newFinalStates.add(fr.getFinalState());

        newTransitionList = formNFA();
        NFAtTable = generateTuplesForTT(newTransitionList);
        formDFA();
        DFAtTable = generateTuplesForTT(newTransitionList);
        writeDFAToFile();
        printDFA();
    }

    //this function go through the list transition list and
    //by comparing two transitions, separates new transitions
    //that needs to form NFA. The newly formed states are also
    //being stored in an ArrayList of String.
    private ArrayList<String> findNewTransitions() {

        ArrayList<String> list = fr.getTransitionTableList();

        String transition;
        ArrayList<String> newTransition = new ArrayList<>();

        String firstElement, secondElement;
        String thirdElement, fourthElement;

        int firstIndex = 0, secondIndex = firstIndex + 1;
        int thirdIndex = firstIndex + 3, fourthIndex = thirdIndex + 1;

        for (int i=0; i<list.size()-3; i+=3) {
            firstElement = list.get(firstIndex);
            secondElement = list.get(secondIndex);
            thirdElement = list.get(thirdIndex);
            fourthElement = list.get(fourthIndex);

            if (firstElement.equals(thirdElement) && secondElement.equals(fourthElement)){
                transition = firstElement + " " + secondElement + " " + list.get(secondIndex+1).concat(list.get(fourthIndex+1));
                fr.setArrayListStr(newTransition,transition);

                if(!newStates.contains(transition.substring(4)))
                    newStates.add(transition.substring(4));
            }

            firstIndex+=3;
            secondIndex+=3;
            thirdIndex+=3;
            fourthIndex+=3;
        }

        return newTransition;
    }

    //helper function for generating tuples from tranisition list
    private List<ArrayList<String>> generateTuplesForTT(ArrayList<String> list){

        List<ArrayList<String>> arrayList = new ArrayList<>();

        int lineSize = 3; //characters on each line of the transition table, hard set
        int listCount = list.size()/lineSize; //no. of tuples
        int index = 0;

        for (int i=0; i<listCount; i++){
            ArrayList<String> tempArr = new ArrayList<>();
            for (int j=0; j<lineSize; j++, index++)
                tempArr.add(list.get(index));
            arrayList.add(tempArr);
        }

        return arrayList;
    }

    //This function forms NFA by comparing the input transition table with the
    //transitions found using findNewTransitions function. It compares the input
    //transitions with the third element in every tuple for newly found transitions.
    //If the match is found, the function ignores the transitions from the input file
    //and write the newly formed to a temporary list. If there exist a transition in
    //the input file that do not form any new transition, the function adds that to
    //the temporary list as it.
    private ArrayList<String> formNFA() {

        List<ArrayList<String>> originalTTable = generateTuplesForTT(fr.getTransitionTableList());
        List<ArrayList<String>> transitionsToAdd = generateTuplesForTT(findNewTransitions());

        ArrayList<String> tempList = new ArrayList<>();


        int j = 0;
        for (int i=0; i < originalTTable.size(); i++){
            //A 0 A
            //A 1 B
            //concat: AB
            if (i < originalTTable.size()-1  && j< transitionsToAdd.size()) {
                String concat = originalTTable.get(i).get(2).concat(originalTTable.get(i + 1).get(2));
                if (concat.equals(transitionsToAdd.get(j).get(2))) {
                    for (int k=0; k<3; k++)
                        tempList.add(transitionsToAdd.get(j).get(k));
                    j++;
                    i++;
                }
                else {
                    for (int k=0; k<3; k++)
                        tempList.add(originalTTable.get(i).get(k));
                }
            }
            else {
                for (int k=0; k<3; k++)
                    tempList.add(originalTTable.get(i).get(k));
            }
        }
        return tempList;
    }


    //This function forms the DFA, utilizing the new transition list & NFA transition table.
    //The function unions the transitions by considering the newly formed states and their
    //symbols to already present transitions in the NFA table.
    private void formDFA (){

        String currentState;
        ArrayList<Character> charsToCompare = new ArrayList<>();

        for (int i=0; i < newStates.size(); i++){
            currentState = newStates.get(i);

            for (int j=0; j<currentState.length(); j++){
                charsToCompare.add(currentState.charAt(j));
            }

            String temp1, temp2 = "";
            //ALPHABETS: 0 1
            for (int alphabet=0; alphabet < fr.getAlphabets().size(); alphabet++){

                for (int ch = 0; ch<charsToCompare.size(); ch++){
                    temp1 = getTransitionFromNFAtTable(charsToCompare.get(ch).toString(), fr.getAlphabets().get(alphabet));

                    if (!temp1.equals("NONE")) {
                        temp2 += temp1;
                        temp2 = removeDuplicate(temp2);
                    }

                    if(!newStates.contains(temp2) && !fr.getStates().contains(temp2) && temp2!="")
                        newStates.add(temp2);
                }
                fr.setArrayListStr(newTransitionList,currentState + " " + alphabet + " " + temp2);
                temp2 = "";
            }
            charsToCompare.clear();
        }
    }


    //Helper function for finding transition needs to form DFA
    private String getTransitionFromNFAtTable(String characterToCompare, String alphabetToCompare) {

        for (int i = 0; i < NFAtTable.size(); i++) {

            if (NFAtTable.get(i).get(0).equals(characterToCompare)) {
                if (NFAtTable.get(i).get(1).equals(alphabetToCompare)) {
                    return NFAtTable.get(i).get(2); //return this
                }
            }
        }
        return "NONE";
    }

    //This function is taken from geekforgeeks.com
    //The purpose for using it to remove any duplicates formed
    //For example: AB + BC will result in ABBC while we only need ABC
    private String removeDuplicate(String str) {
        char temp[] = str.toCharArray();
        Arrays.sort(temp);

        int index = 0;
        for (int i = 0; i < temp.length; i++) {
            int j;
            for (j = 0; j < i; j++) {
                if (temp[i] == temp[j])
                    break;
            }

            if (j == i) {
                temp[index++] = temp[i];
            }
        }
        return String.valueOf(Arrays.copyOf(temp, index));
    }


    private void printTuples(List<ArrayList<String>> tTable){

        // printing arrays
        for (int i=0; i<tTable.size(); i++){
//            System.out.println("Printing " + i + " tuple...");
            for (int j=0; j<tTable.get(i).size(); j++){
                System.out.print(tTable.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    private void writeDFAToFile(){
        try {
            FileWriter fileWriter = new FileWriter("output.txt");

            fileWriter.write("ALPHABET\n");
            for (int i=0; i<fr.getAlphabets().size(); i++)
                fileWriter.write(fr.getAlphabets().get(i)+"\n");

            fileWriter.write("STATES\n");
            for (int i=0; i<fr.getStates().size(); i++)
                fileWriter.write(fr.getStates().get(i)+"\n");
            for (int i=0; i<newStates.size(); i++)
                fileWriter.write(newStates.get(i)+"\n");

            fileWriter.write("START\n");
                fileWriter.write(fr.getStartState()+"\n");

            fileWriter.write("FINAL\n");
            //adding new final states to final states list
            for (int i=0; i<newStates.size(); i++)
                if(newStates.get(i).contains(fr.getFinalState()))
                    newFinalStates.add(newStates.get(i));

            for (int i=0; i<newFinalStates.size(); i++)
                fileWriter.write(newFinalStates.get(i)+"\n");

            fileWriter.write("TRANSITIONS\n");
            for (int i=0; i<DFAtTable.size(); i++)
                for (int j=0; j<DFAtTable.get(i).size(); j++)
                    fileWriter.write(DFAtTable.get(i).get(j) + " ");
                fileWriter.write("\n");

            fileWriter.write("END");

            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void printDFA(){

        System.out.print("ALPHABET\n");
        for (int i=0; i<fr.getAlphabets().size(); i++)
            System.out.print(fr.getAlphabets().get(i)+"\n");

        System.out.print("STATES\n");
        for (int i=0; i<fr.getStates().size(); i++)
            System.out.print(fr.getStates().get(i)+"\n");
        for (int i=0; i<newStates.size(); i++)
            System.out.print(newStates.get(i)+"\n");

        System.out.print("START\n");
        System.out.print(fr.getStartState()+"\n");

        System.out.print("FINAL\n");
        for (int i=0; i<newFinalStates.size(); i++)
            System.out.print(newFinalStates.get(i)+"\n");

        System.out.print("TRANSITIONS\n");
        printTuples(DFAtTable);

        System.out.print("END");

    }

}
