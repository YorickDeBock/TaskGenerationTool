package be.uantwerpen.taskCreator;

import java.util.*;

import scpsolver.constraints.*;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.*;

public class ILPSolver {
	
	private List<WCET> programs;
	private double[] values;
	private double[] lowerBounds;

	private LinearProgramSolver solver;
	
	public ILPSolver(List<WCET> programs)
	{
		this.programs = programs;
		values = new double[programs.size()];
		lowerBounds = new double[programs.size()];
		for(int i=0;i<programs.size();i++)
        {
        	values[i]=programs.get(i).getExecTime();
        	lowerBounds[i] = 0;
        }
		
		solver = SolverFactory.getSolver("lpsolver");
	}
	//true  = binary, false = integer
	public List<WCET> findProgramCombination(double wantedExecTime, boolean binaryOrInteger) 
	{
		LinearProgram lp = new LinearProgram(values); 
		for(int i = 0; i < lp.getDimension(); i++)
			if(binaryOrInteger)
				lp.setBinary(i);
			else
			{
				lp.setInteger(i);
			}
		
		lp.setLowerbound(lowerBounds);
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(values, wantedExecTime*1000,"c1"));
		
		lp.setMinProblem(false);//search for the max value
		
		double[] sol = solver.solve(lp);
		
		List<WCET> combinationOfPrograms = new ArrayList<WCET>();
		WCET wcet;
		for(int i=0;i<sol.length;i++)
		{
			wcet = new WCET(programs.get(i).getProgramName(),programs.get(i).getExecTime());
			wcet.setNumberOfExec((int)sol[i]);
			combinationOfPrograms.add(wcet);	
		}
		return combinationOfPrograms;
	}
}
