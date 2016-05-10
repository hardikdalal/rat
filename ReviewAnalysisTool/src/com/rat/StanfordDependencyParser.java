package com.rat;

import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import edu.stanford.nlp.ling.*;

import java.util.*;
import java.io.*;

public class StanfordDependencyParser {
	
	private static Map<String,Map<String,Integer>> otMap = new HashMap<String,Map<String,Integer>>();
	
	public static void parseReviewFile(String fileName) {
		
		//Lemmatization		
		System.out.println("Lemmatizing reviews first for parsing.");
		lemmatizeText(fileName);
		
		//Parsing starts here
		long startTime = System.nanoTime();
		System.out.println("Parsing reviews for aspects and opinion words now. This will take a while.");
		parseText(fileName);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000000;
		duration = duration/60;
		System.out.println("\nDuration: "+String.valueOf(duration)+" minutes(s)");
	}
	
	private static void parseText(String fileName) {
				
		LexicalizedParser lp = LexicalizedParser.loadModel(GlobalVars.parserModel);		
		
		// This option shows loading, sentence-segmenting and tokenizing a file using DocumentPreprocessor.		
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures())
			gsf = tlp.grammaticalStructureFactory();
		// You could also create a tokenizer here (as below) and pass it to DocumentPreprocessor.
		
		Map<String,String> opinionWords = new HashMap<String,String>();
		Map<String,String> aspectTerms = new HashMap<String,String>();		
		opinionWords.put("null","good");
		opinionWords.put("null","bad");
		Vector adjectiveTags = new Vector();
		Vector nounTags = new Vector();
		adjectiveTags.add("JJ");
		adjectiveTags.add("JJR");
		adjectiveTags.add("JJS");
		nounTags.add("NN");
		nounTags.add("NNS");
		int iCount = 0;
		boolean flag = true;		
		while(flag) {
			++iCount;
			flag = false;
			int i = 0;
			for (List<HasWord> sentence : new DocumentPreprocessor(fileName)) {
				Tree parse = lp.apply(sentence);
				++i;
				// Print sentence with POS tags
				// List taggedWords = parse.taggedYield();
				// sentencesWPOS.add(taggedWords);
				
				if (gsf != null) {
					GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
					Collection tdl = gs.typedDependenciesCCprocessed();					
					Iterator iterator = tdl.iterator();
					int count = 0;
					int j = 0;
					while (iterator.hasNext()) {
						++j;
						TypedDependency td = (TypedDependency) iterator.next();
						GrammaticalRelation gr = td.reln();						
						String dependent = td.dep().originalText();
						String depTag = td.dep().tag();
						String governor =td.gov().originalText();
						String govTag = td.gov().tag();						
						//System.out.println("Relation: " +gr.toString()+"\nGovernor: "+td.gov()+"\nDependent: "+td.dep());						
						
						String govLoc = String.valueOf(i)+","+String.valueOf(j)+","+String.valueOf(0);
						String depLoc = String.valueOf(i)+","+String.valueOf(j)+","+String.valueOf(1);
																		
						if(opinionWords.containsValue(dependent)) {
							//R11
							if(gr.toString().contains("amod")) {
								if(nounTags.contains(govTag)) {
									if(!aspectTerms.containsKey(govLoc)) {
										aspectTerms.put(govLoc,governor);										
										flag = true;
									}
									updateOTMap(governor,dependent);
								}
							}
							//R12
							if(gr.toString().contains("dobj")) {
								Collection tdl2 = gs.typedDependenciesCCprocessed();
								int k = 0;
								for (Object o2 : tdl2) {
									++k;
									TypedDependency td2 = (TypedDependency) o2;
									GrammaticalRelation gr2 = td2.reln();
									String dependent2 = td2.dep().originalText();
									String depTag2 = td2.dep().tag();
									String governor2 = td2.gov().originalText();
									
									String govLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(0);
									String depLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(1);

									if(gr2.toString().contains("nsubj") && governor2.equalsIgnoreCase(governor)) {									
										if(nounTags.contains(depTag2)) {
											if(!aspectTerms.containsKey(depLoc2)) {
												aspectTerms.put(depLoc2,dependent2);												
												flag = true;
											}
											updateOTMap(dependent2,dependent);
										}
									}
								}								
							}
							//R41
							if(gr.toString().contains("conj")) {
								if(opinionWords.containsValue(dependent)) {
									if(adjectiveTags.contains(govTag)) {										
										if(!opinionWords.containsKey(govLoc)) {
											opinionWords.put(govLoc,governor);											
											flag = true;											
										}
									}
								}
							}
							//R42
							Collection tdl2 = gs.typedDependenciesCCprocessed();
							int k = 0;
							for (Object o2 : tdl2) {
								++k;
								TypedDependency td2 = (TypedDependency) o2;
								GrammaticalRelation gr2 = td2.reln();
								String dependent2 = td2.dep().originalText();
								String depTag2 = td2.dep().tag();
								String governor2 = td2.gov().originalText();

								String govLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(0);
								String depLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(1);

								if(gr2.toString().contains(gr.toString()) && governor.equalsIgnoreCase(governor2)) {
									if(adjectiveTags.contains(depTag2)) {
										if(!opinionWords.containsKey(depLoc2)) {
											opinionWords.put(depLoc2,dependent2);											
											flag = true;											
										}										
									}
								}
							}
						}
						else if(aspectTerms.containsValue(dependent)) {							
							//R22
							if(gr.toString().contains("nsubj")) {
								Collection tdl2 = gs.typedDependenciesCCprocessed();
								int k = 0;
								for (Object o2 : tdl2) {
									++k;
									TypedDependency td2 = (TypedDependency) o2;
									GrammaticalRelation gr2 = td2.reln();
									String dependent2 = td2.dep().originalText();
									String depTag2 = td2.dep().tag();
									String governor2 = td2.gov().originalText();
									
									String govLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(0);
									String depLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(1);
									
									if(gr2.toString().contains("dobj") && governor2.equalsIgnoreCase(governor)) {
										if(adjectiveTags.contains(depTag2)) {											
											if(!opinionWords.containsKey(depLoc2)) {
												flag = true;
												opinionWords.put(depLoc2,dependent2);												
											}
											updateOTMap(dependent,dependent2);
										}
									}
								}								
							}
							//R31
							if(gr.toString().contains("conj")) {
								if(nounTags.contains(govTag)) {																	
									if(!aspectTerms.containsKey(govLoc)) {
										flag = true;
										aspectTerms.put(govLoc,governor);
									}									
								}
							}
							//R32
							if(gr.toString().contains("nsubj")) {
								if(governor.equalsIgnoreCase("has")) {
									//Find governor in dobj relation
									Collection tdl2 = gs.typedDependenciesCCprocessed();
									int k = 0;
									for (Object o2 : tdl2) {
										++k;
										TypedDependency td2 = (TypedDependency) o2;
										GrammaticalRelation gr2 = td2.reln();
										String dependent2 = td2.dep().originalText();
										String depTag2 = td2.dep().tag();
										String governor2 = td2.gov().originalText();

										String govLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(0);
										String depLoc2 = String.valueOf(i)+","+String.valueOf(k)+","+String.valueOf(1);
										
										if(gr2.toString().contains("dobj") && governor2.equalsIgnoreCase(governor)) {										
											if(nounTags.contains(depTag2)) {
												if(!aspectTerms.containsKey(depLoc2)) {
													aspectTerms.put(depLoc2,dependent2);
													flag = true;													
												}
											}
										}
									}
								}
							}
						}
						if(aspectTerms.containsValue(governor)) {
							//R21
							if(gr.toString().contains("amod")) {
								if(adjectiveTags.contains(depTag)) {									
									if(!opinionWords.containsKey(depLoc)) {
										opinionWords.put(depLoc,dependent);										
										flag = true;
									}
									updateOTMap(governor,dependent);
								}
							}
						}
					}
				}
			}
			System.out.println("Iteration#"+String.valueOf(iCount));
			System.out.println("Number of Opinion words in list: "+String.valueOf(opinionWords.size()));
			System.out.println("Number of Aspect terms in list: "+String.valueOf(aspectTerms.size()));
		}
		System.out.println("\nParsing successfully completed.");
		System.out.println("Number of iterations: " + String.valueOf(iCount));
		Map<String,Integer> opinionDict = new HashMap<String,Integer>();
		Map<String,Integer> aspectDict = new HashMap<String,Integer>();
		
		for(Map.Entry<String,String> term : opinionWords.entrySet()) {
			int frequency = 1;
			if(opinionDict.containsKey(term.getValue())) {
				frequency = opinionDict.get(term.getValue());
				opinionDict.put(term.getValue(),++frequency);
			}
			else
				opinionDict.put(term.getValue(),frequency);
		}
		for(Map.Entry<String,String> term : aspectTerms.entrySet()) {
			int frequency = 1;
			if(aspectDict.containsKey(term.getValue())) {
				frequency = aspectDict.get(term.getValue());
				aspectDict.put(term.getValue(),++frequency);
			}
			else
				aspectDict.put(term.getValue(),frequency);
		}
		
		PrintWriter writer = null;
		try	{			
			writer = new PrintWriter(GlobalVars.aspectsFileName, "UTF-8");			
			for(Map.Entry<String,Integer> term : aspectDict.entrySet()) {
				writer.println(term.getKey()+","+String.valueOf(term.getValue()));
			}
			writer.close();
			System.out.println("Aspect terms written to file.");
			writer = new PrintWriter(GlobalVars.opinionsFileName, "UTF-8");			
			for(Map.Entry<String,Integer>  opinion : opinionDict.entrySet()) {
				writer.println(opinion.getKey()+ "," + String.valueOf(opinion.getValue()));
			}
			writer.close();
			System.out.println("Opinion words written to file.");
						
			writer = new PrintWriter(GlobalVars.otPairsFileName, "UTF-8");			
			for(Map.Entry<String,Map<String,Integer>>  otPair : otMap.entrySet()) {
				StringBuilder opinionStr = new StringBuilder();				
				Map<String,Integer> opinionMap = otPair.getValue();				
				for(Map.Entry<String,Integer> opinion : opinionMap.entrySet()) {
					opinionStr.append(opinion.getKey());
					opinionStr.append("/");
					opinionStr.append(opinion.getValue());					
					opinionStr.append(",");
				}
				opinionStr.setLength(opinionStr.length()-1);
				writer.println(otPair.getKey()+ "," + String.valueOf(opinionStr));
			}
			writer.close();
			System.out.println("Opinion-Aspect term pairs written to file.");		
		}
		catch(Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage() );				
			System.exit(0);
		}
		
		// Print PCGF Tree
		// parse.pennPrint();
		// System.out.println();

		// Print sentence with POS tags
		// List taggedWords = parse.taggedYield();
		// System.out.println(taggedWords);
		// System.out.println();

	}
	
	private static void updateOTMap(String aspect, String opinion) {		
		if(otMap.containsKey(aspect)) {
			Map<String,Integer> opinions = otMap.get(aspect);
			otMap.remove(aspect);
			if(opinions.containsKey(opinion)) {
				int frequency = opinions.get(opinion);
				opinions.remove(opinion);
				++frequency;
				opinions.put(opinion,frequency);
			}
			else {
				int frequency = 1;
				opinions.put(opinion,frequency);
			}			
			otMap.put(aspect,opinions);
		}
		else {
			Map<String,Integer> opinions = new HashMap<String,Integer>();
			int frequency = 1;
			opinions.put(opinion,frequency);
			otMap.put(aspect,opinions);
		}
	}

	
	private static void lemmatizeText(String fileName) {
		BufferedReader br = null;
		PrintWriter writer = null;
		try {
			br = new BufferedReader(new FileReader(fileName)); 
			String line = "",text = "";		
			while ((line = br.readLine()) != null) {
			   text += line;
			}
			br.close();
			
			StanfordLemmatizer slem = new StanfordLemmatizer();			
			Vector<String> lemmatizedText = slem.lemmatize(text);
			writer = new PrintWriter(fileName, "UTF-8");
			for(String sentence : lemmatizedText) {
				writer.println(sentence);
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