package FindBug.Automater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;

public class MultipleProjectRunner {

	
	Formatter logFormatter;
	
	public MultipleProjectRunner() throws FileNotFoundException {

		logFormatter = new Formatter(Settings.logPath);
	
	}
	
	
	public ArrayList<Project> listProjects()
	{
		ArrayList<Project> projectList = new ArrayList<Project>();
		
		File projectPathFolder = new File(Settings.projectsPath);
		
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
	
	
	
	public void runClocOnSingleProject(Project project) throws IOException, InterruptedException
	{
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		System.out.println("------ running on "+ project.getName()+" ----------");
		
		String[] command = {"cloc",project.getPath()};
		String clocResultWholeProject = pr.runCommand(command, Settings.projectsPath);
		System.out.println(clocResultWholeProject);
		logFormatter.format("project:\n%s\n", clocResultWholeProject);
		ArrayList<String> testPaths = pr.findTestSourceDirectories();
		testPaths.add(0, "cloc");
		logFormatter.format("command:\n%s\n", testPaths);
		System.out.println(testPaths);
		String testCloc = pr.runCommand(testPaths.toArray(new String[testPaths.size()]), Settings.projectsPath);
		System.out.println("test code:\n"+testCloc);
		logFormatter.format("test code:\n%s\n", testCloc);
		
		String[] grepCommand = {"grep","-r","@Test"};
		int unitTestNum = (pr.runCommand(grepCommand, project.getPath()).split("\r\n|\r|\n")).length;
		logFormatter.format("number of unit tests : %d\n", unitTestNum);
		System.out.println("number of Unit Tests : "+ unitTestNum);
		
	}
	
	
	
	public void runPMDOnSingleProject(Project project) throws IOException, InterruptedException
	{
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		System.out.println("------ running on "+ project.getName()+" ----------");
		
		ArrayList<String> testPaths = pr.findTestSourceDirectories();
		String[] command = {Settings.PMDPath, "pmd", "-f", "html", "-rulesets", "java-basic,java-design","-dir"};
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(Arrays.asList(command));
		cmd.add(testPaths.get(0));
		logFormatter.format("command:\n%s\n", cmd);
		System.out.println(cmd);
		String testCloc = pr.runCommand(cmd.toArray(new String[cmd.size()]), Settings.projectsPath);
		System.out.println("test code:\n"+testCloc);
		logFormatter.format("test code:\n%s\n", testCloc);
		
	}
	
	public void runSonarQubeOnSingleProject(Project project) throws IOException, InterruptedException
	{
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		System.out.println("------ running on "+ project.getName()+" ----------");
		
		String[] command = {"mvn","sonar:sonar"};
		String sonarResultWholeProject = pr.runCommand(command, project.getPath());
		System.out.println(sonarResultWholeProject);
		
	}
	
	public void runFindBugsOnSingleProject(Project project) throws IOException, InterruptedException
	{
		
		
		
		
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		
		
		System.out.println("-------Building Project " + project.getName() + " -----------");
		
		
		String buildLog = pr.buildProject();
		System.out.println(buildLog);
		logFormatter.format("%s\n", buildLog);
		
		if (buildLog.contains("BUILD SUCCESS"))
			System.out.println("----------BUILD SUCCESS----------");
		
		
//		ArrayList<String> testPaths = pr.findTestPaths();
//		
//		for (int i = 0; i < testPaths.size(); i++) {
//			System.out.println(testPaths.get(i));
//		}
		
//		pr.setTestPaths(testPaths);
		
		ArrayList<String> path = new ArrayList<String>();
		path.add(project.path);
		pr.setTestPaths(path);
		
		System.out.println("-------Running FindBugs on " + project.getName() + " -----------");
		
		
		String findBugResult = pr.runFindBug(); 
		System.out.println(findBugResult);

		if(findBugResult.contains("missing"))
		{
			System.out.println("-------- Re-running the findBugs ----------");
			pr.findMissingDependencies(findBugResult);
			System.out.println(pr.runFindBug());
		}
		
		
		
		logFormatter.format("%s\n", findBugResult);
		
		System.out.println("-------Result saved in " + project.getName() + ".html -----------");
	}
	
	
	public void runMultipleProjects() throws IOException, InterruptedException
	{
		ArrayList<Project> projects = listProjects();
		for (int i = 0; i < projects.size(); i++) {
//			runClocOnSingleProject(projects.get(i));
			runFindBugsOnSingleProject(projects.get(i));
//			runPMDOnSingleProject(projects.get(i));
//			runSonarQubeOnSingleProject(projects.get(i));
		}
		
		logFormatter.flush();
		logFormatter.close();
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
//		ProjectCloner pc = new ProjectCloner(Settings.projectListPath, Settings.projectsPath);
//		pc.cloneProjects();
		
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		mpr.runMultipleProjects();

	}
	
	
}
