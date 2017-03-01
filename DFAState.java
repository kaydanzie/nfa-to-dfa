public class DFAState{

	int[] current, endStates;
	String symbol;

	DFAState(int[] current, String symbol, int[] endStates){
			this.current = current;
			this.symbol = symbol;
			this.endStates = endStates;
	}

}
