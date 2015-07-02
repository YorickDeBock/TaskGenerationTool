package be.uantwerpen.taskCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TaskCreator {
	
	static final String ARCHITECTURE 	= "architecture";
	static final String SPEED	 		= "CPUSpeedMHz";
	static final String CORES 			= "cores";
	static final double MAX_DEVIATION 	= 2.0;
	
	private Map<String, List<String>> paramsTarget;
	private Map<String, List<String>> paramsBenchmark; //These parameters have no required parameters. The template is: name of parameter', {include or not include, when not defined include or no include}
	private String benchMarkLocation;
	private String tasksetsFile;
	private List<WCET> usablePrograms;
	private List<TaskSet> tasksets;
	public TaskCreator(String fileName, String benchMarkLocation,String tasksetsFile)
	{
		checkInput(fileName);
		this.benchMarkLocation = benchMarkLocation;
		this.tasksetsFile = tasksetsFile;
		this.usablePrograms = new ArrayList<WCET>();
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
		WCET tupple = null;
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
			tupple = new WCET(parameters.get("name").get(0),Double.parseDouble(parameters.get("WCET").get(1)));
			usablePrograms.add(tupple);
		}	
		
		
		//TODO: include the selection parameters for the benchmarks			
	}
	//Create a sequence of programs based on the input of the benchmark program available
	//Using a Integer Linear Program solver
	public void createProgramSequence()
	{
        ILPSolver solver = new ILPSolver(usablePrograms);
        List<WCET> programSequence = null;
        PrintWriter writer;
        String resultString = null;
        double combinedWCET;
        int ILP = 0;
        try {
        	File dir = new File("TaskSet_"+tasksetsFile.substring(0, tasksetsFile.length()-4));
        	dir.mkdir();
        	for(TaskSet ts:tasksets)
        	{
				writer = new PrintWriter("TaskSet_"+tasksetsFile.substring(0, tasksetsFile.length()-4)+"/"+ts.getTaskSetName()+".xml");
				writer.print("<taskSet name=\""+ts.getTaskSetName()+"\" load=\""+ts.getLoad()+"\">\n");
				
		        for(Task t:ts.getTasks())
				{
		        	programSequence = solver.findProgramCombination(t.getExe(),true);
		        	for(WCET w:usablePrograms)
		    			System.out.println(w.getProgramName()+" "+w.getExecTime());
		        	System.out.println();
		        	for(WCET w:programSequence)
		    			System.out.println(w.getProgramName()+" "+w.getExecTime());
		        	combinedWCET = 0;
		        	
		        	for(WCET w:programSequence)
		        	{
		        		combinedWCET+=w.getExecTime()*w.getNumberOfExec()/1000;
		        	}
		        	if((t.getExe()-combinedWCET)/t.getExe()*100>MAX_DEVIATION)
		        	{
		        		combinedWCET = 0;
	        			programSequence = solver.findProgramCombination(t.getExe(),false);
			        	for(WCET w:programSequence)
			        		combinedWCET+=w.getExecTime()*w.getNumberOfExec()/1000;
			        	ILP++;
		        	}
		        	
		        	resultString = "<task name=\""+t.getName()+"\" p=\""+t.getPeriod()+"\" d=\""+t.getDeadline()+"\" wanted_e=\""+t.getExe()+"\" real_e=\""+combinedWCET+"\" >\n";
		        	
		        	for(WCET w:programSequence)
		        	{
		        		if(w.getNumberOfExec()!=0)
		        			resultString = resultString+"<program name=\""+w.getProgramName()+"\" n=\""+w.getNumberOfExec()+"\" />\n";
		        	}
		        	resultString = resultString +"</task>\n";
		        	writer.print(resultString);
		        		
				}  
		        writer.print("</taskSet>\n");
		        writer.close();
        	}
        	System.out.println(1000 - ILP + " of a 1000 Tasks BLP and " + ILP +" through ILP");
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) 
	{			
		TaskCreator gen = new TaskCreator("user.xml","C:/Users/Onderzoeker/CloudStation/DOCTORAAT/2015_STSM/","yorick.xml");
		gen.readDescriptionFilesAndTaskSets();
		gen.createProgramSequence();
		
	}

}
