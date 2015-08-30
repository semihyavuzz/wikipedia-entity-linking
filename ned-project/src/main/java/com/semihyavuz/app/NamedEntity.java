package com.semihyavuz.app;

import java.sql.SQLException;
import java.util.ArrayList;

import de.tudarmstadt.ukp.wikipedia.api.*;

import java.util.*;

import de.tudarmstadt.ukp.wikipedia.api.exception.*;

/*
 * This class is exactly corresponding to a named entity provided by user 
 * by special formatting of the query text. 
 * 
 * Each NamedEntity in query is extracted in Query class.
 * 
 */

public class NamedEntity {
	private String text;
	private Query neQuery;
	
	public NamedEntity(String s, Query q) {
		this.text = s;
		this.neQuery = q;
	}
	
	public String getString() {
		return this.text;
	}
	
	public Query getQuery () {
		
		return this.neQuery;
	}
	
	
	public ArrayList<Candidate> getCandidates (Database db, Wikipedia wiki) throws WikiApiException, java.io.IOException, SQLException {
		ArrayList<Candidate> result = new ArrayList<>();
		
		// If a query is apple or bantam or something like this begining with lower case.
		if (!text.contains(" ")) {
			String first = text.substring(0, 1).toUpperCase();
			text = first+text.substring(1, text.length());
		}
		
		
		String pageSearchHandle2 = text;
		String pageSearchHandle1 = text.replaceAll(" ",  "_")+"_(disambiguation)";
		
		
		
		if (wiki.existsPage(pageSearchHandle1)) {
			Page disambigPage = wiki.getPage(pageSearchHandle1);
			Set<Page> candPages = disambigPage.getOutlinks();
			for (Page p: candPages) {
				result.add(new Candidate(p, this, db));
			}
			return result;
		}
		else {
			if (wiki.existsPage(pageSearchHandle2)) {
				Page possibleDisambigPage = wiki.getPage(pageSearchHandle2);
				String check1 = "may refer to:";
				String check2 = "may also refer to:";
				String pageContent = possibleDisambigPage.getPlainText().toLowerCase();
				// In this case it is still a disambiguation page
				if (pageContent.contains(check1) || pageContent.contains(check2) ) {
					Set<Page> candPages = possibleDisambigPage.getOutlinks();
					for (Page p: candPages) {
						result.add(new Candidate(p,this, db));
					}
					return result;
				}
				// We add page itself
				else {
					result.add(new Candidate(possibleDisambigPage, this, db));
					return result;
				}
			}
			else {
				// No corresponding page found.
				Page noCandidateFound = wiki.getPage(20455);
				result.add(new Candidate(noCandidateFound, this, db));
				return result;
			}
		}
	}
}
