import java.util.*;

public class DFAState {
  ArrayList<Integer> startStates, endStates;
  String symbol;

  DFAState(ArrayList<Integer> startStates, String symbol, ArrayList<Integer> endStates){
    this.startStates = startStates;
    this.symbol = symbol;
    this.endStates = endStates;
  }


  public boolean equalToStart(ArrayList<Integer> find){
    int matches = 0;
    if(find.size() != startStates.size()) return false;
    for(int g=0; g< find.size(); ++g){
      if(startStates.contains(find.get(g))) matches++;
    }
    return matches == find.size();
  }

}
