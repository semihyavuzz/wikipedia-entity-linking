package com.semihyavuz.app;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.*;

import java.util.Map;


/*	As can be understood from its name, Query is the object we create right when
 * 	we take input query from user. 
 * 
 *	Through this object, we parse query string and 
 *	extract useful information that will be used in 
 *	the next levels of the whole pipeline such as 
 *	'wordOccurrences' via getWordOccurrences() and 
 *	'NamedEntities' via getNamedEntities ().
 *	
 * 
 */

public class Query {
	private int numOfDocs;
	private String queryText;
	private static Pattern PATTERN = Pattern.compile("\\[([^\\]\\[]+)\\]");
	private static Pattern PATTERN2 = Pattern.compile("\\w+");
	
	private Map<String, Integer> wordOccurrences;
	
	public Query (String qText) {
		this.setQueryText(qText);
		this.numOfDocs=0;
	}

	
	public String getQueryText () {
		return queryText;
	}
	
	public void setQueryText (String text) {
		this.queryText = text;
		setWordOccurrences();
	}
	
	public ArrayList<NamedEntity> getNamedEntities () {
		
		ArrayList<NamedEntity> ner = new ArrayList<>();
		Matcher mat = PATTERN.matcher(queryText);
		while (mat.find()) {
			ner.add(new NamedEntity(mat.group(1), this));
		}
		return ner;
	}
	
	private void setWordOccurrences() {
		Map<String, Integer> occurrences = new TreeMap<>();
		Matcher mat = PATTERN2.matcher(queryText);
		
		while (mat.find()) {
			String w = mat.group();
			if (occurrences.containsKey(w)) {
				occurrences.put(w, occurrences.get(w)+1);
			}
			else {
				occurrences.put(w, 1);
			}
		}
		
		this.wordOccurrences = occurrences;
	}
	
	public Map<String, Integer> getWordOccurrences() {
		return wordOccurrences;
	}
	
	public void setNumOfDocs (int num) {
		this.numOfDocs=num;
	}
	
	public int getNumOfDocs () {
		return this.numOfDocs;
	}	
	
}
