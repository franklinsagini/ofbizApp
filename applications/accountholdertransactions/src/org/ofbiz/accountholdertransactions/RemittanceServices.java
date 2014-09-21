package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.webapp.event.EventHandlerException;

/**
 * @author Japheth Odonya  @when Sep 18, 2014 12:39:40 PM
 * 
 * Remittance Operations
 * 
 * RemittanceServices.generateExpectedPaymentStations
 * **/
public class RemittanceServices {
	
	public static Logger log = Logger.getLogger(RemittanceServices.class);

	public static String generateExpectedPaymentStations(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		List<GenericValue> memberELI = null;
		 Map<String, String> userLogin = (Map<String, String>) request
		 .getAttribute("userLogin");


		try {
			memberELI = delegator.findList("Member", null, null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		Set<String> setMemberStations =  new HashSet<String>();
		String stationId = null;
		for (GenericValue member : memberELI) {
			//Add station Id to set
			stationId = member.getString("stationId");
			if (stationId != null){
				setMemberStations.add(stationId);
			}
		}
		String createdBy = (String) request.getAttribute("userLoginId");
		String month = getCurrentMonth();
		//With the set IDs create ExpectatedStation
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (String tempStationId : setMemberStations) {
			createExpectedStation(tempStationId, month, createdBy);
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//Add shares
		String shareCode = getShareCode();
		addExpectedShares(shareCode);
		


		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}

	/***
	 * Get shares code
	 * */
	private static String getShareCode() {
		GenericValue codesSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			codesSetup = delegator.findOne("CodesSetup",
					UtilMisc.toMap("name", "SHARES"),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get CodesSetup  ");
		}
		return codesSetup.getString("code");
	}

	private static void addExpectedShares(String shareCode) {
		
		//for each member save a share expected
		List<GenericValue> memeberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memeberELI = delegator.findList("Member",
					EntityCondition.makeCondition("memberStatus", "ACTIVE"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue member : memeberELI) {
			
			addMemberExpectedShares(shareCode, member);
		}
		
	}

	private static void addMemberExpectedShares(String shareCode,
			GenericValue member) {
			GenericValue station = findStation(member.getString("stationId"));
			String month = getCurrentMonth();
			String employerName = getEmployer(station.getString("employerId"));
			
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			//Create an expectation
			GenericValue expectedPaymentSent = null;
			
			String employeeNames = "";

			if (member.getString("firstName") != null) {
				employeeNames = employeeNames + member.getString("firstName");
			}

			if (member.getString("middleName") != null) {
				employeeNames = employeeNames + " "
						+ member.getString("middleName");
			}

			if (member.getString("lastName") != null) {
				employeeNames = employeeNames + " " + member.getString("lastName");
			}

			
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
			}
			expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
					UtilMisc.toMap("isActive", "Y",
							"branchId", member.getString("branchId"),
							"remitanceCode", shareCode,
							"stationNumber", station.getString("stationNumber"),
							"stationName", station.getString("stationName"),
							
							"payrollNo", member.getString("payrollNumber"),
							"loanNo", "0",
							"employerNo", employerName,
							"amount", member.getBigDecimal("shareAmount"),
							"remitanceDescription", "SHARES",
							"employeeName", employeeNames,
							
							"month",month));
			try {
				delegator.createOrStore(expectedPaymentSent);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			try {
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
			
	}

	private static String getEmployer(String employerId) {
		GenericValue employer = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			employer = delegator.findOne("Employer",
					UtilMisc.toMap("employerId", employerId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Employer  ");
		}
		return employer.getString("name");
	}

	private static String getCurrentMonth() {
		// TODO Auto-generated method stub
		Calendar now = Calendar.getInstance();
		
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		
		month = month+1;
		
		String monthName = "";
		if (month < 10)
		{
			monthName = "0"+month;
		} else{
			monthName = String.valueOf(month);
		}
		String currentMonth = monthName+String.valueOf(year);
		return currentMonth;
	}

	/***
	 * @author Japheth Odonya  @when Sep 18, 2014 1:02:52 PM
	 * 
	 * Create StationExpectation 
	 * 
	 * */
	private static void createExpectedStation(String tempStationId, String month, String createdBy) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue station = findStation(tempStationId);
		/***
		 * <field name="isActive" type="indicator"></field>
		<field name="createdBy" type="name"></field>
		<field name="updatedBy" type="name"></field>
		<field name="branchId" type="name"></field>
		<field name="stationNumber" type="name"></field>
		<field name="stationName" type="name"></field>
		
        <field name="month" type="name"></field>
		 * 
		 * **/
		String branchId = station.getString("branchId");
		String stationNumber = station.getString("stationNumber");
		String stationName = station.getString("name");
		GenericValue stationExpectation = null;
		stationExpectation = delegator.makeValue("StationExpectation",
				UtilMisc.toMap("isActive", "Y",
						"createdBy", createdBy, "branchId",
						branchId, "stationNumber", stationNumber,
						"stationName", stationName, "month",
						month));
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		try {
			delegator.createOrStore(stationExpectation);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
	}

	private static GenericValue findStation(String tempStationId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		 GenericValue station = null;
		try {
			station = delegator
					.findOne("Station", UtilMisc.toMap(
							"stationId", tempStationId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return station;
	}

}
