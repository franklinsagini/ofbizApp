import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

chequeNumber = parameters.chequeNumber
monthyear = parameters.monthyear
branchId = parameters.branchId
stationId = parameters.stationId
createdBy = parameters.createdBy

//Get Station Number
station = null;
globalEmployerCode = null;

if ((stationId != null) && (!stationId.equals(""))){
	station = delegator.findOne("Station", [stationId : stationId], false);
	globalEmployerCode = station.employerCode
	
	//Get Employers with defaulters
	stationList = delegator.findByAnd("Station",  [employerCode : globalEmployerCode], null, false);
	
} else{
	stationList = delegator.findByAnd("Station",  null, null, false);

}

listReceivedCheques = delegator.findByAnd("StationAccountTransaction",  null, null, false);



context.listReceivedCheques = listReceivedCheques;