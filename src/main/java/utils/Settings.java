package utils;

public class Settings {
	
	public static final String mavenLocalRep = "/home/arash/.m2/repository";
	public static final String mavenPath = "mvn";
	public static final String findBugPath = "/home/arash/Desktop/bug/findbugs-3.0.0/bin/findbugs";
	public static final String PMDPath = "/home/arash/Desktop/pmd-bin-5.2.1/bin/run.sh" ;
	public static String projectsPath = "/Users/Arash/Research/repos" ;
	public static final String logPath = "log.txt";
	public static final String bugReportPath = "bugreports/" ;
	public static final String projectListPath = "/Users/Arash/Research/repos";
	public static final String jiraXmlsPath = "/Users/Arash/Desktop/bugs/xmls";
	public static final String issuesApache = "https://issues.apache.org/jira/si/jira.issueviews:issue-xml/";
	public static final String[] jiraProjects = {"YARN","SQOOP", "SLIDER", "SHALE", "SAMZA", "ONAMI", "NUTCH", "MESOS", "MAPREDUCE",
			"LENS", "JCR", "HDFS", "HCATALOG", "HBASE", "HADOOP", "DERBY", "ACCUMULO", "CLOUDSTACK", "FLUME"};
	
	public static String getJiraProjectsRegex() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("(");
		for (int i = 0 ; i < jiraProjects.length - 1; i ++)
		{
			sb.append(jiraProjects[i]);
			sb.append("|");
		}
		sb.append(jiraProjects[jiraProjects.length-1]);
		sb.append(")");
		
		return sb.toString();
	}
}
