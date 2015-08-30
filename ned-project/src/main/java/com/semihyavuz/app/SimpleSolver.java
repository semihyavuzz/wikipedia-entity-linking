package com.semihyavuz.app;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;


/*
 * Simple Solver directly gets the weighted scores of candidates,
 * and computes the best candidate based on this score. 
 * 
 * This means that it does not normalize popularity and
 * context similarity scores into [0,1] interval.
 */

public class SimpleSolver implements Solver {
	
	private double mixer;
	
	public SimpleSolver() {
		this.mixer = 0.5;
	}
	
	public SimpleSolver (double m) {
		this.mixer = m;
	}
	
	
	@Override
	public Map<NamedEntity, Candidate> solve(Wikipedia wiki, Map<NamedEntity, ArrayList<Candidate>> c) {
		
		Map<NamedEntity, Candidate> res = new HashMap<>();
		
		for (Map.Entry<NamedEntity, ArrayList<Candidate>> entry: c.entrySet()) {
			Candidate top = entry.getValue().get(0);
			double topScore = top.getWeightedScore(this.mixer);
			double currentScore;
			for (Candidate currentCandidate: entry.getValue()) {
				currentScore = currentCandidate.getWeightedScore(mixer);

				if (currentScore>topScore) {
					top = currentCandidate;
					topScore = currentScore;
				}
			}
			
			res.put(entry.getKey(), top);
		}
		return res;
	}
}
