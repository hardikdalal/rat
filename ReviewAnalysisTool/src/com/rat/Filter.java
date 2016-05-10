package com.rat;
import java.util.*;
import java.io.*;

public class Filter {

	public static void apply(int top) {		
		sortAspects();
		System.out.println("\nFiltering top "+top+" aspects.");
		filterByRank(top);
	}
	private static void sortAspects() {
		BufferedReader br = null;
		PrintWriter writer = null;
		Map<String,Integer> aspectsMap = new HashMap<String,Integer>();
		try {
			br = new BufferedReader(new FileReader(GlobalVars.aspectsFileName)); 
			String line = "";
			
			while ((line = br.readLine()) != null) {				
				String aspectFrequency[] = line.split(",");				
				aspectsMap.put(aspectFrequency[0],Integer.parseInt(aspectFrequency[1]));
			}
			br.close();
			
			LinkedHashMap sortedAspectsMap = sortHashMapByValuesD(aspectsMap);
			
			writer = new PrintWriter(GlobalVars.sortedAspectsFileName, "UTF-8");
			Set<Map.Entry> entries = sortedAspectsMap.entrySet();
			for(Map.Entry entry : entries) {
				writer.println(entry.getKey()+","+String.valueOf(entry.getValue()));
			}
			writer.close();
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
	private static LinkedHashMap sortHashMapByValuesD(Map passedMap) {
		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		Collections.reverse(mapValues);
		Collections.sort(mapKeys);
		Collections.reverse(mapKeys);

		LinkedHashMap sortedMap = new LinkedHashMap();

		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
		   Object val = valueIt.next();
		   Iterator keyIt = mapKeys.iterator();

		   while (keyIt.hasNext()) {
			   Object key = keyIt.next();
			   String comp1 = passedMap.get(key).toString();
			   String comp2 = val.toString();

			   if (comp1.equals(comp2)){
				   passedMap.remove(key);
				   mapKeys.remove(key);
				   sortedMap.put((String)key, (Integer)val);
				   break;
			   }

		   }

		}
		return sortedMap;
	}

	private static void filterByRank(int top) {
		BufferedReader br = null;
		PrintWriter writer = null;
		LinkedHashMap<String,Integer> sortedAspectsMap = new LinkedHashMap<String,Integer>();
		Map<String,Map<String,Integer>> otMap = new HashMap<String,Map<String,Integer>>();
		try {
			br = new BufferedReader(new FileReader(GlobalVars.sortedAspectsFileName)); 
			String line = "";
			int counter = 1;
			while ((line = br.readLine()) != null && counter <= top) {
				String aspectFrequency[] = line.split(",");
				sortedAspectsMap.put(aspectFrequency[0],Integer.parseInt(aspectFrequency[1]));
				++counter;
			}
			br.close();
			
			br = new BufferedReader(new FileReader(GlobalVars.otPairsFileName));			
			line = "";
			while ((line = br.readLine()) != null) {
				String pair[] = line.split(",");
				Map<String,Integer> opinions = new HashMap<String,Integer>();
				
				for(int i = 1; i<pair.length; ++i) {					
					String opinionFrequency[] = pair[i].split("/");					
					opinions.put(opinionFrequency[0],Integer.parseInt(opinionFrequency[1]));
				}
				otMap.put(pair[0],opinions);
				
			}
			br.close();
			
			writer = new PrintWriter(GlobalVars.filteredOTPairsFileName, "UTF-8");			
			for(Map.Entry<String,Integer> entry : sortedAspectsMap.entrySet()) {
				StringBuilder opinionStr = new StringBuilder();				
				Map<String,Integer> opinionMap = otMap.get(entry.getKey());
				if(opinionMap!=null) {
					for(Map.Entry<String,Integer> opinion : opinionMap.entrySet()) {
						opinionStr.append(opinion.getKey());
						opinionStr.append("/");
						opinionStr.append(opinion.getValue());					
						opinionStr.append(",");
					}
					opinionStr.setLength(opinionStr.length()-1);
					writer.println(entry.getKey()+ "," + String.valueOf(opinionStr));
				}
			}
			
			writer.close();
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