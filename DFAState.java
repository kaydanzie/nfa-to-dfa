import java.util.*;

public class DFAState {
  ArrayList<Integer> startStates, endStates;
  String symbol;

  DFAState(ArrayList<Integer> startStates, String symbol, ArrayList<Integer> endStates){
    this.startStates = startStates;
    this.symbol = symbol;
    this.endStates = endStates;
  }

  public String printDFA(){
    String returnString = "{";

    for(int i=0; i<startStates.size(); ++i){
      returnString += (startStates.get(i) + ", ");
    }

    returnString += ("} , " + letter + " = {");

    for(int i=0; i<endStates.size(); ++i){
      returnString += (endStates.get(i) + ", ");
    }

    return returnString+ "}";
  }
}
