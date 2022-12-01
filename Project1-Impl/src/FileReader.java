import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;

public class FileReader {

    private int noOfAlphabets;
    private int noOfStates;
    private String startState;
    private String finalState;
    private ArrayList<String> states = new ArrayList<>();
    private ArrayList<String> alphabets = new ArrayList<>();
    private ArrayList<String> transitionTableList = new ArrayList<>(); //just to read from the txt file
    private List<ArrayList<String>> tTable = new ArrayList<ArrayList<String>>(); //transition table tuples


    public FileReader() {}


    public FileReader(File fileObj) {

        String tempStr = "";
        boolean alphabetsFlag = false, statesFlag = false, startFlag = false, finalFlag = false, transitionsFlag = false, endFlag = false;

        try {
            Scanner scanner = new Scanner(fileObj);
            scanner.nextLine();
            alphabetsFlag = true;

            while (endFlag != true) {

                //reading alphabets
                while (alphabetsFlag) {
                    tempStr = scanner.nextLine();
                    if (tempStr.equals("STATES")) {
                        alphabetsFlag = false;
                        statesFlag = true;
                    } else {
                        alphabets.add(tempStr);
                        noOfAlphabets++;
//                        System.out.println(tempStr);
                    }
                }

                //reading states
                while (statesFlag) {
                    tempStr = scanner.nextLine();
                    if (tempStr.equals("START")) {
                        statesFlag = false;
                        startFlag = true;
                    }
                    else {
                        states.add(tempStr);
                        noOfStates++;
//                        System.out.println(tempStr);
                    }
                }

                //reading start state
                while (startFlag) {
                    tempStr = scanner.nextLine();
                    if (tempStr.equals("FINAL")) {
                        startFlag = false;
                        finalFlag = true;
                    }
                    else {
                        startState = tempStr;
//                        System.out.println(tempStr);
                    }
                }

                //reading final state
                while (finalFlag) {
                    tempStr = scanner.nextLine();
                    if (tempStr.equals("TRANSITIONS")) {
                        finalFlag = false;
                        transitionsFlag = true;
                    }
                    else {
                        finalState = tempStr;
//                        System.out.println(tempStr);
                    }
                }

                //reading transition table for NFA
                while (transitionsFlag) {
                    tempStr = scanner.nextLine();
                    if (tempStr.equals("END")) {
                        transitionsFlag = false;
                        endFlag = true;
                    }
                    else {
                        setArrayListStr(transitionTableList, tempStr);
//                        System.out.println(tempStr);
                    }
                }
            } //eof

        } //end of try block
        catch (FileNotFoundException e) {
            System.out.println("File not found in FileReader.java");
            e.printStackTrace();
        }
    }


    //getters
    public String getStartState() {
        return startState;
    }

    public String getFinalState() {
        return finalState;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public List<ArrayList<String>> getTTable() {
        return tTable;
    }

    public ArrayList<String> getAlphabets() {
        return alphabets;
    }

    public ArrayList<String> getTransitionTableList() {
        return transitionTableList;
    }

    //other functions

    public void setArrayListStr (ArrayList<String> list, String str){
        String[] arr = str.split("\\s+");
        for (int i=0; i<arr.length; i++){
            list.add(arr[i]);
        }
    }




}
