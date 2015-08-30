package com.semihyavuz.app;

import java.util.ArrayList;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

public interface Solver {
	
	public abstract Map<NamedEntity, Candidate> solve(Wikipedia wiki, Map<NamedEntity, ArrayList<Candidate>> c);

}
