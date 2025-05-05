package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

public interface AIEmbeddingSession extends AISession {
	/**
	 * Gets vector embeddings for the given input text.
	 * 
	 * @param text the text to convert to embeddings
	 * @return a float array containing the embedding vectors
	 * @throws PageException if an error occurs during the embedding process
	 */
	public float[] getEmbeddings(String text) throws PageException;
}