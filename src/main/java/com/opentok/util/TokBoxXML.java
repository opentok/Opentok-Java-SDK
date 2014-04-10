package com.opentok.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokRequestException;

public class TokBoxXML {

	private Document xml;

	public TokBoxXML(String xmlString) throws OpenTokException {
		
		try {
			this.xml = TokBoxUtils.setupDocument(xmlString);
		} catch(Exception e) {
			throw new OpenTokRequestException(500, "Error response: message: " + e.getMessage());	
		}
	}


	public boolean hasElement(String elementName, String parentElement) {
		Node parentNode = TokBoxUtils.parseXML(parentElement, this.xml.getElementsByTagName(parentElement));
		if(parentNode == null) {
			return false;
		}
		Node searchNode = TokBoxUtils.parseXML(elementName, parentNode.getChildNodes());

		return null != searchNode;
	}
	
	public String getElementValue(String elementName, String parentElement) {
		Node parentNode = TokBoxUtils.parseXML(parentElement, this.xml.getElementsByTagName(parentElement));
		Node searchNode = TokBoxUtils.parseXML(elementName, parentNode.getChildNodes());

		return null == searchNode ? null : searchNode.getTextContent();
	}


}
