package com.rat;
import java.util.Vector;
import java.util.List;
import java.util.Properties;

import java.io.PrintStream;
import java.io.OutputStream;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");        
        this.pipeline = new StanfordCoreNLP(props);
    }

    public Vector<String> lemmatize(String documentText) {		
        Vector<String> lemmatizedSentences = new Vector<String>();		       
        Annotation document = new Annotation(documentText);        
        this.pipeline.annotate(document);        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);		
        for(CoreMap sentence: sentences) {            
			String lemmas = "";
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                lemmas+=String.valueOf(token.get(LemmaAnnotation.class))+" ";
            }
			lemmatizedSentences.add(lemmas);
        }
        return lemmatizedSentences;
    }
}