package utils;

public class Project {
	
	String name;
	String path;
	boolean isMaven;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public Project(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	
	
	public boolean isMaven() {
		return isMaven;
	}
	public void setMaven(boolean isMaven) {
		this.isMaven = isMaven;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name + " " + path;
	}
//	public String updatePathToFirstPOM()
//	{
//		return null;
//	}

}
