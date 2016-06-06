package com.rat;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextReviewHandler {
	public static ArrayList<Review> parseXML(String fileName) {
		ArrayList<Review> reviewArray = new ArrayList<Review>();
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
			br = new BufferedReader(new FileReader(fileName)); 
			String line = "";
			String regex = "\\[.\\d\\]";
			Pattern pattern = Pattern.compile(regex);
			String remRegex = "\\[\\w{1,2}\\]";
			while ((line = br.readLine()) != null) {
				if(line.contains("[t]"))
					continue;
				if(line.indexOf("##") == 0) {
					String text = line.substring(line.lastIndexOf("##") + 1);
					reviewList.add(text);
				}
				else {
					String[] tempArray = line.split("##");					
					String text = tempArray[1];
					//text = text.replaceAll("\\#", "");
					reviewList.add(text);
					String aspectsSentimentRaw = tempArray[0].trim();					
					String[] aspectsSentiments = aspectsSentimentRaw.split(",");					
					for(String aspectSentimentPair : aspectsSentiments) {
						String pair = aspectSentimentPair.replaceAll(remRegex, "");						
						if(pair.equals("") || pair == null)
							continue;
						Matcher matcher = pattern.matcher(pair);
						List<Integer> sentimentValues = new LinkedList<Integer>();
						while(matcher.find()) {
							String tempStr = matcher.group().replace('[', ' ');
							tempStr = tempStr.replace(']', ' ');
							tempStr = tempStr.trim();
							sentimentValues.add(Integer.parseInt(tempStr));
						}
						String str = pair.replaceAll(regex, ";delimiter;").toLowerCase();
						String[] parts = str.split(";delimiter;");
						int count = 0;
						for(String part : parts) {							
							aspectMap.put(part.trim(),sentimentValues.get(count));
							count++;
						}
					}
				}
			}
			br.close();			
			File file = new File(PropertiesFactory.getPropertyValue("reviewfilename"));
			if(file.exists())
				file.delete();
			file.createNewFile();
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			for(String reviewItem : reviewList) {
				if(reviewItem.length()>180)
					continue;
				writer.println(reviewItem);
			}			
			writer.close();
			file = new File(PropertiesFactory.getPropertyValue("goldstandardaspectsfilename"));
			if(file.exists())
				file.delete();
			file.createNewFile();
			writer = new PrintWriter(PropertiesFactory.getPropertyValue("goldstandardaspectsfilename"), "UTF-8");			
			for(Map.Entry<String,Integer> entry : aspectMap.entrySet()) {				
				writer.println(entry.getKey()+","+entry.getValue());				
			}			
			writer.close();			
		}
		catch(Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
	}
}