package bugRepo;

import java.util.Date;
import java.util.ArrayList;

public class Commit {
	
	String message;
	Date date;
	ArrayList<Patch> patchs;
	
	public Commit(String message, Date date, ArrayList<Patch> patchs)
	{
		this.message = message;
		this.date = date;
		this.patchs = patchs;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "message: "+ message + ", patches: "+ patchs;
	}
}
