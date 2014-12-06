package FindBug.Automater;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.unix4j.Unix4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FindBugResultXMLParser {

	 ArrayList<FindBugsBugReport> bugList = new ArrayList<FindBugsBugReport>();
	
	public static void main(String[] args) throws Exception {
		FindBugResultXMLParser fbrxp = new FindBugResultXMLParser();
		fbrxp.loadResults();
		fbrxp.writeTofile();
//		fbrxp.analyzeTypeResult();

}
	
	
	private void analyzeProjectResult()
	{
		HashMap<String, ProjectResult> projectResults = new HashMap<String, ProjectResult>();
		
		for (FindBugsBugReport bug : bugList) {

			if(!projectResults.containsKey(bug.project))
			{
				projectResults.put(bug.project,new ProjectResult(bug.project));
			}
				projectResults.get(bug.project).severity[bug.getRank().ordinal()]++;
				projectResults.get(bug.project).confidence[bug.priority]++;
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
		
		for (FindBugsBugReport bug : bugList) {

			if(!projectResults.containsKey(bug.shortMessage))
			{
				projectResults.put(bug.shortMessage,new ProjectResult(bug.shortMessage));
			}
				projectResults.get(bug.shortMessage).severity[bug.getRank().ordinal()]++;
				projectResults.get(bug.shortMessage).confidence[bug.priority]++;
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
	
	private void writeTofile() throws FileNotFoundException {
		Formatter fm = new Formatter("result.cvs");
	    for (FindBugsBugReport emp : bugList) {
	      System.out.println(emp);
	      fm.format("%s\n", emp);
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
	    
	   ArrayList<String> results =  (ArrayList<String>) Unix4j.find("/home/arash/Desktop/FindBugRunner", "*.xml").toStringList();
	    
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
	    					   String end = ((Element) info).getAttribute("end"); 
	    					   if( end != null && !end.equals(""))
	    						   bugInstance.source.add(sourceFile + " at "+ end );
	    					   else
	    						   bugInstance.source.add("in " + sourceFile);
	    						   
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
}
