package com.semihyavuz.app;

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

/*
 * This class is for an offline process of finding document frequencies of words
 * appearing in Wikipedia articles. 
 * 
 * More precisely: It spans through the articles in Wikipedia and for each word it keeps track of the count
 * of the documents in which the words appear.
 * 
 * It excludes STOP-WORDS and words that are shorter than 3 characters in length.
 * 
 * IN SHORT: It writes the document frequencies in table named "DocumentFrequencies"
 * in wikipedia DATABASE.
 * 
 */

public class DocumentFrequencyFinder {
    
    public static void main (String [] args) throws MalformedURLException, WikiInitializationException, SQLException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException  {
        
        Database database = new Database("localhost", "wikipedia", "root", "**********password*********");
        database.connect();
        
        Wikipedia wiki = new Wikipedia(database.getDbConfig());
        
        Iterable<Page> pages = wiki.getArticles();
        
        
        for (Page currentPage: pages) {
            String currentContent = currentPage.getText();
            Map<String, Integer> distinctWordList = PageParser.parse(currentContent);
            
            for (Map.Entry<String, Integer> entry : distinctWordList.entrySet()) {
                String currentWord = entry.getKey();
                database.updateFrequency(currentWord);
            }
        }
    }
} 