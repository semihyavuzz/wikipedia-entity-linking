package com.semihyavuz.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ThisExpression;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

/*
 * This class is to test the performance of 
 * our fine-tuned named entity disambiguator.
 */

public class Test {
	
	private App disambiguator;
	private String testQueries, testLinks;
	
	private final static double opt_alpha = 0.7;
	private final static String TEST_QUERY_FILE = "/Users/semihyavuz/Desktop/EntityLinking/testQueries.txt";
	private final static String TEST_LINK_FILE = "/Users/semihyavuz/Desktop/EntityLinking/testLinks.txt";
	
	private final static String ALL_QUERY_FILE = "/Users/semihyavuz/Desktop/EntityLinking/allQueries.txt";
	private final static String ALL_LINK_FILE = "/Users/semihyavuz/Desktop/EntityLinking/allLinks.txt";
	
	
	public Test (App a, String q, String l) {
		this.disambiguator=a;
		this.testQueries=q;
		this.testLinks=l;
	}
	
	
	public  Map<String, ArrayList<String>> getTestExamples () {
		
		Map<String, ArrayList<String>> resultExamples = new HashMap<String, ArrayList<String>>();
		
		BufferedReader queryReader = null;
		BufferedReader linkReader = null;
		 
		try {
			String currentQuery, currentLink;
			
			queryReader = new BufferedReader(new FileReader(this.testQueries));
			linkReader = new BufferedReader (new FileReader(this.testLinks));
 
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
	
	
	public double getAccuracy (Map<String, ArrayList<String>> examples) throws IOException {
		double count=0;
		
		File file = new File ("/Users/semihyavuz/Desktop/EntityLinking/TestResults.txt");
		
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
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
				bw.write("true \t");
				count++;
			}	
			else {
				bw.write("false \t");
			}
			
			for (Map.Entry<String, String> f: foundLinks.entrySet()) {
				bw.write(f.getKey()+"\t");
				bw.write(f.getValue()+"\n");
			}
		}
		
		bw.write("Accuracy :"+ count);
		
		bw.close();
		
		return count;
	}

	public static void main (String [] args) {
		
		try {
			App.DB.connect();
		} catch (MalformedURLException | ClassNotFoundException
				| InstantiationException | IllegalAccessException
				| SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Wikipedia wiki=null;
		try {
			wiki = new Wikipedia(App.DB.getDbConfig());
		} catch (WikiInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ProSolver s = new ProSolver (Test.opt_alpha);
		App app = new App(s, wiki);
		
		Test te = new Test (app, Test.TEST_QUERY_FILE , Test.TEST_LINK_FILE);
		
		double testAccuracy = 0;
		try {
			testAccuracy = te.getAccuracy(te.getTestExamples());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Accuracy: "+ testAccuracy);
	}

}
