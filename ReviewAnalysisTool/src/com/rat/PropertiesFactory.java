package com.rat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

class PropertiesFactory {
	final static private String PROPERTIESFILENAME = "config.properties";
//	public void setProperty(String propertyName, String propertyValue) {
//		PropertiesConfiguration prop = null;
//		try {
//			prop = new PropertiesConfiguration(getClass().getClassLoader().getResource("./"+PROPERTIESFILENAME).toString());
//	        prop.setProperty(propertyName, propertyValue);
//	        prop.save();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
		

//		Properties prop = new Properties();
//		OutputStream output = null;		
//		try {			
//			System.out.println(PropertiesFactory.getPropertyValue(propertyName));
//			//File file = new File(PROPERTIESFILENAME);
//			//System.out.println(new File(".").getAbsolutePath());
//			//file.setWritable(true);
//			output = new FileOutputStream(new File(".").getAbsolutePath()+PROPERTIESFILENAME);	
//	        prop.setProperty(propertyName, propertyValue);
//	        prop.store(output, null);
//	        System.out.println(PropertiesFactory.getPropertyValue(propertyName));
//		}
//		catch(IOException e) {
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//		}
//		finally {
//			if(output != null) {
//				try {
//					output.close();
//				}
//				catch(IOException e) {
//					e.printStackTrace();				
//				}
//			}
//		}
//	}

	public static String getPropertyValue(String propertyName) {
		String propertyValue = "";
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = PropertiesFactory.class.getClassLoader().getResourceAsStream(PROPERTIESFILENAME);
			if (input == null)
				throw (new IOException());
			prop.load(input);
			Enumeration<?> enumProp = prop.propertyNames();
			while (enumProp.hasMoreElements()) {
				String key = (String) enumProp.nextElement();
				String value = prop.getProperty(key);
				if (key.equalsIgnoreCase(propertyName))
					propertyValue = value;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return propertyValue;
	}
}