package org.um.feri.ears.experiments;

import java.util.ArrayList;

import org.um.feri.ears.algorithms.MOAlgorithm;
import org.um.feri.ears.algorithms.moo.gde3.D_GDE3;
import org.um.feri.ears.algorithms.moo.ibea.D_IBEA;
import org.um.feri.ears.algorithms.moo.moead_dra.D_MOEAD_DRA;
import org.um.feri.ears.algorithms.moo.moead_dra.D_MOEAD_STM;
import org.um.feri.ears.algorithms.moo.nsga3.D_NSGAIII;
import org.um.feri.ears.algorithms.moo.pesa2.D_PESAII;
import org.um.feri.ears.algorithms.moo.spea2.D_SPEA2;
import org.um.feri.ears.benchmark.RatingBenchmark;
import org.um.feri.ears.benchmark.RatingEnsemble;
import org.um.feri.ears.problems.results.BankOfResults;
import org.um.feri.ears.qualityIndicator.QualityIndicator.IndicatorName;
import org.um.feri.ears.rating.Player;
import org.um.feri.ears.rating.ResultArena;
import org.um.feri.ears.util.Reporting;
import org.um.feri.ears.util.Util;

public class IEEEexperiment {
	
	public static void main(String[] args)
	{

		Util.rnd.setSeed(System.currentTimeMillis());
		RatingBenchmark.debugPrint = true; //prints one on one results
		ArrayList<MOAlgorithm> players = new ArrayList<MOAlgorithm>();

		players.add(new D_MOEAD_DRA());
		players.add(new D_NSGAIII());
		players.add(new D_SPEA2());
		players.add(new D_GDE3());
		players.add(new D_IBEA());

		MOAlgorithm.setRunWithOptimalParameters(true);

		ResultArena ra = new ResultArena(100);

		ArrayList<IndicatorName> indicators = new ArrayList<IndicatorName>();
		
		indicators.add(IndicatorName.IGDPlus);
		indicators.add(IndicatorName.IGD);
		indicators.add(IndicatorName.NativeHV);
		indicators.add(IndicatorName.Epsilon);
		indicators.add(IndicatorName.MaximumSpread);
		indicators.add(IndicatorName.R2);

		RatingEnsemble re = new RatingEnsemble(indicators, 1e-8, true); //Create banchmark
		for (MOAlgorithm al:players) {
			ra.addPlayer(al.getID(), 1500, 350, 0.06,0,0,0); //init rating 1500
			re.registerAlgorithm(al);
		}
		BankOfResults ba = new BankOfResults();
		re.run(ra, ba, 30); //repeat competition 50X
		ArrayList<Player> list = new ArrayList<Player>();
		list.addAll(ra.recalcRangs()); //new ranks

		Reporting.saveLeaderboard(list, "D:\\Benchmark results\\IEEE_benchmark_leaderboard_rand.txt");
    	Reporting.createLatexTable(list, "D:\\Benchmark results\\IEEE_benchmark_table_rand.tex");

		for (Player p: list) System.out.println(p); //print ranks
	}
}
