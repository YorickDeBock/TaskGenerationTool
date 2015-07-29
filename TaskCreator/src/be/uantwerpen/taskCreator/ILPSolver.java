package be.uantwerpen.taskCreator;

import java.util.*;

import scpsolver.constraints.*;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.*;

/*IMPORTANT: JAVA 32bits version is required to use the Solver*/

public class ILPSolver {
	
	private List<WCET> programs;
	private double[] values;
	private double[] lowerBounds;
	private double cacheFlushTime;
	private LinearProgramSolver solver;
	
	public ILPSolver(List<WCET> programs, double cacheFlushTime)
	{
		this.programs = programs;
		solver = SolverFactory.newDefault();
		this.cacheFlushTime = cacheFlushTime;
	}

	public List<WCET> findProgramCombinationI(double wantedExecTime) 
	{
		
		List<WCET> combinationOfPrograms = new ArrayList<WCET>();
		WCET wcet;
		values = new double[programs.size()+1];
		lowerBounds = new double[programs.size()+1];
		
		for(int i=0;i<programs.size()+1;i++)
        {
        	if(i<programs.size())
        		values[i]=programs.get(i).getExecTime();
        	else
        		values[i] = cacheFlushTime;
        	
        	lowerBounds[i] = 0;
        }
		LinearProgram lp = new LinearProgram(values); 
		for(int i = 0; i < lp.getDimension(); i++)
		{
			lp.setInteger(i);
		}
		
		lp.setLowerbound(lowerBounds);
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(values, wantedExecTime*1000,"c1"));//combination of program can not be greater than the wanted WCET
		double[] constraint = new double[values.length];
		//double[] constraint2 = new double[values.length];
		
		for(int j=0;j<constraint.length;j++)
		{
			constraint[j]=1;
		}
		constraint[constraint.length-1] = -1;
		
		lp.addConstraint(new LinearEqualsConstraint(constraint, 0, "c"));
		lp.setMinProblem(false);//search for the max value
		
		double[] sol = solver.solve(lp);
		
		for(int i=0;i<sol.length-1;i++)
		{			
			wcet = new WCET(programs.get(i).getProgramName(),programs.get(i).getExecTime(),programs.get(i).getLocation());
			wcet.setNumberOfExec((int)sol[i]);
			combinationOfPrograms.add(wcet);	
		}
		for(double v:sol)
			System.out.println(v);
		return combinationOfPrograms;
	}
	public List<WCET> findProgramCombinationB(double wantedExecTime) 
	{
		List<WCET> combinationOfPrograms = new ArrayList<WCET>();
		WCET wcet;
		values = new double[programs.size()];
		
		for(int i=0;i<programs.size();i++)
        {
        	values[i]=programs.get(i).getExecTime();
        }
		LinearProgram lp = new LinearProgram(values); 
		for(int i = 0; i < lp.getDimension(); i++)
		{
			lp.setBinary(i);
		}
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(values, wantedExecTime*1000,"c1"));//combination of program can not be greater than the wanted WCET
		lp.setMinProblem(false);//search for the max value
		
		double[] sol = solver.solve(lp);
		
		for(int i=0;i<sol.length;i++)
		{			
			wcet = new WCET(programs.get(i).getProgramName(),programs.get(i).getExecTime(),programs.get(i).getLocation());
			wcet.setNumberOfExec((int)sol[i]);
			combinationOfPrograms.add(wcet);	
		}
		for(double v:sol)
			System.out.println(v);
		return combinationOfPrograms;
	}
}
