package FindBug.Automater;

public class ProjectResult {
	
	String name;
	
	int[] severity = new int[5];
	int[] confidence = new int[3];
	
	public ProjectResult(String name) {
		// TODO Auto-generated constructor stub
		
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		ProjectResult o = (ProjectResult) obj;
		return this.name.equals(o.name);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		String str = name;
		
		for (int i = 0; i < severity.length; i++) {
			str += ", " + severity[i];
		}
		for (int i = 0; i < confidence.length; i++) {
			str += ", " + confidence[i];
		}
		
		return str;
		
		
		
	}

}
