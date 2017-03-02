import java.util.*;

public class DFAState {
  ArrayList<Integer> startStates, endStates;
  String symbol;

  DFAState(ArrayList<Integer> startStates, String symbol, ArrayList<Integer> endStates){
    this.startStates = startStates;
    this.symbol = symbol;
    this.endStates = endStates;
  }

}
