package org.um.feri.ears.problems;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.um.feri.ears.benchmark.MORatingBenchmark;
import org.um.feri.ears.benchmark.RatingBenchmarkBase;

public abstract class TaskBase<T extends ProblemBase> {
	
	protected EnumStopCriteria stopCriteria;
	protected int maxEvaluations; // for Stop criteria
	protected int numberOfEvaluations = 0; // for Stop criteria
	protected double epsilon; // Stop criteria for global optimum
	protected boolean isStop;
	protected boolean isGlobal;
	protected int precisionOfRealNumbersInDecimalPlaces; //used only for discreet problem presentation (bit presentation in GA)
	protected T p;
	private int resetCount;
	
	protected long timerStart;
	protected long allowedCPUTime;
	protected int numberOfIterations = 0;
	protected int maxIterations;
	
	protected StringBuilder ancestorSB;
	protected boolean isAncestorLogginEnabled = false;
	
	public TaskBase() {
		resetCount = 0;
	}
	
	public void startTimer()
	{
		timerStart = System.nanoTime();
	}
	
	public void incrementNumberOfIterations() throws StopCriteriaException
	{
		if (numberOfIterations >= maxIterations)
			throw new StopCriteriaException("Max iterations");
		numberOfIterations++;
		if (numberOfIterations >= maxIterations)
			isStop = true;
	}

	/**
     * When you subtract 2 solutions and difference is less or equal epsilon,
     * solution are treated as equal good (draw in algorithm match)!
     * 
     * @return condition that is used when 2 solutions are equal good!
     */
	public double getEpsilon() {
	    return epsilon;
	}
	
	public int getDimensions() {
		return p.getNumberOfDimensions();
	}
	
	public int getNumberOfConstrains() {
	    return p.numberOfConstraints;
	}
	
	public void enableAncestorLogging()
	{
		isAncestorLogginEnabled = true;
		ancestorSB = new StringBuilder();
	}

	public void disableAncestorLogging()
	{
		isAncestorLogginEnabled = false;
		ancestorSB.setLength(0);
	}
	
	public void saveAncestorLogging(String fileName) {
		
		try {
			FileOutputStream fos = new FileOutputStream(fileName+".csv");
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(ancestorSB.toString());
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Used only for discreet problem presentation (bit presentation in GA)
	 * @return
	 */
	public int getPrecisionMinDecimal() {
	    return precisionOfRealNumbersInDecimalPlaces;
	}
	
	public int getMaxEvaluations() {
        return maxEvaluations;
    }
	
	public int getMaxIteratirons() {
		return maxIterations;
	}
	
	public long getAllowedCPUTime() {
		return allowedCPUTime;
	}
	
	public int getNumberOfEvaluations(){
		return numberOfEvaluations;
	}
	
	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	
	public long getAvailableCPUTime() {
		return allowedCPUTime - System.nanoTime();
	}
	
	public long getUsedCPUTime() {
		return System.nanoTime() - timerStart;
	}
	
	public boolean isStopCriteria() {
		
		if(stopCriteria == EnumStopCriteria.CPU_TIME)
		{
			isCPUTimeExceeded();
		}
		
		return isStop||isGlobal;
	}
	
	public boolean isCPUTimeExceeded()
	{
		if(System.nanoTime() - timerStart > allowedCPUTime)
		{
			isStop = true;
			return true;
		}
		return false;
	}
	
	public String getStopCriteriaDescription() {
        if (stopCriteria == EnumStopCriteria.EVALUATIONS) {
            return "E="+getMaxEvaluations();
        }
        if (stopCriteria == EnumStopCriteria.GLOBAL_OPTIMUM_OR_EVALUATIONS) {
                return "Global optimum epsilon="+epsilon+" or  E="+getMaxEvaluations();
        }
        return "not defined";
	}
	
	protected void incEvaluate() throws StopCriteriaException {
		if (numberOfEvaluations >= maxEvaluations)
			throw new StopCriteriaException("Max evaluations");
		numberOfEvaluations++;
		if (numberOfEvaluations >= maxEvaluations)
			isStop = true;
	}
	
	/**
	 * @deprecated
	 * Deprecated is because it is better to use individuals and
	 * isFirstBetter that already is influenced by this parameter.
	 * Returns true id global maximum searching!
	 * 
	 * @return
	 */
	public boolean isMaximize() {
	    return !p.minimum;
	}
	
    /**
     * @return
     */
    public String getProblemName() {
        return p.getName();
    }
    
    public int getProblemHashCode()
    {
    	return p.hashCode();
    }
    
    public void resetCounter() {
    	resetCount++;
        numberOfEvaluations = 0;
        numberOfIterations = 0;
        isStop = false;
        isGlobal = false;
        timerStart = System.nanoTime();
    }
    
    public int getResetCount()
    {
    	return resetCount;
    }
    
    @Override
    public String toString() {
        return "Task [stopCriteria=" + stopCriteria + ", maxEvaluations=" + maxEvaluations + ", numberOfEvaluations=" + numberOfEvaluations + ", epsilon="
                + epsilon + ", isStop=" + isStop + ", isGlobal=" + isGlobal + ", precisionOfRealNumbersInDecimalPlaces="
                + precisionOfRealNumbersInDecimalPlaces + ", p=" + p + "]";
    }
    
    /**
     * Returns a string containing all the tasks information that doesen't change.
     * @return
     */
    public String taskInfo() {

    	if(stopCriteria == EnumStopCriteria.EVALUATIONS) {
    		return "Task = " + p +" stopCriteria=" + stopCriteria + ", maxEvaluations=" + maxEvaluations + ", epsilon="
    				+ epsilon + ", precisionOfRealNumbersInDecimalPlaces="
    				+ precisionOfRealNumbersInDecimalPlaces;
    	}
    	else if(stopCriteria == EnumStopCriteria.ITERATIONS) {
    		return "Task = " + p +" stopCriteria=" + stopCriteria + ", maxIterations=" + maxIterations + ", epsilon="
    				+ epsilon + ", precisionOfRealNumbersInDecimalPlaces="
    				+ precisionOfRealNumbersInDecimalPlaces;
    	}
    	else if(stopCriteria == EnumStopCriteria.CPU_TIME){
    		return "Task = " + p +" stopCriteria=" + stopCriteria + ", allowedCPUTime=" + allowedCPUTime + ", epsilon="
    				+ epsilon + ", precisionOfRealNumbersInDecimalPlaces="
    				+ precisionOfRealNumbersInDecimalPlaces;
    	}
    	else
    		return "";
    }

}
