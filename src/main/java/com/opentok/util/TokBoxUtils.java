package com.opentok.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.opentok.exception.OpenTokException;

public class TokBoxUtils {
	public static Node parseXML(String matchToken, NodeList nodelist) {
		Node token = null;
		int index = 0;
		while((token = nodelist.item(index)) != null) {
			if(token.getNodeName().equals(matchToken)) {
				break;
			}
			
			index++;
		}	
		
		return token;
	}

	public static Document setupDocument(String xmlResponse) throws ParserConfigurationException, SAXException, IOException, OpenTokException {
		if(null == xmlResponse) {
			throw new OpenTokException("There was an error in retrieving the response. Please make sure that you are pointing to the correct server");
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document;
		
		builder = dbFactory.newDocumentBuilder();
		document = builder.parse(new InputSource(new StringReader(xmlResponse)));
		Node errorNodes = TokBoxUtils.parseXML("error", document.getElementsByTagName("error"));

		if(null != errorNodes) {
			throw new OpenTokException(xmlResponse);
		}

		return document;
	}
}
