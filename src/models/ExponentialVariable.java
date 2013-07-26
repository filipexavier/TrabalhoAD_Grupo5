package models;

import models.abstracts.RandomVariable;

public class ExponentialVariable extends RandomVariable {

	private float lambda;
	
	public ExponentialVariable(float lambda) {
		super();
		this.lambda = lambda; 
	}
	
	@Override
	protected float generateSample(float randomNumber) {
		return (float) (Math.log(1 - randomNumber) / lambda);
	}

}
