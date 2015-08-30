package com.semihyavuz.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;


/*
 * Unlike SimpleSolver, ProSolver takes normalized popularity and context similarity scores
 * when computing the scoring and performing the actual ranking. 
 * 
 * This is the solver used in our experiments.
 */

public class ProSolver implements Solver {
	
	private double mixer;
	
	public ProSolver() {
		this.mixer = 0.5;
	}
	
	public ProSolver (double m) {
		this.mixer = m;
	}

	@Override
	public Map<NamedEntity, Candidate> solve(Wikipedia wiki, Map<NamedEntity, ArrayList<Candidate>> candidates) {
		Map<NamedEntity, Candidate> result = new HashMap<>();
		
		for (Map.Entry<NamedEntity, ArrayList<Candidate>> entry: candidates.entrySet()) {
			double popularitySum = 0;
			double contextSimSum = 0; 
			
			for (Candidate c: entry.getValue()) {
				popularitySum += c.getPopularityScore();
				contextSimSum += c.getContextSimilarityScore();
			}
			
			Candidate top = entry.getValue().get(0);
			double topScore = ((1-this.mixer)*(top.getPopularityScore()/popularitySum))+
								(this.mixer*(top.getContextSimilarityScore()/contextSimSum));
			double currentScore;
			for (Candidate currentCandidate: entry.getValue()) {
				currentScore = ((1-this.mixer)*(currentCandidate.getPopularityScore()/popularitySum))+
									(this.mixer*(currentCandidate.getContextSimilarityScore()/contextSimSum));

				if (currentScore>topScore) {
					top = currentCandidate;
					topScore = currentScore;
				}
			}
			
			result.put(entry.getKey(), top);
		}
		
		return result;
	}
	
}
