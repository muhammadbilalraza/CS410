import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.util.Scanner;


class FileReader {

    private int nInStates; //number of states in input alphabets
    private int nTapeVar; //number of variables in tape alphabets
    private int nStates; //number of states
    private ArrayList<String> states = new ArrayList<>();  //states
    private String startState; //start state
    private String acceptState; //accept state
    private String rejectState; //reject state
    private String blank; // blank symbol
    private ArrayList<String> tapeAlphabets = new ArrayList<>(); //the tape alphabets
    private ArrayList<Integer> inAlphabet = new ArrayList<>(); //the input alphabet
    private ArrayList<String> tempTTable = new ArrayList<>(); //transition table (just to read)
    private List<ArrayList<String>> tTable = new ArrayList<ArrayList<String>>(); //transition table tuples
    private ArrayList<String> inputs = new ArrayList<>(); // input STRINGS


    private void setArrayListStr (ArrayList<String> list, String str){
        String[] arr = str.split("\\s+");
        for (int i=0; i<arr.length; i++){
            list.add(arr[i]);
        }
    }

    private void setArrayListInt (ArrayList<Integer> list, String str){
        String[] arr = str.split("\\s+");
        for (int i=0; i<arr.length; i++){
            list.add(Integer.parseInt(arr[i]));
        }
    }

    private List<ArrayList<String>> generateTuplesForTT(ArrayList<String> list){
        List<ArrayList<String>> arrayList = new ArrayList<>();
        int lineSize = 5;
        int listCount = list.size()/lineSize;
        int index = 0;
        for (int i=0; i<listCount; i++){
            ArrayList<String> tempArr = new ArrayList<>();
            for (int j=0; j<lineSize; j++, index++)
                tempArr.add(list.get(index));
            arrayList.add(tempArr);
        }

        return arrayList;
    }

    public List<ArrayList<String>> gettTable() {
        return tTable;
    }

    public ArrayList<String> getInputs() {
        return inputs;
    }

    public String getStartState() {
        return startState;
    }

    public String getAcceptState() {
        return acceptState;
    }

    public String getRejectState() {
        return rejectState;
    }

    public String getBlank() {
        return blank;
    }

    public FileReader (File fileObj) {
        try {
            Scanner reader = new Scanner(fileObj);

            nInStates = Integer.parseInt(reader.nextLine());
            nTapeVar = Integer.parseInt(reader.nextLine());
            nStates = Integer.parseInt(reader.nextLine());
            setArrayListStr(states, reader.nextLine());
            startState = reader.nextLine();
            acceptState = reader.nextLine();
            rejectState = reader.nextLine();
            blank = reader.nextLine();
            setArrayListStr(tapeAlphabets, reader.nextLine());
            setArrayListInt(inAlphabet, reader.nextLine());

            String temp = "";
            boolean transitionFlag = true;
            boolean inputFlag = false;

            while (reader.hasNextLine()) {

                temp = reader.nextLine();

                //reading transition table
                if (transitionFlag && inAlphabet.contains(Character.getNumericValue(temp.charAt(0)))) {
                    transitionFlag = false;
                    inputFlag = true;
                } //end if

                if (transitionFlag)
                    setArrayListStr(tempTTable, temp);

                //reading rest of the inputs
                if (inputFlag)
                    setArrayListStr(inputs, temp);

            } //end while

            tTable = generateTuplesForTT(tempTTable);

            reader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found in FileReader.java");
            e.printStackTrace();
        }
    }

    public void printTuples(){
        // printing arrays
        for (int i=0; i<tTable.size(); i++){
            System.out.println("Printing " + i + " tuple...");
            for (int j=0; j<tTable.get(i).size(); j++){
                System.out.print(tTable.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }
}


public class SimulateTM {

    private FileReader fr;
    private ArrayList<String> pathTaken = new ArrayList<>();
    private List<ArrayList<String>> tTable;
    private ArrayList<String> inputs;


        public static void main(String[] args) {

        File input = new File("input.txt");
        SimulateTM simulateTM = new SimulateTM(input);
        simulateTM.simulate();
    }

    public SimulateTM(File file){
        fr = new FileReader(file);
        tTable = fr.gettTable();
        inputs = fr.getInputs();
    }

    private ArrayList<String> getTransition(String currentState, String head){
        for (int i=0; i<tTable.size(); i++)
            if(tTable.get(i).get(0).equals(currentState) && tTable.get(i).get(1).equals(head))
                return tTable.get(i);

        ArrayList<String> transition = new ArrayList<>();
        transition.add("NULL");
        return transition;
    }

    private int moveHead(String direction, int headIndex){
        if (direction.equals("R")) headIndex += 1;
        else if (direction.equals("L")) headIndex -= 1;
        return headIndex;
    }

    public void simulate(){

        try {
            FileWriter fw = new FileWriter("output.txt");

            for (int i=0; i<inputs.size(); i++){

                ArrayList<String> input = new ArrayList<>();

                //make a function
                input.add(fr.getBlank());
                for (int j=0; j<inputs.get(i).length(); j++)
                    input.add(String.valueOf(inputs.get(i).charAt(j)));
                input.add(fr.getBlank());
                //

                String currentState = fr.getStartState();
                pathTaken.clear();
                pathTaken.add(currentState);
                int headIndex = 1;
                ArrayList<String> transition;
                int halting = 0; 

                while (!currentState.equals(fr.getAcceptState()) || !currentState.equals(fr.getRejectState())) {
                    transition = getTransition(currentState, input.get(headIndex));
                    
                    //random value for halting problem
                    //considering the input to be the halting problem if a loop runs 3 times the size of the input string
                    if (halting>input.size()*3){
                        System.out.println("Halting problem");
                        break;
                    }
                    halting++;


                    //if prblem in the transition regardless of the input, there doesn't exist a transition for the head
                    //or the current state to update, we take it as a NULL and breaks the loop.
                    if (transition.equals("NULL")) {
                        System.out.println("No transition found.");
                        break;
                    }

                    //updating the current state to the next state
                    currentState = transition.get(4);
                    //updating path taken/route
                    pathTaken.add(currentState);

                    //if the updated current state is accept or reject state, break the loop.
                    if (currentState.equals(fr.getAcceptState()) || currentState.equals(fr.getRejectState())) break;

                    //updating on the input as per the transition infromation to whatever is to write. 
                    input.set(headIndex, transition.get(2));
                    //updating head index
                    headIndex = moveHead(transition.get(3), headIndex);
                }

                if (currentState.equals(fr.getAcceptState())){
                    System.out.print("Route: ");
                    fw.write("Route\n");
                    for (int k =0; k< pathTaken.size(); k++){
                        System.out.print(pathTaken.get(k) + " ");
                        fw.write(pathTaken.get(k) + " ");
                    }
                    System.out.println("\nResult: Accepted.");
                    fw.write("\nResult: Accepted.\n");
                }
                else if (currentState.equals(fr.getRejectState())){
                    System.out.print("Route: ");
                    fw.write("Route\n");
                    for (int k =0; k< pathTaken.size(); k++) {
                        System.out.print(pathTaken.get(k) + " ");
                        fw.write(pathTaken.get(k) + " ");
                    }
                    System.out.println("\nResult: Rejected.");
                    fw.write("\nResult: Rejected.\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}