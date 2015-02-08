package bugRepo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.xml.sax.SAXException;

import bugRepo.EditedLines.LinePair;
import utils.Project;
import utils.ProjectRunner;
import utils.Settings;
import FindBug.Automater.MultipleProjectRunner;


public class GitLogRunner {
	
	long numberOfCommits;
	
	HashMap<String, Commit> mapOfNonTestBugReports = new HashMap<String, Commit>();
	HashSet<String> setOfNonTestBugReports = new HashSet<String>();
	
	
	public String deleteCodeSnippets(String input)
	{
		String open = "<div";
		String close = "</div>";
		int start = input.indexOf("<div class=\"code panel\"");
		int end = input.indexOf(close);
		int cur = start ;

		while (cur != -1 && cur < end)
		{
			cur = input.indexOf(open, cur + 1);
			if ( cur != -1 && cur < end)
				end = input.indexOf(close, end + 1);
		}
		
		return input.substring(0,start) + input.substring(end + close.length());
	}
	
	public void writeCleanedBugReportsToFile( ArrayList<Commit> commits)
	{
		try {
			String r = "(hbase|test|trunk|https|integrated|files|revision|result|patch|rev|fix|failure|stack|fails|run|time|tests|committed|issue|lt|gt|info|thread|debug|master|asf|regionserver|region|pool|waiting|method|orgrun|tilocblob|hbase|zookeeper|native|server|junit|row|current|count|gt|error|lt|release|xb|xe|removed|xa|api|xf|xd|internal|xc|future|proprietary|http|https|patch|warnings|newpatchfindbugswarningshbase|htmlfindbugs|hadoop|tests|hbase|findbugs|trunk|applied|results|author|javac|number|increase|javadoc|total|audit|info|debug|tilocblob|rs|method|native|wait|java|thread|locked|log|state|nid|tid|prio|net|javastate|ipc|main|hbase|integrated|files|revision|resu|trunk|failure|error|git|hadoop|svn|bb|api|fail|failing|ffa|edef|failed|success|newpatchfindbugswarnings|htmlfindbugs|hbase|findbugs|compat|appears|introduce|attachment|generated|output|ing|site|message|integrated|la|include|tags|included|core|current|xb|xe|xa|xf|xd|xc|src|amp|added|class|version|iew|comment|hbase|data|file|automatically|read)";
			for (Commit commit : commits)
			{
				if(commit.jiraBugReport.type.equals("Bug"))
				{
					Formatter fr = new Formatter("cleanedbugreports/" + commit.bugReportID + ".txt" );
					fr.format("%s %s", commit.message.toLowerCase().replaceAll("[0-9A-Za-z]*\\.java", "").replaceAll("/[^ ]*/", "").replaceAll("\\.[^ ]*\\.", "").replaceAll(r, ""),
							commit.jiraBugReport.summary.toLowerCase().replaceAll("[0-9A-Za-z]*\\.java", "").replaceAll("/[^ ]*/", "").replaceAll("\\.[^ ]*\\.", "").replaceAll(r, "") );
					for (String comment : commit.jiraBugReport.comments)
					{
						fr.format("%s", comment.toLowerCase().replaceAll("[0-9A-Za-z]*\\.java", "").replaceAll("/[^ ]*/", "").replaceAll("\\.[^ ]*\\.", "").replaceAll(r, ""));
					}
					
					fr.close();
					
				}
				
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public boolean checkFileIsInTestDir(ArrayList<String> testDirs, List<DiffEntry> changedFiles)
	{
		for(DiffEntry changedFile : changedFiles)
		{
			boolean flag = false;
			for(String testDir : testDirs)
			{
				if(changedFile.getNewPath().toLowerCase().contains(testDir))
				{
					flag = true;
					break;
				}
				
			}
			if(flag == false)
				return false;
			
		}
		
		if (changedFiles.size() == 1 && changedFiles.get(0).getNewPath().contains("CHANGES.txt"))
			return false;
		
		return true;
		
	}
	
	
	public ArrayList<EditedLines> extractLines(EditList editList, RawText a, RawText b)
	{
		
		ArrayList<EditedLines> editedLinesList = new ArrayList<EditedLines>();
		
		for(Edit edit : editList)
		{
			EditedLines editedLines = new EditedLines(edit, a, b);
			editedLinesList.add(editedLines);
		}
		
		return editedLinesList;

	}
	
	
	public ArrayList<Commit> getTestCommits(Git git, ArrayList<String> testDirs) throws Exception
	{
		
		
		ArrayList<Commit> commits = new ArrayList<Commit>();
			
		Repository repo = git.getRepository();
        
        LogCommand log = git.log();
        
        ObjectId lastCommitId = repo.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(repo,1000);
        RevCommit parent = rw.parseCommit(lastCommitId);
        
        rw.sort(RevSort.COMMIT_TIME_DESC);
        rw.markStart(parent);
         
        
//        log.setMaxCount();
        Iterable<RevCommit> logMsgs = log.call();
        
        numberOfCommits = 0;
        for (RevCommit revCommit : logMsgs) {
        	numberOfCommits++;
//            System.out.println("\n\n\n\n\n\n\n\n\n\n----------------------------------------");
//            System.out.println("commit    "  + revCommit);
//            System.out.println("commit.toObjectId()    "  + revCommit.toObjectId());
//            System.out.println(" commit.getAuthorIdent().getName()         "  + revCommit.getAuthorIdent().getName());
//            System.out.println(""  + revCommit.getAuthorIdent().getWhen());
//            System.out.println(" commit.getFullMessage())--- " + revCommit.getFullMessage());
//            System.out.println("---DIF STARTING ------------------------");
             
            
             
            ByteArrayOutputStream out = new ByteArrayOutputStream();
             
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(repo);
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setDetectRenames(true);
  
  
  
//            System.out.println(revCommit.getParentCount());
//            if(revCommit.getParentCount() <= 0 )
//            	continue;
            
            for(RevCommit parentCommit : revCommit.getParents())
            {
            	
            	List<DiffEntry> diffs = df.scan(revCommit.getTree(), parentCommit.getTree());
            	
            	
            	
            	
            	if(checkFileIsInTestDir(testDirs, diffs))
            	{
            		ArrayList<Patch> patches = new ArrayList<Patch>();
            		
            		for (DiffEntry diff : diffs) 
            		{
            			
            			DiffFormatter.FormatResult fr = df.createFormatResult(diff);
            			RawText rawTextA = fr.a;
            			RawText rawTextB = fr.b;
            			
            			ArrayList<ArrayList<EditedLines>> editedLineLists = new ArrayList<ArrayList<EditedLines>>();
            			FileHeader fh = df.toFileHeader(diff);
            			List<HunkHeader> hunkHeaders = (List<HunkHeader>) fh.getHunks();
            			for(HunkHeader hunkHeader : hunkHeaders)
            			{
            				EditList el = hunkHeader.toEditList();
            				editedLineLists.add(extractLines(el, rawTextA, rawTextB));
            			}
            			
            			Patch patch = new Patch(diff.getOldPath(), diff.getNewPath(), editedLineLists);
            			
            			patches.add(patch);
            			
//            		System.out.println("changeType=" + diff.getChangeType().name()
//            				+ " \n newMode=" + diff.getNewMode().getBits()
//            				+ " \nnewPath=" + diff.getNewPath()         
//            				+ " \nold path " + diff.getOldPath()
//            				+ " \nHash code " + diff.hashCode()
//            				+ " \nString  " + diff.toString()
//            				+ " \nchange " + diff.getChangeType().toString()
//            				);
//            		
//            		
//            		
//            		df.format(diff);
//            		
//					String diffText = out.toString("UTF-8");
//					System.out.println(diffText);
            		}
            		
            		
            		// setting double links
            		Commit commit = new Commit(revCommit, revCommit.getFullMessage(), revCommit.getAuthorIdent().getWhen(), patches);
            		for(Patch patch : patches)
            		{
            			patch.setCommit(commit);
            			for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
            			{
            				for(EditedLines editedLines : editedLinesList)
            					editedLines.setPatch(patch);
            			}
            		}
            		
            		commits.add(commit);
            	} else 
            	{
            		
            		ArrayList<Patch> patches = new ArrayList<Patch>();
            		for (DiffEntry diff : diffs) 
            		{
            			Patch patch = new Patch(diff.getOldPath(), diff.getNewPath(), null);
            			patches.add(patch);
            		}
            		
            		
            		Commit commit = new Commit(revCommit, revCommit.getFullMessage(), revCommit.getAuthorIdent().getWhen(), patches);
            		if (commit.checkMessageForBugReport())
            		{
            			setOfNonTestBugReports.add(commit.getBugRepoID());
            			mapOfNonTestBugReports.put(commit.getBugRepoID(),commit);
            		}
            	}
            }
            
            
            df.release();
            out.close();
            parent =   revCommit;  
             
             
             
        }
        

        System.out.println("total number of commits : " + numberOfCommits);		
		return commits;
	}
	
	
	public void printCommits(ArrayList<Commit> commits, Repository repo)
	{
		for(Commit commit : commits)
		{
			System.out.println("*****commit start*****");
			System.out.println(commit.formatDiffs(repo));
		}
	}
	
	public void writeTofile(String fileName,ArrayList<Commit> commits, Repository repo) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		fr.format("number of commits : %d\n", commits.size());
		for(Commit commit : commits)
		{
			fr.format("*****commit start*****\n");
			fr.format("%s\n",commit.formatDiffs(repo));
		}
		
		fr.close();
	}
	
	
	public void createDir(String dirName)
	{
		  File dir = new File(dirName);

		  if (!dir.exists()) {

		    try{
		        dir.mkdir();
		     } catch(SecurityException se){
		    	 se.printStackTrace();
		     }
		  }
	}
	
	public ArrayList<Commit> removeNullBugReports(ArrayList<Commit> commits)
	{
		ArrayList<Commit> removedNulls = new ArrayList<Commit>();
		for (Commit commit : commits)
		{
			if (commit.jiraBugReport != null)
				removedNulls.add(commit);
		}
		return removedNulls;
	}
	
	public ArrayList<Commit> removeNullProdBugReports(ArrayList<Commit> commits)
	{
		ArrayList<Commit> removedNulls = new ArrayList<Commit>();
		for (Commit commit : commits)
		{
			if (commit.jiraBugReport != null && !setOfNonTestBugReports.contains(commit.bugReportID))
				removedNulls.add(commit);
		}
		return removedNulls;
	}
	
	
	public void initializeListNonTestBugReports() throws FileNotFoundException
	{
		if(new File(Settings.listOfNonTestBugReportsPath).exists())
			setOfNonTestBugReports = readNonTestBugReportsFromFile();
		else
			writeNonTestBugReportsToFile();
	}
	

	
	
	public void writeNonTestBugReportsWithPathToFile() throws FileNotFoundException
	{
		Formatter fr = new Formatter(Settings.listOfNonTestBugReportsWithPath);
		StringBuffer sb = new StringBuffer();
		for (Entry<String, Commit> entry : mapOfNonTestBugReports.entrySet())
		{
			sb.append(entry.getKey()+",");
			try{
			for (int i = 0; i < entry.getValue().patchs.size(); i++){
				sb.append(entry.getValue().patchs.get(i).newFilePath);
				if(i != entry.getValue().patchs.size()-1)
					sb.append(",");
			}
			sb.append("\n");
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		fr.format("%s", sb.toString());
		fr.flush();
		fr.close();
	}
	
	public void writeNonTestBugReportsToFile() throws FileNotFoundException
	{
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		
		Formatter fr = new Formatter(Settings.listOfNonTestBugReportsPath);
		
		ArrayList<Project> projects = mpr.listAllProjects();
		
		
		for (Project project : projects) 
		{
			try{
				
				System.out.println("*************"+ project + "**************");
				
				createDir("results/" + project.getName());
				ArrayList<String> testDirs = new ArrayList<String>();
				testDirs.add("test");
				testDirs.add("changes.txt");
				File gitWorkDir = new File(project.getPath());
				Git git = null;
				git = Git.open(gitWorkDir);
				
				ArrayList<Commit> commits = getTestCommits(git, testDirs);
			}catch(Exception e)
			{
				// expected
			}
		}
		
		for(String bugRepoID : setOfNonTestBugReports)
		{
			fr.format("%s\n", bugRepoID);
		}
		
		
		fr.close();
		
		writeNonTestBugReportsWithPathToFile();
		
	}
	
	public HashSet<String> readNonTestBugReportsFromFile() throws FileNotFoundException
	{
		
		HashSet<String> set = new HashSet<String>();
		Scanner sc = new Scanner(new File(Settings.listOfNonTestBugReportsPath));
		
		while(sc.hasNextLine())
			set.add(sc.nextLine());
		
		sc.close();
		return set;
	}
	
	public void runOnMultipleProjects() throws Exception
	{
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		
		Formatter numbersFr = new Formatter("results/stats.csv");
		
		ArrayList<Project> projects = mpr.listAllProjects();
		
		initializeListNonTestBugReports();
		
		
		for (Project project : projects) 
		{
			try{
				
				System.out.println("*************"+ project + "**************");
				
				createDir("results/" + project.getName());
//				ProjectRunner pr = new ProjectRunner(project);
//				ArrayList<String> testDirs = pr.findTestDirNames();
				ArrayList<String> testDirs = new ArrayList<String>();
				testDirs.add("test");
				File gitWorkDir = new File(project.getPath());
				Git git = null;
				git = Git.open(gitWorkDir);
				
				ArrayList<Commit> commits = getTestCommits(git, testDirs);
				System.out.println("number of commits that only change test files : " + commits.size());
//	             ArrayList<Commit> assertionCommits = getCommitsThatHaveKeyword(commits, "assert");
//	             System.out.println("number of commits that change assertions : " + assertionCommits.size());
//	             writeTofile(project + "commitsChangingAssertions.txt",assertionCommits, git.getRepository());
				
				ArrayList<EditedLines> assertionFaults = checkForKeywordChanges(commits, ".*assert.*\\(.*");
				writeEditedLines("results/" + project.getName() + File.separatorChar + project.getName()+"_assertionFaults.txt", assertionFaults);
				ArrayList<EditedLines> wrongControlFlow = checkForKeywordChanges(commits, "[ ]*for[ ]*\\(.*");
				wrongControlFlow.addAll(checkForKeywordChanges(commits, "[ ]*if[ ]*\\(.*\\).*"));
				writeEditedLines("results/" + project.getName() + File.separatorChar + project.getName()+"_wrongControlFlow.txt", wrongControlFlow);
//	             
//	             System.out.println("number of commits that edit assertions : " + assertionFaults.size());
				
				ArrayList<Commit> bugReportCommits = getCommitsWithBugReport(commits);
////	             writeTofile("commitsWithBugReport.txt",bugReportCommits,git.getRepository());
				System.out.println("number of commits that point to a bug report: " + bugReportCommits.size());
				
				Formatter bugRepoFr = new Formatter("results/" + project.getName() + File.separatorChar + project.getName()+"_BugReportCommits.txt");
				
				
				long numberOfBugTypeBugReports = 0;
				long numberOfNonTestCompBugReports = 0; 
				long numberOfNonProductionCodeBugReport = 0;
				for(Commit commit : bugReportCommits)
				{
					try{
						commit.extractJiraBugReport();
						if(commit.jiraBugReport != null )
						{
							if (!setOfNonTestBugReports.contains(commit.bugReportID))
							{
								numberOfNonProductionCodeBugReport++;
								if (commit.jiraBugReport.type.equals("Bug"))
									numberOfBugTypeBugReports++;
								if(commit.jiraBugReport.type.equals("Bug") && !commit.jiraBugReport.component.equals("test") )
								{
									bugRepoFr.format("%s,%s\n", commit.bugReportID, commit.jiraBugReport.link);
									numberOfNonTestCompBugReports ++;
									commit.writeBugReportToFile("savedBugReports/");
//	            		 System.out.println("Jira Link : " + commit.jiraBugReport.link);
//	            		 System.out.println("commit message : "+commit.message);
//	            		 System.out.println("jira topic : " +  commit.jiraBugReport.summary);
									
								}
								
							}
						}
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				bugRepoFr.close();
				
				
				bugReportCommits = removeNullBugReports(bugReportCommits);
				ArrayList<Commit> testBugReportCommits = removeNullBugReports(bugReportCommits);
				
				numbersFr.format("%s,%s,%s,%s,%s,%s,%s\n",project.getName(), numberOfCommits, commits.size(), bugReportCommits.size(),numberOfNonProductionCodeBugReport,numberOfBugTypeBugReports,numberOfNonTestCompBugReports);
				
				
				try{
					String stats = getBugReportStatistics(testBugReportCommits);
					writeCleanedBugReportsToFile(testBugReportCommits);
					Formatter statFr = new Formatter("results/" + project.getName() + File.separatorChar + project.getName()+"_stat.txt");
					statFr.format("%s\n", stats);
					statFr.close();
					System.out.println(stats);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
				numberOfFilesChangedMetric("results/" + project.getName() + File.separatorChar + project.getName()+"_numberOfFilesChangedMetric.txt", commits);
//	             writeCommitsWithZeroPatches("zero.txt", commits, git.getRepository());
				
				numberOfEditedLinesPerFile("results/" + project.getName() + File.separatorChar + project.getName()+"_editedLinesPercommit.csv", commits);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
	           
	      }
		
		numbersFr.close();
	}
	
	
	public void numberOfEditedLinesPerFile(String fileName, ArrayList<Commit> commits) throws FileNotFoundException
	{
		
		
		Formatter fr = new Formatter(fileName);
		
		for(Commit commit : commits)
		{
			int num = 0;
			for(Patch patch : commit.patchs)
			{
				for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
				{
					for(EditedLines editedLines : editedLinesList)
					{
						num += editedLines.addedLines.size();
						num += editedLines.removedLines.size();
					}
				}
			}
			fr.format("%s,%d\n", commit.revCommit.getId(), num);
		}
		fr.close();
	}
	
	public void writeCommitsWithZeroPatches(String fileName, ArrayList<Commit> commits, Repository repo) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		
		for (Commit commit : commits)
		{
			if (commit.patchs.size() == 0)
				fr.format("%s\n", commit.formatDiffs(repo));
		}
		
		fr.close();
	}
	
	public void numberOfFilesChangedMetric(String fileName, ArrayList<Commit> commits) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		for(Commit commit : commits)
		{
			fr.format("%s ,%d\n",commit.revCommit.getId().toString() , commit.patchs.size());
		}
		fr.close();
	}
	
	
	public void collectMetric(ArrayList<Commit> commits)
	{
		
	}
	
	public String getBugReportStatistics(ArrayList<Commit> commits)
	{
		HashMap<String, HashMap<String,Integer>> statMap = new HashMap<String, HashMap<String,Integer>>();
		HashMap<String, Integer> componentMap = new HashMap<String, Integer>();
		StringBuffer sb = new StringBuffer();
		
		for ( Commit commit : commits)
		{
			if (!statMap.containsKey(commit.jiraBugReport.project))
			{
				statMap.put(commit.jiraBugReport.project, new HashMap<String, Integer>());
			}
			
			HashMap<String, Integer> typeMap = statMap.get(commit.jiraBugReport.project);
			
			if (!typeMap.containsKey(commit.jiraBugReport.type))
			{
				typeMap.put(commit.jiraBugReport.type, 1);
			}
			else
			{
				typeMap.put(commit.jiraBugReport.type, typeMap.get(commit.jiraBugReport.type) + 1);
			}
			
			if(commit.jiraBugReport.type.equals("Bug"))
			{
				componentMap.put(commit.jiraBugReport.component, (componentMap.get(commit.jiraBugReport.component) == null ? 0 : componentMap.get(commit.jiraBugReport.component)) + 1);
			}
		}
	
		for (Map.Entry<String, HashMap<String, Integer>> projectEntry : statMap.entrySet())
		{
			sb.append(projectEntry.getKey() + " : \n");
			
			for ( Map.Entry<String, Integer> typeEntry : projectEntry.getValue().entrySet() )
			{
				sb.append("\t" + typeEntry.getKey() + " : " + typeEntry.getValue() + "\n");
				
				if (typeEntry.getKey().equals("Bug"))
				{
					for (Map.Entry<String, Integer> entry : componentMap.entrySet())
					{
						sb.append("\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
					}
				}
			}
		}
		
		
		
		return sb.toString();
		
	}
	
	
	
	public ArrayList<Commit> getCommitsThatHaveKeyword(ArrayList<Commit> commits, String keyword)
	{
//		final String keyword = "assert";
		ArrayList<Commit> assertionCommits = new ArrayList<Commit>();
		for(Commit commit : commits)
		{
			outerloop:
			for(Patch patch : commit.patchs)
			{
				for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
				{
					for(EditedLines editedLines : editedLinesList)
					{
						for(String addedLine : editedLines.addedLines)
						{
							if(addedLine.contains(keyword))
							{
								assertionCommits.add(commit);
								break outerloop;
								
							}
						}
						for(String removedLine : editedLines.removedLines)
						{
							if(removedLine.contains(keyword))
							{
								assertionCommits.add(commit);
								break outerloop;
							}
						}
						
					}
				}
			}
		}
		
		return assertionCommits;
	}
	
	
	
	public ArrayList<EditedLines> checkForKeywordChanges(ArrayList<Commit> commits, String regrex)
	{
//		final String keyword = "assert";
		ArrayList<EditedLines> editedKeywordLines = new ArrayList<EditedLines>();
		for(Commit commit : commits)
		{
			outerloop:
			for(Patch patch : commit.patchs)
			{
				for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
				{
					for(EditedLines editedLines : editedLinesList)
					{
						boolean keywordRemoved = false, keywordAdded = false;
						for(String addedLine : editedLines.addedLines)
						{
							if(addedLine.matches(regrex))
							{
								keywordAdded = true;
								break;
							}
						}
						for(String removedLine : editedLines.removedLines)
						{
							if(removedLine.matches(regrex))
							{
								keywordRemoved = true;
								break;
							}
						}
						
						if(keywordRemoved & keywordAdded)
						{
							editedKeywordLines.add(editedLines);
						}
					}
				}
			}
		}
		
		return editedKeywordLines;
	}

	
	
	public void writeEditedLines(String fileName, ArrayList<EditedLines> editedLinesList) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		fr.format("number of edits : %d\n", editedLinesList.size());
		for(EditedLines editedLine : editedLinesList)
		{
			
			fr.format("***********\n file: %s\ncommit: %s\nnumber of editedLines : %d\n\n",editedLine.getPatch().newFilePath,editedLine.getPatch().getCommit().message, editedLine.getPatch().getCommit().getNumberOfEditedLines());
			
			
			for(LinePair pair : editedLine.matches)
			{
				fr.format("- %s\n",pair.a);
				fr.format("+ %s\n",pair.b);
			}
			
//			for(String removedLine : editedLine.removedLines)
//				fr.format("- %s\n",removedLine);
//			
//			for(String addedLine : editedLine.addedLines)
//				fr.format("+ %s\n",addedLine);
			
			fr.format("\n");
		}
		
		fr.close();
	}
	
	
	public ArrayList<Commit> getCommitsWithBugReport(ArrayList<Commit> commits)
	{
		ArrayList<Commit> bugReportCommits = new ArrayList<Commit>();
		for(Commit commit : commits)
		{
			if(commit.checkMessageForBugReport())
				bugReportCommits.add(commit);
		}
		
		return bugReportCommits;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Execution started at " +  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		GitLogRunner glr = new GitLogRunner();
//		glr.runOnMultipleProjects();
		glr.writeNonTestBugReportsToFile();
		System.out.println("Execution finished at " +  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		
	}
	
		
		
}

