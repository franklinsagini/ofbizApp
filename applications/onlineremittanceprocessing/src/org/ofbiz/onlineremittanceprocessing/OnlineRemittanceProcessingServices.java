package org.ofbiz.onlineremittanceprocessing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.party.party.SaccoUtility;

public class OnlineRemittanceProcessingServices {
	
	private static Logger log = Logger
			.getLogger(OnlineRemittanceProcessingServices.class);

	/****
	 * Adds all stations online - to the online list
	 * */
	public static String addAllStationsOnline(HttpServletRequest request,
			HttpServletResponse response) {
		// Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		// Get all stations
		List<GenericValue> stationELI = new ArrayList<GenericValue>();
		try {
			stationELI = delegator.findList("Station", null, null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> onlineStationItemELI = new ArrayList<GenericValue>();
		// Add each station to the online list
		for (GenericValue genericValue : stationELI) {
			// Add station to Online Stations List
			onlineStationItemELI.add(addStationToOnline(genericValue,
					delegator, userLoginId));
		}

		try {
			delegator.storeAll(onlineStationItemELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static GenericValue addStationToOnline(GenericValue genericValue,
			Delegator delegator, String userLoginId) {

		GenericValue onlineStationItem;
		Long onlineStationItemId;
		onlineStationItemId = SaccoUtility.getNextSequenc("OnlineStationItem");
		onlineStationItem = delegator.makeValidValue("OnlineStationItem",
				UtilMisc.toMap("onlineStationItemId", onlineStationItemId,
						"stationId", genericValue.getString("stationId"),
						"isActive", "Y", "createdBy", userLoginId));
		return onlineStationItem;
	}

	/***
	 * Clears the online list - removing all stations
	 * */
	public static String removeAllStationsOnline(HttpServletRequest request,
			HttpServletResponse response) {
		// Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Get all stations
		List<GenericValue> olineStationItemELI = new ArrayList<GenericValue>();
		try {
			olineStationItemELI = delegator.findList("OnlineStationItem", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			delegator.removeAll(olineStationItemELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	/***
	 * Removes a station from online
	 * */
	public static String removeStationOnline(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		String onlineStationItemIdStr = (String) request
				.getParameter("onlineStationItemId");

		onlineStationItemIdStr = onlineStationItemIdStr.replaceAll(",", "");
		Long onlineStationItemId = Long.valueOf(onlineStationItemIdStr);

		try {
			delegator.removeByCondition("OnlineStationItem", EntityCondition
					.makeCondition("onlineStationItemId", onlineStationItemId));
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	/***
	 * Returns the total number of Stations that are online
	 * 
	 * */
	public static Long getOnlineStationsCount() {
		Long stationCount = 0L;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> olineStationItemELI = new ArrayList<GenericValue>();
		try {
			olineStationItemELI = delegator.findList("OnlineStationItem", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		stationCount = (long) olineStationItemELI.size();

		return stationCount;
	}

	/***
	 * Push All Stations pushAllStations
	 ***/
	public static String pushAllStations(HttpServletRequest request,
			HttpServletResponse response) {
		// Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		return "success";
	}

	/****
	 * Pull All Stations pullAllStations
	 * */
	public static String pullAllStations(HttpServletRequest request,
			HttpServletResponse response) {
		// Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		return "success";
	}

	/****
	 * Overload the push and pull all stations methods to be called from the
	 * service (xml services) methods
	 * */
	public static String pushAllStations(Map<String, String> userLogin, Long pushMonthYearId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String userLoginId = userLogin.get("userLoginId");
		
		
		log.info(" The push is being done by ///////////////// "+userLoginId);
		log.info(" The Pull pushMonthYear is ................ "+pushMonthYearId);

		return "success";
	}

	public static String pullAllStations(Map<String, String> userLogin, Long pushMonthYearId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String userLoginId = userLogin.get("userLoginId");

		log.info(" The Pull is being done by ................ "+userLoginId);
		log.info(" The Pull pushMonthYear is ................ "+pushMonthYearId);
		return "success";
	}
	
	public static String createPushAndPullStationItems(Long pushMonthYearId, Map<String, String> userLogin){
		
		//Create the PushMonthYearItem
		log.info("Will be creating PUSH Records");
		List<GenericValue> onlineStationItemList = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		onlineStationItemList = getOnlineStations();
		
		List<GenericValue> pushItemList = new ArrayList<GenericValue>();
		
		String userLoginId = (String) userLogin.get("userLoginId");
		for (GenericValue genericValue : onlineStationItemList) {
			//Create a PUSH Reccord Item
			pushItemList.add(createPushRecord(pushMonthYearId, genericValue, userLoginId));
		}

		//Save the PUSH Reccords

		try {
			delegator.storeAll(pushItemList);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 		List<GenericValue> pullItemList = new ArrayList<GenericValue>();
		//Create the PullMonthYearItem
		for (GenericValue genericValue : onlineStationItemList) {
			//Create a PULL Record Item
			pullItemList.add(createPullRecord(pushMonthYearId, genericValue, userLoginId));
		}
		
		//Save the PULL Records
		try {
			delegator.storeAll(pullItemList);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Will be creating PULL Records");
		
		return "success";
		
	}

	/***
	 * Creating the PULL Records
	 * 
	 * */
	private static GenericValue createPullRecord(Long pushMonthYearId,
			GenericValue genericValue, String userLoginId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue pullMonthYearItem = null;
		
		Long pullMonthYearItemId = delegator.getNextSeqIdLong("PullMonthYearItem", 1);
		
		String stationId = genericValue.getString("stationId");
		GenericValue station =	LoanUtilities.getStation(stationId);
		
		String employerCode = station.getString("employerCode");
		String employerName = station.getString("employerName");
		
		pullMonthYearItem = delegator.makeValidValue("PullMonthYearItem",
				UtilMisc.toMap("pullMonthYearItemId", pullMonthYearItemId,
						"isActive", "Y", "createdBy", userLoginId, 
						
						"stationId", stationId,
						
						"employerCode", employerCode,
						"employerName", employerName, 
						
						"pulled", "N"));
		
		return pullMonthYearItem;
	}

	private static GenericValue createPushRecord(Long pushMonthYearId,
			GenericValue genericValue, String userLoginId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue pushMonthYearItem = null;
		
		Long pushMonthYearItemId = delegator.getNextSeqIdLong("PushMonthYearItem", 1);
		
		String stationId = genericValue.getString("stationId");
		GenericValue station =	LoanUtilities.getStation(stationId);
		
		String employerCode = station.getString("employerCode");
		String employerName = station.getString("employerName");
		
		pushMonthYearItem = delegator.makeValidValue("PushMonthYearItem",
				UtilMisc.toMap("pushMonthYearItemId", pushMonthYearItemId,
						"isActive", "Y", "createdBy", userLoginId, 
						
						"stationId", stationId,
						
						"employerCode", employerCode,
						"employerName", employerName, 
						
						"pushed", "N"));
		
		return pushMonthYearItem;
	}

	private static List<GenericValue> getOnlineStations() {
		// TODO Auto-generated method stub
		//OnlineStationItem
		List<GenericValue> onlineStationItemELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			onlineStationItemELI = delegator.findList("OnlineStationItem", null, null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return onlineStationItemELI;
	}
	
	public static String pushStation(Map<String, String> userLogin, Long pushMonthYearItemId){
		
		//Get PushMonthYearItem item
		GenericValue pushMonthYearItem = getPushMonthYearItem(pushMonthYearItemId);
		//Get station from the PushMonthYear and see if it exists
		//Get station from the PullMonthYearItem and see if it exists
		GenericValue station = LoanUtilities.getStation(pushMonthYearItem.getString("stationId"));
		String employerCode = station.getString("employerCode");
		employerCode = employerCode.trim();
		
		//Concatenate Month and Year to get month to use in getting the data
		GenericValue pullMonthYear = getPullMonthYear(pushMonthYearItem.getLong("pushMonthYearId"));
		
		String month = pullMonthYear.getLong("month").toString()+pullMonthYear.getLong("year").toString();
		month = month.trim();
		
		
		//If not exist return no data
		List<GenericValue> expectedPaymentSentELI = null;
		expectedPaymentSentELI = getExpectedPaymentSentList(employerCode, month);
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String helperOnlineStations = delegator.getGroupHelperName("org.ofbiz.onlinestations");
		
		SQLProcessor sqlproc = new SQLProcessor(new GenericHelperInfo("org.ofbiz.onlinestations", helperOnlineStations));
		//If not exist return no data
		try {
			sqlproc.prepareStatement("SELECT * FROM expected where ((employercode = ?) and (Month = ?) and (SE = 'REQ'))");

			sqlproc.setValue(employerCode);
			sqlproc.setValue(month);
			//	sqlproc.set("employercode", employerCode);
		} catch (GenericDataSourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResultSet rsExpected = null;
		try {
		 rsExpected = sqlproc.executeQuery();
		} catch (GenericDataSourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GenericValue member = null;
		String names = "";
		//If already pushed say data already pulled
		for (GenericValue genericValue : expectedPaymentSentELI) {
			//Add this to the exp
			member = LoanUtilities.getMemberGivenPayrollNumber(genericValue.getString("payrollNo"));
			names = getNames(member);
			try {
				sqlproc.prepareStatement("INSERT INTO expected ([employercode], [Month], [Loan No], [Emp No], [Rem Code], [Amount], [Rem code Description], [PayrollNo], [Name], [status], [SE] ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				sqlproc.setValue(employerCode);
				sqlproc.setValue(month);
				sqlproc.setValue(genericValue.getString("loanNo").trim());
				sqlproc.setValue(member.getString("employeeNumber"));
				sqlproc.setValue(genericValue.getString("remitanceCode"));
				sqlproc.setValue(genericValue.getBigDecimal("amount"));
				sqlproc.setValue(genericValue.getString("remitanceDescription"));
				sqlproc.setValue(member.getString("payrollNumber"));
				sqlproc.setValue(names);
				sqlproc.setValue("Active");
				sqlproc.setValue("REQ");
			} catch (GenericDataSourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				sqlproc.executeUpdate();
			} catch (GenericDataSourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return "success";
	}
	
	
	private static List<GenericValue> getExpectedPaymentSentList(
			String employerCode, String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentSentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedPaymentSentConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return expectedPaymentReceivedELI;
	}

	private static GenericValue getPushMonthYearItem(Long pushMonthYearItemId) {
		GenericValue pushMonthYearItem = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			pushMonthYearItem = delegator.findOne("PushMonthYearItem",
					UtilMisc.toMap("pushMonthYearItemId", pushMonthYearItemId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return pushMonthYearItem;
	}

	public static String pullStation(Map<String, String> userLogin, Long pushMonthYearItemId){
		
		//Get PullMonthYearItem item
		GenericValue pullMonthYearItem = getPullMonthYearItem(pushMonthYearItemId);
		
		//Get station from the PullMonthYearItem and see if it exists
		GenericValue station = LoanUtilities.getStation(pullMonthYearItem.getString("stationId"));
		String employerCode = station.getString("employerCode");
		
		//Concatenate Month and Year to get month to use in getting the data
		GenericValue pullMonthYear = getPullMonthYear(pullMonthYearItem.getLong("pushMonthYearId"));
		
		
		String month = pullMonthYear.getLong("month").toString()+pullMonthYear.getLong("year").toString();
		month = month.trim();
		
		//org.ofbiz.onlinestations
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		String helperOnlineStations = delegator.getGroupHelperName("org.ofbiz.onlinestations");
		
		SQLProcessor sqlproc = new SQLProcessor(new GenericHelperInfo("org.ofbiz.onlinestations", helperOnlineStations));
		//If not exist return no data
		try {
			sqlproc.prepareStatement("SELECT * FROM expected where ((employercode = ?) and (Month = ?) and (SE = 'Rem'))");

			sqlproc.setValue(employerCode);
			sqlproc.setValue(month);
			//	sqlproc.set("employercode", employerCode);
		} catch (GenericDataSourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResultSet rsExpected = null;
		try {
		 rsExpected = sqlproc.executeQuery();
		} catch (GenericDataSourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int count = 0;
		try {
			while((rsExpected != null) && (rsExpected.next())){
				//Get the pulled item and add to list
				count = count + 1;
				log.info(count+" Employer Code "+rsExpected.getString("employercode")+" Month "+rsExpected.getDouble("Month")+" Amount "+rsExpected.getDouble("Amount"));
				
				String payrollNo = rsExpected.getString("PayrollNo");
				String remitanceCode = rsExpected.getString("Rem Code");
				String loanNo = rsExpected.getString("Loan No");
				String remitanceDescription = rsExpected.getString("Rem code Description");
				
				createExpectation(station, month, (String)userLogin.get("userLoginId"), rsExpected.getDouble("Amount"), payrollNo, remitanceCode, loanNo, remitanceDescription);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//If already pulled say data already pulled
		
		return "success";
	}

	private static void createExpectation(GenericValue station, String month,
			String userLoginId, double amount, String payrollNo, String remitanceCode, String loanNo, String remitanceDescription) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		String branchId = station.getString("branchId");
		String stationNumber = station.getString("stationNumber").trim();
		String stationName = station.getString("name");
		String employerCode = station.getString("employerCode");
		// String employerCode = station.getString("employerCode");
		String employerName = station.getString("employerName").trim();
		
		GenericValue expectedPaymentReceived = null;
		//GenericValue member = LoanUtilities.getMemberGivenPayrollNumber(payrollNo);
		GenericValue member = LoanUtilities.getMemberGivenEmployeeNumber(payrollNo);
		
		String employeeNames = getNames(member);
		
		expectedPaymentReceived = delegator.makeValue("ExpectedPaymentReceived",
				UtilMisc.toMap("isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						remitanceCode, "stationNumber", stationNumber,
						"stationName", stationName,

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", employerCode, "employeeNumber",
						member.getString("employeeNumber"), "memberNumber",
						member.getString("memberNumber"),

						"loanNo", loanNo, "employerNo", employerName, "amount",
						amount, "remitanceDescription",
						remitanceDescription, "employeeName",
						employeeNames, "expectationType",
						"", "month", month));
		
		//accountProduct.getString("code")
		try {
			delegator.createOrStore(expectedPaymentReceived);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
	}

	private static GenericValue getPullMonthYear(Long pushMonthYearId) {
		GenericValue pushMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			pushMonthYear = delegator.findOne("PushMonthYear",
					UtilMisc.toMap("pushMonthYearId", pushMonthYearId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return pushMonthYear;
	}

	private static GenericValue getPullMonthYearItem(Long pushMonthYearItemId) {
		GenericValue pullMonthYearItem = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			pullMonthYearItem = delegator.findOne("PullMonthYearItem",
					UtilMisc.toMap("pushMonthYearItemId", pushMonthYearItemId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return pullMonthYearItem;
	}
	
	
	private static String getNames(GenericValue member) {
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

		return employeeNames;
	}

}
