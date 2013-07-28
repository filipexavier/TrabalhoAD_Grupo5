package models.utils;

import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ConfidenceInterval {
	private static SummaryStatistics stats;
	private static TDistribution tDistribution;

	public static double getPrecision(List<Double> data, double t) {
		return 100*t*stats.getStandardDeviation()/(stats.getMean()*Math.sqrt(stats.getN()));
	}
	
	private static double getConfidenceIntervalWidth(SummaryStatistics summaryStatistics, double t) {
		  return t * summaryStatistics.getStandardDeviation() / Math.sqrt(summaryStatistics.getN());
		}

	public static String getConfidenceInterval(List<Double> data) {
		if (data != null && data.size() > 0) {
			stats = new SummaryStatistics();
			for (Double d : data) {
				stats.addValue(d);
			}
			tDistribution = new TDistribution(stats.getN() - 1);
						
			double avarege = stats.getMean();
			
			double t = tDistribution.inverseCumulativeProbability(1.0 - 0.1/2);
			
			double lowerLimit = avarege - getConfidenceIntervalWidth(stats, t);
			double uperLimit = avarege + getConfidenceIntervalWidth(stats, t);

			return "(" + Math.round(lowerLimit * 10000) / 10000.0 + ", " + Math.round(uperLimit * 10000) / 10000.0 + ")";
		}

		return null;
	}

}
