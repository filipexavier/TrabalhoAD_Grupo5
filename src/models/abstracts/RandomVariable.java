package models.abstracts;

import models.utils.HighQualityRandom;

public abstract class RandomVariable {
	
	protected HighQualityRandom randomGenerator;
	
	public RandomVariable() {
		randomGenerator = new HighQualityRandom();
	}
	
	public Double getSample() {
		return generateSample(randomGenerator.nextDouble());
	}
	
	protected abstract Double generateSample(Double randomNumber);
}
