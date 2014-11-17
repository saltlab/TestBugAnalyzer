package FindBug.Automater;

import java.util.ArrayList;

public class Result {

	String project;
	ArrayList<String> source;
	String shortMessage;
	String longMessage;
	String type;
	String code;
	public Result() {
		// TODO Auto-generated constructor stub
		
		source = new ArrayList<String>();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return this.project+", "+this.type +", "+ this.shortMessage+ ", "+ this.longMessage+", "+this.source.toString().replace(",", "");
	}
}
