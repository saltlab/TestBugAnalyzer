package bugRepo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JiraBugReport {

	String project;
	String key;
	String summary;
	String link;
	String type;
	String priority;
	String status;
	String resolution;
	String component = "not specified";
	String description;
	ArrayList<String> comments;
	public JiraBugReport(String key, String project, String summary, String link) {
		// TODO Auto-generated constructor stub
		this.key = key;
		this.project = project;
		this.summary = summary;
		this.link = link;
		
	}
	
	public JiraBugReport(String xml)
	{
		
		try{
			this.comments = new ArrayList<String>();
			InputSource is = new InputSource(new StringReader(xml));
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(is);
			
			NodeList nodeList = document.getElementsByTagName("item");
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				Node node = nodeList.item(i);
				if (node instanceof Element) 
				{
					this.key = ((Element) node).getElementsByTagName("key").item(0).getTextContent();
					this.project = ((Element) node).getElementsByTagName("project").item(0).getTextContent();
					this.summary = ((Element) node).getElementsByTagName("summary").item(0).getTextContent();
					this.link =  ((Element) node).getElementsByTagName("link").item(0).getTextContent();
					this.type =  ((Element) node).getElementsByTagName("type").item(0).getTextContent();
					this.priority =  ((Element) node).getElementsByTagName("priority").item(0).getTextContent();
					this.status =  ((Element) node).getElementsByTagName("status").item(0).getTextContent();
					this.resolution =  ((Element) node).getElementsByTagName("resolution").item(0).getTextContent();
					if (((Element) node).getElementsByTagName("component").getLength() != 0)
						this.component =  ((Element) node).getElementsByTagName("component").item(0).getTextContent();
					
					NodeList commentList = document.getElementsByTagName("comment");
					
					for (int j = 0 ; j < commentList.getLength() ; j ++)
					{
						Node comment = commentList.item(j);
						comments.add(comment.getTextContent());
						
						
					}
					
				}
			}
				
		}catch(IOException e)
		{
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

      

	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return this.project+", "+this.key +", "+ this.summary;
	}
}
