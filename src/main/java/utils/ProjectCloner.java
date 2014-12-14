package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class ProjectCloner {

	String projectsDir;
	File projectURLsFiles;
	Scanner projectURLScanner;
	
	public ProjectCloner(String projectListPath, String projectsDir) throws FileNotFoundException {

		this.projectURLsFiles = new File(projectListPath);
		this.projectsDir = projectsDir;
		projectURLScanner = new Scanner(projectURLsFiles);
	
	}
	
	public void cloneProjects() throws IOException, InterruptedException
	{
		
		while(projectURLScanner.hasNextLine())
		{
			cloneProject(projectURLScanner.nextLine());
		}
		
		projectURLScanner.close();
	}
	
	public boolean cloneNextProject() throws IOException, InterruptedException
	{
		if(projectURLScanner.hasNextLine())
		{
			cloneProject(projectURLScanner.nextLine());
			return true;
		}
		else
			return false;
	}
	
	
	public void cloneProject(String projectURL) throws IOException, InterruptedException
	{
		System.out.println("cloning "+ projectURL);
		String[] command = {"git", "clone", projectURL};
		ProjectRunner.runCommand(command,projectsDir);
	}
	
//	public static void main(String[] args) throws IOException, InterruptedException {
//		
//		ProjectCloner pc = new ProjectCloner("/Users/Arash/projects/projects.txt", "/Users/Arash/projects");
//		pc.cloneProjects();
//	}

}
