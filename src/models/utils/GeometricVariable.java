package models.utils;

import models.abstracts.RandomVariable;

/**
 * 
 * Implementação de uma variável aleatória com distribuição geométrica.
 * 
 * @see RandomVariable, ExponentialVariable
 *
 */
public class GeometricVariable extends RandomVariable {

	/**
	 * Média da distribuição geométrica.
	 */
	private Double mean;
	
	/**
	 * Constrói uma variável aleatória geométrica com a média fornecida.
	 * @param mean média da geométrica.
	 */
	public GeometricVariable(Double mean) {
		this.mean = mean;
	}
	
	/**
	 * Implementação da geração de amostras da geométrica, dado
	 * um número gerado por um gerador de números aleatórios.
	 */
	@Override
	protected Double generateSample(Double randomNumber) {
		return Math.log(randomNumber) / Math.log(1 - 1/mean);
	}

}
