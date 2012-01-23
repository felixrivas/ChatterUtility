/**
 * 
 */
//Copyright (c) 2009, 2012, Felix J. Rivas, Strategice Services, Salesforce.com Inc.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.salesforce.chatter.provisioner.PropertyManager;
import com.salesforce.chatter.provisioner.beans.ChatterUser;
import com.salesforce.chatter.provisioner.util.CSVUtil;
import com.salesforce.chatter.provisioner.util.JSONUtil;

/**
 * @author frivas
 *
 */
public class ChatterRESTWrapper {
	private static ChatterRESTWrapper chatterRESTWrapper = null;
	public static enum OperationType {INSERT, DELETE};
	private Map<String, ChatterUser> chatterUserLookup = new HashMap<String, ChatterUser>();
	
	private ChatterRESTWrapper() {

	}
	
	public static ChatterRESTWrapper getInstance() {
		if (chatterRESTWrapper == null) {
			chatterRESTWrapper = new ChatterRESTWrapper();
		}
		return chatterRESTWrapper ;
	}
	
	public void processFileUpload (List<File> files) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String userIDReplaceToken = propMgr.getStringProperty("user_id_placeholder");
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost ;
		MultipartEntity reqEntity ;
		String chattFileUploadRESTURL;
		
		Map<String, ChatterUser> userIdFileMap = retrieveUserIdFromFilenames(files) ;
		FileBody bin ;
		String userId ;

		for (ChatterUser chatterUserFileUpload : userIdFileMap.values()) {//TODO add null check
			userId = chatterUserFileUpload.getUserId() ;
			if (!StringUtils.isEmpty(userId)) {
				chattFileUploadRESTURL = StringUtils.replace(getChatterRESTURL(ChatterRESTUrl.USER_PHOTO), userIDReplaceToken, chatterUserFileUpload.getUserId()) ;
				httppost = new HttpPost(chattFileUploadRESTURL);
				reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				bin = new FileBody(chatterUserFileUpload.getFile());
				reqEntity.addPart("fileUpload", bin);
				httppost.setEntity(reqEntity);
				httppost.setHeader("Authorization", "OAuth " + ChatterRESTOAuthTokenStore.getAccessToken());
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity resEntity = response.getEntity();
	
				if (resEntity != null) {
					String page = EntityUtils.toString(resEntity);
					//TODO Add logging
					System.out.println("PAGE :" + page);
				}
			} else {
				//TODO Add logging
				System.out.println("User Not Found:" + chatterUserFileUpload.getUserName());
			}
		}		
	}
	
	public void processGroupFileUpload (List<File> files) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String groupIDReplaceToken = propMgr.getStringProperty("group_id_placeholder");
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost ;
		MultipartEntity reqEntity ;
		String chattFileUploadRESTURL;
		
		Map<String, ChatterUser> groupIdFileMap = retrieveGroupIdFromFilenames(files) ;
		FileBody bin ;
		String groupId ;

		for (ChatterUser chatterUserFileUpload : groupIdFileMap.values()) {//TODO add null check
			groupId = chatterUserFileUpload.getGroupId() ;
			if (!StringUtils.isEmpty(groupId)) {
				chattFileUploadRESTURL = StringUtils.replace(getChatterRESTURL(ChatterRESTUrl.GROUP_PHOTO), groupIDReplaceToken, chatterUserFileUpload.getGroupId()) ;
				httppost = new HttpPost(chattFileUploadRESTURL);
				reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				bin = new FileBody(chatterUserFileUpload.getFile());
				reqEntity.addPart("fileUpload", bin);
				httppost.setEntity(reqEntity);
				httppost.setHeader("Authorization", "OAuth " + ChatterRESTOAuthTokenStore.getAccessToken());
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity resEntity = response.getEntity();
	
				if (resEntity != null) {
					String page = EntityUtils.toString(resEntity);
					//TODO Add logging
					System.out.println("PAGE :" + page);
				}
			} else {
				//TODO Add logging
				System.out.println("Group Not Found:" + chatterUserFileUpload.getGroupName());
			}
		}		
	}
	
	private Map<String, ChatterUser> retrieveUserIdFromFilenames(List<File> fileList) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		StringBuilder query = new StringBuilder();
		query.append(propMgr.getStringProperty("user_id_query"));
		
		Map<String, ChatterUser> userIdFileMap = new HashMap<String, ChatterUser>();
		if(fileList != null && fileList.size() > 0) {
			query.append("(");
			ChatterUser userFileUpload ;
			for(int i = 0; i < fileList.size(); i++ ) {
				File file = fileList.get(i);
				userFileUpload = new ChatterUser();
				userFileUpload.setUserName(FilenameUtils.removeExtension(file.getName())) ;
				
				userFileUpload.setFile(file) ;
				userIdFileMap.put(userFileUpload.getUserName(),userFileUpload) ;
				query.append("'" + FilenameUtils.removeExtension(file.getName()) + "'");
				if(i != (fileList.size()-1)) {
					query.append(", ");
				}
			}
			query.append(")");

			String restJsonResponse = performRESTQuery(query.toString());
			if(restJsonResponse != null) {
				processUsernameLookup(restJsonResponse, userIdFileMap);
			} else {
				//TODO Add logging
				System.out.println("performRESTUserFileUploadQuery method returned null string");
			}
		}
		return userIdFileMap;
	}
	
	private Map<String, ChatterUser> retrieveGroupIdFromFilenames(List<File> fileList) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		StringBuilder query = new StringBuilder();
		query.append(propMgr.getStringProperty("group_id_query"));
		
		Map<String, ChatterUser> groupIdFileMap = new HashMap<String, ChatterUser>();
		if(fileList != null && fileList.size() > 0) {
			query.append("(");
			ChatterUser groupFileUpload ;
			for(int i = 0; i < fileList.size(); i++ ) {
				File file = fileList.get(i);
				groupFileUpload = new ChatterUser();
				groupFileUpload.setGroupName(FilenameUtils.removeExtension(file.getName())) ;
				
				groupFileUpload.setFile(file) ;
				groupIdFileMap.put(groupFileUpload.getGroupName(),groupFileUpload) ;
				query.append("'" + FilenameUtils.removeExtension(file.getName()) + "'");
				if(i != (fileList.size()-1)) {
					query.append(", ");
				}
			}
			query.append(")");

			String restJsonResponse = performRESTQuery(query.toString());
			if(restJsonResponse != null) {
				processGroupNameLookupForFileUpload(restJsonResponse, groupIdFileMap);
			} else {
				//TODO Add logging
				System.out.println("performRESTUserFileUploadQuery method returned null string");
			}
		}
		return groupIdFileMap;
	}
	
	public void processGroupMembership (OperationType operationType, String csvFilePath) throws ClientProtocolException, IOException, URISyntaxException {
		Map<String, List<ChatterUser>> chatterGroupMembersMap = retrieveIdsFromCSV(operationType, csvFilePath) ;
		
		switch (operationType) {
			case INSERT: {
				processGroupMembershipInserts(chatterGroupMembersMap);
				break;
			}
			case DELETE: {
				processGroupMembershipDeletes();
				break;
			}
			
		}
	}
	
	private void processGroupMembershipInserts (Map<String, List<ChatterUser>> chatterGroupMembersMap) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String groupIDReplaceToken = propMgr.getStringProperty("group_id_placeholder");
		String encoding = propMgr.getStringProperty("encoding");
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost ;
		String chatterRESTURL;
		
		String userId ;
		//TODO add null check
		for (List<ChatterUser> chatterUsers : chatterGroupMembersMap.values()) {
			for(ChatterUser chatterUser : chatterUsers) {
				userId = chatterUser.getUserId() ;
				List<NameValuePair> formparams;
				UrlEncodedFormEntity entity ;
				if (!StringUtils.isEmpty(userId)) {
					chatterRESTURL = StringUtils.replace(getChatterRESTURL(ChatterRESTUrl.USER_GROUP_MEMBER_INSERT), groupIDReplaceToken, chatterUser.getGroupId()) ;
					
					httppost = new HttpPost(chatterRESTURL);
					formparams = new ArrayList<NameValuePair>();
					formparams.add(new BasicNameValuePair("userId", userId));
					entity = new UrlEncodedFormEntity(formparams, encoding);
					httppost.setEntity(entity);
					httppost.setHeader("Authorization", "OAuth " + ChatterRESTOAuthTokenStore.getAccessToken());
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity resEntity = response.getEntity();
		
					if (resEntity != null) {
						String page = EntityUtils.toString(resEntity);
						//TODO Add logging
						System.out.println("PAGE :" + page);
					}
				} else {
					//TODO Add logging
					System.out.println("User Not Found:" + chatterUser.getUserName());
				}
			}
		}		
	}
	
	public void processGroupMembershipDeletes () throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String groupMembershipIDReplaceToken = propMgr.getStringProperty("group_membership_id_placeholder");
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete httpdelete ;
		String chatterRESTURL;
		
		String userId ;
		//TODO add null check
		for(ChatterUser chatterUser : chatterUserLookup.values()) {
			userId = chatterUser.getUserId() ;
			for(String membershipId : chatterUser.getMemberships()) {
				if (!StringUtils.isEmpty(userId)) {
					chatterRESTURL = StringUtils.replace(getChatterRESTURL(ChatterRESTUrl.USER_GROUP_MEMBER_DELETE), groupMembershipIDReplaceToken, membershipId) ;
					httpdelete = new HttpDelete(chatterRESTURL);
					httpdelete.setHeader("Authorization", "OAuth " + ChatterRESTOAuthTokenStore.getAccessToken());
					HttpResponse response = httpclient.execute(httpdelete);
					HttpEntity resEntity = response.getEntity();
		
					if (resEntity != null) {
						String page = EntityUtils.toString(resEntity);
						//TODO Add logging
						System.out.println("PAGE :" + page);
					}
				} else {
					//TODO Add logging
					System.out.println("User Not Found:" + chatterUser.getUserName());
				}
			}
		}
	}	
	
	private Map<String, List<ChatterUser>> retrieveIdsFromCSV(OperationType operationType, String csvFilePath) {
		Map<String, List<ChatterUser>> chatterGroupMembersMap = CSVUtil.getChatterGroupMembersFromCSV(csvFilePath);
		try {
			retrieveGroupIdsFromMapKeys(chatterGroupMembersMap);
			retrieveUserIdsFromList(chatterGroupMembersMap.values()); 
			if (operationType == OperationType.DELETE) {
				retrieveMembershipsFromList(chatterGroupMembersMap.values());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO get UserIds
		return chatterGroupMembersMap;
	}
	
	private void retrieveGroupIdsFromMapKeys(Map<String, List<ChatterUser>> chatterGroupMembersMap)
			throws ClientProtocolException,	IOException, URISyntaxException{
		PropertyManager propMgr = PropertyManager.getInstance() ;
		StringBuilder query = new StringBuilder();
		query.append(propMgr.getStringProperty("group_id_query"));
		
		int mapKeySize = chatterGroupMembersMap.keySet().size();
		if(chatterGroupMembersMap != null && mapKeySize > 0) {
			query.append("(");
			int i=0;
			for(String groupName : chatterGroupMembersMap.keySet() ) {
				query.append("'" + groupName + "'");
				if(i != (mapKeySize-1)) {
					query.append(", ");
				}
				i++;
			}
			query.append(")");
			String restJsonResponse = performRESTQuery(query.toString());
			
			if(restJsonResponse != null) {
				processGroupNameLookup(restJsonResponse, chatterGroupMembersMap);
			} else {
				//TODO Add logging
				System.out.println("performRESTUserFileUploadQuery method returned null string");
			}
		}
	}
	
	private void retrieveUserIdsFromList(Collection<List<ChatterUser>> collection) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		StringBuilder query ;
		Map<String, ChatterUser> groupMemberMap = new HashMap<String, ChatterUser>();
		if(collection != null && collection.size() > 0) {
			for(List<ChatterUser> chatterUsers : collection) {
				int batchSize = propMgr.getIntProperty("batch_size");
				int remainer = chatterUsers.size() % batchSize ;
				int numberOfBatches = chatterUsers.size() / batchSize ;
				if(remainer > 0) 
						numberOfBatches += 1;
				
				int fromIndex = 0;
				int toIndex = batchSize - 1;
				List<ChatterUser> subList ;
				
				//Batch in groups of 1000
				for (int i=0; i<numberOfBatches; i++) {
					if (numberOfBatches > 1) {
						subList =  chatterUsers.subList(fromIndex, toIndex);
					} else  {
						subList = chatterUsers;
					}
				
					query = new StringBuilder();
					query.append(propMgr.getStringProperty("user_id_query"));
					query.append("(");
					ChatterUser chatterUser;
					boolean userAddedToQuery = false;
					for(int j = 0; j < subList.size(); j++) {
						chatterUser = subList.get(j);
						if(chatterUserLookup.containsKey(chatterUser.getUserName())) {
							chatterUser.setUserId(chatterUserLookup.get(chatterUser.getUserName()).getUserId() );
							groupMemberMap.put(chatterUser.getUserName(), chatterUser);
						} else {
							if(j > 0 && userAddedToQuery) {
								query.append(", ");
							}
							query.append("'" + chatterUser.getUserName() + "'");
							groupMemberMap.put(chatterUser.getUserName(), chatterUser);
							if(!userAddedToQuery) {
								userAddedToQuery = true;
							}
						}
					}
					query.append(")");
					if(userAddedToQuery) {
						String restJsonResponse = performRESTQuery(query.toString());
						if(restJsonResponse != null) {
							processUsernameLookup(restJsonResponse, groupMemberMap);
						} else {
							//TODO Add logging
							System.out.println("performRESTUserFileUploadQuery method returned null string");
						}
					}					
					
					fromIndex = fromIndex + batchSize; 
					int tempToIndex = toIndex + batchSize;
					if(chatterUsers.size() > tempToIndex) {
						toIndex = tempToIndex;
					} else {
						toIndex = chatterUsers.size() ;
					}
						
				}	
					
				
			}
		}
	}
	
	private void retrieveMembershipsFromList(Collection<List<ChatterUser>> collection) throws ClientProtocolException, IOException, URISyntaxException {
		chatterUserLookup.clear();//Clear lookup map as it will be reference in deletes
		PropertyManager propMgr = PropertyManager.getInstance() ;
		
		StringBuilder query = new StringBuilder();
		query.append(propMgr.getStringProperty("group_member_id_query"));
		
		String userIDReplaceToken = propMgr.getStringProperty("user_id_placeholder");
		String groupIDReplaceToken = propMgr.getStringProperty("group_id_placeholder");
		
		if(collection != null && collection.size() > 0) {
			for(List<ChatterUser> chatterUsers : collection) {
				ChatterUser chatterUser;
				String queryS, userId, groupId ;
				for(int i = 0; i < chatterUsers.size(); i++) {
					chatterUser = chatterUsers.get(i);
					userId = chatterUser.getUserId();
					groupId = 		chatterUser.getGroupId();
					if(StringUtils.isNotEmpty(userId)  && StringUtils.isNotEmpty(groupId) ) {   
						queryS = StringUtils.replace(query.toString(), userIDReplaceToken, chatterUser.getUserId());				
						queryS = StringUtils.replace(queryS, groupIDReplaceToken, chatterUser.getGroupId());
						if(!chatterUserLookup.containsKey(chatterUser.getUserId())) {
							chatterUserLookup.put(chatterUser.getUserId(), chatterUser);
						}
						String restJsonResponse = performRESTQuery(queryS);
						if(restJsonResponse != null) {
							processGroupMembershipLookup(restJsonResponse, chatterUserLookup);
						} else {
							//TODO Add logging
							System.out.println("performRESTUserFileUploadQuery method returned null string");
						}
					}
				}
			}
		}
	}	

	private String performRESTQuery(String query) throws ClientProtocolException, IOException, URISyntaxException {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String protocol = propMgr.getStringProperty("protocol");
		String salesforce_full_domain = propMgr.getStringProperty("salesforce_full_domain");
		String userRESTURI = propMgr.getStringProperty("user_rest_uri");
		String encoding = propMgr.getStringProperty("encoding");
		
		HttpClient httpClient = new DefaultHttpClient();

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("q", query.toString()));

		URI uri = URIUtils.createURI(protocol, salesforce_full_domain, -1, userRESTURI, 
				URLEncodedUtils.format(qparams, encoding), null);
 
		HttpGet get = new HttpGet(uri);
		get.setHeader("Authorization", "OAuth " + ChatterRESTOAuthTokenStore.getAccessToken());

		HttpResponse response = httpClient.execute(get);
		HttpEntity resEntity = response.getEntity();

		String jsonData = null;
		if (resEntity != null) {
			jsonData = EntityUtils.toString(resEntity);
			//TODO Add logging
			System.out.println("CHATTER RESPONSE :" + jsonData);
		}
		return jsonData;
	}
	
	private void processUsernameLookup(String jsonData, Map<String, ChatterUser> userIdFileMap) {
		int totalSize = JSONUtil.retrieveIntFromJSON(jsonData, "totalSize");
		if (totalSize > 0) {
			ChatterUser chatterUser;
			for (int i = 0; i < totalSize; ++i) {
				String userId = JSONUtil.retrieveStringFromJSONArray(jsonData,
						"records", i, "Id");
				String userName = JSONUtil.retrieveStringFromJSONArray(
						jsonData, "records", i, "Username");
				chatterUser = userIdFileMap.get(userName);
				chatterUser.setUserId(userId);
				chatterUserLookup.put(userName, chatterUser);
			}
		} else {
			// TODO add logging
			System.out.println("No records founded.");
		}
	}	
	
	private void processGroupNameLookupForFileUpload(String jsonData, Map<String, ChatterUser> userIdFileMap) {
		int totalSize = JSONUtil.retrieveIntFromJSON(jsonData, "totalSize");
		if (totalSize > 0) {
			ChatterUser chatterUser;
			for (int i = 0; i < totalSize; ++i) {
				String groupId = JSONUtil.retrieveStringFromJSONArray(jsonData,
						"records", i, "Id");
				String groupName = JSONUtil.retrieveStringFromJSONArray(
						jsonData, "records", i, "Name");
				chatterUser = userIdFileMap.get(groupName);
				chatterUser.setGroupId(groupId);
			}
		} else {
			// TODO add logging
			System.out.println("No records founded.");
		}
	}	
	
	private void processGroupNameLookup(String jsonData, Map<String, List<ChatterUser>> chatterGroupMembersMap) {
		System.out.println("In processGroupNameLookup");
		int totalSize = JSONUtil.retrieveIntFromJSON(jsonData, "totalSize");
		if (totalSize > 0) {
			List<ChatterUser> ChatterUserList;
			String groupId, groupName;
			for (int i = 0; i < totalSize; ++i) {
				groupId = JSONUtil.retrieveStringFromJSONArray(jsonData,
						"records", i, "Id");
				groupName = JSONUtil.retrieveStringFromJSONArray(
						jsonData, "records", i, "Name");
				ChatterUserList = chatterGroupMembersMap.get(groupName);
				for(ChatterUser ChatterUser : ChatterUserList) {
					ChatterUser.setGroupId(groupId);
				}
			}
		} else {
			// TODO add logging
			System.out.println("No records found.");
		}
	}
	
	private void processGroupMembershipLookup(String jsonData, Map<String, ChatterUser> userIdFileMap) {
		int totalSize = JSONUtil.retrieveIntFromJSON(jsonData, "totalSize");
		if (totalSize > 0) {
			ChatterUser chatterUser;
			for (int i = 0; i < totalSize; ++i) {
				String memberShipId = JSONUtil.retrieveStringFromJSONArray(jsonData,
						"records", i, "Id");
				String memberId = JSONUtil.retrieveStringFromJSONArray(jsonData,
						"records", i, "MemberId");				
				chatterUser = userIdFileMap.get(memberId);
				chatterUser.getMemberships().add(memberShipId);
			}
		} else {
			// TODO add logging
			System.out.println("No records founded.");
		}		
	}
	
	private String getChatterRESTURL(ChatterRESTUrl chatterRESTUrl) {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(propMgr.getStringProperty("protocol")).append("://")
		  .append(propMgr.getStringProperty("salesforce_instance")).append(".")
		  .append(propMgr.getStringProperty("saleforce_domain"));
		
		switch (chatterRESTUrl) {
			case USER_PHOTO : {
				sb.append(propMgr.getStringProperty("user_rest_photo_uri")) ;
				break;
			}
			case USER_GROUP_MEMBER_INSERT : {
				sb.append(propMgr.getStringProperty("user_group_member_uri")) ;
				break;				
			}
			
			case USER_GROUP_MEMBER_DELETE : {
				sb.append(propMgr.getStringProperty("user_group_membership_uri")) ;
				break;				
			}			
			case GROUP_PHOTO : {
				sb.append(propMgr.getStringProperty("group_rest_photo_uri")) ;
				break;
			}
			default: {
				//TODO Add logging
				System.out.println("Operation not supported") ;
				throw new UnsupportedOperationException ("Operation not supported.") ;
			}
		}
		return sb.toString() ;
	}
}
