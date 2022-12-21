package localsearch.applications.queen;

import localsearch.model.*;
import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.*;
import localsearch.search.TabuSearch;

import java.io.PrintWriter;
import java.util.*;

class MyMove{
	int i;
	int v;
	public MyMove(int i, int v){
		this.i = i; this.v = v;
	}
}
public class Queen {
	int numberOfQueen;
	LocalSearchManager localSearchManager;
	ConstraintSystem constraintSystem;
	VarIntLS[] x;
	Random random;
	
	public Queen(int numberOfQueen){
		this.numberOfQueen = numberOfQueen;
		random = new Random();
	}
	public void stateModel(){
		LocalSearchManager ls=new LocalSearchManager();
		constraintSystem = new ConstraintSystem(ls);
		x = new VarIntLS[numberOfQueen];
		for (int i = 0; i < numberOfQueen; i++){
			x[i] = new VarIntLS(ls, 0, numberOfQueen - 1);
		}
		
		constraintSystem.post(new AllDifferent(x));
		
		IFunction[] f1=new IFunction[numberOfQueen];
		for (int i = 0; i < numberOfQueen; i++)
			f1[i] =  new FuncPlus(x[i], i);
		constraintSystem.post(new AllDifferent(f1));
		
		IFunction[] f2 = new IFunction[numberOfQueen];
		for (int i = 0; i < numberOfQueen; i++)
			f2[i] = new FuncPlus(x[i], -i);
		constraintSystem.post(new AllDifferent(f2));
		
		ls.close();
		
	}
	public void search(){
		int it = 0;
		ArrayList<MyMove> L = new ArrayList<MyMove>();
		
		while(it < 100000 && constraintSystem.violations() > 0){
			int sel_i = -1;
			int sel_v = -1;
			int min_delta = 1000000;
			L.clear();
			for(int i = 0; i < numberOfQueen; i++){
				for(int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++){
					int delta = constraintSystem.getAssignDelta(x[i], v);
					if(delta < min_delta){
						min_delta = delta;
						L.clear();
						L.add(new MyMove(i,v));
					}else if(delta == min_delta){
						L.add(new MyMove(i,v));
					}
				}
			}
			int idx = random.nextInt(L.size());
			MyMove m = L.get(idx);
			sel_i = m.i;
			sel_v = m.v;
			x[sel_i].setValuePropagate(sel_v);// local move
			it++;
		}
	}
	
	public void printHTML(){
		
		try{
			PrintWriter out = new PrintWriter("queen.html");
			out.println("<table border = 1>");
			for(int i = 0; i < numberOfQueen; i++){
				out.println("<tr>");
				for(int j = 0; j < numberOfQueen; j++)
					if(x[j].getValue() == i)
						out.println("<td width=20 height=20, bgcolor='red'></td>");
					else
						out.println("<td width=20 height=20, bgcolor='blue'></td>");
				out.println("</tr>");
			}
			out.println("</table>");
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public void tabuSearch(){
		TabuSearch ts = new TabuSearch();
		ts.search(constraintSystem, 30, 10, 100000, 200);
		
	}
	public void printSolution(){
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Queen Q = new Queen(20);
		Q.stateModel();
		Q.tabuSearch();
		Q.printHTML();
	}

}
