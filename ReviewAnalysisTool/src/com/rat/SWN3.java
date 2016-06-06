package com.rat;
import java.util.*;
import java.io.*;

public class SWN3 {
	
	public static void calculateSentimentScore() {
		System.out.println("Calculating sentiment score for each aspects.");
		Map<String,Map<String,Integer>> otMap = new LinkedHashMap<String,Map<String,Integer>>();
		Map<String,Double> summaryMap = new LinkedHashMap<String,Double>();
		BufferedReader br = null;
		PrintWriter writer = null;
		try {
			br = new BufferedReader(new FileReader(PropertiesFactory.getPropertyValue("filteredotpairsfilename")));
			String line = "";
			Set<String> opinions = new HashSet<String>();
			while ((line = br.readLine()) != null) {
				String aspectOpinionPair[] = line.split(",");
				Map<String,Integer> opinionsMap = new HashMap<String,Integer>();				
				for(int i = 1; i<aspectOpinionPair.length;++i) {
					String opinionFrequency[] = aspectOpinionPair[i].split(";");
					opinionsMap.put(opinionFrequency[0],Integer.parseInt(opinionFrequency[1]));
					opinions.add(opinionFrequency[0]);
				}
				otMap.put(aspectOpinionPair[0],opinionsMap);
			}
			br.close();			
			
			Map<String,Double> sentiMap = new HashMap<String,Double>();			
			br = new BufferedReader(new FileReader(PropertiesFactory.getPropertyValue("swnfilename"))); 
			line = "";
			while ((line = br.readLine()) != null) {
				String rowValues[] = line.split("\t");
				String opinionWords = rowValues[4];
				double sentiScore = Double.parseDouble(rowValues[2]) - Double.parseDouble(rowValues[3]);
				opinionWords = opinionWords.replaceAll("\\p{Punct}\\d","");				
				String words[] = opinionWords.split(" ");
				for(String word : words) {
					if(opinions.contains(word)) {
						sentiMap.put(word,sentiScore);						
					}
				}
			}
			br.close();			
			for(Map.Entry<String,Map<String,Integer>> otPair : otMap.entrySet()) {				
				Map<String,Integer> opinionsMap = otPair.getValue();
				double score = 0;				
				for(Map.Entry<String,Integer> opinion : opinionsMap.entrySet()) {
					String opinionStr = opinion.getKey();
					int frequency = opinion.getValue();					
					if(sentiMap.get(opinionStr)!=null) {
						double sentiScore = sentiMap.get(opinionStr);
						sentiScore*=frequency;
						score+=sentiScore;
					}
				}
				summaryMap.put(otPair.getKey(),score);
			}
			writer = new PrintWriter(PropertiesFactory.getPropertyValue("summaryfilename"), "UTF-8");
			writer.println("Rank\tAspect -> Sentiment Score");
			int counter = 0;
			for(Map.Entry<String,Double> entry : summaryMap.entrySet()) {
				++counter;
				writer.println(String.valueOf(counter)+"\t\t"+entry.getKey()+" -> "+entry.getValue());
			}
			writer.close();
			System.out.println("Refer "+PropertiesFactory.getPropertyValue("summaryfilename")+" for results");
		}
		catch(Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage() );				
			System.exit(0);
		}
		finally {
			try {
				br.close();
				writer.close();
			}
			catch(Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage() );				
				System.exit(0);
			}
		}
	}
}