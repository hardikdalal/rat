package com.rat;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class TextReviewHandler {
	public static ArrayList parseXML(String fileName) {
		ArrayList reviewArray = new ArrayList();
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(fileName);			
			NodeList reviewsList = doc.getElementsByTagName("sentence");
			for (int i = 0; i < reviewsList.getLength(); ++i) {			
				Element sentenceNode = (Element) reviewsList.item(i);
				NodeList textNodes = sentenceNode.getElementsByTagName("text");
				Element text = (Element) textNodes.item(0);			
				String textValue = text.getFirstChild().getNodeValue();			
				NodeList aspectTerms = sentenceNode.getElementsByTagName("aspectTerms");
				if(aspectTerms.getLength() == 0) {				
					Review review = new Review(textValue,"null","null",9);
					reviewArray.add(review);
				}
				else {				
					for (int j = 0; j < aspectTerms.getLength(); ++j) {
						Element value = (Element) aspectTerms.item(j);				
						NodeList aspects = value.getElementsByTagName("aspectTerm");				
						for (int k = 0; k < aspects.getLength(); ++k)
						{
							Element aspect = (Element) aspects.item(k);
							String pos = aspect.getAttribute("pos");
							String term = aspect.getAttribute("term");
							String polarity = aspect.getAttribute("polarity");					
							int numPolarity = 0;
							if(polarity.equals("negative"))
								numPolarity = -1;
							else if(polarity.equals("positive"))
								numPolarity = 1;						
							Review review = new Review(textValue,term,pos,numPolarity);
							reviewArray.add(review);
						}
					}
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace(System.err);				
			System.exit(0);
		}		
		return reviewArray;
	}	
	public static void parseTextFile(String fileName) {
		ArrayList<String> reviewList = new ArrayList<String>();
		Map<String,Integer> aspectMap = new HashMap<String,Integer>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("resources/dataset/"+fileName)); 
			String line = "";
			while ((line = br.readLine()) != null) {
				if(line.contains("[t]"))
					continue;
				if(line.indexOf("##") == 0) {
					String text = line.substring(line.lastIndexOf("##") + 1);
					reviewList.add(text);
				}
				else {
					String[] temp = line.split("##");
					String text = temp[1];
					reviewList.add(text);
					String aspectsSentimentRaw = temp[0];
					String[] aspectsSentiments = aspectsSentimentRaw.split(",");
					for(String aspectSentimentPair : aspectsSentiments) {
//						String[] parts = aspectSentimentPair.split("\\[");
//						String aspect = parts[0];
//						String part2 = parts[1].substring(0,parts[1].length() - 1);						
//						int sentimentValue = Integer.parseInt(part2);
						String[] parts = aspectSentimentPair.split("\\[?(\\-|\\+)\\d\\]");
						
//						aspectMap.put(aspect,sentimentValue);
					}					
				}								
			}
			br.close();
			int counter = 0;
			File reviewFileName = new File(GlobalVars.reviewFileName);
			Files.deleteIfExists(reviewFileName.toPath());
			reviewFileName.createNewFile();
			PrintWriter writer = new PrintWriter(reviewFileName, "UTF-8");
			for(String reviewItem : reviewList) {
				writer.println(reviewItem);
				++counter;
			}
			writer.close();
			System.out.println(counter);
			counter = 0;
			writer = new PrintWriter(GlobalVars.goldStandardAspectsFileName, "UTF-8");			
			for(Map.Entry<String,Integer> entry : aspectMap.entrySet()) {				
				writer.println(entry.getKey()+","+entry.getValue());
				++counter;
			}
			writer.close();
			System.out.println(counter);
		}
		catch(Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}