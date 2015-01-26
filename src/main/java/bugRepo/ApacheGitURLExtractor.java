package bugRepo;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

public class ApacheGitURLExtractor {
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		
		Scanner sc = new Scanner(new File("stat.csv"));
		StringBuffer sb = new StringBuffer();
		while(sc.hasNext())
		{
			sb.append(sc.nextLine());
		}
		
		String jira = sb.toString();
		Document doc = Jsoup.connect("http://git.apache.org").get();
		
//		Document doc = Jsoup.parseBodyFragment(sb.toString());
//		Elements tds = doc.select("tbody tr td")
		
		Formatter fr = new Formatter("projects.txt");
		Elements body = doc.getElementsByTag("tbody");
		
		for (int i = 0; i < body.get(0).childNodeSize() - 1; i++)
		{
			try{
				
				Element tr = body.get(0).child(i);
//			System.out.println(body.get(0).child(i));
				String abbr = tr.child(0).text();
				String title = tr.child(1).text();
				String link = tr.child(2).child(0).text();
				
				System.out.println(abbr.replace(".git", "")+","+ title);
				

			}catch(Exception e)
			{
//				e.printStackTrace();
			}
			
		}
		
		fr.close();
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

class ApacheProject{
	String name, gitURL;
	
	public ApacheProject(String name, String gitURL)
	{
		this.name = name;
		this.gitURL = gitURL;
	}
}