package org.ofbiz.onlineremittanceprocessing;

import java.math.BigDecimal;
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
import org.ofbiz.accountholdertransactions.RemittanceServices;
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

	public static String SYSTEMSHARECAPITALCODE = "902";
	public static String ONLINESTATIONSHARECAPITALCODE = "D136";

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
	public static String pushAllStations(Map<String, String> userLogin,
			Long pushMonthYearId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String userLoginId = userLogin.get("userLoginId");

		log.info(" The push is being done by ///////////////// " + userLoginId);
		log.info(" The Pull pushMonthYear is ................ "
				+ pushMonthYearId);

		return "success";
	}

	public static String pullAllStations(Map<String, String> userLogin,
			Long pushMonthYearId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String userLoginId = userLogin.get("userLoginId");

		log.info(" The Pull is being done by ................ " + userLoginId);
		log.info(" The Pull pushMonthYear is ................ "
				+ pushMonthYearId);
		return "success";
	}

	public static String createPushAndPullStationItems(Long pushMonthYearId,
			Map<String, String> userLogin) {

		// Create the PushMonthYearItem
		log.info("Will be creating PUSH Records");
		List<GenericValue> onlineStationItemList = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		onlineStationItemList = getOnlineStations();

		List<GenericValue> pushItemList = new ArrayList<GenericValue>();

		String userLoginId = (String) userLogin.get("userLoginId");
		for (GenericValue genericValue : onlineStationItemList) {
			// Create a PUSH Reccord Item
			pushItemList.add(createPushRecord(pushMonthYearId, genericValue,
					userLoginId));
		}

		// Save the PUSH Reccords

		try {
			delegator.storeAll(pushItemList);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<GenericValue> pullItemList = new ArrayList<GenericValue>();
		// Create the PullMonthYearItem
		for (GenericValue genericValue : onlineStationItemList) {
			// Create a PULL Record Item
			pullItemList.add(createPullRecord(pushMonthYearId, genericValue,
					userLoginId));
		}

		// Save the PULL Records
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

		Long pullMonthYearItemId = delegator.getNextSeqIdLong(
				"PullMonthYearItem", 1);

		String stationId = genericValue.getString("stationId");
		GenericValue station = LoanUtilities.getStation(stationId);

		// String employerCode = station.getString("employerCode");
		String onlineCode = station.getString("Onlinecode");
		String employerName = station.getString("employerName");

		pullMonthYearItem = delegator.makeValidValue("PullMonthYearItem",
				UtilMisc.toMap("pullMonthYearItemId", pullMonthYearItemId,
						"isActive", "Y", "createdBy", userLoginId,
						"pushMonthYearId", pushMonthYearId, "stationId",
						stationId,

						"employerCode", onlineCode, "employerName",
						employerName, "Onlinecode", onlineCode, "pulled", "N"));

		return pullMonthYearItem;
	}

	private static GenericValue createPushRecord(Long pushMonthYearId,
			GenericValue genericValue, String userLoginId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue pushMonthYearItem = null;

		Long pushMonthYearItemId = delegator.getNextSeqIdLong(
				"PushMonthYearItem", 1);

		String stationId = genericValue.getString("stationId");
		GenericValue station = LoanUtilities.getStation(stationId);

		// String employerCode = station.getString("employerCode");
		String onlineCode = station.getString("Onlinecode");
		String employerName = station.getString("employerName");

		pushMonthYearItem = delegator.makeValidValue("PushMonthYearItem",
				UtilMisc.toMap("pushMonthYearItemId", pushMonthYearItemId,
						"isActive", "Y", "createdBy", userLoginId,
						"pushMonthYearId", pushMonthYearId,

						"stationId", stationId,

						"employerCode", onlineCode, "employerName",
						employerName,

						"Onlinecode", onlineCode,

						"pushed", "N"));

		return pushMonthYearItem;
	}

	private static List<GenericValue> getOnlineStations() {
		// TODO Auto-generated method stub
		// OnlineStationItem
		List<GenericValue> onlineStationItemELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			onlineStationItemELI = delegator.findList("OnlineStationItem",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return onlineStationItemELI;
	}

	public static String pushStation(Map<String, String> userLogin,
			Long pushMonthYearItemId) {

		// Get PushMonthYearItem item
		GenericValue pushMonthYearItem = getPushMonthYearItem(pushMonthYearItemId);
		// Get station from the PushMonthYear and see if it exists
		// Get station from the PullMonthYearItem and see if it exists
		GenericValue station = LoanUtilities.getStation(pushMonthYearItem
				.getString("stationId"));
		// String employerCode = station.getString("employerCode");
		String onlinecode = station.getString("Onlinecode");
		onlinecode = onlinecode.trim();

		// Concatenate Month and Year to get month to use in getting the data
		GenericValue pullMonthYear = getPullMonthYear(pushMonthYearItem
				.getLong("pushMonthYearId"));

		String month = pullMonthYear.getLong("month").toString()
				+ pullMonthYear.getLong("year").toString();
		month = month.trim();

		// The Month Data for the employerCode should be missing on the
		// destination side
		// (Send Once)
		// TODO
		Boolean alreadyPushed = false;
		alreadyPushed = stationAlreadyPushed(onlinecode, month);
		if (alreadyPushed)
			return "alreadypushed";

		// If not exist return no data
		List<GenericValue> expectedPaymentSentELI = null;
		expectedPaymentSentELI = getExpectedPaymentSentList(onlinecode,
				station.getString("employerCode"), month);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String helperOnlineStations = delegator
				.getGroupHelperName("org.ofbiz.onlinestations");

		SQLProcessor sqlproc = new SQLProcessor(new GenericHelperInfo(
				"org.ofbiz.onlinestations", helperOnlineStations));
		// If not exist return no data

		GenericValue member = null;
		String names = "";
		String systemShareCapitalCode = "";
		String onlineShareCapitalCode = "";
		// If already pushed say data already pulled
		for (GenericValue genericValue : expectedPaymentSentELI) {
			// Add this to the exp
			member = LoanUtilities.getMemberGivenPayrollNumber(genericValue
					.getString("payrollNo"));
			names = getNames(member);
			try {
				sqlproc.prepareStatement("INSERT INTO expected ([employercode], [Month], [Loan No], [Emp No], [Rem Code], [Amount], [Rem code Description], [PayrollNo], [Name], [status], [SE] ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				sqlproc.setValue(onlinecode);
				sqlproc.setValue(month);
				sqlproc.setValue(genericValue.getString("loanNo").trim());
				sqlproc.setValue(member.getString("employeeNumber"));

				// if remittance code is the share capital code then
				systemShareCapitalCode = getSystemShareCapitalCode();

				if ((systemShareCapitalCode == null)
						|| (systemShareCapitalCode.equals("")))
					systemShareCapitalCode = SYSTEMSHARECAPITALCODE;

				onlineShareCapitalCode = getOnlineShareCapitalCode();
				if ((onlineShareCapitalCode == null)
						|| (onlineShareCapitalCode.equals("")))
					onlineShareCapitalCode = ONLINESTATIONSHARECAPITALCODE;

				// Check if the code in the data being processes is a code of
				// intrest - the share capital code in this case
				if (genericValue.getString("remitanceCode").trim()
						.equals(systemShareCapitalCode.trim())) {
					// Replace the code with the online code
					sqlproc.setValue(onlineShareCapitalCode);
				} else {
					// Leave the code as is
					sqlproc.setValue(genericValue.getString("remitanceCode"));
				}

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

		// Update ,
		pushMonthYearItem.set("pushed", "Y");
		try {
			delegator.createOrStore(pushMonthYearItem);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static String getSystemShareCapitalCode() {
		GenericValue stationProductCodeMap = null;
		List<GenericValue> stationProductCodeMapELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> stationCodeMapOrderFields = new ArrayList<String>();
		stationCodeMapOrderFields.add("-stationProductCodeMapId");
		try {
			stationProductCodeMapELI = delegator.findList(
					"StationProductCodeMap", null, null,
					stationCodeMapOrderFields, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (stationProductCodeMapELI == null)
			return null;
		if (stationProductCodeMapELI.size() < 1)
			return null;

		stationProductCodeMap = stationProductCodeMapELI.get(0);
		return stationProductCodeMap.getString("systemShareCapitalCode");
	}

	private static String getOnlineShareCapitalCode() {
		GenericValue stationProductCodeMap = null;
		List<GenericValue> stationProductCodeMapELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> stationCodeMapOrderFields = new ArrayList<String>();
		stationCodeMapOrderFields.add("-stationProductCodeMapId");
		try {
			stationProductCodeMapELI = delegator.findList(
					"StationProductCodeMap", null, null,
					stationCodeMapOrderFields, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (stationProductCodeMapELI == null)
			return null;
		if (stationProductCodeMapELI.size() < 1)
			return null;

		stationProductCodeMap = stationProductCodeMapELI.get(0);
		return stationProductCodeMap.getString("onlineShareCapitalCode");
	}

	private static Boolean stationAlreadyPushed(String employerCode,
			String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String helperOnlineStations = delegator
				.getGroupHelperName("org.ofbiz.onlinestations");

		log.info(" Attempting to check if pushed !!! ");

		SQLProcessor sqlproc = new SQLProcessor(new GenericHelperInfo(
				"org.ofbiz.onlinestations", helperOnlineStations));
		// If not exist return no data
		try {
			sqlproc.prepareStatement("SELECT * FROM expected where ((employercode = ?) and (Month = ?) and (SE = 'REQ'))");

			sqlproc.setValue(employerCode);
			sqlproc.setValue(month);
			// sqlproc.set("employercode", employerCode);
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

		try {
			if (rsExpected.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private static List<GenericValue> getExpectedPaymentSentList(
			String onlinecode, String employerCode, String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		// String employerCode
		EntityConditionList<EntityExpr> expectedPaymentSentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedPaymentSentConditions, null,
					null, null, false);

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
					UtilMisc.toMap("pushMonthYearItemId", pushMonthYearItemId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return pushMonthYearItem;
	}

	public static String pullStation(Map<String, String> userLogin,
			Long pushMonthYearItemId) {

		// Get PullMonthYearItem item
		GenericValue pullMonthYearItem = getPullMonthYearItem(pushMonthYearItemId);

		// Get station from the PullMonthYearItem and see if it exists
		GenericValue station = LoanUtilities.getStation(pullMonthYearItem
				.getString("stationId"));
		String employerCode = station.getString("employerCode");
		String Onlinecode = station.getString("Onlinecode");
		Onlinecode = Onlinecode.trim();
		// Concatenate Month and Year to get month to use in getting the data
		GenericValue pullMonthYear = getPullMonthYear(pullMonthYearItem
				.getLong("pushMonthYearId"));

		String month = pullMonthYear.getLong("month").toString()
				+ pullMonthYear.getLong("year").toString();
		month = month.trim();

		// The Month Data for the Month Must not have been received
		// TODO
		Boolean alreadyPulled = false;
		log.info(" PPPPPPPPPPP Just before checking if pulled !!!");
		alreadyPulled = getStationAlreadyPulled(employerCode, month);
		if (alreadyPulled)
			return "alreadypulled";

		log.info(" PPPPPPPPPPP Now going to pull !!!");

		// org.ofbiz.onlinestations
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		String helperOnlineStations = delegator
				.getGroupHelperName("org.ofbiz.onlinestations");

		SQLProcessor sqlproc = new SQLProcessor(new GenericHelperInfo(
				"org.ofbiz.onlinestations", helperOnlineStations));
		// If not exist return no data
		try {
			sqlproc.prepareStatement("SELECT * FROM expected where ((employercode = ?) and (Month = ?) and (SE = 'Rem'))");

			sqlproc.setValue(Onlinecode);
			sqlproc.setValue(month);
			// sqlproc.set("employercode", employerCode);
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
		String onlineShareCapitalCode = "";
		String systemShareCapitalCode = "";
		
		Boolean missingMember = false;
		try {
			while ((rsExpected != null) && (rsExpected.next())) {
				// Get the pulled item and add to list
				count = count + 1;
				log.info(count + " Employer Code "
						+ rsExpected.getString("employercode") + " Month "
						+ rsExpected.getString("Month") + " Amount "
						+ rsExpected.getBigDecimal("Amount"));

				String onlineCode = rsExpected.getString("employercode");

				Float tempEmployeeNo = rsExpected.getFloat("Emp No");

				int intEmployeeNo = tempEmployeeNo.intValue();
				String employeeNumber = String.valueOf(intEmployeeNo);

				GenericValue member = null;
				member = LoanUtilities.getMemberGivenEmployeeNumber(
						employeeNumber, onlineCode);

				// if member is null, then there are missing members, please fix
				// them first

				String payrollNo = "";
				Boolean localMissing = false;
						
				if (member != null){
					payrollNo = member.getString("payrollNumber");
				} else{
					localMissing = true;
					missingMember = true;
					
					//Add a missing member (member number for station)
					//TODO
					//String employerCode = LoanUtilities.getS
					RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month, employerCode, rsExpected.getString("Rem Code"), String.valueOf(rsExpected.getLong("Loan No")), employeeNumber);
				}
				// rsExpected.getString("PayrollNo");
				// if remittance code is the share capital code then
				systemShareCapitalCode = getSystemShareCapitalCode();

				if ((systemShareCapitalCode == null)
						|| (systemShareCapitalCode.equals("")))
					systemShareCapitalCode = SYSTEMSHARECAPITALCODE;

				onlineShareCapitalCode = getOnlineShareCapitalCode();
				if ((onlineShareCapitalCode == null)
						|| (onlineShareCapitalCode.equals("")))
					onlineShareCapitalCode = ONLINESTATIONSHARECAPITALCODE;

				String remitanceCode = rsExpected.getString("Rem Code");
				// Check if the code in the data being processes is a code of
				// intrest - the share capital code in this case
				if (remitanceCode.trim().equals(onlineShareCapitalCode.trim())) {
					// Replace the code with the System Share Capital Code
					remitanceCode = systemShareCapitalCode;
				}

				String loanNo = String.valueOf(rsExpected.getLong("Loan No"));

				String remitanceDescription = rsExpected
						.getString("Rem code Description");

				if (!localMissing)
				createExpectation(station, month,
						(String) userLogin.get("userLoginId"),
						rsExpected.getBigDecimal("Amount"), payrollNo,
						remitanceCode, loanNo, remitanceDescription);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// If already pulled say data already pulled
		pullMonthYearItem.set("pulled", "Y");
		try {
			delegator.store(pullMonthYearItem);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (missingMember){
			//Delete records added for this station and month
			RemittanceServices.deleteReceivedPaymentBreakdown(userLogin, employerCode, month);
			// TODO
			return "failed";
		}
		return "success";
	}

	private static Boolean getStationAlreadyPulled(String employerCode,
			String month) {

		// List<GenericValue> expectedPaymentReceivedELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (expectedPaymentReceivedELI.size() > 0)
			return true;

		return false;
	}

	private static void createExpectation(GenericValue station, String month,
			String userLoginId, BigDecimal amount, String payrollNo,
			String remitanceCode, String loanNo, String remitanceDescription) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		String branchId = station.getString("branchId");
		String stationNumber = station.getString("stationNumber").trim();
		String stationName = station.getString("name");
		String employerCode = station.getString("employerCode");
		// String employerCode = station.getString("employerCode");
		String employerName = station.getString("employerName").trim();

		GenericValue expectedPaymentReceived = null;
		// GenericValue member =
		// LoanUtilities.getMemberGivenPayrollNumber(payrollNo);
		GenericValue member = LoanUtilities.getMemberGivenPayrollNumber(payrollNo);
				//.getMemberGivenEmployeeNumber(payrollNo);

		String employeeNames = getNames(member);

		expectedPaymentReceived = delegator.makeValue(
				"ExpectedPaymentReceived", UtilMisc.toMap("isActive", "Y",
						"branchId", member.getString("branchId"),
						"remitanceCode", remitanceCode, "stationNumber",
						stationNumber, "stationName", stationName,

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", employerCode.trim(), "employeeNumber",
						member.getString("employeeNumber"), "memberNumber",
						member.getString("memberNumber"),

						"loanNo", loanNo, "employerNo", employerCode,
						"employerName", employerName,
						"amount",
						amount, "remitanceDescription", remitanceDescription,
						"employeeName", employeeNames, "expectationType", "",
						"month", month.trim()));

		// accountProduct.getString("code")
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

	private static GenericValue getPullMonthYearItem(Long pullMonthYearItemId) {
		GenericValue pullMonthYearItem = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			pullMonthYearItem = delegator.findOne("PullMonthYearItem",
					UtilMisc.toMap("pullMonthYearItemId", pullMonthYearItemId),
					false);
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
	
	
	public static String generateTheMonthStationExpectation(Map<String, String> userLogin, Long pushMonthYearStationId){
		log.info(" ########## "+pushMonthYearStationId);
		GenericValue pushMonthYearStation = LoanUtilities.getEntityValue("PushMonthYearStation", "pushMonthYearStationId", pushMonthYearStationId);
		String stationId = pushMonthYearStation.getString("stationId");
		
		//Get Station Employer code
		GenericValue station = LoanUtilities.getEntityValue("Station", "stationId", stationId);
		
		String employerCode = station.getString("employerCode");
		
		//Get all stations with under this employer
		// String employerCode
		EntityConditionList<EntityExpr> employerStationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim())

				), EntityOperator.AND);
		
		List<GenericValue> stationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator.findList(
					"Station", employerStationConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		log.info("SSSSSSSSS The Stations ##########");
		for (GenericValue genericValue : stationELI) {
			String currentStationId = genericValue.getString("stationId");
			log.info(" IIIIIIII "+genericValue.getString("stationId"));
			
			//Get members and process
			processStationMembers(currentStationId, pushMonthYearStationId);
		}
		
		return "success";
	}

	/*******
	 * Process Station Members
	 * @author Japheth Odonya
	 * */
	private static void processStationMembers(String currentStationId,
			Long pushMonthYearStationId) {
		//Get all the members belonging to this station
		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationId", EntityOperator.EQUALS,
						Long.valueOf(currentStationId.trim())),
						
					EntityCondition.makeCondition("memberStatusId", EntityOperator.EQUALS , LoanUtilities.getMemberStatusId("ACTIVE"))	

				), EntityOperator.AND);
		
		
		List<GenericValue> memberELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList(
					"Member", memberConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		log.info(" The Active Members Count is "+memberELI.size());
		for (GenericValue member : memberELI) {
			RemittanceServices.addMemberExpectedAccountContributions(member, pushMonthYearStationId);
		}
		
	}

}
