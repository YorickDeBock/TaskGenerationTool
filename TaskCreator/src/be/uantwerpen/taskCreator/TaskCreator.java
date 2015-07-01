package be.uantwerpen.taskCreator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TaskCreator {
	
	static final String ARCHITECTURE 	= "architecture";
	static final String SPEED	 		= "CPUSpeedMHz";
	static final String CORES 			= "cores";

	
	private Map<String, List<String>> paramsTarget;
	private Map<String, List<String>> paramsBenchmark; //These parameters have no required parameters. The template is: name of parameter', {include or not include, when not defined include or no include}
	private String benchMarkLocation;
	private String tasksetsFile;
	private Map<String, Integer> usablePrograms;
	private List<TaskSet> tasksets;
	public TaskCreator(String fileName, String benchMarkLocation,String tasksetsFile)
	{
		checkInput(fileName);
		this.benchMarkLocation = benchMarkLocation;
		this.tasksetsFile = tasksetsFile;
		this.usablePrograms = new HashMap<String, Integer>();
	}
	
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		paramsBenchmark = parser.getParameters("benchMark");
		paramsTarget = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!paramsTarget.containsKey(ARCHITECTURE))
			check = false;
		if(!paramsTarget.containsKey(SPEED))
			check = false;
		if(!paramsTarget.containsKey(CORES))
			check = false;
		
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	//reads the description files and selects the programs based on the target platform and benchmark parameters
	private void readDescriptionFilesAndTaskSets()
	{
		File desFolder = new File(benchMarkLocation+"/TACLeBench/descriptionFiles");
		Map<String,List<String>> parameters = new HashMap<String, List<String>>();
		XMLParser parser;
		XMLParser parser2 = new XMLParser(this.tasksetsFile);
		tasksets  = parser2.parseTaskset();
		
		if(!desFolder.isDirectory())
			System.err.println("No directory excist");
		
		 FilenameFilter filter = new FilenameFilter() {
		        public boolean accept(File directory, String fileName) {
		            return fileName.endsWith(".xml");
		        }
		        };
		
		for(File f:desFolder.listFiles(filter))
		{
			parser = new XMLParser(f);
			parameters = parser.parseDesriptionFile(f,paramsTarget.get(ARCHITECTURE).get(0));
			if(parameters == null)
				continue;
			usablePrograms.put(parameters.get("name").get(0),Integer.parseInt(parameters.get("WCET").get(1)));
		}	
		//TODO: include the selection parameters for the benchmarks			
	}
	
	public void createProgramSequence()
	{
		
        int[] weight = {0,58831,4830246,721212,3144388,225079,73883,20810614};
        
       
	}
	
	public static void main(String[] args) 
	{			
		TaskCreator gen = new TaskCreator("user.xml","C:/Users/Onderzoeker/CloudStation/DOCTORAAT/2015_STSM/","yorick.xml");
		gen.readDescriptionFilesAndTaskSets();
		gen.createProgramSequence();
		
	}

}
