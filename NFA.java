import java.io.*;
import java.util.ArrayList;

public class NFA{
	String[] states, alphabet, acceptStates;
	//transition functions direct from file
	ArrayList<String> transFunction = new ArrayList<String>();
	//transitions as States
	ArrayList<State> transFStates = new ArrayList<State>();
	String start;

	ArrayList<String> startPrime = new ArrayList<String>();

	public static void main(String[] args){

		NFA ex = new NFA();
		ex.readFile("example.txt");

		ex.convertToStates();
		// printList(ex.alphabet);
		// printArray(ex.transFunction);

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
			this.start = fullFile.get(2).toString();
			this.start = this.start.replaceAll("[^\\d.]", "");
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

			adding = new State(Integer.parseInt(function[0]), function[1], Integer.parseInt(function[2].replaceAll("\\s+","")));
			transFStates.add(adding);
		}
	}
}
