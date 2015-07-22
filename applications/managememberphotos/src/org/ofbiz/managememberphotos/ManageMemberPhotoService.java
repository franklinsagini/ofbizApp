package org.ofbiz.managememberphotos;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/***
 * @author Japheth Odonya  @when Jul 20, 2015 5:42:36 PM
 * Loading Member Photos
 * 
 * org.ofbiz.managememberphotos.ManageMemberPhotoService.loadMembersPhotos(sourcepath, destinationPath, userLogin)
 * */
public class ManageMemberPhotoService {
	
	public static Logger log = Logger.getLogger(ManageMemberPhotoService.class);
	
	public static String loadMembersPhotos(String sourcepath, String destinationPath, Map<String, String> userLogin){
		String success = "success";
		
		File folder = new File("/F:/projects/vergeofbiz/vergesacco/sourceimages");
		String imagesPath = "";
		String destinationpath = "";
		if (!folder.exists()){
			imagesPath = "/home/online/sourceimages";
			destinationpath = "/home/samuel/installations/chaisacco/framework/images/members"; 
		} else{
			//imagesPath = "/F:/projects/vergeofbiz/vergesacco/sourceimages";
			imagesPath = "/F:/verge/chaisacco_dbs_23012015/PHOTOS";
			destinationpath = "/F:/projects/vergeofbiz/vergesacco/framework/images/members"; 
		}

		//FileUtils.copyDirectory(srcDir, destDir);
		
//		File source = new File("H:\\work-temp\\file");
//		File dest = new File("H:\\work-temp\\file2");
//		try {
//		    FileUtils.copyDirectory(source, dest);
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
		
		Long partyId = 1000L;
		String memberId = "";
		File source = new File(imagesPath+"/"+"photo_"+memberId+".jpg");
		File dest = new File(destinationpath+"/"+partyId.toString()+"/"+"photourl.jpg");
		
		
		
		//Copy photourl
		partyId = 1000L;
		memberId = "";
		source = new File(imagesPath+"/"+"photo_"+memberId+".jpg");
		dest = new File(destinationpath+"/"+partyId.toString()+"/"+"photourl.jpg");
		try {
		    FileUtils.copyDirectory(source, dest);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		//copy idback
		
		//copy idhind
		
		//copy signature
		
		log.info("source folder .... "+source.getParent());
		log.info("destination folder .... "+dest.getParent());
		
		//Get 100 members from members without photos and load the photos
		
		
		//load the photo
		//check in the path directory for photo, id front, id back and signature
		
		//create a directory in the destination folder with name as member party id
		//copy the id into directory named photorurl, idfronturl, idbackurl, signatureurl
		
		//repeat until the list of 100 members is zero
		
		return success;
	}

}
