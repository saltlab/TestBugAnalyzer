package FindBug.Automater;

import java.util.ArrayList;

public class JiraBugReport {

	String project;
	String key;
	String summary;
	String link;
	
	public JiraBugReport(String key, String project, String summary, String link) {
		// TODO Auto-generated constructor stub
		this.key = key;
		this.project = project;
		this.summary = summary;
		this.link = link;
		
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return this.project+", "+this.key +", "+ this.summary;
	}
}
