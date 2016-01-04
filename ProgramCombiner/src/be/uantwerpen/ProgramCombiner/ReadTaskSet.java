package be.uantwerpen.ProgramCombiner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ReadTaskSet {
	
	static final String NAME 	= "name";
	static final String LOCATION 	= "tasksetlocation";
	
	private Map<String, List<String>> paramsTarget;
	private Map<String, List<String>> paramsTaskSet;
	private Map<String, List<String>> paramsBenchmark;
	
	public ReadTaskSet(String inputParameters)
	{
		checkInput(inputParameters);
	}
	private void checkInput(String fileName)
	{
		XMLParser parser = new XMLParser(fileName);
		boolean check=true;
		parser.parseFile();
		paramsTaskSet = parser.getParameters("taskSet");
		paramsBenchmark = parser.getParameters("benchMark");
		paramsTarget = parser.getParameters("TargetHardware");
		//check if all parameters are included
		if(!paramsTaskSet.containsKey(NAME))
			check = false;
		if(!check)
		{
			System.err.println("Some parameters are missing");
		}
	}
	public void scanTaskSet()
	{
		File f = new File(paramsTaskSet.get(LOCATION).get(0)+"/"+paramsTaskSet.get(NAME).get(0));
		//File f = new File(".");
		FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory()&&pathname.getName().contains("TaskSet"))
	        		return true;
	        	return false;
			}
		};
		
		for(File t:f.listFiles(filter))
		{
			cFilePreparation(t);
			createProgramCombination(t);
		}
		
	}
	public void cFilePreparation(File taskSetFolder)
	{
		BufferedReader reader;
		InputStream in;
		StringBuilder out;
		BufferedWriter writer;
		String name;
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory())
	        		return true;
	        	return false;
			}
		};
		
		for(File folder:taskSetFolder.listFiles(filter))
		{
			try {
				name = folder.getName();
				System.out.println(name);
				in = new FileInputStream(new File(taskSetFolder.getAbsolutePath()+"/"+name+"/"+name+".c"));
				reader = new BufferedReader(new InputStreamReader(in));
		        out = new StringBuilder();
		        String line;
		        boolean remove=false;
		        while ((line = reader.readLine()) != null) {
		        	if(line.contains("int main"))
		        	{
		        		//line = "int main_"+name+"(void)";
		        		remove = true;
		        	}
		        	if(line.contains("}")&&remove)
		        		remove=false;
		        	if(!remove)
		        		out.append(line+"\n");
		        }
		        reader.close();			       
		        File file = new File(taskSetFolder.getAbsolutePath()+"/"+name+"_new.c");
		        writer = new BufferedWriter(new FileWriter(file));
		        writer.write(out.toString());
		        writer.close();
			}catch (IOException excep)  {
				// TODO Auto-generated catch block
				excep.printStackTrace();
			} 
		}
	
	}
	private void createProgramCombination(File taskSetFolder)
	{		
		XMLParser parser = new XMLParser(new File(taskSetFolder.getAbsolutePath()+"/taskset.xml"));
		TaskSet taskset = parser.parseTasks();
		BufferedWriter writer;
		System.out.println(taskset.toString());
		
		File taskCFile;
		try {
			for(Task t:taskset.getTasks())
			{
				taskCFile = new File(taskSetFolder.getAbsolutePath()+"/"+t.getName()+".c");
				writer = new BufferedWriter(new FileWriter(taskCFile));
				writer.write("int main(void)\n");
				writer.write("{\n");
				for(Entry<String,Integer> e:t.getPrograms().entrySet())
				{
					if(e.getValue()>1)
					{
						writer.write("\tfor(int i = 0; i <"+e.getValue()+"; i++)\n\t{\n");
						writer.write("\t\tmain_"+e.getKey()+"();\n\t}\n");
					}
					else
					{
						writer.write("\tmain_"+e.getKey()+"();\n");
					}
					
				}
				writer.write("}\n");
				writer.close();
		       
			}
		 } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	public static void main(String[] args)
	{
		ReadTaskSet reader = new ReadTaskSet("./user.xml");
		reader.scanTaskSet();
		//reader.cFilePreparation(new File("./TaskSet"));
	}

}
