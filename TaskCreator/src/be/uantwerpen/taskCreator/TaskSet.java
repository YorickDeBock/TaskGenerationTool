package be.uantwerpen.taskCreator;

import java.util.ArrayList;
import java.util.List;

public class TaskSet {
	
	private List<Task> tasks;
	private String taskSetName;
	
	public TaskSet(String name)
	{
		taskSetName = name;
		tasks = new ArrayList<Task>();

	}
	public void addTask(Task t)
	{
		tasks.add(t);
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public String getTaskSetName() {
		return taskSetName;
	}
	public void setTaskSetName(String taskSetName) {
		this.taskSetName = taskSetName;
	}
	
}
