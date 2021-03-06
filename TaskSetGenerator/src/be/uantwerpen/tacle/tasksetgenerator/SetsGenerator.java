package be.uantwerpen.tacle.tasksetgenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


public class SetsGenerator {

	/*
	 * @param: osScheduler: de scheduler of the hypervisor
	 * @Param: periodmin,periodmax,periodStep: the uniform distribution of taks period between [periodmax,periodmin] with a step x ms
	 * @Param:setsPerLoad: per load and binomal distribution to generated sets must be generated (min is 1)
	 */
	
	static final String NAME 			= "name";
	static final String LOCATION 		= "tasksetlocation";
	static final String UTIL_MIM 		= "globalUtilizationMin";
	static final String UTIL_MAX 		= "globalUtilizationMax";
	static final String UTIL_STEP 		= "utilizationStep";
	static final String NUM_TASKS 		= "numberOfTasks";
	static final String NUM_TASKSETS 	= "numberOfTaskSets";
	static final String PERIOD_MIN 		= "periodMin";
	static final String PERIOD_MAX 		= "periodMax";
	static final String PERIOD_STEP 	= "periodStep";
	static final String SEED 			= "seed";

	
	private List<Task> taskset;
	private Map<String, List<String>> params;
	
	public SetsGenerator (String fileName)
	{
		checkInput(fileName);
	}
	
	public void generateTaskset()
	{
		int setsperload = Integer.parseInt(params.get(NUM_TASKSETS).get(0));
		String name = params.get(NAME).get(0);
		String location = params.get(LOCATION).get(0);
		String taskSetName = null;
		double minLoad =  Double.parseDouble(params.get(UTIL_MIM).get(0));
		double maxLoad = Double.parseDouble(params.get(UTIL_MAX).get(0));
		double stepLoad = Double.parseDouble(params.get(UTIL_STEP).get(0));
		int minPeriod = Integer.parseInt(params.get(PERIOD_MIN).get(0));
		int maxPeriod = Integer.parseInt(params.get(PERIOD_MAX).get(0));
		int stepPeriod = Integer.parseInt(params.get(PERIOD_STEP).get(0));
		int numberTasks = Integer.parseInt(params.get(NUM_TASKS).get(0));
		int seed = Integer.parseInt(params.get(SEED).get(0));
		int i = 1;
		
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(name+".xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writer.print("<taskSets>\n");
		for(double load=minLoad;load<=maxLoad;load+=stepLoad)
		{
			for(int j=0;j<setsperload;j++)
			{
				taskSetName = "TaskSet_"+new Double(Math.round(100*load)).toString()+"_v"+j;
				createTasks(minPeriod,maxPeriod, stepPeriod ,load,numberTasks,seed+(j+1)*(int)Math.round(10*load));
				writer.print(XMLgenerator(taskSetName,Math.round(100*load)));
				i++;
			}
		}
		writer.print("</taskSets>\n");

		writer.close();
	}
	
	private List<Double> genTaskUtils(int numTask, double utilization, int seed)
	{
		boolean discard;
		List<Double> taskUtils = null;
		double nextSumU;
		double sumU;
		Random random = new Random(seed);
		do {
			sumU = utilization;
			taskUtils = new ArrayList<Double>();
			discard = false;
			for (int i = 0; i < numTask - 1; i++) {
				nextSumU = sumU * Math.pow(random.nextDouble(), (double) 1 / (numTask - (i + 1)));
				taskUtils.add(i,sumU - nextSumU);
				sumU = nextSumU;
				if (taskUtils.get(i) > 0.95) {
					discard = true;
				}
			}
			taskUtils.add(numTask-1,sumU);
			if (taskUtils.get(numTask - 1) > 1) {
				discard = true;
			}
		} while (discard || !(getTasksetUtil(taskUtils)<=Math.ceil(utilization)));
		return taskUtils;
	}
	
	private void createTasks(double periodmin, double periodmax, double periodStep, double utilization, int numberTasks, int seed)
	{
		Random random = new Random(seed);
		double period=0,exec;
		List<Double> taskUtils;

		Task task = null;
		taskset = new ArrayList<Task>();
		taskUtils = genTaskUtils(numberTasks, utilization, seed*2);
		
		for(Double u: taskUtils)
		{
			period = (int)(Math.round((random.nextDouble()*(periodmax/periodStep-periodmin/periodStep)+periodmin/periodStep))*periodStep);
			if(taskUtils.indexOf(u)==taskUtils.size()-1)
			{
				u=utilization-getTasksetU(taskset);
				exec = (int)Math.floor(u*period);
			}
			else
				exec = (int)Math.round(u*period);
			task = new Task();
			task.setExe(exec);
			task.setPeriod(period);
			task.setDeadline(period);
			taskset.add(task);
		}
		Collections.sort(taskset);
		
		for(int i=0;i<taskset.size();i++)
			taskset.get(i).setName("Task"+(i+1));
	}
	private double getTasksetU(List<Task> taskset)
	{
		double util=0;
		for(Task t:taskset)
			util+=t.getExe()/t.getPeriod();
		return util;
	}
	private double getTasksetUtil(List<Double> utils)
	{
		double util=0;
		for(Double t:utils)
			util+=t;
		return util;
	}
	private String XMLgenerator(String taskSetName, double load)
	{
		String resultString = "<taskSet name=\""+taskSetName+"\" load=\""+load+"\">\n";
		
		for(Task t: taskset)
		{
			resultString = resultString+"<task name=\""+t.getName()+"\" p=\""+t.getPeriod()+"\" d=\""+t.getDeadline()+"\" e=\""+t.getExe()+"\" />\n";
		}
		resultString +=  "</taskSet>\n";
		return resultString;
	}
	
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		params = parser.getParameters("TaskSet");
		//check if all parameters are included
		if(!params.containsKey(LOCATION))
			check = false;
		if(!params.containsKey(UTIL_MIM))
			check = false;
		if(!params.containsKey(UTIL_MAX))
			check = false;
		if(!params.containsKey(UTIL_STEP))
			check = false;
		if(!params.containsKey(NUM_TASKS))
			check = false;
		if(!params.containsKey(NUM_TASKSETS))
			check = false;
		if(!params.containsKey(PERIOD_MIN))
			check = false;
		if(!params.containsKey(PERIOD_MAX))
			check = false;
		if(!params.containsKey(PERIOD_STEP))
			check = false;
		if(!params.containsKey(SEED))
			check = false;
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	
	public static void main(String[] args) 
	{	
		SetsGenerator gen;
		if(args.length==1)
			gen = new SetsGenerator(args[0]);
		else
			gen = new SetsGenerator("user.xml");
		gen.generateTaskset();
	}
}
