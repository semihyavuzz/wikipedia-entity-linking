package com.semihyavuz.app;


import java.io.*;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.*;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;


/*
 * This class is to learn the weighting between popularity and context similarity
 * 
 * This will return us the mixer scalar.
 */

public class Tuner {
	private App disambiguator;
	private String trainingQueries, trainingLinks;
	
	private static ArrayList<Double> coarseScalarList = new ArrayList<>();
	
	static {
		for (double i=0;i<=10;i++) {
			coarseScalarList.add(i/10);
		}
	}
	
	public final static String TRAINING_QUERY_FILE = "/Users/semihyavuz/Desktop/EntityLinking/trainingQueries.txt";
	public final static String TRAINING_LINK_FILE = "/Users/semihyavuz/Desktop/EntityLinking/trainingLinks.txt";
	
	
	public Tuner (App a, String q, String l) {
		this.disambiguator = a;
		this.trainingQueries = q;
		this.trainingLinks = l;
	}
	
	public  Map<String, ArrayList<String>> getTrainingExamples () {
		
		Map<String, ArrayList<String>> resultExamples = new HashMap<String, ArrayList<String>>();
		
		BufferedReader queryReader = null;
		BufferedReader linkReader = null;
		 
		try {
			String currentQuery, currentLink;
			
			queryReader = new BufferedReader(new FileReader(this.trainingQueries));
			linkReader = new BufferedReader (new FileReader(this.trainingLinks));
 
			while (((currentQuery = queryReader.readLine())!=null) && ((currentLink = linkReader.readLine()) != null)) {
				ArrayList<String> listOfLinks = new ArrayList<>();
				listOfLinks.add(currentLink);
				resultExamples.put(currentQuery, listOfLinks);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (queryReader != null) queryReader.close();
				if (linkReader != null) linkReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return resultExamples;
	}
	
	
	public Map<Double, Double> tune (Map<String, ArrayList<String>> examples, ArrayList<Double> scalarList) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
		
		Map<Double, Double> results = new HashMap<>();
		double numOfExamples = examples.size();
		
		
		for (Double scalar: scalarList) {
			this.disambiguator.setSolver(new ProSolver(scalar));
			double count=0;
			
			for (Map.Entry<String, ArrayList<String>> example: examples.entrySet()) {
				String exampleQuery = example.getKey();
				ArrayList<String> correctLinks = example.getValue();
				
				Map<String, String> foundLinks = new HashMap<>();
				try {
					 foundLinks= disambiguator.findWikipediaLinks(exampleQuery);
				} catch (WikiApiException | ClassNotFoundException
						| InstantiationException | IllegalAccessException
						| IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				boolean test = true;
				// Check if found links are correct
				for (String found: foundLinks.values()) {
					if (correctLinks.contains(found)) {
						continue;
					}
					else {
						test = false;
						break;
					}
				}
				
				if (test) {
					count++;
				}	
			}
			
			try {
				App.DB.updateTrainingTable(scalar, count);
			} catch (SQLException e) {
				System.out.println("Updating Training table failed!");
				e.printStackTrace();
			}
			results.put(scalar, count);
			System.out.println("-------------------------------Iteration for "+scalar+" is complete----------------------");
		}
		
		System.out.println("Coarse Training is complete");
		return results;
	}
	
	public double fineTrain (Map<String, ArrayList<String>> examples) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
		
		Map<Double, Double> coarseTraininingResults = this.tune(examples, Tuner.coarseScalarList);
		
		double bestScalar = 0;
		double bestScore = 0;
		for (Map.Entry<Double, Double> coarseEntry: coarseTraininingResults.entrySet()) {
			double candidateScalar= coarseEntry.getKey();
			double candidateScore = coarseEntry.getValue();
			if (candidateScore>bestScore) {
				bestScalar = candidateScalar;
			}
		}
		
		ArrayList<Double> fineScalarList = new ArrayList<>();
		
		double pivotScalar = bestScalar/2;
		double increment = bestScalar/10;
		
		for (int i=0;i<=10;i++) {
			fineScalarList.add(pivotScalar+i*increment);
		}
		
		
		Map<Double, Double> fineTrainingResults = this.tune(examples, fineScalarList);
		
		double optimalScore=0;
		double optimalScalar=0;
		for (Map.Entry<Double, Double> entry: fineTrainingResults.entrySet()) {
			double key = entry.getKey();
			double val = entry.getValue();
			
			if (val>optimalScore) {
				optimalScalar = key;
				optimalScore = val;
			}
		}
		
		return optimalScalar;
	}
	
	public static void main (String [] args) {
		try {
			App.DB.connect();
			Wikipedia wiki = new Wikipedia(App.DB.getDbConfig());
			SimpleSolver s = new SimpleSolver();
			App app = new App(s, wiki);
			Tuner tr = new Tuner (app, Tuner.TRAINING_QUERY_FILE, Tuner.TRAINING_LINK_FILE);
			
			// Tuning
			Map<String, ArrayList<String>> examples = tr.getTrainingExamples();
			Double optimalWeighting = tr.fineTrain(examples);
			
			System.out.println(optimalWeighting);
			
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
		} finally {
			try {
				App.DB.close();
			} catch (SQLException e) {
				System.out.println("Database APP.DB could not be closed!");
				e.printStackTrace();
			}
		}
	}
}
