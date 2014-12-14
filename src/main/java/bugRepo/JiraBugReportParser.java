package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.unix4j.Unix4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.Settings;



public class JiraBugReportParser {

	
	
	
	 ArrayList<JiraBugReport> bugList = new ArrayList<JiraBugReport>();
	
	public static void main(String[] args) throws Exception {
		JiraBugReportParser jbrp = new JiraBugReportParser();
		jbrp.loadResults();
		

	}
	
	private ArrayList<JiraBugReport> randomlyChoose(int num)
	{
		
		ArrayList<JiraBugReport>shuffledBugList = (ArrayList<JiraBugReport>) bugList.clone(); 
		Collections.shuffle(shuffledBugList);
		ArrayList<JiraBugReport> cuttedArray = new ArrayList<JiraBugReport>(num);
		for (int i = 0; i < num; i++) {
			cuttedArray.add(shuffledBugList.get(i));
		}
		System.out.println(cuttedArray);
		return cuttedArray;
	}

	
	private void writeTofile(ArrayList <JiraBugReport> bugsList, String fileName)
	{
		Formatter fr;
		try {
			fr = new Formatter(fileName);
			for (int i = 0; i < bugsList.size(); i++) 
			{
				fr.format("%s,%s\n", bugsList.get(i).key, bugsList.get(i).link);
			}
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadResults()	throws ParserConfigurationException {
		
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	    DocumentBuilder builder = factory.newDocumentBuilder();

	    
	   ArrayList<String> results =  (ArrayList<String>) Unix4j.find(Settings.jiraXmlsPath, "*.xml").toStringList();
	    
	   
	   for (String xmlFile : results) 
	   {
	
		   bugList = new ArrayList<JiraBugReport>();
		    try
		    {
		    	
		    	Document document = builder.parse(new File(xmlFile));
		    
	
	
			    NodeList nodeList = document.getElementsByTagName("item");
			    for (int i = 1; i < nodeList.getLength(); i++) 
			    {
		
			      Node node = nodeList.item(i);
			      if (node instanceof Element) 
			      {
			    	  String key = ((Element) node).getElementsByTagName("key").item(0).getTextContent();
			    	  String project = ((Element) node).getElementsByTagName("project").item(0).getTextContent();
			    	  String summary = ((Element) node).getElementsByTagName("summary").item(0).getTextContent();
			    	  String link =  ((Element) node).getElementsByTagName("link").item(0).getTextContent();
			    	  bugList.add(new JiraBugReport(key, project, summary, link));
			      }
	
		      
	
			    }
		    
		    
		    }catch(Exception e){
		    	System.out.println(xmlFile+ " is malformatted");
		    }
		    
		    System.out.format("%s : %d\n", xmlFile, bugList.size() );
		    writeTofile(randomlyChoose(50), xmlFile.replace(".xml", ".csv"));
		    
		    
	   }
	   
	}
}
