package org.um.feri.ears.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.um.feri.ears.benchmark.EnumBenchmarkInfoParameters;
import org.um.feri.ears.problems.DoubleSolution;
import org.um.feri.ears.problems.SolutionBase;
import org.um.feri.ears.problems.StopCriteriaException;
import org.um.feri.ears.problems.TaskBase;
import org.um.feri.ears.util.Cache;

public abstract class AlgorithmBase<T extends TaskBase, T2 extends SolutionBase> {
	
	/**
	 * Search for best solution.
	 * 
	 * if StopCriteriaException is thrown tasks isStopCriteria method is not used properly.
	 * 
	 * @param taskProblem
	 * @return best solution
	 * @throws StopCriteriaException 
	 */
    protected boolean debug;
    protected boolean display_data = false;
    protected boolean save_data = false;
    protected static Cache caching = Cache.None;
   
    protected String version = "1.0";
    protected Author au;
    protected AlgorithmInfo ai;
    protected AlgorithmInfo tmpAi;
    protected AlgorithmRunTime art;
    public void addRunDuration(long duration) {
        if (art==null) {
            art = new AlgorithmRunTime();
        }
        art.addRunDuration(duration);
    }
    public void setAlgorithmTmpInfo(AlgorithmInfo aii) {
        tmpAi = ai;
        ai = aii;
    }
    public void setAlgorithmInfoFromTmp() {
        ai = tmpAi;
    }
    
    public void setAlgorithmInfo(AlgorithmInfo aii) {
        ai = aii;
    }
    
    /**
     * 
     * 
     * @param taskProblem
     * @return
     * @throws StopCriteriaException
     */
	public abstract T2 run(T taskProblem) throws StopCriteriaException;
	
	/**
	 * It is called every time before every run! 
	 */
	public abstract void resetDefaultsBeforNewRun();
	public boolean isDebug() {
        return debug;
	}

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Author getImplementationAuthor() {
        return au;
    }
	public AlgorithmInfo getAlgorithmInfo(){
	    return ai;
	}
	public String getID() {
	    return ai.getVersionAcronym();
	}
	/**
	 * Returns algorithms with different settings for selecting the best one!
	 * maxCombinations is usually set to 8!
	 * If maxCombinations==1 than return combination that is expected to perform best!
	 * 
	 * NOTE not static because jave doesnt support abstract static!
	 * 
	 * @param taskProblem
	 * @return
	 */
	public List<AlgorithmBase> getAlgorithmParameterTest(EnumMap<EnumBenchmarkInfoParameters, String> parameters, int maxCombinations) {
	    List<AlgorithmBase> noAlternative = new ArrayList<AlgorithmBase>();
	    noAlternative.add(this);
	    return noAlternative;
	}
    public void resetDuration() {
        art = new AlgorithmRunTime();
    }
    
    public void setDisplayData(boolean b)
    {
    	display_data = b;
    }
    
    public boolean getDisplayData()
    {
    	return display_data;
    }
    
    public void setSaveData(boolean b)
    {
    	save_data = b;
    }

    public boolean getSaveData()
    {
    	return save_data;
    }
    
    public void setCaching(Cache c)
    {
    	caching = c;
    }
}
