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

package com.salesforce.chatter.provisioner.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;
import com.salesforce.chatter.provisioner.beans.ChatterGroupBean;
import com.salesforce.chatter.provisioner.beans.ChatterUser;

/**
 * @author frivas
 *
 */
public class CSVUtil {

	public static List<ChatterGroupBean> getChatterGroupsFromCSV(String csvFilePath) {
		List<ChatterGroupBean> chatterGroups = new ArrayList<ChatterGroupBean>();
		CsvReader groups = null;
		try {
			groups = new CsvReader(csvFilePath);
			groups.readHeaders();
			ChatterGroupBean chatterGroupBean;
			while (groups.readRecord()) {
				chatterGroupBean = new ChatterGroupBean();
				chatterGroupBean.setGroupName(groups.get("Name"));
				chatterGroupBean.setCollaborationType(groups.get("CollaborationType"));
				chatterGroupBean.setOwnerEmail(groups.get("OwnerEmail"));
				chatterGroups.add(chatterGroupBean);
			}
		} catch (FileNotFoundException fne) {
			//TODO Add logging
			fne.printStackTrace();
		} catch (IOException ioe) {
			//TODO Add logging
			ioe.printStackTrace();
		} finally {
			if(groups != null) {
				groups.close();
			}
		}
		return chatterGroups;
	}
	
	public static Map<String, List<ChatterUser>> getChatterGroupMembersFromCSV(String csvFilePath) {
		Map<String, List<ChatterUser>> chatterGroupMembersMap = new HashMap<String, List<ChatterUser>>();
		CsvReader groupMembers = null;
		try {
			groupMembers = new CsvReader(csvFilePath);
			groupMembers.readHeaders();
			ChatterUser ChatterUser;
			List<ChatterUser> groupMemberList;
			String groupName;
			while (groupMembers.readRecord()) {
				ChatterUser = new ChatterUser();
				groupName = groupMembers.get("GroupName");
				ChatterUser.setGroupName(groupName);
				ChatterUser.setUserName(groupMembers.get("Email"));
				if(chatterGroupMembersMap.containsKey(groupName)) {
					chatterGroupMembersMap.get(groupName).add(ChatterUser);
				} else {
					groupMemberList = new ArrayList<ChatterUser>();
					groupMemberList.add(ChatterUser);
					chatterGroupMembersMap.put(groupName, groupMemberList);
				}
			}
		} catch (FileNotFoundException fne) {
			//TODO Add logging
			fne.printStackTrace();
		} catch (IOException ioe) {
			//TODO Add logging
			ioe.printStackTrace();
		} finally {
			if(groupMembers != null) {
				groupMembers.close();
			}
		}
		return chatterGroupMembersMap;
	}
}
