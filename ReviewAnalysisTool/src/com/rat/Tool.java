package com.rat;

import java.util.*;
import java.sql.*;
import java.io.*;

public class Tool {
	public static void main(String args[]) {
		Scanner sc = null;		
		try {		
			sc = new Scanner(System.in);
			int choice = 0;
			ArrayList<String> reviewFileNames = null;
			while(true) {
				System.out.println("-----------------Review Analysis Tool-----------------");
				System.out.println("Select a product review dataset to proceed:");												
				int i = 0;
				reviewFileNames = new ArrayList<String>();
				File folder = new File("resources/dataset/");
				for (File fileEntry : folder.listFiles()) {
					reviewFileNames.add(fileEntry.getName());					
					System.out.println("Press " + String.valueOf(++i) + " for " + fileEntry.getName());
				}				
				System.out.println("Enter your choice:");
				choice = sc.nextInt();
				--choice;
				if(choice<0 || choice>reviewFileNames.size()-1) {
					System.out.println("USER INPUT ERROR: Enter one of the given values.\n\n");
					continue;
				}
				break;
			}			
			System.out.println("Following dataset will be used in further processing: "+reviewFileNames.get(choice));			
			System.out.println("Data collection in progress.");
			TextReviewHandler.parseTextFile(reviewFileNames.get(choice));
			/*
			createTable();
			int reviewCount = loadReviewsInDatabase(TextReviewHandler.parseXML(GlobalVars.xmlFileName));		
			if(reviewCount < 1) {
				System.out.println("No instances found in XML file");
				System.exit(0);
			}			
			generateReviewTextFile();
			*/
			StanfordDependencyParser.parseReviewFile(GlobalVars.reviewFileName);
			
			choice = 0;
			while(true) {				
				System.out.println("\nSelect top N aspects for summary, enter value of N (less than 20): ");				
				choice = sc.hasNextLine()?sc.nextInt():0;				
				if(choice<1 || choice>20) {
					System.out.println("USER INPUT ERROR: Enter value of N between 1 and 20.");
					continue;
				}
				break;
			}
			
			sc.close();
			
			Filter.apply(choice);
			SWN3.calculateSentimentScore();
			Evaluation.compare();
		}
		catch(Exception ex) {
			ex.printStackTrace(System.err);				
			System.exit(0);
		}
		finally {
			try {			
				sc.close();
			}
			catch(Exception ex) {
				ex.printStackTrace(System.err);				
				System.exit(0);
			}
		}
	}
//	public static void createTable() {
//		/*
//		Checks for Review.db file in the root directory, creates one if it doesn't exist
//		PARAMETERS: NONE
//		RETURN VALUE: VOID		
//		*/
//		Connection c = null;
//		Statement stm = null;
//		try {
//			File dbFile = new File(GlobalVars.dbPath+GlobalVars.dbName);
//			dbFile.createNewFile();			
//			if(dbFile.exists()) {				
//				Class.forName("org.sqlite.JDBC");				
//				c = DriverManager.getConnection("jdbc:sqlite:" + GlobalVars.dbPath+GlobalVars.dbName);				
//				stm = c.createStatement();				
//				//Master consists of review text, aspect term, POS tag, and polarity
//				String sqlReviews = "CREATE TABLE IF NOT EXISTS " + GlobalVars.masterTableName +
//					   "(ID INT PRIMARY KEY     NOT NULL," +
//					   " TEXT           TEXT    NOT NULL, " + 
//					   " ASPECT            CHAR(50), " + 					   
//					   " POS        CHAR(10), " + 					   
//					   " POLARITY         INT)";					   
//				stm.executeUpdate(sqlReviews);				
//				System.out.println(GlobalVars.masterTableName + " table created in DB.");
//			}
//		}
//		catch(Exception ex) {
//			ex.printStackTrace(System.err);				
//			System.exit(0);
//		}
//		finally {
//			try {
//				
//				stm.close();				
//				c.close();				
//			}
//			catch(Exception ex) {
//				ex.printStackTrace(System.err);				
//				System.exit(0);
//			}
//		}
//	}
//	@SuppressWarnings("finally")
//	public static int loadReviewsInDatabase(ArrayList<?> reviews) {
//		/*
//		Checks if database is created, creates one if needed and loads Database with reviews from ArrayList
//		PARAMETERS: ArrayList with reviews from XML file
//		RETURN VALUE: Number of reviews loaded in database
//		*/
//		Connection c=null;
//		PreparedStatement pstm=null;
//		Statement stm = null;
//		int counter = 0;
//		File dbFile=null;
//		try {
//			dbFile = new File(GlobalVars.dbPath+GlobalVars.dbName);			
//			if(dbFile.exists()) {
//				Class.forName("org.sqlite.JDBC");				
//				c = DriverManager.getConnection("jdbc:sqlite:" + GlobalVars.dbPath+GlobalVars.dbName);				
//				String sqlDelete = "DELETE FROM "+ GlobalVars.masterTableName;	
//				stm = c.createStatement(); 
//				stm.executeUpdate(sqlDelete);
//				System.out.println("Loading reviews into "+GlobalVars.masterTableName+". This might take some time.");
//				for(int i = 0; i < reviews.size(); ++i ) {
//					Review review = (Review)reviews.get(i);					
//					String sqlInsert = "INSERT INTO " + GlobalVars.masterTableName + " (ID, TEXT, ASPECT,POS, POLARITY) values (?,?,?,?,?)";				
//					pstm = c.prepareStatement(sqlInsert);				
//					pstm.setInt(1,review.id);
//					pstm.setString(2,review.text);
//					pstm.setString(3,review.aspect);
//					pstm.setString(4,review.pos);
//					pstm.setInt(5,review.polarity);
//					pstm.executeUpdate();
//					++counter;
//					pstm.close();
//				}				
//				System.out.println("DB loaded with "+String.valueOf(counter) + " review(s)");
//			}
//			else {
//				createTable();
//				TextReviewHandler.parseTextFile(GlobalVars.reviewFileName);
//			}
//		}
//		catch(Exception ex) {
//			ex.printStackTrace(System.err);				
//			System.exit(0);
//		}
//		finally {
//			try {				
//				stm.close();
//				c.close();				
//			}	
//			catch(Exception ex) {
//				ex.printStackTrace(System.err);				
//				System.exit(0);
//			}
//			finally {
//				return counter;
//			}
//		}		
//	}	
//	public static void generateReviewTextFile() {
//		/*
//		Generate POS tagged review text file with dependency relations
//		PARAMETERS: NONE
//		RETURN VALUE: VOID
//		*/
//		File dbFile = null;
//		Connection c = null;
//		Statement stm = null;
//		ResultSet rs = null;
//		PrintWriter writer = null;
//		try {
//			System.out.println("Collecting reviews in text file for further steps.");
//			writer = new PrintWriter(GlobalVars.reviewFileName, "UTF-8");
//			dbFile = new File(GlobalVars.dbPath+GlobalVars.dbName);
//			if(dbFile.exists()) {
//				Class.forName("org.sqlite.JDBC");
//				c = DriverManager.getConnection("jdbc:sqlite:" + GlobalVars.dbPath + GlobalVars.dbName);	
//				stm = c.createStatement();
//				rs = stm.executeQuery("SELECT TEXT FROM " + GlobalVars.masterTableName + ";");
//				while ( rs.next() ) {
//					if(rs.getString("TEXT").length()<180) {
//						writer.println(rs.getString("TEXT"));
//					}					
//				}
//				System.out.println("Raw reviews are available in "+GlobalVars.reviewFileName+".");
//			}
//		}
//		catch(Exception ex) {
//			ex.printStackTrace(System.err);				
//			System.exit(0);
//		}
//		finally {
//			try {
//				rs.close();
//				stm.close();
//				c.close();
//				writer.close();
//			}
//			catch(Exception ex) {
//				ex.printStackTrace(System.err);				
//				System.exit(0);
//			}
//		}
//	}
}