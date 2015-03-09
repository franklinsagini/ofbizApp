package org.ofbiz.stationtransfer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

public class StationTransferServices {
	public static Logger log = Logger.getLogger(StationTransferServices.class);

	

	public static String tranferStation(String stationId) {
		
		if (stationId == null){
			log.error("############### No station ID");
			return "NO Station";
		}
		
		
		GenericValue station = LoanUtilities.getStation(stationId);
		
		//Get the station employer code
		String employerCode = station.getString("employerCode");
		String newBranchId = station.getString("newBranchId");
		
		if (newBranchId == null){
			log.error("############### No New Branch ID");
			return "No New Branch";
		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//Get all the station Ids for the employer code above
		List<String> listStationIds = LoanUtilities.getStationIds(employerCode);	
		//Change all the members whose stations are in the station Ids above to the 
		//new branch Id
		String fromBranchId = station.getString("branchId");
		int count = 0;
		for (String tempStationId : listStationIds) {
			
			log.info("############### Going through Stations ");
			
			//Update all the members for this station to new Branch
			count = count + updateMemberBranches(tempStationId, newBranchId);
			
			//Update Station to new Branch
			//updateStationBranch(tempStationId, newBranchId);
			GenericValue stationProcessed = LoanUtilities.getStation(tempStationId);
			stationProcessed.set("branchId", newBranchId);
			
			
			try {
				delegator.createOrStore(stationProcessed);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		//Create Log
		GenericValue stationTransfer;
		Long stationTransferId = delegator.getNextSeqIdLong("StationTransfer", 1);
		//loanApplicationId = loanApplicationId.replaceAll(",", "");
		stationTransfer = delegator.makeValue("StationTransfer", UtilMisc.toMap(
				"stationTransferId", stationTransferId, 
				"employerCode",	station.getString("employerCode").trim(),
				
				"stationId", station.getString("stationId"),
				
				"fromBranchId", fromBranchId,
				"toBranchId", newBranchId,
				
				"comments", " Moved "+count+" Members from "+LoanUtilities.getBranchName(fromBranchId)+" to "+LoanUtilities.getBranchName(newBranchId)));

		try {
			delegator.createOrStore(stationTransfer);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		return "SUCCESS";

	}





	private static int updateMemberBranches(String tempStationId,
			String newBranchId) {
		
		log.info("############### Changing Members !!!!!!!!  ");
		tempStationId = tempStationId.replaceAll(",", "");
		Long stationId = Long.valueOf(tempStationId);
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("stationId", stationId),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> toUpdateMembers = new ArrayList<GenericValue>();
		
		int count = 0;
		for (GenericValue genericValue : memberELI) {
			//Update the Member Branch ID
			genericValue.set("branchId", newBranchId);
			toUpdateMembers.add(genericValue);
			count = count + 1;
		}
		
		try {
			delegator.storeAll(toUpdateMembers);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
		
	}

}