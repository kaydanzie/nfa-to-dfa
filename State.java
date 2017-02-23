public class State{

	int current, endState;
	String symbol;

	State(int current, String symbol, int endState){
			this.current = current;
			this.symbol = symbol;
			this.endState = endState;
	}

}
