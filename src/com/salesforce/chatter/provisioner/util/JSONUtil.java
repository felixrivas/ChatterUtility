//Copyright (c) 2012, Felix J. Rivas, Strategice Services, Salesforce.com Inc.
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//1.    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
//2.    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
//3.    Neither the name of the salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this
//      software without specific prior written permission. 
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
//SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
//LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.salesforce.chatter.provisioner.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {
	public static String retrieveStringFromJSON(String jsonBody, String field) {
		String fieldValue = null;
		JSONObject json = null;
		try {
			json = new JSONObject(jsonBody);
			fieldValue = json.getString(field);
		} catch (JSONException e) {
			//TODO add logging
			e.printStackTrace();
			System.err.println(jsonBody);
		}
		return fieldValue;
	}
	
	public static int retrieveIntFromJSON(String jsonBody, String field) {
		int fieldValue = 0;
		JSONObject json = null;
		try {
			json = new JSONObject(jsonBody);
			fieldValue = json.getInt(field);
		} catch (JSONException e) {
			//TODO add logging
			e.printStackTrace();
			System.err.println(jsonBody);
		}
		return fieldValue;
	}
	
	public static String retrieveStringFromJSONArray(String jsonBody, String jsonArrayName, int index, String field) {
		String fieldValue = "";
		JSONObject json, jsonRec; 
		JSONArray  records = null;
		try {
			json = new JSONObject(jsonBody);
			records = json.getJSONArray(jsonArrayName);	
			jsonRec = records.getJSONObject(index);
			fieldValue = jsonRec.getString(field) ;
		} catch (JSONException e) {
			//TODO add logging
			e.printStackTrace();
			System.err.println(jsonBody);
		}
		return fieldValue;
	}
}
