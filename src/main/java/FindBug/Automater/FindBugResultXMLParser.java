package FindBug.Automater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.unix4j.Unix4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FindBugResultXMLParser {

	 ArrayList<FindBugsBugReport> bugList = new ArrayList<FindBugsBugReport>();
	 HashMap<String, FindBugProject> projects = new HashMap<String, FindBugProject>();
	 HashSet<String> testBugReports = new HashSet<String>();
	public static void main(String[] args) throws Exception {
		FindBugResultXMLParser fbrxp = new FindBugResultXMLParser();
		
		Scanner sc = new Scanner(new File("findBugResultList.txt"));
		String relativePath = "../Research/";
		while (sc.hasNextLine())
		{
			String path = sc.nextLine();
//			System.out.println(path);
			fbrxp.parseFile(relativePath+path, path);
		}
		
//		fbrxp.loadResults();
		fbrxp.writeTofile();
		fbrxp.analyzeTypeResult();
		fbrxp.analyzeProjectResult();

}
	
	
	private void analyzeProjectResult()
	{
		HashMap<String, ProjectResult> projectResults = new HashMap<String, ProjectResult>();
		for (Entry<String, FindBugProject> entry : projects.entrySet())
		{
			for (FindBugsBugReport bug : entry.getValue().testBugList) {
				
				if(!projectResults.containsKey(bug.project))
				{
					projectResults.put(bug.project,new ProjectResult(bug.project));
				}
				projectResults.get(bug.project).severity[bug.getRank().ordinal()]++;
				projectResults.get(bug.project).confidence[bug.priority]++;
			}
			
		}
		


		
		try {
			Formatter fr = new Formatter("projectWithPriority.cvs");
			for (ProjectResult projectResult : projectResults.values()) {
				fr.format("%s\n", projectResult.toString());
				
			}
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	private void analyzeTypeResult()
	{
		HashMap<String, ProjectResult> projectResults = new HashMap<String, ProjectResult>();
		for (Entry<String, FindBugProject> entry : projects.entrySet())
		{
			for (FindBugsBugReport bug : entry.getValue().testBugList) {
				
				if(!projectResults.containsKey(bug.shortMessage))
				{
					projectResults.put(bug.shortMessage,new ProjectResult(bug.shortMessage));
				}
				projectResults.get(bug.shortMessage).severity[bug.getRank().ordinal()]++;
				projectResults.get(bug.shortMessage).confidence[bug.priority]++;
			}
			
		}


		
		try {
			Formatter fr = new Formatter("projectWithPriority.csv");
			for (ProjectResult projectResult : projectResults.values()) {
				fr.format("%s\n", projectResult.toString());
				
			}
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeTofile() throws FileNotFoundException 
	{
		Formatter fm = new Formatter("result5.csv");
//	    for (FindBugsBugReport emp : bugList) {
//	      System.out.println(emp);
//	      fm.format("%s\n", emp);
//	    }
//	    fm.close();
		
		
		for (Entry<String, FindBugProject> entry : projects.entrySet())
		{
			if (entry.getValue().testBugList.size() != 0)
			{
				fm.format("%s,%d,%d\n", entry.getKey(), entry.getValue().testBugList.size(), entry.getValue().productionBugList.size());
				fm.flush();
			}
		}
		
		fm.close();
	}

	private void loadResults()
			throws ParserConfigurationException {
		//Get the DOM Builder Factory
	    DocumentBuilderFactory factory = 
	        DocumentBuilderFactory.newInstance();

	    //Get the DOM Builder
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    //Load and Parse the XML document
	    //document contains the complete XML as a Tree.
	    
	   ArrayList<String> results =  (ArrayList<String>) Unix4j.find("/home/arash/Desktop/FindBugRunner", "findbugsXml.xml").toStringList();
	    
	   bugList = new ArrayList<FindBugsBugReport>();
	   for (String xmlFile : results) {
	
	    try{
	    	
	    	Document document = 
	      builder.parse(
	        new File(xmlFile));
	    


	    NodeList nodeList = document.getDocumentElement().getChildNodes();
	    //Iterating through the nodes and extracting the data.

	    for (int i = 1; i < nodeList.getLength(); i++) {

	      //We have encountered an <employee> tag.
	      Node node = nodeList.item(i);
	      if (node instanceof Element) {
	    	
	        
	       if(node.getNodeName().equals("BugInstance"))
	       {
	    	   if(node.getAttributes().getNamedItem("category").getNodeValue().equals("CORRECTNESS"))
	    	   {
	    		   FindBugsBugReport bugInstance = new FindBugsBugReport();
	    		   bugInstance.type = node.getAttributes().getNamedItem("type").getNodeValue();
	    		   bugInstance.rank = Integer.parseInt(node.getAttributes().getNamedItem("rank").getNodeValue());
	    		   bugInstance.priority = Integer.parseInt(node.getAttributes().getNamedItem("priority").getNodeValue());
	    		   
	    		   
	    		   NodeList infoList = node.getChildNodes();
	    		   for (int j = 0; j < infoList.getLength(); j++) {
	    			   
	    			   Node info = infoList.item(j);
	    			   if (info instanceof Element) {
	    				   if(info.getNodeName().equals("ShortMessage") ){
	    					   bugInstance.shortMessage = info.getTextContent().replace(",", " ");;
	    				   }
	    				   else if(info.getNodeName().equals("LongMessage") )
	    				   {
	    					   bugInstance.longMessage = info.getTextContent().replace(",", " ");
	    				   }
	    				   else if(info.getNodeName().equals("SourceLine") ){
	    					   String sourceFile = ((Element) info).getAttribute("sourcefile");
	    					   
	    					   String start = ((Element) info).getAttribute("end"); 
	    					   String end = ((Element) info).getAttribute("end"); 
	    					   
	    					   Fault fault = new Fault();
	    					   fault.file = sourceFile;
	    					   fault.start = start;
	    					   fault.end = end;
	    					   bugInstance.faults = fault;   
	    						   
	    				   }
	    				   
				}
	    		   
	    	   }
	    		   
	    		   bugInstance.project = xmlFile.replace(".xml", "");
	    		   bugList.add(bugInstance);
	       }
	      }
	    }

	      

	  }
	    
	    
	    }catch(Exception e){
	    	System.out.println(xmlFile+ " is malformatted");
	    }
}
	}
	
	
	
	
	
	
	public void parseFile(String xmlFile, String path) throws ParserConfigurationException, SAXException, IOException
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    Document document = builder.parse(new File(xmlFile));
	    
	    
	    
	    NodeList projectNodes = document.getElementsByTagName("Project");
	    
	    if (projectNodes.getLength() > 1)
	    	System.out.println("unexpected one file has two projects !!");
	    
	    	Node project = projectNodes.item(0);
//	    	System.out.println(project.getAttributes().getNamedItem("projectName").getNodeValue());
//	    	String projectName = project.getAttributes().getNamedItem("projectName").getNodeValue();
	    	String projectName = getProjectFromPath(path);
	    	FindBugProject fbProject;
	    	if (!projects.containsKey(projectName))
	    	{
	    		fbProject = new FindBugProject();
	    		fbProject.name = projectName;
	    		ArrayList<Node> srcDirList = findElementByTag(project, "SrcDir");
	    		fbProject.srcDir.addAll(getTextValues(srcDirList));
	    		projects.put(projectName, fbProject);
	    	}
	    	else
	    	{
	    		fbProject = projects.get(projectName);
	    		ArrayList<Node> srcDirList = findElementByTag(project, "SrcDir");
	    		fbProject.srcDir.addAll(getTextValues(srcDirList));
	    	}
	    
	    NodeList bugInsList = document.getElementsByTagName("BugInstance");
	    for (int i = 0 ; i < bugInsList.getLength(); i++)
	    {
	    	Node bugIns = bugInsList.item(i);
	    	
	    	if(bugIns.getAttributes().getNamedItem("category").getNodeValue().equals("CORRECTNESS") || bugIns.getAttributes().getNamedItem("category").getNodeValue().equals("MT_CORRECTNESS"))
	    	   {
	    		   
	    		   String type = bugIns.getAttributes().getNamedItem("type").getNodeValue();
	    		   int rank = Integer.parseInt(bugIns.getAttributes().getNamedItem("rank").getNodeValue());
	    		   int priority = Integer.parseInt(bugIns.getAttributes().getNamedItem("priority").getNodeValue());
	    		   String shortMessage = null;
	    		   String longMessage = null;
	    		   NodeList infoList = bugIns.getChildNodes();
	    		   for (int j = 0; j < infoList.getLength(); j++) {
	    			   
	    			   Node info = infoList.item(j);
	    			   if (info instanceof Element) {
	    				   if(info.getNodeName().equals("ShortMessage") ){
	    					   shortMessage = info.getTextContent().replace(",", " ");;
	    				   }
	    				   else if(info.getNodeName().equals("LongMessage") )
	    				   {
	    					    longMessage = info.getTextContent().replace(",", " ");
	    				   }
	    				   else if(info.getNodeName().equals("SourceLine") ){
	    					   String sourcePath = ((Element) info).getAttribute("sourcepath");
	    					   String start = ((Element) info).getAttribute("start"); 
	    					   String end = ((Element) info).getAttribute("end"); 
	    					   
	    					   Fault fault = new Fault();
	    					   fault.file = sourcePath;
	    					   fault.start = start;
	    					   fault.end = end;
	    					   FindBugsBugReport bugInstance = new FindBugsBugReport();
	    					   bugInstance.faults = fault;
	    					   bugInstance.type = type;
	    					   bugInstance.rank = rank;
	    					   bugInstance.priority = priority;
	    					   bugInstance.shortMessage = shortMessage;
	    					   bugInstance.longMessage = longMessage;
	    					   bugInstance.project = projectName;
	    					   if (isInTestDir(sourcePath))
	    					   {
	    						   System.out.println(fault);
	    						   fbProject.testBugList.add(bugInstance);
	    					   }
	    					   else
	    						   fbProject.productionBugList.add(bugInstance);
	    				   }
	    				   
				}
	    		   
	    	   }
	    		   
	    		   
	       }
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	}
	
	
	boolean isInTestDir(String path)
	{
		return path.contains("test") && !path.contains("Latest");
	}
	
	ArrayList<String> getTextValues(ArrayList<Node> nodes)
	{
		ArrayList<String> textValues = new ArrayList<String>();
		for(Node node : nodes)
		{
			textValues.add(node.getTextContent());
		}
		
		return textValues;
	}
	
	ArrayList<Node> findElementByTag(Node node, String tag)
	{
		NodeList nodeList = node.getChildNodes();
		ArrayList<Node> resultList = new ArrayList<Node>();
		for (int i = 0 ; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals(tag))
				resultList.add(nodeList.item(i));
		
		return resultList;
	}
	
	public String getProjectFromPath(String path)
	{
		int start = path.indexOf(File.separatorChar);
		int end = path.indexOf(File.separatorChar, start+1);
		
		return path.substring(start+1, end);
	}
	
	
}




class FindBugProject
{
	String name;
	HashSet<String> srcDir = new HashSet<String>();
	ArrayList<FindBugsBugReport> testBugList = new ArrayList<FindBugsBugReport>();
	ArrayList<FindBugsBugReport> productionBugList = new ArrayList<FindBugsBugReport>();
	
}
