package models.utils;

import models.abstracts.RandomVariable;

public class ExponentialVariable extends RandomVariable {

	private double lambda;
	
	public ExponentialVariable(double lambda) {
		super();
		this.lambda = lambda; 
	}
	
	@Override
	protected Double generateSample(Double randomNumber) {
		return -(Math.log(1 - randomNumber) / lambda);
	}

}
