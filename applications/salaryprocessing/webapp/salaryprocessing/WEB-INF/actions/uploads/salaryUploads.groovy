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
imageFilenameFormat = UtilProperties.getPropertyValue('salaries', 'image.filename.format');
imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("salaries", "image.server.path"), context);
imageUrlPrefix = UtilProperties.getPropertyValue('salaries', 'image.url.prefix');
context.imageFilenameFormat = imageFilenameFormat;
context.imageServerPath = imageServerPath;
context.imageUrlPrefix = imageUrlPrefix;

filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
context.imageNameSmall  = imageUrlPrefix + "/" + filenameExpander.expandString([location : 'members', id : partyId, type : 'small']);
context.imageNameMedium = imageUrlPrefix + "/" + filenameExpander.expandString([location : 'members', id : partyId, type : 'medium']);
context.imageNameLarge  = imageUrlPrefix + "/" + filenameExpander.expandString([location : 'members', id : partyId, type : 'large']);
context.imageNameDetail = imageUrlPrefix + "/" + filenameExpander.expandString([location : 'members', id : partyId, type : 'detail']);
context.imageNameOriginal = imageUrlPrefix + "/" + filenameExpander.expandString([location : 'members', id : partyId, type : 'original']);

// Start MemberContent stuff
//memberContent = null;
//if (member) {
//	memberContent = member.getRelated('MemberContent', null, ['memberContentTypeId'], false);
//}
//context.memberContent = memberContent;
// End MemberContentent stuff

tryEntity = true;
if (request.getAttribute("_ERROR_MESSAGE_")) {
	tryEntity = false;
}
//if (!member) {
//	tryEntity = false;
//}

if ("true".equalsIgnoreCase((String) request.getParameter("tryEntity"))) {
	tryEntity = true;
}
context.tryEntity = tryEntity;

// UPLOADING STUFF
forLock = new Object();
contentType = null;
String fileType = request.getParameter("upload_file_type");

String photourl = request.getParameter("photourl");
String idfronturl = request.getParameter("idfronturl");
String idbackurl = request.getParameter("idbackurl");
String signatureurl = request.getParameter("signatureurl");
String fileNameToSave = "";
//Set the file name
if (photourl.equals("Y")){
	fileNameToSave = "photourl";
} else if (idfronturl.equals("Y")){
	fileNameToSave = "idfronturl";
} else if (idbackurl.equals("Y")){
	fileNameToSave = "idbackurl";
} else if (signatureurl.equals("Y")){
	fileNameToSave = "signatureurl";
}

if (fileType) {

	context.fileType = fileType;

	//fileLocation = filenameExpander.expandString([location : 'members', id : partyId, type : fileType]);
	fileLocation = filenameExpander.expandString([location : 'members', id : partyId, type : fileNameToSave]);
	filePathPrefix = "";
	filenameToUse = fileLocation;
	if (fileLocation.lastIndexOf("/") != -1) {
		filePathPrefix = fileLocation.substring(0, fileLocation.lastIndexOf("/") + 1); // adding 1 to include the trailing slash
		filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
	}

	int i1;
	if (contentType && (i1 = contentType.indexOf("boundary=")) != -1) {
		contentType = contentType.substring(i1 + 9);
		contentType = "--" + contentType;
	}

	defaultFileName = filenameToUse + "_temp";
	uploadObject = new HttpRequestFileUpload();
	uploadObject.setOverrideFilename(defaultFileName);
	uploadObject.setSavePath(imageServerPath + "/" + filePathPrefix);
	uploadObject.doUpload(request);

	clientFileName = uploadObject.getFilename();
	if (clientFileName) {
		context.clientFileName = clientFileName;
	}

	if (clientFileName && clientFileName.length() > 0) {
		if (clientFileName.lastIndexOf(".") > 0 && clientFileName.lastIndexOf(".") < clientFileName.length()) {
			filenameToUse += clientFileName.substring(clientFileName.lastIndexOf("."));
		} else {
			filenameToUse += ".jpg";
		}

		context.clientFileName = clientFileName;
		context.filenameToUse = filenameToUse;

		characterEncoding = request.getCharacterEncoding();
		imageUrl = imageUrlPrefix + "/" + filePathPrefix + java.net.URLEncoder.encode(filenameToUse, characterEncoding);

		try {
			file = new File(imageServerPath + "/" + filePathPrefix, defaultFileName);
			file1 = new File(imageServerPath + "/" + filePathPrefix, filenameToUse);
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
			e.printStackTrace();
		}

		if (imageUrl && imageUrl.length() > 0) {
			context.imageUrl = imageUrl;
			//member.set(fileType + "ImageUrl", imageUrl);

//			if (photourl.equals("Y")){
//				member.set("photourl", imageUrl);
//				memberimageurl = "photourl";
//			} else if (idfronturl.equals("Y")){
//				member.set("idfronturl", imageUrl);
//				memberimageurl = "idfronturl";
//			} else if (idbackurl.equals("Y")){
//				member.set("idbackurl", imageUrl);
//				memberimageurl = "idbackurl";
//			} else if (signatureurl.equals("Y")){
//				member.set("signatureurl", imageUrl);
//				memberimageurl = "signatureurl";
//			}

			// call scaleImageInAllSize
			//			if (fileType.equals("original")) {
			//				result = ScaleImage.scaleImageInAllSize(context, filenameToUse, "main", "0");
			//
			//				if (result.containsKey("responseMessage") && result.get("responseMessage").equals("success")) {
			//					imgMap = result.get("imageUrlMap");
			//					imgMap.each() { key, value ->
			//						//member.set(key + "ImageUrl", value);
			//						member.set(memberimageurl, value);
			//
			//					}
			//				}
			//			}

			//member.store();
		}
	}
}