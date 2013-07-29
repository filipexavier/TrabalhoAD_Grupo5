package models.utils;

import models.abstracts.RandomVariable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * 
 * Implementação de uma variável aleatória com distribuição exponencial
 * com taxa lambda.
 * 
 * @see RandomVariable, GeometricVariable
 *
 */
public class ExponentialVariable extends RandomVariable {

	/**
	 * Taxa da distribuição exponencial.
	 */
	private double lambda;
	
	/**
	 * Constrói uma variável aleatória exponencial com a taxa fornecida.
	 * @param lambda taxa.
	 */
	public ExponentialVariable(double lambda) {
		super();
		this.lambda = lambda; 
	}
	
	/**
	 * Implementação da geração de amostras da exponencial, dado
	 * um número gerado por um gerador de números aleatórios.
	 */
	@Override
	protected Double generateSample(Double randomNumber) {
		return -(Math.log(randomNumber) / lambda);
	}
}
