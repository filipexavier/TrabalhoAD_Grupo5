package models.abstracts;

import java.util.Random;

public abstract class RandomVariable {
	
	protected Random randomGenerator;
	
	public RandomVariable() {
		// FIXME: Analisar a seed sendo passada
		randomGenerator = new Random(System.currentTimeMillis());
	}
	
	public Double getSample() {
		return generateSample(randomGenerator.nextDouble());
	}
	
	protected abstract Double generateSample(Double randomNumber);
}
