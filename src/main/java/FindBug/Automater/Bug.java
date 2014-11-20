package FindBug.Automater;

import java.util.ArrayList;

public class Bug {

	String project;
	ArrayList<String> source;
	String shortMessage;
	String longMessage;
	String type;
	String code;
	int rank;
	int priority;
	public Bug() {
		// TODO Auto-generated constructor stub
		
		source = new ArrayList<String>();
	}
	
	
	
	
	public Severity getRank() {
		
		if( rank < 5)
			return Severity.scariest;
		else if(rank < 10)
			return Severity.scary;
		else if(rank < 15)
			return Severity.troubling;
		else if(rank < 20)
			return Severity.concern;
		return Severity.notAConcern;
	}
	

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return this.project+", "+this.type +", "+ this.shortMessage+ ", "+ this.longMessage+", "+this.source.toString().replace(",", "")+ ", "+this.getRank();
	}
}

enum Severity
{
	scariest, scary, troubling, concern, notAConcern
}