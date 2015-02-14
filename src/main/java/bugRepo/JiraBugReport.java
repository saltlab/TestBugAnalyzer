package bugRepo;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
	
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	static {
		dateFormatter.setLenient(false);
	}
	boolean parsed = false;
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
	Date createdDate ;
	Date resolvedDate;
	long timeSpent, timeEstimate;
	int numberOfComments;
	int numberOfAuthors;
	int numberOfWatcher;
	int numberOfVotes;
	
	ArrayList<String> comments = new ArrayList<String>();
	public JiraBugReport(String key, String project, String summary, String link) {
		// TODO Auto-generated constructor stub
		this.key = key;
		this.project = project;
		this.summary = summary;
		this.link = link;
		
	}
	
	
	public long getMinutesToFix()
	{
		return TimeUnit.MILLISECONDS.toMinutes(resolvedDate.getTime() - createdDate.getTime());
	}
	
	public JiraBugReport()
	{
		
	}
	
    public JiraBugReport(String xml)
    {
        
        init(xml, "");
        
    }
    
    
    public JiraBugReport(String xml, String bugRepoNumber)
    {
        
        init(xml, bugRepoNumber);
    }
    
    
    private void init(String xml, String bugRepoNumber) {
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
                    this.key = getByTagName(node,"key");
                    this.project = getByTagName(node,"project");
                    this.summary = getByTagName(node,"summary");
                    this.link =  getByTagName(node,"link");
                    this.type =  getByTagName(node,"type");
                    this.priority =  getByTagName(node,"priority");
                    this.status =  getByTagName(node,"status");
                    this.resolution =  getByTagName(node,"resolution");
                    try {
                        String created = getByTagName(node,"created");
                        this.createdDate = JiraBugReport.dateFormatter.parse(created);
                        String resolved = getByTagName(node,"resolved");
                        this.resolvedDate = JiraBugReport.dateFormatter.parse(resolved);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        //						e.printStackTrace();
                    }
                    
                    if(((Element) node).getElementsByTagName("timeestimate").getLength() != 0)
                        this.timeEstimate = Long.parseLong(((Element) node).getElementsByTagName("timeestimate").item(0).getAttributes().getNamedItem("seconds").getNodeValue());
                    if(((Element) node).getElementsByTagName("timespent").getLength() != 0)
                        this.timeEstimate = Long.parseLong(((Element) node).getElementsByTagName("timespent").item(0).getAttributes().getNamedItem("seconds").getNodeValue());
                    
                    
                    
                    if (((Element) node).getElementsByTagName("component").getLength() != 0)
                        this.component =  ((Element) node).getElementsByTagName("component").item(0).getTextContent();
                    
                    NodeList commentList = document.getElementsByTagName("comment");
                    
                    this.numberOfComments = commentList.getLength();
                    
                    HashSet<String> authors = new HashSet<String>();
                    
                    for (int j = 0 ; j < commentList.getLength() ; j ++)
                    {
                        Node comment = commentList.item(j);
                        String author = comment.getAttributes().getNamedItem("author").getNodeValue();
                        if (!author.equals(-1))
                            authors.add(author);
                        
                        //						comments.add(comment.getTextContent().replaceAll("<[^<>]*>", ""));
                    }
                    
                    
                    
//                    NodeList assigneeList = ((Element) node).getElementsByTagName("assignee");
//                    
//                    for (int j = 0; j < assigneeList.getLength(); j++)
//                    {
//                        String assignee =  assigneeList.item(j).getAttributes().getNamedItem("username").getTextContent();
//                        if(!assignee.equals("-1"))
//                            authors.add(assignee);
//                    }
                    
                    NodeList reporterList = ((Element) node).getElementsByTagName("reporter");
                    
                    for (int j = 0; j < reporterList.getLength(); j++)
                    {
                        String reporter =  reporterList.item(j).getAttributes().getNamedItem("username").getTextContent();
                        if (!reporter.equals("-1"))
                            authors.add(reporter);
                    }
                    
                    if (((Element) node).getElementsByTagName("votes").getLength() != 0)
                        this.numberOfVotes = Integer.parseInt(((Element) node).getElementsByTagName("votes").item(0).getTextContent());
                    if (((Element) node).getElementsByTagName("watches").getLength() != 0)
                        this.numberOfWatcher = Integer.parseInt(((Element) node).getElementsByTagName("watches").item(0).getTextContent());
                    
                    this.numberOfAuthors = authors.size();
                    parsed = true;
                }
            }
            
        }catch(IOException e)
        {
            System.out.println(bugRepoNumber);
            //            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            System.out.println(bugRepoNumber);
            //            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            System.out.println(bugRepoNumber);
            //            e.printStackTrace();
        }
    }

    

	private String getByTagName(Node node, String tag) {
		if(((Element) node).getElementsByTagName(tag).getLength() != 0)
			return ((Element) node).getElementsByTagName(tag).item(0).getTextContent();
		return "";
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return this.project+", "+this.key +", "+ this.summary;
	}
}
