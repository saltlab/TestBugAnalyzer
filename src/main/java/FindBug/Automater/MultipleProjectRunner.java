package FindBug.Automater;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import utils.Project;
import utils.ProjectRunner;
import utils.Settings;

public class MultipleProjectRunner {

	
	Formatter logFormatter;
	Formatter statLogger;
	public MultipleProjectRunner() throws FileNotFoundException {

		logFormatter = new Formatter(Settings.logPath);
		statLogger = new Formatter("stat.txt");
	
	}
	
	public ArrayList<Project> listMavenProjects()
	{
		ArrayList<Project> projectList = new ArrayList<Project>();
		
		File projectPathFolder = new File(Settings.projectsPath);
		
		File[] projectFolders = projectPathFolder.listFiles();
		
		for (int i = 0; i < projectFolders.length; i++) {
			
			File[] filesInFolder = projectFolders[i].listFiles();
			
			boolean isMaven = false;
			
			if (filesInFolder == null)
				continue;
			
			for ( File file : filesInFolder)
			{
				if (file.getName().equals("pom.xml"))
				{
					isMaven = true;
					break;
				}
				
			}
			
			if (isMaven)
			{
				Project project = new Project(projectFolders[i].getName(), projectFolders[i].getAbsolutePath()+File.separatorChar);
				projectList.add(project);
				System.out.println(project.getName() + " : " + project.getPath());
			}
			
			
//			List<String> pomPaths = Unix4j.find(projectFolders[i].getAbsolutePath(), "pom.xml").toStringList();
//			if(!pomPaths.isEmpty())
//			{
//				Project project = new Project(projectFolders[i].getName(), findRootPOM(pomPaths));
//				projectList.add(project);
//				System.out.println(project.getName() + " : " + project.getPath());
//			}
		}
		
		
		
		System.out.println(projectList);
		
		return projectList;
	}
	
	
	public ArrayList<Project> listAllProjects()
	{
		ArrayList<Project> projectList = new ArrayList<Project>();
		
		File projectPathFolder = new File(Settings.projectsPath);
		
		File[] projectFolders = projectPathFolder.listFiles();
		
		for (int i = 0; i < projectFolders.length; i++) {
			
				Project project = new Project(projectFolders[i].getName(), projectFolders[i].getAbsolutePath()+File.separatorChar);
				projectList.add(project);
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
	
	
	
	public void runFindBugsOnWholeProject(Project project) throws FileNotFoundException, IOException, InterruptedException
	{
		
		Formatter fr = new Formatter("compileResults.txt");
		
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
//		pr.addFindBugsPluginToPOM(pr.mavenModel);
		
		String buildLog = pr.buildProject();
//		System.out.println(buildLog);
		logFormatter.format("%s\n", buildLog);
		
		boolean buildSuccess = false;
		boolean findBugsSuccess = false;
		
		if (buildLog.contains("BUILD SUCCESS"))
		{
			buildSuccess = true;
			System.out.println(project.getName()+"----BUILD SUCCESSED");
			
			
		}
		else
		{
			System.out.println(project.getName()+"----BUILD FAILED");
		}
		
		String[] cmd = {"mvn","findbugs:findbugs"};
		String result = ProjectRunner.runCommand(cmd, project.getPath());
//		System.out.println(result);
		logFormatter.format("%s\n", result);
		if (result.contains("BUILD SUCCESS"))
		{
			findBugsSuccess = true;
			System.out.println(project.getName()+"----FINDBUGS BUILD SUCCESSED");
			
		}
		else
		{
			System.out.println(project.getName()+"----FINDBUGS BUILD FAILED");
		}
			
			
		
		fr.format("%s,%s,%s\n", project.getName(), buildSuccess, findBugsSuccess);
		fr.flush();
	}
	
	public void runFindBugsOnSingleProject(Project project) throws IOException, InterruptedException
	{
		
		
		
		
		ProjectRunner pr = new ProjectRunner(project.getName(), project.getPath());
		pr.addFindBugsPluginToPOM(pr.mavenModel);
		
		System.out.println("-------Building Project " + project.getName() + " -----------");
		
		
		String buildLog = pr.buildProject();
		System.out.println(buildLog);
		logFormatter.format("%s\n", buildLog);
		
		if (buildLog.contains("BUILD SUCCESS"))
			System.out.println("----------BUILD SUCCESS----------");
		
		
		ArrayList<String> testPaths = pr.findTestPaths();
		
		for (int i = 0; i < testPaths.size(); i++) {
			System.out.println(testPaths.get(i));
		}
		
		pr.setTestPaths(testPaths);
		
//		ArrayList<String> path = new ArrayList<String>();
//		path.add(project.path);
//		pr.setTestPaths(path);
		
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
		ArrayList<Project> projects = listMavenProjects();
		for (int i = 0; i < projects.size(); i++) {
//			runClocOnSingleProject(projects.get(i));
//			runFindBugsOnSingleProject(projects.get(i));
//			runPMDOnSingleProject(projects.get(i));
//			runSonarQubeOnSingleProject(projects.get(i));
			runFindBugsOnWholeProject(projects.get(i));
		}
		
		logFormatter.flush();
		logFormatter.close();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		mpr.runMultipleProjects();
	}
	

	
	
}
