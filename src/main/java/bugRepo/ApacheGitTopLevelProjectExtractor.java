package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

public class ApacheGitTopLevelProjectExtractor {
	
	public static HashMap<String, CountProject> lowToTopProjects = new HashMap<String, CountProject>();
	public static HashMap<String, ArrayList<CountProject>> topToLowProjects = new HashMap<String, ArrayList<CountProject>>();
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		
		
		
		
		readTopToLowRelations();
		
		readCountCSV();
		
//		addJiraResults();
		
		
		analysis();
		
//		writeSums();
		writeAllResults();
		
		
	}
	
	
	public static void addJiraResults()
	{
		lowToTopProjects.get("DERBY").num += 658;
		lowToTopProjects.get("HDFS").num += 256;
		lowToTopProjects.get("HBASE").num += 251;
		lowToTopProjects.get("HADOOP").num += 233;
		lowToTopProjects.get("ACCUMULO").num += 196;
		lowToTopProjects.get("MAPREDUCE").num += 156;
		lowToTopProjects.get("MESOS").num += 72;
		lowToTopProjects.get("CLOUDSTACK").num += 57;
		lowToTopProjects.get("JCR").num += 46;
		lowToTopProjects.get("FLUME").num += 35;
		lowToTopProjects.get("AXISCPP").num += 34;
		lowToTopProjects.get("AMBER").num += 27;
		lowToTopProjects.get("SLIDER").num += 15;
		lowToTopProjects.get("HCATALOG").num += 15;
		lowToTopProjects.get("SAMZA").num += 9;
		lowToTopProjects.get("SHALE").num += 6;
		lowToTopProjects.get("GIRAPH").num += 5;
		lowToTopProjects.get("SQOOP").num += 3;
		lowToTopProjects.get("NUTCH").num += 3;
		lowToTopProjects.get("ONAMI").num += 3;
		lowToTopProjects.get("TAJO").num += 1;
		lowToTopProjects.get("YARN").num += 1;
		
	}
	
	public static void analysis()
	{
		
		int count = 0;
		int haveBug = 0;
		for (Entry<String, CountProject> entry : lowToTopProjects.entrySet())
		{
			count ++;
			if (entry.getValue().num > 0)
				haveBug ++;
			
		}
		
		System.out.printf("out of %d asf projects %d have at least one bug report ", count, haveBug);
	}
	
	public static void writeSums() throws FileNotFoundException
	{
		Formatter fr = new Formatter("topLevelProjectsSum.csv");
		
		for(Entry<String, ArrayList<CountProject>> entry : topToLowProjects.entrySet())
		{
			
			int sum = 0;
			for(CountProject cp : entry.getValue())
			{
				sum += cp.num;
			}
			if (sum != 0)
				fr.format("%s,%d\n", entry.getKey(), sum);
		}
		
		fr.close();
	}
	public static void writeAllResults() throws FileNotFoundException
	{
		Formatter fr = new Formatter("lowLevelProjectsSum.csv");
		
		for(Entry<String, CountProject> entry : lowToTopProjects.entrySet())
		{
			
			if (entry.getValue().num != 0)
				fr.format("%s,%d\n", entry.getValue().name, entry.getValue().num);
		}
		
		fr.close();
	}


	private static void readTopToLowRelations() throws IOException {
		Document doc = Jsoup.connect("https://issues.apache.org/jira/secure/BrowseProjects.jspa#all").get();
		
		Elements body = doc.getElementsByClass("aui-page-panel-content");
		
		
		for (int i = 0; i < body.get(0).childNodeSize() - 1; i++)
		{
			try{
				
				
				Element tr = body.get(0).child(i);
				Elements headers = tr.getElementsByClass("mod-header");
				String topLevelProject = headers.get(0).child(0).text();
				System.out.println(topLevelProject);
				
				ArrayList<CountProject> lowLevelProjects = new ArrayList<CountProject>();
				
				Elements contents = tr.getElementsByAttributeValue("data-cell-type", "name");
					for(Element content : contents)
					{
						
						CountProject cp = new CountProject();
						String key = content.siblingElements().get(content.siblingIndex()-2).text();
						cp.name = content.text();
						cp.key = key;
						cp.topLevelProject = topLevelProject;
						lowToTopProjects.put(key, cp);
						lowLevelProjects.add(cp);
						
						
						
//						System.out.println("\t" + content.child(0).text());
						System.out.println("\t" + content.siblingElements().get(content.siblingIndex()-2).text());
					}
				
				
				topToLowProjects.put(topLevelProject, lowLevelProjects);
				
				

			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	
	public static void readCountCSV() throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File("count.csv"));
		sc.useDelimiter(", *|\n");
	
		while(sc.hasNext())
		{
			String key = sc.next();
			int num = Integer.parseInt(sc.next());
			System.out.println(key);
			System.out.println(num);
			lowToTopProjects.get(key).num = num;
		}
		
	
		
	}
//	public static void main1(String[] args) throws ParserConfigurationException, SAXException, IOException {
//		
//		Scanner sc = new Scanner(new File("stat.csv"));
//		StringBuffer sb = new StringBuffer();
//		while(sc.hasNext())
//		{
//			sb.append(sc.nextLine());
//		}
//		
//		String jira = sb.toString();
//		Document doc = Jsoup.connect("http://git.apache.org").get();
//		
////		Document doc = Jsoup.parseBodyFragment(sb.toString());
////		Elements tds = doc.select("tbody tr td")
//		
//		Formatter fr = new Formatter("projects.txt");
//		Elements body = doc.getElementsByTag("tbody");
//		
//		for (int i = 0; i < body.get(0).childNodeSize() - 1; i++)
//		{
//			try{
//				
//				Element tr = body.get(0).child(i);
////			System.out.println(body.get(0).child(i));
//				
//				String title = tr.child(1).text();
//				String link = tr.child(2).child(0).text();
//				
//				title = title.toLowerCase().replace("apache", "");
//				String[] words = title.split(" ");
//				
//				boolean flag = false;
//				
//				for (String word : words)
//				{
//					if (jira.indexOf(word) != -1)
//					{
//						flag = true;
//						break;
//					}
//				}
//				if(flag)
//				{
//					System.out.println(title + " " + link);
//					fr.format("git clone %s\n", link);
//				}
//			}catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//			
//		}
//		
//		fr.close();
//	}
	
	

}
class CountProject{
	
	String topLevelProject;
	String key;
	String name;
	int num;
	
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.key == ((CountProject)obj).key;
	}
	
}

