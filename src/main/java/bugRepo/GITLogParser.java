//package bugRepo;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.ArrayList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//public class GITLogParser {
//	
//	public ArrayList<Commit> parseLog(String log) throws ParserConfigurationException, SAXException, IOException
//	{
//		
//		ArrayList<Commit> commits = new ArrayList<Commit>();
//		
//		
//		String[] logs = log.split("\u0003");
//		
//		for(int i = 1; i < logs.length; i++)
//		{
//			String[] tags = logs[i].split("\u0002");
//			System.out.println(tags[0]);
//		}
//		
//		
//		
//		
//		
//		return commits;
//		
////		
////		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
////
////	    DocumentBuilder builder = factory.newDocumentBuilder();
////	    
////	    InputSource is = new InputSource(new StringReader(log));
////	    
////	    Document document = builder.parse(is);
////	    
////	    NodeList nodeList = document.getElementsByTagName("bug");
////	    
////	    for(int i = 0; i < nodeList.getLength(); i++)
////	    {
////	    	Node node = nodeList.item(i);
////	    	
////	    	String subject = ((Element) node).getElementsByTagName("subject").item(0).getTextContent();
////	    	String date = ((Element) node).getElementsByTagName("commit_date").item(0).getTextContent();
////	    	String body = ((Element) node).getElementsByTagName("message_body").item(0).getTextContent();
////	    	String patch = ((Element) node).getElementsByTagName("patch").item(0).getTextContent();
////	    	
////	    	System.out.println(subject);
////	    	
////	    	commits.add(new Commit(file,subject,date,body, new Patch(patch)));
////	    	
////	    }
//	    
//	    
//	    
//	    
//	}
//
//}
