package com.semihyavuz.app;


import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;


/*
 * PageParser is one of the most useful classes as it is a must
 * for both offline and online processes we need to perform 
 * in this project.
 * 
 * Given a plain text stored in String, Static method parse below
 * fetches every distinct word and numbers in special format
 * along with the count of their occurrences, and returns them
 * in a Map<String, Integer>. 
 */

public class PageParser {
    
    private final static Pattern WORD_PATTERN = Pattern.compile("(?:[a-zA-Z']{3,}|[a-zA-Z]{3,}|\\d+.?\\d*%|\\d{4})+(?!.*\\|)");
    private final static Set<String> stopWords = new TreeSet<>(); 
    
    static {
		BufferedReader br = null;
		 
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(App.IGNORE_LIST_PATH));
 
			while ((sCurrentLine = br.readLine()) != null) {
				String line[] = sCurrentLine.split("\t");
				for (String s: line){
	    			stopWords.add(s.toLowerCase());
	    		}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
    }
    
    public static Map<String, Integer> parse (String text) throws java.io.IOException {
        Map<String, Integer> result = new HashMap<>();
        String plainText = convertToPlainText(text);
        Matcher matcher = WORD_PATTERN.matcher(plainText);
        while(matcher.find()) {
            String match = matcher.group();
            String check = match;
            if (stopWords.contains(check.toLowerCase())) { 
            	continue;
            }
            Integer count = result.get(match);
            if(count == null) 
                result.put(match, new Integer(1));
            else
                result.put(match, new Integer(count + 1));
        }
        return result;
    }
    
    private static String convertToPlainText(String markup) throws IOException {
        String plainText = "";
        ProcessBuilder builder = new ProcessBuilder(App.PYTHON_PATH, App.PARSER_PATH);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        out.write(markup, 0, markup.length());
        out.close();
        
        char[] buffer = new char[markup.length()];
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int charsRead = in.read(buffer, 0, markup.length());
        plainText = new String(buffer, 0, charsRead);
        in.close();
        
        return plainText;
    }
    
}