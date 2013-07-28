package models.abstracts;

import models.utils.ExponentialVariable;
import models.utils.HighQualityRandom;

/**
 * 
 * Classe abstrata que fornece um esqueleto de uma variável aleatória, 
 * com o intuito de minimizar o esforco requirido para implementar variáveis aleatórias com diferentes tipos de distribuição.
 * <p>
 * Para implementar uma variável aleatória que segue uma determinada distribuição, o programador só precisará extender esta classe 
 * e implementar o método <code>generateSample</code> para que ele retorne uma amostra da
 * distribuição que a variável está representando.
 * 
 * @see ExponentialVariable
 *
 */
public abstract class RandomVariable {
	
	/**
	 * Gerador de números aleatórios para prover amostras entre [0,1] para o método que irá prover a amostra da variável aleatória
	 */
	protected HighQualityRandom randomGenerator;
	
	/**
	 * 
	 * Construtor padrão de uma variável aleatória, o qual instancia um gerador de números aleatórios para ser utilizado.
	 * <p>
	 * Este construtor DEVE ser chamado por qualquer construtor que for criado em qualquer subclasse.
	 * 
	 * @see ExponentialVariable
	 */
	public RandomVariable() {
		randomGenerator = new HighQualityRandom();
	}
	
	/**
	 * 
	 * Retorna uma amostra de uma variável aleatória com determinada distribuição, que será implementada pela classe que extender esta classe;
	 * O método simplesmente pega um <code>double</double> fornecido pelo gerador de números aleatórios e chama o método <code>generateSample</code>,
	 * o qual irá prover uma amostra da variável aleatória com o número fornecido a ele.
	 * 
	 * @return	amostra da variavel aleatoria
	 */
	public Double getSample() {
		return generateSample(randomGenerator.nextDouble());
	}
	
	/**
	 * Recebe uma amostra de um número aleatório entre 0 e 1, e retorna uma amostra da variável aleatória de uma determinada distribuição
	 * 
	 * @param randomNumber número aleatório entre 0 e 1
	 * @return amostra de uma variável aleatória de uma determinada distribuição
	 */
	protected abstract Double generateSample(Double randomNumber);
}
