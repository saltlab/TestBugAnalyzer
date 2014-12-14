package bugRepo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

import utils.Project;
import utils.ProjectRunner;
import FindBug.Automater.MultipleProjectRunner;


public class GitLogRunner {
	
	
	
	public boolean checkFileIsInTestDir(ArrayList<String> testDirs, List<DiffEntry> changedFiles)
	{
		for(DiffEntry changedFile : changedFiles)
		{
			boolean flag = false;
			for(String testDir : testDirs)
			{
				if(changedFile.getNewPath().contains(testDir))
				{
					flag = true;
					break;
				}
				
				if(flag == false)
					return false;
			}
			
		}
		
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
        for (RevCommit revCommit : logMsgs) {
        	
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
            df.setDiffComparator(RawTextComparator.DEFAULT);
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
            		
            		commits.add(new Commit(revCommit.getFullMessage(), revCommit.getAuthorIdent().getWhen(), patches));
            	}
            }
            
            
            df.release();
            out.close();
            parent =   revCommit;  
             
             
             
        }
        

		
		
		return commits;
	}
	
	
	
	
	public void runOnMultipleProjects() throws Exception
	{
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		
		
		ArrayList<Project> projects = mpr.listProjects();
		for (Project project : projects) 
		{
			
			

	        	
	        	ProjectRunner pr = new ProjectRunner(project);
	        	ArrayList<String> testDirs = pr.findTestDirNames();
	             File gitWorkDir = new File(project.getPath());
	             Git git = null;
	             git = Git.open(gitWorkDir);
	             
	             ArrayList<Commit> commits = getTestCommits(git, testDirs);
	             System.out.println(commits.size());
	           
	      }
	}
	
	
	
	public static void main(String[] args) throws Exception {
		GitLogRunner glr = new GitLogRunner();
		glr.runOnMultipleProjects();
		
	}
	
		
		
}

