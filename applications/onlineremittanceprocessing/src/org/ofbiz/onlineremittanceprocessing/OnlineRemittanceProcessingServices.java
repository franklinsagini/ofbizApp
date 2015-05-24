package org.ofbiz.onlineremittanceprocessing;

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
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
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
		
		//Get station from the PushMonthYear and see if it exists
		
		//If not exist return no data
		
		//If already pushed say data already pulled
		
		return "success";
	}
	
	
	public static String pullStation(Map<String, String> userLogin, Long pushMonthYearItemId){
		
		//Get PullMonthYearItem item
		
		//Get station from the PullMonthYearItem and see if it exists
		
		//If not exist return no data
		
		//If already pulled say data already pulled
		
		return "success";
	}

}
