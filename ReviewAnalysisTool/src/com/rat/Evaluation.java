package com.rat;
import java.util.*;
import java.io.*;
import java.sql.*;

public class Evaluation {

	
	private static void sortAspects() {
		BufferedReader br = null;
		PrintWriter writer = null;
		Map<String,Integer> aspectsMap = new HashMap<String,Integer>();
		try {
			br = new BufferedReader(new FileReader(PropertiesFactory.getPropertyValue("goldstandardaspectsfilename"))); 
			String line = "";
			
			while ((line = br.readLine()) != null) {				
				String aspectFrequency[] = line.split(",");				
				aspectsMap.put(aspectFrequency[0],Integer.parseInt(aspectFrequency[1]));
			}
			br.close();
			
			LinkedHashMap sortedAspectsMap = sortHashMapByValuesD(aspectsMap);
			
			writer = new PrintWriter(PropertiesFactory.getPropertyValue("sortedgoldstandardaspectsfilename"), "UTF-8");
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
	
	public static void compare() {		
		BufferedReader br = null;
		PrintWriter writer = null;
		Map<String,Integer> aspectsMap = new HashMap<String,Integer>();
		Map<String,Integer> aspectsMapGS = new HashMap<String,Integer>();
		try {
			br = new BufferedReader(new FileReader(PropertiesFactory.getPropertyValue("goldstandardaspectsfilename"))); 
			String line = "";
			while ((line = br.readLine()) != null) {				
				String aspectFrequency[] = line.split(",");
				aspectsMapGS.put(aspectFrequency[0],Integer.parseInt(aspectFrequency[1]));
			}
			br.close();
						
			br = new BufferedReader(new FileReader(PropertiesFactory.getPropertyValue("aspectsfilename")));			
			while ((line = br.readLine()) != null) {				
				String aspectFrequency[] = line.split(",");
				aspectsMap.put(aspectFrequency[0],Integer.parseInt(aspectFrequency[1]));
			}
			br.close();
			
			int extractedAspectCount = aspectsMap.size(),gsAspectCount = aspectsMapGS.size();
			int commonAspectCount = 0;
			float precision = 0,recall = 0;
			for(Map.Entry<String,Integer> aspects : aspectsMapGS.entrySet()) {
				for(Map.Entry<String,Integer> aspectsGS : aspectsMap.entrySet()) {
					String aspectStr = aspects.getKey().toLowerCase();
					String aspectGSStr = aspectsGS.getKey().toLowerCase();
					if(aspectGSStr.contains(aspectStr)) {						
						++commonAspectCount;
					}
				}
			}
			precision = (float)commonAspectCount/extractedAspectCount;
			recall = (float)commonAspectCount/gsAspectCount;
			System.out.println("Precision = " + precision + "\tRecall = " + recall+"\tF-Measure = "+2*((precision*recall)/(precision+recall)));
		}
		catch(Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());				
			System.exit(0);
		}
	}
}
