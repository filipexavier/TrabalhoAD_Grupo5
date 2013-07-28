package models.utils;

import models.abstracts.RandomVariable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ExponentialVariable extends RandomVariable {

	private double lambda;
	
	public ExponentialVariable(double lambda) {
		super();
		this.lambda = lambda; 
	}
	
	@Override
	protected Double generateSample(Double randomNumber) {
		return -(Math.log(randomNumber) / lambda);
	}
	
	public static void main(String[] args) {
		ExponentialVariable variable = new ExponentialVariable(1);
		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < 50000; i++) {
			stats.addValue(variable.getSample());
		}
		System.out.println(stats.getMean());
	}
}
