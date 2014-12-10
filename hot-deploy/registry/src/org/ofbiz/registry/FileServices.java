package org.ofbiz.registry;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

public class FileServices {
	public static String module = FileServices.class.getName();
	public static Logger log = Logger.getLogger(FileServices.class);

	public static Boolean updateFileStatus(GenericValue fileRequest, Delegator delegator) {
		String fileId = fileRequest.getString("fileId");
		log.info("FileId Has been Fetched Guyz ################################ " + fileId + "########################################");
		GenericValue file = null;
		try {
			file = delegator.findOne("RegistryFiles", UtilMisc.toMap("fileId", fileId), false);
			log.info("File Has been Fetched Guyz ################################ " + file + "########################################");
		} catch (GenericModelException e) {
			Debug.logError(e.toString(), "Cannot Primary Find File", module);
		}catch (GenericEntityException e) {
			Debug.logError(e, "Cannot Find File", module);
		}



		return true;
	}
	
	//================================ COUNTING FILE VOLUMES ====================================================
	
	public static String getFileVolumeCount(String memberPlusPersonId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String volumeCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryFileVolume", EntityCondition.makeCondition("memberPlusPersonId", memberPlusPersonId), null, null, null, false);
		
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (GenericValue genericValue : volumELI) {
			count++;
		}
		volumeCount= String.valueOf(count);
		log.info("NUMBER OF FILE VOLUMES ################################ " + volumeCount + "########################################");
				
		return volumeCount;
		
	}
	
	
	public static String getFileDocCount(String memberPlusPersonId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String nextDocFolioCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryDocuments", EntityCondition.makeCondition("memberPlusPersonId", memberPlusPersonId), null, null, null, false);
		
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (GenericValue genericValue : volumELI) {
			count++;
		}
		nextDocFolioCount= String.valueOf(count+1);
		log.info("NUMBER OF FILE VOLUMES ################################ " + nextDocFolioCount + "########################################");
				
		return nextDocFolioCount;
		
	}
	
	public static String getArchingDateCount(String docType, Date receiptDate) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String retentionPeriod = null;
		GenericValue DocumentType = null;
		String archiveDate= "NA";
	      try {
	    	  DocumentType = delegator.findOne("RegistryDocumentType", 
	             	UtilMisc.toMap("DocumentTypeId", docType), false);
	           	log.info("++++++++++++++carryOverLeaveGV++++++++++++++++" +DocumentType);
	             }
	       catch (GenericEntityException e) {
	            e.printStackTrace();;
	       }  
		if (DocumentType != null) {
			retentionPeriod=DocumentType.getString("retentionPeriod");
			
			
			log.info("RETENTION PERIOD ################################ " + retentionPeriod + "########################################");
			
			if(retentionPeriod == "PERMANENT"){
				 archiveDate= "NA";
				
			} 
			else{
				LocalDate reDate = new LocalDate(receiptDate);
				LocalDate bodDate = reDate.plusYears(Integer.valueOf(retentionPeriod));
				archiveDate= bodDate.toString();
				log.info("ARCHIVING DATE ################################ " + bodDate + "########################################");
			}
		}
		
		
		return archiveDate;
		
	}
	
	
	
	

}
