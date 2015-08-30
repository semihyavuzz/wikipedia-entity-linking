package com.semihyavuz.app;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;



/* 
 * Candidate object representative of candidate entity that has a corresponding Wikipedia page,
 * and scores: popularity and contextSimilarityScores.
 */
public class Candidate {
	
	private Page wikiPage;
	private NamedEntity namedEntity;
	private double popularityScore;
	private double contextSimilarityScore;
	private static double N = 4743570;
	
	public Candidate (Page p, NamedEntity n, Database db) throws java.io.IOException, SQLException, WikiTitleParsingException{
		this.wikiPage = p;
		this.namedEntity = n;
		this.computePopularityScore();
		this.computeContextSimilarityScore(db);
	}
	
	public Page getWikiPage() {
		return this.wikiPage;
	}
	
	private void computePopularityScore() {
		
		this.popularityScore = (double) wikiPage.getNumberOfInlinks();
	}
	
	
	// RETRIEVE THE IDF VALUE FOR Words
	private double idf (Database db, String w) throws SQLException {
		String table = "DocumentFrequency";
		double docFrequency = (double) db.fetchDocumentFrequency(w, table);
		double inverseFrequency=0;
		
		
		if (table.equalsIgnoreCase("TempFrequency")) {
			double onlineDocNumber = this.namedEntity.getQuery().getNumOfDocs();
			inverseFrequency = onlineDocNumber/1+docFrequency;
		}
		else {
			inverseFrequency = N/(1+docFrequency);
		}
		return Math.log(inverseFrequency);
	}
	
	private static double norm (Collection<Double> c) {
		
		double sum=0;
		for (Double d: c) {
			sum+=(d*d);
		}
		
		return Math.sqrt(sum);
	}
	
	private void computeContextSimilarityScore(Database db) throws java.io.IOException, SQLException{
		
		Map<String, Integer> queryWords = this.namedEntity
														.getQuery()
														.getWordOccurrences();
		
		Map<String, Double> queryVector = new HashMap<>();
		
		for (Map.Entry<String, Integer> queryEntry: queryWords.entrySet()) {
			String word = queryEntry.getKey();
			int tf = queryEntry.getValue();
			double tfIdf = tf*(this.idf(db, word));
			queryVector.put(word, tfIdf);
		}
		
		
		
		Map<String, Integer> pageWords = PageParser.parse(this.wikiPage.getText());
		
		Map<String, Double> pageVector = new HashMap<>();
		
		for (Map.Entry<String, Integer> pageEntry: pageWords.entrySet()) {
			String word = pageEntry.getKey();
			int tf = pageEntry.getValue();
			double tfIdf = tf*(this.idf(db,word));
			pageVector.put(word, tfIdf);
		}
		
		double innerProduct=0;
		
		for (Map.Entry<String, Integer> q: queryWords.entrySet()) {
			String qWord = q.getKey();
			if (pageVector.containsKey(qWord)) {
				double pageIDF = pageVector.get(qWord);
				double queryIDF = q.getValue();
				innerProduct+=(pageIDF*queryIDF);
			}
		}
		
		double queryVectorNorm = norm(queryVector.values());
		double pageVectorNorm = norm(pageVector.values());
		
		
		// SET Context Similarity here
		this.contextSimilarityScore = innerProduct/(queryVectorNorm*pageVectorNorm);
	}
	
	public double getPopularityScore() {
		return java.lang.Math.log(1+popularityScore);
	}

	public double getContextSimilarityScore() {
		return contextSimilarityScore;
	}
	
	public String getURI() throws WikiTitleParsingException {
		
		String resultURI =  "http://en.wikipedia.org/wiki/"+this.wikiPage.getTitle().getWikiStyleTitle();
		
		return resultURI;
	}
	
	public double getWeightedScore(double mixer) {

		return mixer*this.getPopularityScore()+(1-mixer)*this.getContextSimilarityScore();
	}

}
