package be.uantwerpen.taskCreator;

import java.util.List;
import java.util.Map;

public class TaskCreator {
	
	static final String ARCHITECTURE 	= "architecture";
	static final String SPEED	 		= "CPUSpeedMHz";
	static final String CORES 			= "cores";

	
	private Map<String, List<String>> params;

	
	public TaskCreator(String fileName)
	{
		checkInput(fileName);
	}
	
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		params = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!params.containsKey(ARCHITECTURE))
			check = false;
		if(!params.containsKey(SPEED))
			check = false;
		if(!params.containsKey(CORES))
			check = false;
		
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	public static void main(String[] args) 
	{			
		TaskCreator gen = new TaskCreator("user.xml");
		
	}

}
