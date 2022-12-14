import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.LinkedHashSet;
import java.util.Set;


class FileReader {

    private int noOfNonTerminals;
    private int noOfTerminals;
    private final ArrayList<String> nonTerminal;
    private final ArrayList<String> terminal;
    private final ArrayList<String> rules;
    private String start;

    public FileReader (File file) {

        noOfTerminals = 0;
        noOfNonTerminals = 0;
        nonTerminal = new ArrayList<>();
        terminal = new ArrayList<>();
        rules = new ArrayList<>();


        try {

            Scanner scanner = new Scanner(file);

            boolean nonTerminalsRead = false;
            boolean terminalsRead = false;
            boolean rulesRead = false;
            boolean startRead = false;

            String temp;

            while (!startRead){
                scanner.nextLine(); //NON-TERMINAL

                while (!nonTerminalsRead){
                    temp = scanner.nextLine();
                    if (temp.equals("TERMINAL")){
                        nonTerminalsRead = true;
                    }
                    else {
                        nonTerminal.add(temp);
                        noOfNonTerminals++;
                    }
                }

                while (!terminalsRead){
                    temp = scanner.nextLine();
                    if (temp.equals("RULES")){
                        terminalsRead = true;
                    }
                    else {
                        terminal.add(temp);
                        noOfTerminals++;
                    }
                }

                while (!rulesRead){
                    temp = scanner.nextLine();
                    if (temp.equals("START")){
                        rulesRead = true;
                    }
                    else {
                        rules.add(temp);
                    }
                }

                start = scanner.nextLine();
                startRead = true;

                scanner.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    public int getNoOfNonTerminals() {
        return noOfNonTerminals;
    }

    public int getNoOfTerminals() {
        return noOfTerminals;
    }

    public ArrayList<String> getNonTerminal() {
        return nonTerminal;
    }

    public ArrayList<String> getTerminal() {
        return terminal;
    }

    public ArrayList<String> getRules() {
        return rules;
    }

    public String getStart() {
        return start;
    }
}


class CFGtoCNF {

    private FileReader fileReader;
    private ArrayList<String> newRules;
    private String newStart;
    private ArrayList<String> newNonTerminals;
    private static boolean epsilonExists;

    public static void main(String[] args){
        File file = new File("G2.txt");
        epsilonExists = false;
        new CFGtoCNF(file).output();
    }

    private CFGtoCNF(File file){
        fileReader = new FileReader(file);
        newRules = fileReader.getRules();
        newNonTerminals = fileReader.getNonTerminal();
        addNewStartState();
        removeNullProduction();
        removeUnitProduction();
        removeNonTerminalsGreaterThan2();
        modifyRuleForTerminal();
        modifyRulesIfHasTerminalsGreaterThanOne();
    }



    // STEP 1: Introduce new stat state
    /*
    Check if a new start needs to be introduced
     */
    private boolean checkStartStateNeeded() {

        for (String newRule : newRules) {
            String[] temp = newRule.split(":");
            if (temp[1].contains(fileReader.getStart())) {
                return true;
            }
        }

        return false;
    }


    /*
        The function adds a new start state to the transitions
        The new start state is introduced as R and is added to the
        newStart. Also, to the list of non-terminals
    */
    private void addNewStartState() {

        //check if the start state needs to be added
        if (checkStartStateNeeded()) {
            newStart = "R";
            newRules.add(0, "R:S");
            newNonTerminals.add(0, newStart);
        }
        else {
            newStart = fileReader.getStart();
        }
    }

    // STEP 2: Remove Null rules

    /*
        The function returns an ArrayList<String> of non-terminals
        leading to epsilon.
     */
    private ArrayList<String> listOfNonTerminalsLeadingToEpsilon() {

        ArrayList<String> nonTerminalsLeadingToEpsilon = new ArrayList<>();

        for (int i=0; i<newNonTerminals.size(); i++){
            for (String newRule : newRules) {
                String[] temp = newRule.split(":");
                if (temp[1].contains(newNonTerminals.get(i)) && !nonTerminalsLeadingToEpsilon.contains(newNonTerminals.get(i)))
                    nonTerminalsLeadingToEpsilon.add(newNonTerminals.get(i));
            }
        }

        return nonTerminalsLeadingToEpsilon;
    }


    /*
    *S->ABAC
    *S->BAC
    *S->ABC
    *S->BC
    **/


    /*
        To remove the epsilon, we get a list of possible rules
        The function checks for each rule and against the non-terminal that needs to removed and returns a
        list of possible combinations.

     */
    private ArrayList<String> getPossibleProductionCombinations(String rule, String eliminateFor){

        String[] ruleArray = rule.split(":");

        ArrayList<String> ruleWithOutEpsilon = new ArrayList<>();

        char toEliminate;

        int countCharInString = 0;
        for (int x=0; x<ruleArray[1].length(); x++){
            if (ruleArray[1].charAt(x) == eliminateFor.charAt(0))
                countCharInString++;
        }

        if (countCharInString>1) {
            for (int i = 0; i < ruleArray[1].length(); i++) {
                toEliminate = ruleArray[1].charAt(i);
                if (toEliminate == eliminateFor.charAt(0)) {
                    String temp1 = ruleArray[1];
                    String toConcat = temp1.substring(0, i) + temp1.substring(i + 1);
                    ruleWithOutEpsilon.add(ruleArray[0] + ":" + toConcat);
                }

            }
        }

        //eliminate all e
        if (!(countCharInString==1 && ruleArray[1].equals(eliminateFor)))
            ruleWithOutEpsilon.add(ruleArray[0] + ":" + ruleArray[1].replaceAll(eliminateFor, ""));

        return ruleWithOutEpsilon;
    }

    /*
        Using previous 2 helper functions, remove null production
     */
    private void removeNullProduction() {

        ArrayList<String> nonTerminalsLeadingToEpsilon = listOfNonTerminalsLeadingToEpsilon();
        ArrayList<String> productionRulesToBeAdded;
        ArrayList<String> newRulesToBeReplaced = new ArrayList<>();

        for (int i=0; i<newRules.size(); i++) {
            String[] temp = newRules.get(i).split(":");
            if (!temp[1].equals("e")) {
                epsilonExists = true;
                newRulesToBeReplaced.add(newRules.get(i));

                for (int j = 0; j < nonTerminalsLeadingToEpsilon.size(); j++) {

                    //&& condition was old new start (Both might not be required)
                    if (temp[1].contains(nonTerminalsLeadingToEpsilon.get(j)) && !temp[1].equals(newStart)) {
                        productionRulesToBeAdded = getPossibleProductionCombinations(newRules.get(i), nonTerminalsLeadingToEpsilon.get(j));

                        newRulesToBeReplaced.addAll(productionRulesToBeAdded);
                    }
                }
            }
        }

        //removing redundant rules
        newRules.clear();
        for (int x=0; x<newRulesToBeReplaced.size(); x++){
            for (int y=x+1; y<newRulesToBeReplaced.size(); y++){
                if (newRulesToBeReplaced.get(x).equals(newRulesToBeReplaced.get(y))){
                    newRulesToBeReplaced.remove(y);
                }
            }
        }

        newRules = newRulesToBeReplaced;
    }


    // STEP 3: Remove unit rules

    /*
    check if a rule is a unit production
    if it is a unit production, find for Non-Terminal [1] a rule that starts with Non-Terminal [0] and has terminal on [1]
    */
    private boolean isRuleUnitProduction(String str){

        String[] rule = str.split(":");

        if (newStart.equals(rule[0]) && fileReader.getStart().equals(rule[1]))
            return  false; // here
        else {
            for (String newNonTerminal : newNonTerminals) {
                if (rule[1].equals(newNonTerminal)) {
                    return true;
                }
            }
        }

        return false;
    }
    /*
        S:B --- B:a
        if rule[2] in rule[0] of some rule then
        if rule[2] is a terminal
        add this to the new rules and remove the rule that was passed as a parameter
    */

    private ArrayList<String> findTerminalForUnitProduction(String str){

        String[] rule = str.split(":");
        ArrayList<String> terminalRule = new ArrayList<>();

        for (int i=0; i< newRules.size(); i++) {
            if (rule[1].equals(newRules.get(i).split(":")[0])) {
                for (int j = 0; j < fileReader.getTerminal().size(); j++) {
                  if (fileReader.getTerminal().get(j).equals(newRules.get(i).split(":")[1]))
                      terminalRule.add(rule[0] + ":" + newRules.get(i).split(":")[1]);
                }
            }
        }

        return terminalRule;
    }

    private void removeUnitProduction() {

        ArrayList<String> unitProductionsToRemove = new ArrayList<>();
        ArrayList<String> rulesToAdd = new ArrayList<>();

        //finding the rules to be removed and the rules to be added
        for (int i=0; i< newRules.size(); i++){
            if (isRuleUnitProduction(newRules.get(i))){
                unitProductionsToRemove.add(newRules.get(i));
                rulesToAdd.addAll(findTerminalForUnitProduction(newRules.get(i)));
            }
        }


        //updating newRules by adding the rules which should be added and removing the ones which needs removal
        for (int j=0; j<newRules.size(); j++){
            for (int k=0; k<unitProductionsToRemove.size(); k++){
                if (unitProductionsToRemove.get(k).equals(newRules.get(j))){
                    newRules.remove(j);
                    for (int l=0; l<rulesToAdd.size(); l++) {
                        if (unitProductionsToRemove.get(k).split(":")[0].equals(rulesToAdd.get(l).split(":")[0]))
                            newRules.add(j, rulesToAdd.get(l));
                    }
                }
            }
        }


        //removing redundant rules
        Set rulesSet = new LinkedHashSet();
        rulesSet.addAll(newRules);
        newRules.clear();
        newRules.addAll(rulesSet);

        //removing unreachable rules
        newRules = removeUnreachableRules();
    }

    private ArrayList<String> removeUnreachableRules(){

        ArrayList<String> temp = new ArrayList<>();

        for (int i=0; i<newRules.size(); i++){
            String toCompare = newRules.get(i).split(":")[0];
            //searching only for terminal rules if they are unreachable
            if (fileReader.getTerminal().contains(newRules.get(i).split(":")[1])){
                for (int j=0; j<newRules.size(); j++){
                    if (newRules.get(j).split(":")[1].contains(toCompare))
                        temp.add(newRules.get(i));
                }
            }
            else {
                temp.add(newRules.get(i));
            }

        }

        Set rulesSet = new LinkedHashSet();
        rulesSet.addAll(temp);
        temp.clear();
        temp.addAll(rulesSet);

        return temp;
    }


    // STEP 4: correct the remaining rules for non terminals
    //removing variables length more than 2
    private void removeNonTerminalsGreaterThan2() {

        ArrayList<String> toRemove = new ArrayList<>();
        ArrayList<String> newRuleToAdd = new ArrayList<>();

        for (int i=0; i< newRules.size(); i++){
            if(newRules.get(i).split(":")[1].length()>2)
                toRemove.add(newRules.get(i));
        }


        for (int j=0; j<toRemove.size(); j++){
            String str = "";
            for (int k=1; k<toRemove.get(j).split(":")[1].length(); k++){
                str += toRemove.get(j).split(":")[1].charAt(k);
            }
            if (!str.equals(""))
                newRuleToAdd.add(getNewNonTerminalSymbol() + ":" + str);
        }
        newRules.addAll(newRuleToAdd);

        newRules = modifyRuleForNonTerminal(newRules, newRuleToAdd);
    }

    /*
        Following function generates a new Non-Terminal symbol and adds it to the
        Non-Terminal list.
     */
    private String getNewNonTerminalSymbol(){
        String x = newNonTerminals.get(newNonTerminals.size()-1);
        String y = "" + (char)(x.charAt(0)+1);
        newNonTerminals.add(y);

        return y;
    }

    /*
        The following function is utilized by the removeNonTerminalsGreaterThan2 function,
        accepts a list of newRules and a list of rules that needs to be added. It then compares
        and add the rules to the newRules list
     */
    private ArrayList<String> modifyRuleForNonTerminal(ArrayList<String> str1, ArrayList<String> str2){

        for (int i=0; i<str1.size(); i++){
            for (int j=0; j<str2.size(); j++){
                if (!str1.get(i).equals(str2.get(j))) {
                    if (str1.get(i).split(":")[1].contains(str2.get(j).split(":")[1])) {
                        str1.add(i, str1.get(i).substring(0, 3) + str2.get(j).substring(0, 1));
                        str1.remove(i + 1);
                    }
                }
            }
        }

        return str1;
    }





    // STEP 5: correct the rules for terminals

    /*
        go through the rules
        if rule has a terminal and non terminal
        make a new non terminal make it go to terminal
        and add the non terminal in place of the non terminal
     */

    /*
        The following function checks if the rule has both terminal and non-terminal on the right side of ":"
     */
    private boolean checkIfRuleHasBothTerminalAndNonTerminal(String str){

        boolean flag1 = false;
        boolean flag2 = false;

        ArrayList<String> terminals = fileReader.getTerminal();

        //for length of the str split
        String rule = str.split(":")[1];

        //checking for non-terminal
        flag1 = isFlag(rule, newNonTerminals);


        //checking for terminal
        flag2 = isFlag(rule, terminals);

        if (flag1 && flag2)
            return true;

        return false;
    }

    /*
        Helper function for last function
     */
    private boolean isFlag(String rule, ArrayList<String> newNonTerminals) {
        for (int i=0; i<rule.length(); i++){
            for (int j = 0; j< newNonTerminals.size(); j++){
                char x = rule.charAt(i);
                char y = newNonTerminals.get(j).charAt(0);
                if (y == x){
                    return true;
                }
            }
        }
        return false;
    }


    private String checkIfNonTerminalLeadingToTerminalExist(String str){

        //go through the rules
        //check if the length of the rule is 1
        //if the terminal equals str
        //return non-terminal

        String[] temp;

        for (int i=0; i<newRules.size(); i++){
            temp = newRules.get(i).split(":");
            if (temp[1].equals(str) &&  !temp[0].equals(newStart) ){
                return temp[0];
            }
        }

        return "NONE";
    }

    private void modifyRuleForTerminal(){

        boolean firstLetterLowerCase;
        boolean firstLetterUpperCase;

        for (int i=0; i<newRules.size(); i++){
            if(checkIfRuleHasBothTerminalAndNonTerminal(newRules.get(i))) {

                String[] rule = newRules.get(i).split(":");
                String str = rule[1];

                firstLetterLowerCase = false;
                firstLetterUpperCase = false;


                if (str.length()>1){
                    if (Character.isLowerCase(str.charAt(0)) || Character.isDigit(str.charAt(0)))
                        firstLetterLowerCase = true;
                    else firstLetterUpperCase = true;
                }

                //check needed in last step if a nonterminal leading to terminal already exist use that
                //no need for new terminal

                String nonTerminal;
                //making new rule
                if (firstLetterLowerCase){
                    nonTerminal = checkIfNonTerminalLeadingToTerminalExist(""+str.charAt(0));
                    if (nonTerminal.equals("NONE")) {
                        nonTerminal = getNewNonTerminalSymbol();
                        newRules.add(nonTerminal + ":" + str.charAt(0));
                    }
                    newRules.remove(i);
                    newRules.add(i, rule[0] + ":" + nonTerminal + str.charAt(1));
                }
                else if (firstLetterUpperCase){
                    nonTerminal = checkIfNonTerminalLeadingToTerminalExist(""+str.charAt(1));
                    if (nonTerminal.equals("NONE")) {
                        nonTerminal = getNewNonTerminalSymbol();
                        newRules.add(nonTerminal + ":" + str.charAt(1));
                    }
                    newRules.remove(i);
                    newRules.add(i, rule[0] + ":" + str.charAt(0) + nonTerminal);
                }

            }
        }
    }


    /*
        The following function modifies the function if a rule has more than
        one terminal. Creates a new non-terminal leading to a terminal and
        replace the terminal in the rule with non-terminal.
     */
    private void modifyRulesIfHasTerminalsGreaterThanOne() {

        for (int i=0; i<newRules.size(); i++){
            //if there exist a rule with two terminals
            String[] rule = newRules.get(i).split(":");
            if (fileReader.getTerminal().contains(""+rule[1].charAt(0)) && rule[1].length()>1){
                //find a rule that contains a terminal of length one similar to the one found previously
                for (int j=0; j<newRules.size(); j++){
                    if (newRules.get(i).charAt(2) == newRules.get(j).charAt(2) && newRules.get(j).length()==3){
                        newRules.add(i, rule[0] + ":" + newRules.get(j).charAt(0) + newRules.get(j).charAt(0));
                        newRules.remove(i+1);
                    }
                }
            }
        }

        if (!newStart.equals(fileReader.getStart())) 
            replaceNewStartRule();
        else if (epsilonExists)
            newRules.add(1, newStart+":e");
    }


    private void replaceNewStartRule(){

        ArrayList<String> toAdd = new ArrayList<>();


        for (int i = 0; i < newRules.size(); i++) {
            if (newRules.get(i).split(":")[0].equals(fileReader.getStart()))
                toAdd.add("R:" + newRules.get(i).split(":")[1]);
        }

        if (epsilonExists)
            toAdd.add("R:e");

        newRules.remove(0);
        for (int j=0; j< toAdd.size(); j++)
            newRules.add(j, toAdd.get(j));

    }

    private void output(){
        System.out.println("NON-TERMINAL");
        for (String newNonTerminal : newNonTerminals) System.out.println(newNonTerminal);
        System.out.println("TERMINAL");
        for (String terminal: fileReader.getTerminal()) System.out.println(terminal);
        System.out.println("RULES");
        for (String rule : newRules) System.out.println(rule);
        System.out.println("START\n"+newStart);

    }

}
