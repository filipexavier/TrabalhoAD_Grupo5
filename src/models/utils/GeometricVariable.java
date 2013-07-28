package models.utils;

import models.abstracts.RandomVariable;

public class GeometricVariable extends RandomVariable {

	private Float mean;
	
	public GeometricVariable(Float mean) {
		this.mean = mean;
	}
	
	@Override
	protected Double generateSample(Double randomNumber) {
		return Math.log(randomNumber) / Math.log(1 - 1/mean);
	}

}
