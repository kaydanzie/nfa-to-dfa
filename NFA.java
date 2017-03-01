import java.io.*;
import java.util.*;

public class NFA{
	String[] states, alphabet, acceptStates;
	//transition functions direct from file
	ArrayList<String> transFunction = new ArrayList<String>();

	//transitions as States, line 5 to EOF
	ArrayList<State> transFStates = new ArrayList<State>();
	int start;

	//create hashmap of hashmaps
	//{ q1:{"a":2,"b":1}, q2:{"a":2} }
	//states have been stripped of non-numeric characters (Integer)
	HashMap<Integer, HashMap<String, Integer[]>> hmap = new HashMap<Integer, HashMap<String, Integer>>();

	ArrayList<Integer> qPrime = new ArrayList<>();

	//create arraylist of strings (transition functions for dfa) to put in output file
	ArrayList<String> dfaTransitions = new ArrayList<String>();

	public static void main(String[] args){

		NFA ex = new NFA();
		ex.readFile("example.txt");

		ex.convertToStates();
		ex.getQPrime();
		ex.acceptableStates();
		ex.getEndStates();

	}

	public void readFile(String filename){
		try{
			FileReader reader = new FileReader(filename);
			BufferedReader buff = new BufferedReader(reader);
			String line;
			ArrayList<String> fullFile = new ArrayList<String>();
			while((line = buff.readLine()) != null){
				fullFile.add(line);
			}

			this.states = (fullFile.get(0)).toString().split("\t");
			this.alphabet = (fullFile.get(1)).toString().split("\t");
		 	String startS = fullFile.get(2).toString();
			this.start = Integer.parseInt(startS.replaceAll("[^\\d.]", ""));
			this.acceptStates = (fullFile.get(3)).toString().split("\t");

			//line 5 to EOF is transition function
			for(int i=4; i<fullFile.size(); ++i){
				//each row after 5 is an equation s,x = s^f
				transFunction.add(fullFile.get(i));
			}

			buff.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void print(Object o){
		System.out.println(o);
	}

	public static void printList(String[] list){
		for(String i : list){
			print(i);
		}
	}

	public static void printArray(ArrayList arr){
		for(int i=0; i< arr.size(); ++i){
			print(arr.get(i));
		}
	}


	public void convertToStates(){
		State adding;
		String currentFunc;
		String[] function;
		for(int i=0; i<this.transFunction.size(); ++i){
			currentFunc = transFunction.get(i);

			//split equation into current state, symbol, end state
			function = currentFunc.split("[,=]");

			//convert function items to format (int, String, int)
			adding = new State(Integer.parseInt(function[0]), function[1].replaceAll("\\s+",""), Integer.parseInt(function[2].replaceAll("\\s+","")));
			transFStates.add(adding);
		}
	}


	public void getQPrime(){
		State currentS;
		//start state of original NFA is always in q prime
		this.qPrime.add(this.start);

		//add states that are reached by an EPS
		for(int q=0; q<transFStates.size(); ++q){
			currentS = transFStates.get(q);

			//if this trans function contains an eps, add end state to q prime
			if(currentS.symbol.equals("EPS")){
				this.qPrime.add(currentS.endState);
			}
		}
	}


	public void acceptableStates(){

		//doesn't need to be a class variable
		HashMap<String, Integer> acceptedStates;

		for(int i=0; i<this.states.length; ++i){
			//accepted states differ by state, needs to be reset
			acceptedStates = new HashMap<String, Integer>();

			//every state needs to go through transFStates to see where every symbol goes
			for(int j=0; j<this.transFStates.size(); ++j){

				//check if it's a rule for the state being checked in i loop, and not epsilon
				if((this.transFStates.get(j).current == Integer.parseInt(this.states[i])) && !this.transFStates.get(j).symbol.equals("EPS")){
					acceptedStates.put(this.transFStates.get(j).symbol, this.transFStates.get(j).endState);
					//print(this.transFStates.get(j).symbol);
				}
			}

			this.hmap.put(Integer.parseInt(this.states[i]), acceptedStates);

		}
	}

	public void getEndStates(){
		int currentState;
		//one function can have multiple start states
		// {1,3}, a = {1,3}
		ArrayList<String> oneFunction = new ArrayList<String>();
		//HashMap<String, Integer> allMappedStates;

		for(int i=0; i< qPrime.size(); ++i){
			currentState = qPrime.get(i);
			//oneFunction = iterateHash(transFStates.get(currentState));
			iterateHash(hmap.get(currentState));

		}
	}

	//given state and letter, get end state
	public getEndStates(State startS, String letter){

	}

	public String[] iterateHash(HashMap<String, Integer> iMap){
		//one state has mult
		ArrayList<String> returnString = new ArrayList<String>();

		for(int i=0; i<alphabet.length; ++i){
			if(iMap.containsKey(alphabet[i])){
				returnString.add((iMap.get(alphabet[i])).toString());
			}
			else{
				returnString.add("EM");
			}
		}

		return returnString;
	}

}
