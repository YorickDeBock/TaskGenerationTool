package be.uantwerpen.taskCreator;

public class WCET {
	
	private double execTime; //in ns
	private String programName;
	private int numberOfExec;
	private String location;
	
	public WCET(String programName, double execTime, String location)
	{
		this.programName = programName;
		this.execTime = execTime; //ns
		this.numberOfExec = 1;
		this.location = location;
	}
	public WCET(String programName, double cycles, double frequency) //frequency in MHz
	{
		this.programName = programName;
		this.execTime = cycles/frequency*1000000000; //ns
	}
	public double getExecTime() {
		return execTime;
	}
	public void setExecTime(double execTime) {
		this.execTime = execTime;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public int getNumberOfExec() {
		return numberOfExec;
	}
	public void setNumberOfExec(int numberOfExec) {
		this.numberOfExec = numberOfExec;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	
}
