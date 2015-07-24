package org.ofbiz.managememberphotos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

/***
 * @author Japheth Odonya  @when Jul 20, 2015 5:42:36 PM
 * Loading Member Photos
 * 
 * org.ofbiz.managememberphotos.ManageMemberPhotoService.loadMembersPhotos(sourcepath, destinationPath, userLogin)
 * */
public class ManageMemberPhotoService {
	
	public static Logger log = Logger.getLogger(ManageMemberPhotoService.class);
	
	public static String loadMembersPhotos(String sourcepath, String destinationPath, Map<String, String> userLogin){
		//String success = "success";
		
//		File folder = new File("/F:/projects/vergeofbiz/vergesacco/sourceimages");
//		String imagesPath = "";
//		String sourcePath = "";
//		if (!folder.exists()){
//			sourcePath = "/home/online/sourceimages/PHOTOS";
//			destinationpath = "/home/samuel/installations/chaisacco/framework/images/members"; 
//		} else{
//			//imagesPath = "/F:/projects/vergeofbiz/vergesacco/sourceimages/PHOTOS";
//			imagesPath = "/F:/verge/chaisacco_dbs_23012015/PHOTOS";
//			destinationpath = "/F:/projects/vergeofbiz/vergesacco/framework/images/members"; 
//		}

		//FileUtils.copyDirectory(srcDir, destDir);
		
//		File source = new File("H:\\work-temp\\file");
//		File dest = new File("H:\\work-temp\\file2");
//		try {
//		    FileUtils.copyDirectory(source, dest);
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
		
//		Long partyId = 1000L;
//		String memberId = "";
//		File source = new File(imagesPath+"/"+"photo_"+memberId+".jpg");
//		File dest = new File(destinationpath+"/"+partyId.toString()+"/"+"photourl.jpg");
//	
//		
//		
//		//Copy photourl
//		partyId = 1000L;
//		memberId = "";
//		source = new File(imagesPath+"/"+"photo_"+memberId+".jpg");
//		dest = new File(destinationpath+"/"+partyId.toString()+"/"+"photourl.jpg");
//		try {
//		    FileUtils.copyDirectory(source, dest);
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
//		
//		//copy idback
//		
//		//copy idhind
//		
//		//copy signature
//		
//		log.info("source folder .... "+source.getParent());
//		log.info("destination folder .... "+dest.getParent());
//		
//		//Get 100 members from members without photos and load the photos
//		
//		
//		//load the photo
//		//check in the path directory for photo, id front, id back and signature
//		
//		//create a directory in the destination folder with name as member party id
//		//copy the id into directory named photorurl, idfronturl, idbackurl, signatureurl
//		
		
//		//repeat until the list of 100 members is zero
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//delegator.findList(entityName, entityCondition, fieldsToSelect, orderBy, findOptions, useCache);
		//18948
		List<GenericValue> memberELI = null; // =
		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"idNumber", EntityOperator.NOT_EQUAL, "0"),

				EntityCondition.makeCondition("idNumber",
						EntityOperator.NOT_EQUAL, "1"),
						
				EntityCondition.makeCondition("idNumber",
								EntityOperator.NOT_EQUAL, ""),
								EntityCondition.makeCondition("idNumber",
										EntityOperator.NOT_EQUAL, "."),
										EntityCondition.makeCondition("idNumber",
												EntityOperator.NOT_EQUAL, "-- NOT SET --"),
								
				EntityCondition.makeCondition("idNumber",
										EntityOperator.NOT_EQUAL, "`")

				), EntityOperator.AND);

		// EntityOperator._emptyMap
		Set<String> memberFields = new HashSet<String>();
		memberFields.add("partyId");
		memberFields.add("idNumber");
		
		
		try {
			memberELI = delegator.findList("Member",
					memberConditions, memberFields, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		memberELI.subList(0, 18948);

		Long memberAccountId = null;
		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		for (GenericValue member : memberELI) {
			//memberAccountId = genericValue.getLong("memberAccountId");
			//Create folder
			//Add photo
			//add id back
			//add id front
			//add signature
		}
		
//		List<String> listMemberId = new ArrayList<String>();
//        listMemberId.add("10000");
//        listMemberId.add("10001");
//        listMemberId.add("10002");
//        listMemberId.add("10003");
//        listMemberId.add("10004");
//        listMemberId.add("10005");
//        listMemberId.add("10006");
//        listMemberId.add("10007");
//        listMemberId.add("10008");
        
//        for (String listMemberId1 : listMemberId) {
//            
//        }
//        
    	BufferedImage image = null;
        try {
         
        	//sourcePath = "/home/online/sourceimages/PHOTOS";
			//destinationpath = "/home/samuel/installations/chaisacco/framework/images/members";
            File url = new File("D:\\photosManage\\LOGOS\\theuoelogo.png");
            image = ImageIO.read(url);
           
//       for (String listMemberId1 : listMemberId ) {
//            String dest="D:\\photosManage\\MyPhotoFolder\\"+listMemberId1+"";
//           Boolean success = (new File(dest)).mkdir();
//            if(success){
//            // File dest = new File("D:\\photosManage\\MyPhotoFolder\\"+photoFol+"");
//            ImageIO.write(image, "jpg",new File(""+dest+"\\"+listMemberId1+".jpg"));
//           
//            }
//          
//        }
            
          // ImageIO.write(image, "gif",new File("D:\\out.gif"));
           // ImageIO.write(image, "png",new File("D:\\out.png"));
           // File dest = new File("D:\\photosManage\\MyPhotoFolder\\theuoelogo.png");
           // FileUtils	(source, dest);
           //  FileUtils.copyDirectory(url, dest);
 
        } catch (IOException e) {
        	e.printStackTrace();
        }
		
		
		return "";
	}
	
	///photo here
	public static String photoCopyManager(HttpServletRequest request, HttpServletResponse response){
		Delegator delegator = (Delegator) request.getAttribute("delegator");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	    String user = userLogin.getString("partyId");
	    
		return "";
	}
	
	

}
