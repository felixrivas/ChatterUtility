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

package com.salesforce.chatter.provisioner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.salesforce.chatter.provisioner.rest.ChatterRESTWrapper;
import com.salesforce.chatter.provisioner.rest.ChatterRESTWrapper.OperationType;

public class ChatterProvisioner {

	private static final String USER_PROFILE_PICTURE_ARG = "UserProfilePicture" ;
	private static final String GROUP_PROFILE_PICTURE_ARG = "GroupPicture" ;
	private static final String GROUP_MEMBER_PROVISION_ARG = "GroupMember" ;
	private static final String GROUP_MEMBER_PROVISION_ARG_INSERT = "I" ;
	private static final String GROUP_MEMBER_PROVISION_ARG_DELETE= "D" ;
	
	
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if(args.length > 0) {
				ChatterProvisioner cp = new ChatterProvisioner() ;
				if (args[0].equalsIgnoreCase(USER_PROFILE_PICTURE_ARG)) {
					cp.executeUserFileUploadProvisioner();	
				} else if(args[0].equalsIgnoreCase(GROUP_PROFILE_PICTURE_ARG)) {
					cp.executeGroupFileUploadProvisioner();	
				} else if(args[0].equalsIgnoreCase(GROUP_MEMBER_PROVISION_ARG)) {
					if(args.length < 2) {
						//TODO showHelp(GROUP_MEMBER_PROVISION_ARG);
						System.out.println("Missing argument for: " + GROUP_MEMBER_PROVISION_ARG);
						System.out.println("\tExpected: ");
						System.out.println("\t\t" + GROUP_MEMBER_PROVISION_ARG + " " + GROUP_MEMBER_PROVISION_ARG_INSERT);
						System.out.println("\t\t" + GROUP_MEMBER_PROVISION_ARG + " " + GROUP_MEMBER_PROVISION_ARG_DELETE);
					} else {
						if(args[1].equalsIgnoreCase(GROUP_MEMBER_PROVISION_ARG_INSERT)) {
							cp.executeGroupMemberProvisioner(ChatterRESTWrapper.OperationType.INSERT);	
						} else if (args[1].equalsIgnoreCase(GROUP_MEMBER_PROVISION_ARG_DELETE)) {
							cp.executeGroupMemberProvisioner(ChatterRESTWrapper.OperationType.DELETE);
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void executeUserFileUploadProvisioner() throws Exception {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		List<File> files = getUserImageFiles();
		
		int batchSize = propMgr.getIntProperty("batch_size");
		int remainer = files.size() % batchSize ;
		int numberOfBatches = files.size() / batchSize ;
		if(remainer > 0) 
				numberOfBatches += 1;
		
		int fromIndex = 0;
		int toIndex = batchSize;
		List<File> fileSubList ;
		
		//Batch in groups of 1000
		for (int i=0; i<numberOfBatches; i++) {
			if (numberOfBatches > 1) {
				fileSubList =  files.subList(fromIndex, toIndex);
			} else  {
				fileSubList = files;
			}

			ChatterRESTWrapper.getInstance().processFileUpload(fileSubList);
			
			fromIndex = fromIndex + batchSize; 			
			int tempToIndex = toIndex + batchSize;
			if(files.size() > tempToIndex) {
				toIndex = tempToIndex;
			} else {
				toIndex = files.size() ;
			}
		}		
	}
	
	private void executeGroupFileUploadProvisioner() throws Exception {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		List<File> files = getGroupImageFiles();
		
		int batchSize = propMgr.getIntProperty("batch_size");
		int remainer = files.size() % batchSize ;
		int numberOfBatches = files.size() / batchSize ;
		if(remainer > 0) 
				numberOfBatches += 1;
		
		int fromIndex = 0;
		int toIndex = batchSize;
		List<File> fileSubList ;
		
		//Batch in groups of 1000
		for (int i=0; i<numberOfBatches; i++) {
			if (numberOfBatches > 1) {
				fileSubList =  files.subList(fromIndex, toIndex);
			} else  {
				fileSubList = files;
			}

			ChatterRESTWrapper.getInstance().processGroupFileUpload(fileSubList);
			
			fromIndex = fromIndex + batchSize; 
			int tempToIndex = toIndex + batchSize;
			if(files.size() > tempToIndex) {
				toIndex = tempToIndex;
			} else {
				toIndex = files.size() ;
			}
		}		
	}	

	private void executeGroupProvisioner() throws Exception {
		//TODO not yet supported by Chatter REST v23
	}
	
	private void executeGroupMemberProvisioner(OperationType operationType) throws Exception {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String csvGroupMemberFile ;
		switch (operationType) {
			case INSERT: {
				csvGroupMemberFile = propMgr.getStringProperty("group_member_provision_insert_csv");
				ChatterRESTWrapper.getInstance().processGroupMembership(operationType, csvGroupMemberFile);
				break ;
			}
			case DELETE: {
				csvGroupMemberFile = propMgr.getStringProperty("group_member_provision_delete_csv");
				ChatterRESTWrapper.getInstance().processGroupMembership(operationType, csvGroupMemberFile);
				break ;
				
			}
		}
	}
	
	private List<File> getUserImageFiles() {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String imagesPathString = propMgr.getStringProperty("user_images_path");
		File imagesPath = new File(imagesPathString) ;
		List<File> files = Arrays.asList(imagesPath.listFiles(new ImageFileFilter())) ;
		return files;
	}
	
	private List<File> getGroupImageFiles() {
		PropertyManager propMgr = PropertyManager.getInstance() ;
		String imagesPathString = propMgr.getStringProperty("group_images_path");
		File imagesPath = new File(imagesPathString) ;
		List<File> files = Arrays.asList(imagesPath.listFiles(new ImageFileFilter())) ;
		return files;
	}	
}
