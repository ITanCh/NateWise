package com.nata.wise.state;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XMLParser {

	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	
	public XMLParser(){
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.err.println("error: cannot create DocumentBuilder!");
		}
	}
	
	/**
	 * parser xml file ,get node list
	 * @param path
	 */
	public NodeList startParser(File xmlFile) {
		if (xmlFile==null||!xmlFile.exists()) {
			System.err.println("error: xml file not exists!");
			return null;
		}

		try {
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("node");
			return nList;
//			for(int i=0;i<nList.getLength();i++){
//				Node node=nList.item(i);
//				System.out.println("\nCurrent Element :" + node.getNodeName());
//				if (node.getNodeType() == Node.ELEMENT_NODE) {
//					 
//					Element eElement = (Element) node;
//		 
//					System.out.println("resource-id : " + eElement.getAttribute("resource-id"));
//					System.out.println("class : " + eElement.getAttribute("class"));
//					System.out.println("clickable : " + eElement.getAttribute("clickable"));
//					System.out.println("long-clickable : " + eElement.getAttribute("long-clickable"));
//					System.out.println("bounds : " + eElement.getAttribute("bounds"));
//				}
//			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println("error: parser xml!");
		}
		return null;
	}

}
