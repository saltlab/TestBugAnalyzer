package FindBug.Automater;

import java.util.ArrayList;

public class FindBugsBugReport {

	String project;
	Fault faults;
	String shortMessage;
	String longMessage;
	String type;
	String code;
	int rank;
	int priority;
	String filePath;
	public FindBugsBugReport() {
		// TODO Auto-generated constructor stub
		
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
		
		return this.project+", "+this.type +", "+ this.shortMessage+ ", "+ this.longMessage+", "+ ", "+this.getRank();
	}
	
	
	
	
	
	
}

class Fault
{
	String file;
	String start, end;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return file + " at " + start+ " - " + end;
	}
}

enum Severity
{
	scariest, scary, troubling, concern, notAConcern
}