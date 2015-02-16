package org.ofbiz.registry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;

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
	
	public static String getFileVolumeCount(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String volumeCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryFileVolume", EntityCondition.makeCondition("partyId", partyId), null, null, null, false);
		
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
	
	
	public static String getFileDocCount(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String nextDocFolioCount = null;
		int count=0;
		List<GenericValue> volumELI = null;
		try {
			volumELI = delegator.findList("RegistryDocuments", EntityCondition.makeCondition("partyId", partyId), null, null, null, false);
		
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
	
	public static String getMemberStatusAndUpdatedDate(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		/*String partyId = new String(request.getParameter("partyId")).toString();*/	
		List<GenericValue> membersELI = null; 
		try {
			membersELI = delegator.findAll("Member", true);
			
			/*membersELI = delegator.findOne("Member", UtilMisc.toMap("partyId", partyId), false);*/
			
			log.info("MEMBERS ################################ " + membersELI + "########################################");
				
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String memberStatus = null ,updatedDate = null, partyId = null;
		/*Date updatedDate = null;*/
		
		for (GenericValue genericValue : membersELI) {
			memberStatus = genericValue.getString("isActive");
			updatedDate = genericValue.getString("lastUpdatedStamp");
			partyId = genericValue.getString("partyId");
			
			if(memberStatus.equalsIgnoreCase("")){
				memberStatus ="Y";
			}
			
		}
		
		log.info("STATUS ################################ " + memberStatus + "########################################");
		log.info("UPDATE DATE ################################ " + updatedDate + "########################################");
		
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFiles",
					UtilMisc.toMap("partyId", partyId), false);

			if (memberFiles != null) { // old records
				memberFiles.set("memberStatus", memberStatus);
				memberFiles.set("inactiveStartDate", updatedDate);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		
		return memberStatus;
	}
	
	
	public static String  getIsReadyToMoveToSemiActiveState(Date inactiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("inactiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(inactiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-TO-SEMI-ACTIVE";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	public static String  getIsReadyToMoveToArchiveState(Date SemiActiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("semiActiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(SemiActiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-TO-ARCHIVE";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	public static String  getIsReadyToMoveToDisposalState(Date ArchiveStartDate) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		int period = 0;
		int currentPeriod = 0;
		Date today = new Date();
		String state = null;
		try {
			GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
					UtilMisc.toMap("setupId", "1"), false);

			if (memberFiles != null) { // old records
				period = Integer.valueOf(memberFiles.getString("archiveState"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		currentPeriod =calculateCalenderDaysBetweenDates(ArchiveStartDate, today);
		
		if ((period*30) <= (currentPeriod)) {
			state = "MOVE-FOR-DISPOSAL";
		} else {
			state = "NOT-YET";

		}
		log.info("+++++++++++++++++++++++period: "+period);
		log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
		log.info("+++++++++++++++++++++++state: "+state);
		
			   
	    return state;


	}
	
	
		
		
		public static String getIsReadyToMoveToSemiActiveStateYYYY(HttpServletRequest request,	HttpServletResponse response) {
			Delegator delegator;
			delegator = DelegatorFactoryImpl.getDelegator(null);
			int period = 0;
			int currentPeriod = 0;
			Date today = new Date();
			String state = null;
			
			List<GenericValue> personsELI = null; 
			try {
				personsELI = delegator.findAll("RegistryFiles", true);
					
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			String partyId ="", inactiveStartDate = "", stageStatus; 
		
			for (GenericValue genericValue : personsELI) {
				partyId = genericValue.getString("partyId");
				inactiveStartDate = genericValue.getString("inactiveStartDate");
				stageStatus = genericValue.getString("stageStatus");

				if (stageStatus.equalsIgnoreCase("INACTIVE")) {
					
					try {
						GenericValue memberFiles = delegator.findOne("RegistryFileSetting",
								UtilMisc.toMap("setupId", "1"), false);

						if (memberFiles != null) { // old records
							period = Integer.valueOf(memberFiles.getString("inactiveState"));
						}
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}	
					
					@SuppressWarnings("deprecation")
					Date inactive = null;
					try {
						inactive = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(inactiveStartDate));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					currentPeriod =calculateCalenderDaysBetweenDates(inactive, today);
					GenericValue party = null;
					try {
						party = delegator.findOne("RegistryFiles", UtilMisc.toMap("partyId", partyId), false);
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}	
					
					List<GenericValue> rolesToMove = null;
					try {
						rolesToMove = delegator.findByAnd("RegistryFiles",
								UtilMisc.toMap("partyId", partyId), null, false);
					} catch (GenericEntityException e) {
						
					}
					
					
					
					
					if ((period*30) <= (currentPeriod)) {
						/*party.set("isReadyForSemiActive", "Y");*/
						
						for (GenericValue attr : rolesToMove) {
							attr.set("isReadyForSemiActive", "Yeees");
						}
						
					} else {
						for (GenericValue attr : rolesToMove) {
							attr.set("isReadyForSemiActive", "Noooo");
						}
						

					}
					
					log.info("+++++++++++++++++++++++partyId: "+partyId);
					log.info("+++++++++++++++++++++++inactiveStartDate: "+inactiveStartDate);
					log.info("+++++++++++++++++++++++stageStatus: "+stageStatus);
					log.info("+++++++++++++++++++++++period: "+period);
					log.info("+++++++++++++++++++++++currentPeriod: "+currentPeriod);
					
				} else {

				}
				
			}
			//log.info("------------------------------------------------" +partyId);
			
			return partyId;
		}
		
		public static String getDurationBtnRequestAndIssue(String partyId) {
			int interDuration =0;
			String duration = null;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			 List<GenericValue> getActivityELI=null;
			 GenericValue activity = null;
			 Date actionDate = null;
			 Date today = new Date();
			
			EntityConditionList<EntityExpr> getActivity = EntityCondition.makeCondition(UtilMisc.toList(
					    EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("fileActionTypeId",EntityOperator.EQUALS, "Request")),EntityOperator.AND);

			try {
				List<String> orderByList = new ArrayList<String>();
				orderByList.add("-actionDate");
				
				
				getActivityELI = delegator.findList("RegistryFileLogs",
						 getActivity, null, orderByList, null, false);
				
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
				
			}
			
				String StringactionDate = null;
				if ((getActivityELI.size() > 0)) {
					activity = getActivityELI.get(0);
					StringactionDate = activity.getString("actionDate");

				}
				
				try {
					actionDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(StringactionDate));
				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				interDuration = calculateCalenderDaysBetweenDates(actionDate, today);
						duration = String.valueOf(interDuration);
			
						log.info("=================+++++++++++++++++++++++duration: "+duration);
			return duration;
			
		}
		
		
	
	public static int calculateCalenderDaysBetweenDates(Date startDate,	Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
				daysCount++;
			

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return daysCount;
	}
	
	public static int calculateWorkingNonHolidayDaysBetweenDates(Date startDate, Date endDate) {
		int daysCount = 0;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);
		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY) 
					
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}
		
		/*int noOfHolidays = getNumberOfHolidays(startDate, endDate);*/
		
		/*daysCount = daysCount - noOfHolidays;*/

		return daysCount;
	}
	
	public static Date calculateEndWorkingDay(Date startDate, String noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());
		int duration = Integer.valueOf(noOfDays);

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek()== DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 1;
		while (count < duration) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}
		log.info("UPDATE DATE ################################ " + localDateEndDate.toDate() + "########################################");

		return localDateEndDate.toDate();
		
		
	}
	
	public static String getDateOfReceivedFile(String partyId, String releasedTo) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String fileDat = null;
         List<GenericValue> dateELI = null;
		
		
		GenericValue lastDate = null;
		
		EntityConditionList<EntityExpr> dateConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
				EntityCondition.makeCondition("releasedTo",EntityOperator.EQUALS, releasedTo)),
					EntityOperator.AND);
		
		try {
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("-timeOut");

			dateELI = delegator.findList("RegistryFileMovement",dateConditions, null, orderByList, null, false);
			
			if (dateELI.size() > 0){
				lastDate = dateELI.get(0); 
				fileDat=lastDate.getString("timeOut");
			
			}
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
		
		Date fileDate = null;
		try {
			fileDate = (Date)(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fileDat));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		LocalDate localDateEndDate = new LocalDate(fileDate.getTime());
		return fileDat;
		
		
	}
	
	public static Boolean isOfficerWithFile(String officerPartyId, String memmberPayrol) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue file = null;
		try {
			file = delegator.findOne("RegistryFiles", UtilMisc.toMap("payrollNumber", memmberPayrol, "currentPossesser", officerPartyId), false);

			log.info("File Has been Fetched Guyz ###### File with payroll>>"+memmberPayrol+" And in possesion of>>"+officerPartyId);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (file == null) {
			
			return false;
		}
		
		return true;
	}
	
	public static String exampleSendEmail(HttpServletRequest request, HttpServletResponse response) {
			// Get the local Service dispatcher from the context
			// Note: Dont forget to import the org.ofbiz.service.* classes
			LocalDispatcher dispatcher =(LocalDispatcher) request.getAttribute("dispatcher");
			String errMsg = null;
			// The following are hardcoded as an example only
			// Your program would set these up from the context or from
			// database lookups
			String mailBody = "This is the body of my email";
			Map<String, String> sendMailContext = new HashMap<String, String>();
			sendMailContext.put("sendTo", "juliandan17@gmail.com");
			//sendMailContext.put("sendCC", "");
			//sendMailContext.put("sendBcc","" );
			sendMailContext.put("sendFrom", "juliandan7@gmail.com");
			sendMailContext.put("subject", "Testing emails sent from an OFBiz Event");
			sendMailContext.put("body", mailBody);
			try {
			// Call the sendMail Service and pass the sendMailContext
			// Map object.
			// Set timeout to 360 and wrap with a new transaction
			Map<String, Object> result = dispatcher.runSync("sendMail", sendMailContext,360, true);
			// Note easy way to get errors when they are returned
			// from another Service
			if (ModelService.RESPOND_ERROR.equals((String)
			result.get(ModelService.RESPONSE_MESSAGE))) {
			Map<String, Object> messageMap = UtilMisc.toMap("errorMessage",	result.get(ModelService.ERROR_MESSAGE));
			errMsg = "Problem sending this email";
			request.setAttribute("_ERROR_MESSAGE_", errMsg);
			return "error";
			}
			}
			catch (GenericServiceException e) {
			// For Events error messages are passed back
			Debug.logWarning(e, "", module);
			// as Request Attributes
			errMsg = "Problem with the sendMail Service call";
			request.setAttribute("_ERROR_MESSAGE_", errMsg);
			return "error";
			}
			return "success";
			}
	

}
