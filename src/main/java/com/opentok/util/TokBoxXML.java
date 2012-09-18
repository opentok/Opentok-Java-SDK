package com.opentok.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.opentok.exception.OpenTokException;
import com.opentok.util.TokBoxUtils;

public class TokBoxXML {

	private Document xml;

	public TokBoxXML(String xmlString) throws OpenTokException{
		
		try {
			this.xml = TokBoxUtils.setupDocument(xmlString);
		} catch(IOException ioe) {
			throw new OpenTokException(ioe.toString());	
		} catch(ParserConfigurationException pce) {
			throw new OpenTokException(pce.toString());
		} catch(SAXException saxe) {
			throw new OpenTokException(saxe.toString());
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
