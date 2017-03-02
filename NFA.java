import java.io.*;
import java.util.*;

public class NFA{

	//Filled from input file in method readFile:
	String[] states, alphabet, acceptStates;
	int start;
	//transition functions direct from file, with brackets / whitespace / other characters
	ArrayList<String> transFunction = new ArrayList<String>();

	//transitions as States, line 5 to EOF
	//initialized in method convertToStates:
	ArrayList<State> transFStates = new ArrayList<State>();

	//stack of end states that need to be in the final output
	//continuously popped and pushed
	//initialized in method getEndStates:
	Stack endStack = new Stack<ArrayList<Integer>>();

	//create hashmap of hashmaps { q1:{"a":2,"b":1}, q2:{"a":2} }
	//states have been stripped of non-numeric characters
	//initialized in method acceptableStates:
	HashMap<Integer, HashMap<String, ArrayList<Integer>>> hmap = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();

	//initialized in method getQPrime:
	ArrayList<Integer> qPrime = new ArrayList<>();

	//filled in getEndStates:
	ArrayList<DFAState> dfaStates = new ArrayList<DFAState>();

	ArrayList<String> finalFunctions = new ArrayList<String>();


	public static void main(String[] args){

		NFA ex = new NFA();
		//ex.readFile("nfa_example.nfa");
		ex.readFile("example.txt");

		ex.convertToStates();
		ex.getQPrime();
		ex.acceptableStates();
		ex.fillEmptySets();
		ex.getEndStates();
		print(ex.hmap);
		ex.fillFinalStrings();

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
			System.out.print(arr.get(i)+ " ");
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

			//convert function items to State class format State(int, String, int)
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

		//initialize empty hashmap so every key at least exists, don't need to check
		for(int k=0; k<this.states.length; ++k){
			this.hmap.put(Integer.parseInt(this.states[k]), new HashMap<String, ArrayList<Integer>>());
		}

		//go through states, adding each one by one to hmap as keys
		for(int i=0; i<this.states.length; ++i){

			//current state being "added" as key to hashmap
			tempStateNum = Integer.parseInt(this.states[i]);

			//every state needs to go through transFStates to see where every symbol goes
			//transFStates is array of states, all rules that exist for the nfa
			for(int j=0; j<this.transFStates.size(); ++j){

				//either set it to new or get key
				ArrayList<Integer> putStates;

				tempCurrent = this.transFStates.get(j);

				//check if it's a rule for the state being checked in i loop, and not epsilon
				if(tempCurrent.current == tempStateNum && !tempCurrent.symbol.equals("EPS")){

					HashMap<String, ArrayList<Integer>> temp = this.hmap.get(tempStateNum);

					if(temp.containsKey(tempCurrent.symbol)){
						putStates = temp.get(tempCurrent.symbol);
					}
					else{
						putStates = new ArrayList<Integer>();
					}

					putStates.add(tempCurrent.endState);
					temp.put(tempCurrent.symbol, putStates);
					this.hmap.put(tempStateNum, temp);

				}
				else if((tempCurrent.current == Integer.parseInt(this.states[i])) && tempCurrent.symbol.equals("EPS")){
					//need to check if the HashMap<String, ArrayList<Integer>> already has all symbols
					//checkKey function adds EPS end state to letters in alphabet
					acceptedStates = this.hmap.get(tempStateNum);
					acceptedStates = checkKeys(acceptedStates, tempCurrent);
					this.hmap.put(tempStateNum, acceptedStates);
				}
				//otherwise a rule for a different state
			}

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


	//get arraylist of end states given start state and letter
	public ArrayList<Integer> getFromHash(int s, String letter){
		for(int i=0; i<this.states.length; ++i){
			HashMap<String, ArrayList<Integer>> t = hmap.get(s);
			if(t.get(letter) != null) return t.get(letter);
			else return null;
		}
		return null;
	}

	//given state and letter, get end states (arraylist)
	public void getEndStates(){
		ArrayList<Integer> combinedEndStates, currentStarts, oneEndStates;
		DFAState tempDFA;

		//start stack with qprime, pop end states, get end states of those, push them
		for(int j=0; j<this.alphabet.length; ++j){
			combinedEndStates = new ArrayList<Integer>();
			currentStarts = new ArrayList<Integer>();

			//for each letter, combine end states of q prime states
			for(int k=0; k<this.qPrime.size(); ++k){

				ArrayList<Integer> endStates = getFromHash(this.qPrime.get(k), this.alphabet[j]);
				currentStarts.add(this.qPrime.get(k));
				if(!(endStates == null)) combinedEndStates = combineArrays(endStates, combinedEndStates);
			}

			tempDFA = new DFAState(currentStarts, this.alphabet[j], combinedEndStates);
			//each letter gets pushed, after iterating through all q prime states
			this.endStack.push(combinedEndStates);
		}


		//stack of end states, which then used as start states
		while(!endStack.empty()){
			currentStarts = (ArrayList<Integer>) endStack.pop();

			//all end states for one state
			oneEndStates = new ArrayList<Integer>();

			//check if class variable dfaStates contains this exact set of start states
			if(!containsStarts(currentStarts)){
				for(int i=0; i<this.alphabet.length; ++i){
					//each letter has different combined end states
					combinedEndStates = new ArrayList<Integer>();

					for(int j=0; j<currentStarts.size(); ++j){
						//end states for this one letter and state, replace with each loop
						//need to get state object from transFStates
						oneEndStates = getFromHash(currentStarts.get(j), this.alphabet[i]);
						if(!(oneEndStates == null)) combinedEndStates = combineArrays(oneEndStates, combinedEndStates);
					}
					//don't do this here
					//formatFunctions(currentStarts, this.alphabet[i], combinedEndStates);
					tempDFA = new DFAState(currentStarts, this.alphabet[i], combinedEndStates);
					dfaStates.add(tempDFA);
					this.endStack.push(combinedEndStates);
				}

			}
			//else start states have already been checked


		}
	}

	public boolean containsStarts(ArrayList<Integer> find){
		//compare start state arraylists of all dfas to find
		DFAState tempDFA;

		for(int h=0; h<this.dfaStates.size(); ++h){
			tempDFA = this.dfaStates.get(h);
			if(tempDFA.equalToStart(find)) return true;
		}
		return false;
	}

	public String formatFunctions(ArrayList<Integer> startStates, String letter, ArrayList<Integer> endStates){
		String returnString = "{";

		for(int i=0; i<startStates.size(); ++i){
			returnString += (i == startStates.size()-1) ? (startStates.get(i)) : (startStates.get(i) + ", ");
		}

		returnString += ("} , " + letter + " = {");

		for(int i=0; i<endStates.size(); ++i){
			returnString += (i == endStates.size()-1) ? (endStates.get(i)) : (endStates.get(i) + ", ");
		}

		return returnString+ "}";
	}


	public void fillEmptySets(){
		//alphabet and end states for one state
		HashMap<String, ArrayList<Integer>> oneState;

		for(int r=0; r<this.states.length; ++r){
			//get each inner hash of symbols/end states
			oneState = this.hmap.get(Integer.parseInt(this.states[r]));
			for(int a=0; a<this.alphabet.length; ++a){
				if(!oneState.containsKey(this.alphabet[a])){
					//if letter doesn't exist for state, enter as null
					//"b" : null
					oneState.put(this.alphabet[a], null);
				}
			}
			//change value for this state so it's hash now includes all letters
			//might not change, might just be putting back original
			this.hmap.put(Integer.parseInt(this.states[r]), oneState);
		}
	}


	public void fillFinalStrings(){
		DFAState tempDFA;
		String t;
		for(int p=0; p<this.dfaStates.size(); ++p){
			tempDFA = this.dfaStates.get(p);
			t = formatFunctions(tempDFA.startStates, tempDFA.symbol, tempDFA.endStates);
			print(t);
			finalFunctions.add(t);
		}
	}


	public ArrayList<Integer> combineArrays(ArrayList<Integer> smallerSet, ArrayList<Integer> allCombined){
		for(int k=0; k<smallerSet.size(); ++k){
			if(!allCombined.contains(smallerSet.get(k))) allCombined.add(smallerSet.get(k));
		}
		return allCombined;
	}


}
