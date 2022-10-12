package org.um.feri.ears.benchmark;

import org.um.feri.ears.algorithms.AlgorithmBase;
import org.um.feri.ears.statistic.rating_system.Player;
import org.um.feri.ears.statistic.rating_system.glicko2.Glicko2Rating;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.visualization.graphing.recording.GraphDataRecorder;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.SolutionBase;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.TaskBase;
import org.um.feri.ears.statistic.rating_system.glicko2.TournamentResults;

import java.util.*;
import java.util.concurrent.*;

public abstract class BenchmarkBase<T extends TaskBase<?>, S extends SolutionBase<?>, A extends AlgorithmBase<T, S>> {

    public enum RatingCalculation {NORMAL, RATING_CONVERGENCE_GRAPH, RATING_CONVERGENCE_SUM}

    public static boolean printInfo = false;
    protected ArrayList<T> tasks;
    protected ArrayList<A> algorithms;
    protected String name;
    protected String shortName;
    protected String info;

    // Default benchmark settings
    protected StopCriterion stopCriterion = StopCriterion.EVALUATIONS;
    protected int maxEvaluations = 1500;
    protected long timeLimit = TimeUnit.MILLISECONDS.toNanos(500); //milliseconds
    protected int maxIterations = 500;
    public double drawLimit = 1e-7;
    protected int dimension = 2;
    protected int numberOfRuns = 15;
    protected boolean runInParallel = false;
    protected boolean displayRatingCharts = true;
    boolean displayRatingIntervalBand = false;
    protected RatingCalculation ratingCalculation= RatingCalculation.NORMAL;
    int evaluationsPerTick = 100;

    TournamentResults tournamentResults = new TournamentResults();
    BenchmarkResults<T, S, A> benchmarkResults = new BenchmarkResults();

    public BenchmarkBase() {
        tasks = new ArrayList<>();
        algorithms = new ArrayList<>();
    }

    public boolean isRunInParallel() {
        return runInParallel;
    }

    public void setRunInParallel(boolean runInParallel) {
        this.runInParallel = runInParallel;
    }

    public TournamentResults getResultArena() {
        return tournamentResults;
    }

    public String getParameters(String name) {
        return ""; //TODO reflection
    }

    public void clearPlayers() {
        algorithms.clear();
        benchmarkResults.clear();
    }

    public BenchmarkResults<T, S, A> getBenchmarkResults() {
        return benchmarkResults;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T tw : tasks) {
            sb.append(tw.toString());
        }
        return sb.toString();
    }

    public ArrayList<T> getAllTasks() {
        return new ArrayList<T>(tasks);
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public StopCriterion getStopCriterion() {
        return stopCriterion;
    }

    /**
     * Remove algorithm from benchmark
     *
     * @param algorithm to be removed from the benchmark
     */
    public void removeAlgorithm(AlgorithmBase<T, S> algorithm) {
        algorithms.remove(algorithm);
        benchmarkResults.removeAlgorithm(algorithm);
    }

    public boolean isDisplayRatingCharts() {
        return displayRatingCharts;
    }

    public void setDisplayRatingCharts(boolean displayRatingCharts) {
        this.displayRatingCharts = displayRatingCharts;
    }

    public abstract void initAllProblems();

    public void addAlgorithm(A al) {
        algorithms.add(al);
    }

    public void addAlgorithms(ArrayList<A> algorithms) {
        this.algorithms.addAll(algorithms);
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName.isEmpty() ? name : shortName;
    }

    public String getInfo() {
        return info;
    }

    /**
     * Run the benchmark
     *
     * @param numberOfRuns number of runs/repetitions of the benchmark
     */
    public void run(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
        initAllProblems();
        long start = System.nanoTime();
        for (int i = 0; i < numberOfRuns; i++) {
            if (printInfo) System.out.println("Current run: " + (i + 1));
            for (T task : tasks) {
                if (printInfo) System.out.println("Current problem: " + task.getProblemName());
                ArrayList<AlgorithmRunResult<S, A, T>> runResults = runOneTask(task);
                benchmarkResults.addResults(i, task, runResults);
            }
        }
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        performStatistics();
    }

    protected ArrayList<AlgorithmRunResult<S, A, T>> runOneTask(T task) {

        if(ratingCalculation == RatingCalculation.RATING_CONVERGENCE_GRAPH || ratingCalculation == RatingCalculation.RATING_CONVERGENCE_SUM) {
            task.enableEvaluationHistory();
            task.setStoreEveryNthEvaluation(evaluationsPerTick);
        }

        ArrayList<AlgorithmRunResult<S, A, T>> runResults = new ArrayList<>();
        if (runInParallel) {
            if(printInfo)
                System.out.println("Threads: " + Runtime.getRuntime().availableProcessors());

            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            CompletionService<AlgorithmRunResult> completionService = new ExecutorCompletionService<>(pool);
            Set<Future<AlgorithmRunResult>> set = new HashSet<>();
            for (A algorithm : algorithms) {
                completionService.submit(algorithm.createRunnable(algorithm, (T) task.clone()));
            }
            int countCompleted = 0;

            while(countCompleted != algorithms.size()){
                try {
                    Future<AlgorithmRunResult> resultFuture = completionService.take(); //blocks if none available
                    AlgorithmRunResult result = resultFuture.get();
                    runResults.add(result);
                    countCompleted++;
                    if (printInfo)
                        System.out.println("Total execution time for " + result.algorithm.getId() + ": " + result.algorithm.getLastRunDuration());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            pool.shutdown();
        } else {
            long start;
            long duration;
            for (A algorithm : algorithms) {
                try {
                    GraphDataRecorder.SetContext(algorithm, task);
                    T taskCopy = (T) task.clone();
                    start = System.nanoTime();
                    S result = algorithm.execute(taskCopy);
                    duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    algorithm.addRunDuration(duration, duration - taskCopy.getEvaluationTimeMs());

                    if (printInfo)
                        System.out.println(algorithm.getId() + ": " + duration / 1000.0);
                    runResults.add(new AlgorithmRunResult(result, algorithm, taskCopy));
                    //TODO generic feasibility check
                } catch (StopCriterionException e) {
                    System.err.println(algorithm.getId() + " StopCriterionException for:" + task + "\n" + e);
                }
            }
        }
        return runResults;
    }

    private void performStatistics() {

        //TODO check if stopping criterion max evaluations
        if (ratingCalculation == RatingCalculation.RATING_CONVERGENCE_GRAPH) {
            int numberOfTicks = maxEvaluations / evaluationsPerTick;
            HashMap<String, Glicko2Rating[]> ratingLists = new HashMap<>();


            for (A algorithm : algorithms) {
                tournamentResults.addPlayer(algorithm.getId());
                ratingLists.put(algorithm.getId(), new Glicko2Rating[numberOfTicks]);
            }

            for (int i = 1; i <= numberOfTicks; i++) {
                performTournament(i * evaluationsPerTick);
                for (Player p : tournamentResults.getPlayers()) {
                    ratingLists.get(p.getId())[i - 1] = p.getGlicko2Rating();
                }

                tournamentResults = new TournamentResults();
                for (A algorithm : algorithms) {
                    tournamentResults.addPlayer(algorithm.getId());
                }
            }

            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, Glicko2Rating[]> entry : ratingLists.entrySet()) {
                //System.out.println(entry.getKey() + " " + Arrays.toString(entry.getValue()));
                String algorithm = entry.getKey();
                Glicko2Rating[] ratings = entry.getValue();
                sb.setLength(0);
                sb.append("Evaluations\tRating\tRD\n");
                for (int i = 0; i < ratings.length; i++) {
                    double rating = ratings[i].getRating();
                    double RD = ratings[i].getRatingDeviation();
                    sb.append((i+1) * evaluationsPerTick).append("\t");
                    sb.append(Util.df1.format(rating)).append("\t");
                    sb.append(RD).append("\n");
                }
                String output = sb.toString();
                output = output.replace(",","");

                Util.writeToFile("D:\\"+algorithm + ".txt", output);
            }

        } else {
            for (A algorithm : algorithms) {
                tournamentResults.addPlayer(algorithm.getId());
            }

            performTournament(-1);
            tournamentResults.displayResults(displayRatingCharts);
        }
    }

    protected abstract void performTournament(int evaluationNumber);

    public void allPlayed() {
        for (AlgorithmBase al : algorithms) {
            al.setPlayed(true);
        }
    }

    public String getStoppingCriterion() {
        switch (stopCriterion) {
            case EVALUATIONS:
                return Integer.toString(maxEvaluations);
            case ITERATIONS:
                return Integer.toString(maxIterations);
            case CPU_TIME:
                return Long.toString(timeLimit);
            case STAGNATION:
                return Integer.toString(tasks.get(0).getMaxTrialsBeforeStagnation()); //TODO stagnation trials
            case GLOBAL_OPTIMUM_OR_EVALUATIONS:
                return Integer.toString(maxEvaluations);
            default:
                return null;
        }
    }

    public String[] getProblems() {
        String[] problems = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            problems[i] = tasks.get(i).getProblemName();
        }
        return problems;
    }

    public void setDisplayRatingIntervalBand(boolean displayRatingIntervalBand) {
        this.displayRatingIntervalBand = displayRatingIntervalBand;
    }

    public void setEvaluationsPerTick(int evaluationsPerTick) {
        this.evaluationsPerTick = evaluationsPerTick;
    }

    public RatingCalculation getRatingCalculation() {
        return ratingCalculation;
    }

    public void setRatingCalculation(RatingCalculation ratingCalculation) {
        this.ratingCalculation = ratingCalculation;
    }
}
