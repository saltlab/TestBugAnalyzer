package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.unix4j.Unix4j;

import FindBug.Automater.MultipleProjectRunner;








public class ProjectRunner {
	
	String projectName;
	String projectPath;
	File projectPathFile;
	
	
	String[] options;
	ArrayList<String> testPaths;
	public Model mavenModel;
	
	Model[] pomsModel;
	
	
	ArrayList<String> auxClassPathDependencies = null;
	
	public static String runCommand(String[] commands, String path) throws IOException, InterruptedException
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
			System.out.println(line);
			
		}
		process.waitFor();
		return result.toString();
		
	}
	
	
	public ProjectRunner(Project project) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		
		this.projectName = project.getName();
		this.projectPath = project.getPath();
//		options = new String[] {"-textui","-output", projectName+".html", "-html:fancy-hist.xsl"};
		
		options = new String[] {"-textui","-output", projectName+"_Prod.xml", " -xml:withMessages"};
		
		projectPathFile = new File(projectPath);
		
		initializeMavenModel();
		
	}
	
	public ProjectRunner(String projectName, String projectPath) {
		// TODO Auto-generated constructor stub
		this.projectName = projectName;
		this.projectPath = projectPath;
//		options = new String[] {"-textui","-output", projectName+".html", "-html:fancy-hist.xsl"};
		
		options = new String[] {"-textui","-output", projectName+"_Prod.xml", "-xml:withMessages"};
		
		projectPathFile = new File(projectPath);
		
		initializeMavenModel();
		
	}
	
	public void backUpPOM(Model model) throws FileNotFoundException, IOException
	{
		MavenXpp3Writer m2pomWriter = new MavenXpp3Writer();
		m2pomWriter.write(new FileOutputStream(model.getPomFile().getAbsolutePath()+".backup"), model);
	}
	
	public void addFindBugsPluginToPOM(Model model) throws FileNotFoundException, IOException
	{
		backUpPOM(model);
		DependencyManagement dm = new DependencyManagement();
		Build b = model.getBuild();
		Plugin p = new Plugin();
		
		p.setGroupId("org.codehaus.mojo");
		p.setArtifactId("findbugs-maven-plugin");
		p.setVersion("3.0.0");

		
		if (b == null)
		{
			b = new Build();
			model.setBuild(b);
		}
		
		

		PluginManagement pluginManagement = b.getPluginManagement();  
		if(pluginManagement == null)
			pluginManagement = new PluginManagement();
		
		if(!b.getPlugins().contains(p))
		{
			b.addPlugin(p);
			System.out.println("plugin added to plugins.");
		}

		if(!pluginManagement.getPlugins().contains(p))
		{
			pluginManagement.addPlugin(p);
			b.setPluginManagement(pluginManagement);
			System.out.println("plugin added to pluginManagement.");
		}
		MavenXpp3Writer m2pomWriter = new MavenXpp3Writer();
		m2pomWriter.write(new FileOutputStream(model.getPomFile().getAbsolutePath()), model);
	}
	
	
	
	void initializeMavenModel()
	{
//		List<String> projectPOMs = Unix4j.find(projectPath, "pom.xml").toStringList();
		
		try {
//			if (projectPOMs.size() != 0)
			{
				MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
				mavenModel = m2pomReader.read( new FileReader( projectPath+"pom.xml" ) );
				mavenModel.setPomFile(new File(projectPath+"pom.xml" ));

//				pomsModel = new Model[projectPOMs.size()];
//				for (int i = 0; i < projectPOMs.size(); i++) {
//					
//					MavenXpp3Reader pomReader = new MavenXpp3Reader();
//					pomsModel[i] = pomReader.read( new FileReader( projectPOMs.get(i) ) );
//				}


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
		
//		String testOutputDirectory = model.getBuild().getTestOutputDirectory();
//        List modules = model.getModules();
	}
	
	public ArrayList<String> findDependantRefs()
	{
		
		ArrayList<String> dependencies = new ArrayList<String>();
		 
		 for (int i = 0; i < pomsModel.length; i++) {
			 if(pomsModel[i] != null && pomsModel[i].getDependencies() != null)
			 {
				 
				 for (Dependency dep : pomsModel[i].getDependencies()) {
					 String depPath = getDependencyPath(dep);
					 if(!depPath.equals(Settings.mavenLocalRep + File.separatorChar) && !dependencies.contains(depPath))
						 dependencies.add(depPath);
				 }
			 }
		}
		 
		 System.out.println("aux class path : " + dependencies);

		 dependencies.addAll(Unix4j.find(projectPath, "*.class").toStringList());
		 
		 return dependencies;
	
	}
	
	public String runFindBug() throws IOException
	{
		ArrayList<String> command = new ArrayList<String>();
		command.add(Settings.findBugPath);
		command.add("-auxclasspathFromInput");
		command.addAll(Arrays.asList(options));
		command.addAll(testPaths);
		
		System.out.println("command for running FindBugs: "+ command);
		
		Process process = new ProcessBuilder(command).start();
		
		
		Formatter fr = new Formatter(process.getOutputStream());
		
		
		ArrayList<String> auxClassPaths = findDependantRefs();
		
		if(auxClassPathDependencies == null)
			auxClassPathDependencies = auxClassPaths;
		else
			auxClassPathDependencies.addAll(auxClassPaths);
		for (String classPath : auxClassPathDependencies) {
			fr.format("%s\n", classPath);
		}
		fr.flush();
		
		fr.close();
		
		
		
		
		
		
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
	
	
	public ArrayList<String> findMissingDependencies(String result)
	{
		Scanner sc = new Scanner(result);
		sc.nextLine();
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			String depPath = getExistingDirParent(Settings.mavenLocalRep+File.separatorChar+line.replace('.', File.separatorChar));
			if(!depPath.equals(Settings.mavenLocalRep+File.separatorChar) && !auxClassPathDependencies.contains(depPath) && auxClassPathDependencies != null)
				auxClassPathDependencies.add(depPath);
		}
		
		return auxClassPathDependencies;
	}
	
	public ArrayList<String> convertRelativePathToAbsolute(ArrayList<String> relativePaths)
	{
		
		ArrayList<String> absolutePaths = new ArrayList<String>(relativePaths.size());
		for(String path : relativePaths)
		{
			File file = new File(path);
			absolutePaths.add(file.getAbsolutePath());
			
		}
		return absolutePaths;
	}
	
	public ArrayList<String> getTestClassSourcePaths()
	{
		ArrayList<String> testClassRelativePaths = new ArrayList<String>();
		ArrayList<String> testSourceDirs = findTestSourceDirectories();
		
		for(String testPath : testSourceDirs)
		{
			testClassRelativePaths.addAll(Unix4j.find(testPath,"*.java").toStringList());
		}
		
		return convertRelativePathToAbsolute(testClassRelativePaths);
	}
	
	public ArrayList<String> findTestPaths()
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
	
	String getDirectoryFromFile(String path)
	{
		int seperatorIndex = path.lastIndexOf(File.separatorChar);
		if(seperatorIndex != path.length() && seperatorIndex != -1)
			return path.substring(0,seperatorIndex+1);
		else if(seperatorIndex == -1)
			return path;
		else if(seperatorIndex == path.length())
			return path;
		return null;
	}
	
	
	
	public ArrayList<String> findTestDirNames()
	{
	
		ArrayList<String> testSourceDirectories = new ArrayList<String>();
		try {
			
			ArrayList<String> poms = findPOM();
			for (String pom : poms) {
			
			
				MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
				Model mModel = m2pomReader.read( new FileReader( pom ) );
				if (mModel != null && mModel.getBuild() != null)
				{
					String testSourceDirectory = mModel.getBuild().getTestSourceDirectory();
					if(testSourceDirectory != null)
					{
						if(!testSourceDirectories.contains(testSourceDirectory))
						{
							testSourceDirectories.add(getDirectory(testSourceDirectory));
						}
					}
					
				}
				
			
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!testSourceDirectories.contains("test"))
			testSourceDirectories.add("test");
		return testSourceDirectories;
		
	}
	
	
	public ArrayList<String> findTestSourceDirectories() {
		
		ArrayList<String> poms = findPOM();
		ArrayList<String> testSourceDirectories = new ArrayList<String>();
		for (String pom : poms) {
			
			
			try {
				MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
				Model mModel = m2pomReader.read( new FileReader( pom ) );
				if (mModel != null && mModel.getBuild() != null)
				{
					String testSourceDirectory = mModel.getBuild().getTestSourceDirectory();
					if(testSourceDirectory != null)
					{
						testSourceDirectory = new File(getDirectoryFromFile(pom)).getAbsolutePath()+testSourceDirectory;
						if(!testSourceDirectories.contains(testSourceDirectory) && new File(testSourceDirectory).exists())
						{
							testSourceDirectories.add(testSourceDirectory);
						}
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
		
		testSourceDirectories.addAll(Unix4j.cd(this.projectPathFile).find(projectPath, "test").toStringList());
		
		return testSourceDirectories;
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
		
		String[] command = {Settings.mavenPath, "test-compile", "-DskipTests=true", "--fail-at-end"};
		return runCommand(command, projectPath);
		
		
	}
	
	
	 List<String> getProjectModules() throws IOException, XmlPullParserException
	{
		
       return mavenModel.getModules();
		
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
	
	
	
	public String getDependencyPath(Dependency dep)
	{
		String path = Settings.mavenLocalRep + File.separatorChar;
		if(dep.getGroupId() != null)
		{
			path += dep.getGroupId().replace('.', File.separatorChar) + File.separatorChar;
			if(dep.getArtifactId() != null && new File(path + dep.getArtifactId() + File.separatorChar).exists())
			{
				path += dep.getArtifactId() + File.separatorChar;
				if(dep.getVersion() != null && new File(path + dep.getVersion()).exists())
				{
					path += dep.getVersion();
				}
			}
		}
		
		// make sure that the path exists
		
		path = getExistingDirParent(path);
		
		return  path;
	}


	private String getExistingDirParent(String path) {
		while (!(new File(path).exists()))
		{
			path = path.substring(0, path.lastIndexOf(File.separatorChar));
		}
		return path;
	}

}
