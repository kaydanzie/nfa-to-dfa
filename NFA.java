import java.io.*;
import java.util.*;

public class NFA{
	String[] states, alphabet, acceptStates;
	//transition functions direct from file
	ArrayList<String> transFunction = new ArrayList<String>();

	//transitions as States, line 5 to EOF
	ArrayList<State> transFStates = new ArrayList<State>();
	int start;
	Stack endStack;

	//create hashmap of hashmaps
	//{ q1:{"a":2,"b":1}, q2:{"a":2} }
	//states have been stripped of non-numeric characters (Integer)
	//change Integer to []?
	HashMap<Integer, HashMap<String, ArrayList<Integer>>> hmap = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();

	ArrayList<Integer> qPrime = new ArrayList<>();

	//create arraylist of strings (transition functions for dfa) to put in output file
	ArrayList<String> dfaTransitions = new ArrayList<String>();

	public static void main(String[] args){

		NFA ex = new NFA();
		ex.readFile("nfa_example.nfa");

		ex.convertToStates();
		ex.getQPrime();
		ex.acceptableStates();

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

			String temp;
			//line 1, all states
			temp = (fullFile.get(0)).toString();
			this.states = temp.replaceAll("\\p{P}", "").split("\\s+");

			//line 2, alphabet/all symbols
			temp = (fullFile.get(1)).toString();
			this.alphabet = temp.replaceAll("\\p{P}", "").split("\\s+");

			//line 3, start states
		 	temp = fullFile.get(2).toString();
			this.start = Integer.parseInt(temp.replaceAll("\\p{P}", ""));

			//line 4, accept states
			temp = (fullFile.get(3)).toString();
			this.acceptStates = (temp.replaceAll("\\p{P}", "")).split("\\s+");

			//line 5 to EOF is transition function
			for(int i=4; i<fullFile.size(); ++i){
				//each row after 5 is an equation s,x = s^f
				transFunction.add(fullFile.get(i).replaceAll("[{}\\s+]", ""));
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
		ArrayList<Integer> allEndStates; //all end states for one letter/symbol, same start state

		for(int i=0; i<this.transFunction.size(); ++i){
			currentFunc = transFunction.get(i);

			//split equation into current state, symbol, end state
			function = currentFunc.split("[,=]");



			//convert function items to format (int, String, int)
			adding = new State(Integer.parseInt(function[0]), function[1], Integer.parseInt(function[2]));
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

//filling hmap { q1:{"a":[2],"b":[1]}, q2:{"a":[2]} }
	public void acceptableStates(){

		//doesn't need to be a class variable
		//inner hashmap
		HashMap<String, ArrayList<Integer>> acceptedStates;
		int tempStateNum;
		State tempCurrent;

		//go through states, adding each one by one to hmap as keys
		for(int i=0; i<this.states.length; ++i){
			//accept states differ by state, needs to be reset
			acceptedStates = new HashMap<String, ArrayList<Integer>>();

			tempStateNum = Integer.parseInt(this.states[i]);

			//every state needs to go through transFStates to see where every symbol goes
			//transFStates is array of states, all rules that exist for the nfa
			for(int j=0; j<this.transFStates.size(); ++j){

				//either set it to new or getKey
				ArrayList<Integer> putStates;

				tempCurrent = this.transFStates.get(j);

				//check if it's a rule for the state being checked in i loop, and not epsilon
				if(tempCurrent.current == Integer.parseInt(this.states[i]) && !tempCurrent.symbol.equals("EPS")){
					if(hmap.containsKey(tempStateNum)){
						//format of temp- "a": [1,2]
						HashMap<String, ArrayList<Integer>> temp = hmap.get(tempStateNum);
						String tempLetter = tempCurrent.symbol;

						//modifying ArrayList<Integer> with put
						//temp.get(tempLetter) is an ArrayList<Integer>
						putStates = temp.get(tempLetter);
						putStates.add(tempCurrent.endState);
						acceptedStates.put(tempLetter, putStates);
					}

					else{
						putStates = new ArrayList<Integer>();
						putStates.add(tempCurrent.endState);
						acceptedStates.put(tempCurrent.symbol, putStates);
					}
				}
				else if((tempCurrent.current == Integer.parseInt(this.states[i])) && tempCurrent.symbol.equals("EPS")){
					//need to check if the HashMap<String, ArrayList<Integer>> already has all symbols
					//checkKey function adds EPS end state to letters in alphabet
					acceptedStates = checkKeys(acceptedStates, tempCurrent);
				}
				//otherwise could be a rule for a different state
			}

			this.hmap.put(tempStateNum, acceptedStates);

		}
	}



	public HashMap<String, ArrayList<Integer>> checkKeys(HashMap<String, ArrayList<Integer>> accepted, State startState){

		//all you need to do is add the end state of EPS start state to the existing list of reachable states for each symbol
		for(int h=0; h<alphabet.length; ++h){
			if(accepted.containsKey(alphabet[h])){
				ArrayList<Integer> t = accepted.get(alphabet[h]);
				t.add(startState.endState);
				accepted.put(alphabet[h], t);
			}
			else{
				ArrayList<Integer> t = new ArrayList<Integer>();
				t.add(startState.endState);
				accepted.put(alphabet[h], t);
			}
		}

		return accepted;
	}


	//given state and letter, get end states (arraylist)
	// public getEndStates(State startS, String letter){
	// 	for(int j=0; j<qPrime.size(); ++j){
	// 		endStack.push(transFStates.get(qPrime.get(j).end))
	// 	}
	// 	endStack.push()
	// 	while(!endStack.empty()){
	//
	// 	}
	// }

	// public void getEndStates(){
	// 	int currentState;
	// 	//one function can have multiple start states
	// 	// {1,3}, a = {1,3}
	// 	ArrayList<String> oneFunction = new ArrayList<String>();
	// 	//HashMap<String, Integer> allMappedStates;
	//
	// 	for(int i=0; i< qPrime.size(); ++i){
	// 		currentState = qPrime.get(i);
	// 		//oneFunction = iterateHash(transFStates.get(currentState));
	// 		iterateHash(hmap.get(currentState));
	//
	// 	}
	// }

	public void iterateHash(HashMap<String, Integer> iMap){

		ArrayList<String> returnString = new ArrayList<String>();

		for(int i=0; i<alphabet.length; ++i){
			if(iMap.containsKey(alphabet[i])){
				returnString.add((iMap.get(alphabet[i])).toString());
			}
			else{
				returnString.add("EM");
			}
		}

		//return returnString;
	}


}
