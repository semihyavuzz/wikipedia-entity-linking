package com.semihyavuz.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.*;

/*
 * App is the class through which everything is merged together.
 * 
 * It has one method called 'findWikipediaLinks(String s)' which takes the
 * query as a string and returns the most coherent Wikipedia entity for each Named Entity
 * detected in the query.
 * 
 */


public class App
{
	private Solver solver;
	private Wikipedia wiki;
	public static final Database DB = new Database("localhost", "wikipedia", "root", "********password*********");
	public static final String PARSER_PATH = "/Users/semihyavuz/Desktop/EntityLinking/Cleaner.py";
	public static final String PYTHON_PATH = "/usr/bin/python";
	public static final String IGNORE_LIST_PATH = "/Users/semihyavuz/Desktop/EntityLinking/stopWords.txt";
	
	public App (Wikipedia w) {
		this.wiki = w;
		this.solver = new SimpleSolver ();
	}
	
	public App(Solver s, Wikipedia w) {
		this.solver = s;
		this.wiki = w;
	}
	
	
	public void setSolver (Solver nSolver) {
		this.solver = nSolver;
	}
	
	

	// This method was written to enable online document frequency computation from among only the candidates.
	private static void writeTable (ArrayList<Page> distPages) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, WikiInitializationException, IOException {
        
        Wikipedia wiki = new Wikipedia(App.DB.getDbConfig());
        for (Page currentPage: distPages) {
            String currentContent = currentPage.getText();
            Map<String, Integer> distinctWordList = PageParser.parse(currentContent);
            
            for (Map.Entry<String, Integer> entry : distinctWordList.entrySet()) {
                String currentWord = entry.getKey();
                App.DB.updateOnlineTable(currentWord);
            }
        }
	}	
	
	// This method was written to enable online document frequency computation from among only the candidates.
	private static void removeTable () throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        App.DB.deleteOnlineTable();
	}
	
	public Map<String, String> findWikipediaLinks(String s) throws WikiApiException, java.io.IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Query query = new Query(s);
		ArrayList<NamedEntity> namedEntities = query.getNamedEntities();
		
		Map<NamedEntity, ArrayList<Candidate>> candidatePages = new HashMap<>();
		for (NamedEntity ne: namedEntities) {
			ArrayList<Candidate> cp = ne.getCandidates(DB, this.wiki);
			candidatePages.put(ne, cp);
		}
		
		Map<NamedEntity, Candidate> resultingLinks = solver.solve(this.wiki, candidatePages);
		
		Map<String,String> result = new HashMap<String, String>();
		
		for (Entry<NamedEntity, Candidate> nc: resultingLinks.entrySet()) {
			String namedEntity = nc.getKey().getString();
			String link = nc.getValue().getURI();
			result.put(namedEntity, link);
		}

		return result;
	}
	

	
	/* 
	 * This main class opens up an interactive console where user can input
	 * queries and get the Wikipedia Links of the desired named entities 
	 * indicated by appropriately formatting in between brackets.
	 */
	
    public static void main( String[] args ) 
    {
        Scanner scan=null;
        try {
        	App.DB.connect();
        	scan = new Scanner(System.in);
			Wikipedia wiki = new Wikipedia(App.DB.getDbConfig());
			
			// Change the mixer value in construction of Solver
			ProSolver s = new ProSolver(0.7);
			App app = new App(s, wiki);
			
			while (scan.hasNextLine()) { 
				Map<String, String> result = app.findWikipediaLinks(scan.nextLine());
				for (Entry<String, String> e: result.entrySet()) {
					System.out.printf("%s => %s => %s \n", e.getKey(), e.getValue(), "http://en.wikipedia.org/wiki/"+e.getValue());
				}
			}
		} catch (WikiInitializationException e) {
			System.out.println("Could not connect to database!");
			e.printStackTrace();
		} catch (java.io.IOException e) {
			System.out.println("IO exception occured.");
			e.printStackTrace();
		} catch (WikiApiException e) {
			System.out.println("Wikipedia API exception occured.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("SQL Exception occurred.");
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			System.out.println("ClassNotFoundException occurred.");
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			System.out.println("Instantiation Exception occurred");
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			System.out.println("IllegalAccessException occurred");
			e1.printStackTrace();
		}finally {
			scan.close();
		}
    }
}
