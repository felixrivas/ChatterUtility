/**
 * 
 */
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

package com.salesforce.chatter.provisioner.rest;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import com.salesforce.chatter.provisioner.PropertyManager;
import com.salesforce.chatter.provisioner.util.JSONUtil;

/**
 * @author frivas
 *
 */
public class ChatterRESTOAuthTokenStore {
	
	private static int poolSize, roundRobinCounter, maxAPILimit;
	
	private static ChatterRestAccessToken currentChatterRestAccessToken = null;
	private static List<ChatterRestAccessToken> chatterRESTAccessTokenList = null;
	        
	        
	private ChatterRESTOAuthTokenStore() {

	}
	
	private static void initialize() throws ClientProtocolException, IOException {
		if(chatterRESTAccessTokenList == null) {
			PropertyManager propMgr = PropertyManager.getInstance() ;
			poolSize = propMgr.getIntProperty("oauth_users_pool_size") ;	
			maxAPILimit = propMgr.getIntProperty("rest_api_limit") ;
			List<ChatterRestAccessToken> chatterRestAccessTokenListTemp = new ArrayList<ChatterRestAccessToken>();
			
			ChatterRestAccessToken chatterRestAccessToken;
			String clientId, clientSecret, userId, password, accessToken;
			for (int i=0; i<poolSize; i++) { 
				clientId = propMgr.getStringProperty("oauth_client_id_" + i);
				clientSecret = propMgr.getStringProperty("oauth_client_secret_" + i);
				userId = propMgr.getStringProperty("salesforce_user_id_" + i);
				password = propMgr.getStringProperty("salesforce_pswd_" + i);
				chatterRestAccessToken = new ChatterRestAccessToken();
				accessToken = authenticate(clientId, clientSecret, userId, password);
				chatterRestAccessToken.setAccessToken(accessToken);
				chatterRestAccessToken.setoAuthUsername(userId);
				chatterRestAccessTokenListTemp.add(chatterRestAccessToken) ;
			}
			
			chatterRESTAccessTokenList = Collections.unmodifiableList(chatterRestAccessTokenListTemp);
			roundRobinCounter = -1;
		}
	}
	
	private static ChatterRestAccessToken getChatterRestAccessToken() throws ClientProtocolException, IOException {
		initialize() ;
		if(currentChatterRestAccessToken == null || 
				(currentChatterRestAccessToken != null && currentChatterRestAccessToken.getNumberOfRESTCalls() == maxAPILimit)) {
			roundRobinCounter = (roundRobinCounter + 1) % poolSize; // % is the remainder operator
			currentChatterRestAccessToken = chatterRESTAccessTokenList.get(roundRobinCounter);
		}
        return currentChatterRestAccessToken;
	}
	
	public static String getAccessToken() throws ClientProtocolException, IOException {
		ChatterRestAccessToken chatterRestAccessToken = getChatterRestAccessToken();
		return chatterRestAccessToken.getAccessToken();
	}

	
	private static String authenticate(String clientId, String clientSecret, String userId, String password) throws ClientProtocolException, IOException {
		PropertyManager propMgr = PropertyManager.getInstance() ;

		String encoding = propMgr.getStringProperty("encoding");
		
		HttpClient httpclient = new DefaultHttpClient();
		String AuthRESTURL = getChatterOAuthRESTURL(ChatterRESTUrl.OAUTH) ;
		
		HttpPost post = new HttpPost(AuthRESTURL);
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("grant_type", "password"));
		formparams.add(new BasicNameValuePair("response_type", "code"));
		formparams.add(new BasicNameValuePair("client_id", clientId));
		formparams.add(new BasicNameValuePair("client_secret", clientSecret));
		formparams.add(new BasicNameValuePair("username", userId));
		formparams.add(new BasicNameValuePair("password", password));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, encoding);
		post.setEntity(entity);
		
		HttpResponse response = httpclient.execute(post);
		HttpEntity resEntity = response.getEntity();
		String responseBody = EntityUtils.toString(resEntity);
		String accessToken = JSONUtil.retrieveStringFromJSON(responseBody, "access_token");		
		return accessToken;
	}
	
	private static String getChatterOAuthRESTURL(ChatterRESTUrl chatterRESTUrl) {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(propMgr.getStringProperty("protocol")).append("://")
		  .append(propMgr.getStringProperty("salesforce_instance")).append(".")
		  .append(propMgr.getStringProperty("saleforce_domain"))
		  .append(propMgr.getStringProperty("oauth_rest_uri")) ;
		return sb.toString() ;
	}
	
}
