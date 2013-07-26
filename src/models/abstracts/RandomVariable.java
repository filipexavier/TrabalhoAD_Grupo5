package models.abstracts;

import java.util.Random;

public abstract class RandomVariable {
	
	protected Random randomGenerator;
	
	public RandomVariable() {
		// FIXME: Analisar a seed sendo passada
		randomGenerator = new Random(System.currentTimeMillis());
	}
	
	public float getSample() {
		return(randomGenerator.nextFloat());
	}
	
	protected abstract float generateSample(float randomNumber);
}
