package org.ofbiz.humanres;

import java.util.List;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * @author RONNY
 * 
 *         HCM Setup validations
 * */
public class HumanResourceSetup {

	private static Logger log = LoggerFactory.getLogger(HumanResourceSetup.class);

	/****
	 * Checking that a paygrade already exists
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.payGradeExists()
	 * */
	public static Boolean payGradeExists(String name) {

		log.info("GGGGGGGGGGGGGGGGG The Grade Type is " + name);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> listPayGradesELI = null;

		try {
			listPayGradesELI = delegator.findList("PayGrade",
					EntityCondition.makeCondition("payGradeName", name), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((listPayGradesELI != null) && (listPayGradesELI.size() > 0)) {
			return true;
		}

		return false;
	}
	/***
	 * Checking that blood group already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.bloodGroupExists()
	 * 
	 * **/
	public static Boolean bloodGroupExists(String BGroupName){
		
		log.info("BBBBbbb   The Blood Group is " + BGroupName);
		
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		
		List<GenericValue> listBloogGroupELI=null;
		
		try{
			
			listBloogGroupELI = delegator.findList("BloodGroup",
					EntityCondition.makeCondition("bloodGroup", BGroupName), null,
					null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listBloogGroupELI != null) && (listBloogGroupELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	/***
	 * Checking that County already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.countyExist()
	 * 
	 * **/
	public static Boolean countyExist(String CountyName){
		log.info("BBBBbbb   The County is " + CountyName);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listCountyELI=null;
		try{
			listCountyELI = delegator.findList("County",
					EntityCondition.makeCondition("county", CountyName), null,
					null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listCountyELI != null) && (listCountyELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	/***
	 * Checking that Reason Type already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.reasonTypeExist()
	 * 
	 * **/
	public static Boolean reasonTypeExist(String name){
		log.info("BBBBbbb   The Leave Reason Type is " + name);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listReasonTypeELI=null;
		try{
			listReasonTypeELI = delegator.findList("EmplLeaveReasonType",
					EntityCondition.makeCondition("reason", name), null,
					null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listReasonTypeELI != null) && (listReasonTypeELI.size() > 0)) {
			return true;
		}
		return false;
	}

	/***
	 * Checking thatThat Family relationship  already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.familyRelationship()
	 * 
	 * **/
	public static Boolean familyRelationship(String name){
		log.info("BBBBbbb   The Leave Reason Type is " + name);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listFamilyRelatiionELI=null;
		try{
			listFamilyRelatiionELI = delegator.findList("FamilyRelations",
					EntityCondition.makeCondition("relationship", name), null,
					null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listFamilyRelatiionELI != null) && (listFamilyRelatiionELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	/***
	 * Checking thatThat Tribe  already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.TribeExist(tribe)
	 * 
	 * **/
	public static Boolean TribeExist(String name){
		log.info("BBBBbbb   The Tribe is " + name);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listTribeELI=null;
		try{
			listTribeELI = delegator.findList("Tribe",
		       EntityCondition.makeCondition("tribe", name), null,
			   null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listTribeELI != null) && (listTribeELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	/***
	 * Checking whether Holiday Date is assigned to another
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.holidayDate()
	 * 
	 * **/
	public static Boolean holidayName(String name){
		log.info("BBBBbbb   The Holiday is " + name);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> listHolidayDateELI=null;
		try{
			listHolidayDateELI = delegator.findList("PublicHolidays",
		       EntityCondition.makeCondition("holidayName", name), null,
			   null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((listHolidayDateELI != null) && (listHolidayDateELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	
	/***
	 * Checking thatThat Employee is Already Posted  already exist
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.employeePostingExist()
	 * 
	 * **/
	public static Boolean employeePostingExist(String name){
		log.info("BBBBbbb   The Employee posting  is " + name);
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> employeePostindELI=null;
		try{
			employeePostindELI = delegator.findList("Employment",
					EntityCondition.makeCondition("partyIdTo", name), null,
					null, null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		if ((employeePostindELI != null) && (employeePostindELI.size() > 0)) {
			return true;
		}
		return false;
	}
	
	
}
