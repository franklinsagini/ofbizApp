/**
 * 
 */
package org.ofbiz.remittancereports;

import java.util.List;

import javolution.util.FastList;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;


/**
 * @author samoei
 *
 */
public class RemittanceUtilityServices {
	
	
	public static final String module = RemittanceUtilityServices.class.getName();
	
	public static List<GenericValue>fetchReceivedStations(Delegator delegator, String monthYear, Boolean isReceived){
		List<GenericValue>stations = null;
		List<GenericValue>receivedStations = null;
		
		//Get all Received Stations for month year
		EntityConditionList<EntityExpr> importedStationsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("month", EntityOperator.EQUALS, monthYear)
				), EntityOperator.AND);
		try {
			receivedStations = delegator.findList("ExpectedPaymentReceived", importedStationsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		List<String> stationNumbers = FastList.newInstance();
		// Create a list of station numbers from the received stations
		for (GenericValue received : receivedStations) {
			stationNumbers.add(received.getString("stationNumber"));
		}
		EntityConditionList<EntityExpr> receivedStationsCond = null;
		if (isReceived) {
			//Get all Received Stations for month year
			receivedStationsCond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("stationNumber", EntityOperator.IN, stationNumbers)
					), EntityOperator.AND);
		}else {
			//Get all NOT Received Stations for month year
			receivedStationsCond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("stationNumber", EntityOperator.NOT_IN, stationNumbers)
					), EntityOperator.AND);
		}

		
		try {
			stations = delegator.findList("Station", receivedStationsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}		
		
		
		return stations;
	}
	

	private static List<GenericValue> getStations(Delegator delegator) {
		List<GenericValue>stations = null;
		try {
			stations = delegator.findList("Station", null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return stations;
	}
	
	public static List<String>getStationNumbers(Delegator delegator){
		List<String> stationNumbers = FastList.newInstance();
		
		List<GenericValue> stations = getStations(delegator);
		
		for (GenericValue station : stations) {
			stationNumbers.add(station.getString("stationNumber"));
		}
		
		return stationNumbers;
	}

}
