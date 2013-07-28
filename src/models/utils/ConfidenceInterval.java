package models.utils;

import java.util.List;

public class ConfidenceInterval {
	public static double getAvarege(List<Double> data) {
		double sum = 0.0;

		if (data.size() > 0) {
			for (double a : data)
				sum += a;
			sum = sum / data.size();
		}

		return sum;
	}

	public static double getVariance(List<Double> data) {
		double avarege = getAvarege(data);
		double temp = 0;

		if (data.size() > 0) {
			for (double a : data)
				temp += (avarege - a) * (avarege - a);

			temp = temp / data.size();
		}

		return temp;
	}

	public static double getDeviation(List<Double> data) {
		return Math.sqrt(getVariance(data));
	}
	
	public static double getPrecision(List<Double> data) {
		if (data.size() >= 30) {
			return 100*1.645*getDeviation(data)/(getAvarege(data)*Math.sqrt(data.size()));
		}
		return 0;
	}

	public static String getConfidenceInterval(List<Double> data) {
		if (data != null && data.size() > 0) {
			double avarege = getAvarege(data);
			double dp = getDeviation(data);
			double z = 1.645;

			double lowerLimit = avarege - z * dp / Math.sqrt(data.size());
			double uperLimit = avarege + z * dp / Math.sqrt(data.size());

			return "(" + lowerLimit + ", " + uperLimit + ") largura: "+getPrecision(data);
		}

		return null;
	}

}
