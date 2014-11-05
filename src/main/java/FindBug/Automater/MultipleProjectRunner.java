package FindBug.Automater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.unix4j.Unix4j;

public class MultipleProjectRunner {

	String projectsPath = "/Users/Arash/projects" ;
	
	
	public ArrayList<Project> listProjects()
	{
		ArrayList<Project> projectList = new ArrayList<Project>();
		
		File projectPathFolder = new File(projectsPath);
		
		File[] projectFolders = projectPathFolder.listFiles();
		
		for (int i = 0; i < projectFolders.length; i++) {
			
			List<String> pomPaths = Unix4j.find(projectFolders[i].getAbsolutePath(), "pom.xml").toStringList();
			if(!pomPaths.isEmpty())
			{
				Project project = new Project(projectFolders[i].getName(), findRootPOM(pomPaths));
				projectList.add(project);
			}
		}
		
		
		
		System.out.println(projectList);
		
		return projectList;
	}

	
	public String findRootPOM(List<String> pomPaths)
	{
		String root = null;
		int min = Integer.MAX_VALUE;
		int numberOfSlashes = 0;
		for (int i = 0; i < pomPaths.size(); i++) {
			String pomPath = pomPaths.get(i);
			numberOfSlashes = 0;
			for (int j = 0; j < pomPath.length(); j++) {
				if(pomPath.charAt(j) == File.separatorChar)
				{
					numberOfSlashes++;
				}
			}
			if(numberOfSlashes < min)
			{
				min = numberOfSlashes;
				root = pomPath;
			}
			
			
		}
		
		
		return root.replace("pom.xml", "");
		
		
	}
	
	
	
	public void runSingleProject(Project project) throws IOException, InterruptedException
	{
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		
		
		System.out.println("-------Building Project " + project.getName() + " -----------");
		
		System.out.println(pr.buildProject());
		
		ArrayList<String> testPaths = pr.findTestPaths();
		
		for (int i = 0; i < testPaths.size(); i++) {
			System.out.println(testPaths.get(i));
		}
		
		pr.setTestPaths(testPaths);
		
		System.out.println("-------Running FindBugs on " + project.getName() + " -----------");
		
		System.out.println(pr.runFindBug());
		
		System.out.println("-------Result saved in " + project.getName() + ".html -----------");
	}
	
	
	public void runMultipleProjects() throws IOException, InterruptedException
	{
		ArrayList<Project> projects = listProjects();
		for (int i = 0; i < projects.size(); i++) {
			runSingleProject(projects.get(i));
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		mpr.runMultipleProjects();

	}
	
	
}
