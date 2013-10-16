/*!
* OpenTok Java Library
* http://www.tokbox.com/
*
* Copyright 2010, TokBox, Inc.
*
*/
package com.opentok.api;
import java.util.*;
import java.net.*;
import java.io.*;


class TokBoxNetConnection {
	
	public String request(String reqString, Map<String, String> paramList, Map<String, String> headers){
		
		//Should we even have a separate IOHelper class or simply make this class a singleton instead?I don't see why not.
		IOHelper io = IOHelper.getHelperInstance();
		return io.requestHelper(reqString, paramList, headers);
		
	}
	
}

