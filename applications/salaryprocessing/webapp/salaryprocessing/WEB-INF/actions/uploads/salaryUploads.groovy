/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.string.*;
import org.ofbiz.product.image.ScaleImage;

context.nowTimestampString = UtilDateTime.nowTimestamp().toString();

// make the image file formats
//salaryFilenameFormat = UtilProperties.getPropertyValue('salaries', 'salary.filename.format');
//salaryServerPath = UtilProperties.getPropertyValue('salaries', 'salaries.server.path');
def folder = new File("/F:/projects/vergeofbiz/vergesacco/thesalary")

if (!folder.exists()){
	salaryServerPath = "/home/online/salaries";
} else{
	salaryServerPath = "/F:/projects/vergeofbiz/vergesacco/thesalary";
}


//linussalaryServerPath = "/home/online/salaries";

//salaryServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("salaries", "salaryfile.server.path"), context);
//salaryServerPath = "";
//FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("member", "image.server.path"), context);
//context.salaryServerPath = salaryServerPath
tryEntity = true;
if (request.getAttribute("_ERROR_MESSAGE_")) {
	tryEntity = false;
}
//if (!member) {
//	tryEntity = false;
//}

salaryMonthYearId = parameters.salaryMonthYearId;

println "######### The Salary Month ID ONE  is "+salaryMonthYearId;

if ("true".equalsIgnoreCase((String) request.getParameter("tryEntity"))) {
	tryEntity = true;
}
context.tryEntity = tryEntity;

// UPLOADING STUFF
forLock = new Object();
contentType = null;


String fileType = request.getParameter("upload_file_type");
if (fileType) {
	println "######### The Salary Month ID TWO - for upload clicked is "+salaryMonthYearId;
	//fileLocation = filenameExpander.expandString([location : 'members', id : partyId, type : fileType]);
	//fileLocation = filenameExpander.expandString([location : 'members', id : partyId, type : fileNameToSave]);


	//defaultFileName = filenameToUse + "_temp";
	//defaultFileName = 'test';
	uploadObject = new HttpRequestFileUpload();
	//uploadObject.setOverrideFilename(defaultFileName);
	//uploadObject.setSavePath(imageServerPath + "/" + filePathPrefix);
	//println " The file Name FFFFFFFFNNNNNNNNNNN "+uploadObject.getFilename();
	//uploadObject.setSavePath(salaryServerPath + "/"+uploadObject.getFilename());
	uploadObject.setSavePath(salaryServerPath + "/");
	
	uploadObject.doUpload(request);
	
	clientFileName = uploadObject.getFilename();
	defaultFileName = clientFileName;
	if (clientFileName) {
		context.clientFileName = clientFileName;
	}
	filenameToUse = clientFileName;
	
	if (clientFileName && clientFileName.length() > 0) {
		//		if (clientFileName.lastIndexOf(".") > 0 && clientFileName.lastIndexOf(".") < clientFileName.length()) {
		//			filenameToUse += clientFileName.substring(clientFileName.lastIndexOf("."));
		//		} else {
		//			filenameToUse;
		//			 //+= ".csv";
		//		}

		context.clientFileName = clientFileName;
		context.filenameToUse = filenameToUse;

		characterEncoding = request.getCharacterEncoding();
		//imageUrl = imageUrlPrefix + "/" + filePathPrefix + java.net.URLEncoder.encode(filenameToUse, characterEncoding);
	
		try {
			file = new File(salaryServerPath + "/", defaultFileName);
			file1 = new File(salaryServerPath + "/", filenameToUse);
			
			println("defaultFileName --- "+defaultFileName);
			println("filenameToUse --- "+filenameToUse);
			
			//			try {
			//				// Delete existing image files
			//				File targetDir = new File(imageServerPath + "/" + filePathPrefix);
			//				// Images are ordered by productId (${location}/${id}/${viewtype}/${sizetype})
			//				if (!filenameToUse.startsWith(partyId + ".")) {
			//					File[] files = targetDir.listFiles();
			//					for(File file : files) {
			//						if (file.isFile() && file.getName().contains(filenameToUse.substring(0, filenameToUse.indexOf(".")+1)) && !fileType.equals("original")) {
			//							file.delete();
			//						} else if(file.isFile() && fileType.equals("original") && !file.getName().equals(defaultFileName)) {
			//							file.delete();
			//						}
			//					}
			//				// Images aren't ordered by productId (${location}/${viewtype}/${sizetype}/${id}) !!! BE CAREFUL !!!
			//				} else {
			//					File[] files = targetDir.listFiles();
			//					for(File file : files) {
			//						if (file.isFile() && !file.getName().equals(defaultFileName) && file.getName().startsWith(partyId + ".")) file.delete();
			//					}
			//				}
			//			} catch (Exception e) {
			//				System.out.println("error deleting existing file (not neccessarily a problem)");
			//			}
			file.renameTo(file1);
		} catch (Exception e) {
		
		//Use Linux
		//linussalaryServerPath
		
			e.printStackTrace();
		}
		
		
		//Process the CSV
		absoluteFileName = salaryServerPath + "/" + filenameToUse;
		
		org.ofbiz.salaryprocessing.SalaryProcessingServices.processCSV(absoluteFileName, salaryMonthYearId);
		
		println "Absolute File Name AAAAAAAAA absoluteFileName "+absoluteFileName;


	}
}