package FindBug.Automater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.unix4j.Unix4j;


public class ProjectRunner {
	
	String projectName;
	String projectPath;
	File projectPathFile;
	final String mavenLocalRep = "/Users/Arash/.m2/repository";
	final String mavenPath = "/Users/Arash/Apps/apache-maven/apache-maven-3.2.3/bin/mvn";
	final String findBugPath = "/Users/Arash/Desktop/CodeStyleCheckers/findbugs-3.0.0/bin/findbugs";
	String[] options;
	ArrayList<String> testPaths;
	Model mavenModel;
	
	public String runCommand(String[] commands, String path) throws IOException, InterruptedException
	{
		StringBuffer result = new StringBuffer();
		
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
		builder.directory(new File(path));
		Process process = builder.start();
		
		
		
		Scanner scInput = new Scanner(process.getInputStream());
		while(scInput.hasNext())
		{
			String line = scInput.nextLine();
			result.append(line);
			result.append("\n");
			
		}
		
		return result.toString();
		
	}
	
	
	public ProjectRunner(Project project) {
		// TODO Auto-generated constructor stub
		
		this.projectName = project.getName();
		this.projectPath = project.getPath();
		options = new String[] {"-textui","-output", projectName+".html", "-html:fancy-hist.xsl"};
		
		projectPathFile = new File(projectPath);
		
		initializeMavenModel();
	}
	
	public ProjectRunner(String projectName, String projectPath) {
		// TODO Auto-generated constructor stub
		this.projectName = projectName;
		this.projectPath = projectPath;
		options = new String[] {"-textui","-output", projectName+".html", "-html:fancy-hist.xsl"};
		
		projectPathFile = new File(projectPath);
		
		initializeMavenModel();
		
	}
	
	void initializeMavenModel()
	{
		MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
		
		try {
			mavenModel = m2pomReader.read( new FileReader( projectPath+"pom.xml" ) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String testOutputDirectory = model.getBuild().getTestOutputDirectory();
//        List modules = model.getModules();
	}
	
	public ArrayList<String> findDependantRefs()
	{
		
		ArrayList<String> dependencies = new ArrayList<String>();
		dependencies.add(projectPath);
		return dependencies;
//		return new ArrayList<String>(Unix4j.find(projectPath, "target").toStringList());
	}
	
	public String runFindBug() throws IOException
	{
		ArrayList<String> command = new ArrayList<String>();
		command.add(findBugPath);
		command.add("-auxclasspath");
		command.addAll(findDependantRefs());
		command.addAll(Arrays.asList(options));
		command.addAll(testPaths);
		
		System.out.println("command for running FindBugs: "+ command);
		
		Process process = new ProcessBuilder(command).start();
		
		Scanner scInput = new Scanner(process.getInputStream());
		Scanner scErr = new Scanner(process.getErrorStream());
		StringBuffer result = new StringBuffer();
		while(scInput.hasNext())
		{
			result.append(scInput.nextLine());
		}
		
		while(scErr.hasNext())
		{
			result.append(scErr.nextLine());
			result.append("\n");
		}
		
		
		return result.toString();
	}
	
	ArrayList<String> findTestPaths()
	{
		ArrayList<String> testPaths = new ArrayList<String>();
		ArrayList<String> testDirectories = findTestDirectories();
		for (String testDirectory : testDirectories) {
			testPaths.addAll(Unix4j.find(projectPath, testDirectory).toStringList());
		}
		return testPaths;
	}
	
	ArrayList<String> findPOM()
	{
		return new ArrayList<String>(Unix4j.find(projectPath, "pom.xml").toStringList());
	}
	
//	ArrayList<String> findTestClasses()
//	{
//		return new ArrayList<String>(Unix4j.find(projectPath, "test-classes").toStringList());
//	}
	
	
	
	String getDirectory(String path)
	{
		int seperatorIndex = path.lastIndexOf(File.separatorChar);
		if(seperatorIndex != path.length() && seperatorIndex != -1)
			return path.substring(seperatorIndex+1);
		else if(seperatorIndex == -1)
			return path;
		else if(seperatorIndex == path.length())
			return getDirectory(path.substring(0, path.length()-1));
		return null;
	}
	
	
	public ArrayList<String> findTestDirectories() {
		
		ArrayList<String> poms = findPOM();
		ArrayList<String> testDirectories = new ArrayList<String>();
		for (String pom : poms) {
			
			
			try {
				MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
				Model mModel = m2pomReader.read( new FileReader( pom ) );
				if (mModel != null && mModel.getBuild() != null)
				{
					String testOutputDirectory = mModel.getBuild().getTestOutputDirectory();
					
					if(testOutputDirectory != null && !testDirectories.contains(testOutputDirectory))
					{
						testDirectories.add(getDirectory(testOutputDirectory));
					}
					
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!testDirectories.contains("test-classes"))
			testDirectories.add("test-classes");
		System.out.println(testDirectories);
		return testDirectories;
	}


	public void setTestPaths(ArrayList<String> testPaths) {
		this.testPaths = testPaths;
	}

	
	public String buildProject() throws IOException, InterruptedException
	{
		
		String[] command = {mavenPath, "test-compile", "-DskipTests=true"};
		return runCommand(command, projectPath);
	}
	
	
	void getProjectModules() throws IOException, XmlPullParserException
	{
		
        mavenModel.getModules();
		
	}
	
	
	String readFile(String path) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File(path));
		StringBuffer content = new StringBuffer();
		
		while(sc.hasNext())
		{
			content.append(sc.nextLine());
		}
		
		return content.toString();
	}

//	public static void main(String[] args) throws IOException, InterruptedException {
//		
//		
//		
//		
//		ProjectRunner pr = new ProjectRunner("Crawlijax", "/Users/Arash/projects/crawljax-crawljax-3.5.1/");
////		System.out.println(pr.mavenModel.getBuild().getOutputDirectory());
////		System.out.println(pr.mavenModel.getDependencies());
////		System.out.println(pr.mavenModel.getModules());
////		System.out.println(pr.mavenModel.getBuild().getTestOutputDirectory());
////		System.out.println(pr.buildProject());
////		
//		ArrayList<String> testPaths = pr.findTestPaths();
//		
//		for (int i = 0; i < testPaths.size(); i++) {
//			System.out.println(testPaths.get(i));
//		}
////		
////		pr.setTestPaths(testPaths);
////		
////		System.out.println(pr.runFindBug());
////		
//		
//		
//		
//	}
}
