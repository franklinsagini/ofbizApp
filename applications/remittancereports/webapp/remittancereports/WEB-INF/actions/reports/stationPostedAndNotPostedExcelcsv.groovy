	import org.ofbiz.entity.util.EntityUtil;
	import org.ofbiz.entity.condition.EntityCondition;
	import org.ofbiz.entity.condition.EntityConditionBuilder;
	import org.ofbiz.entity.condition.EntityConditionList;
	import org.ofbiz.entity.condition.EntityExpr;
	import org.ofbiz.entity.condition.EntityOperator;
	import org.ofbiz.base.util.UtilDateTime;
	import org.ofbiz.entity.util.EntityFindOptions;
	import java.text.SimpleDateFormat; 
	
	monthYear = parameters.month
	context.monthYear = monthYear
	
	
	now = Calendar.getInstance().getTime().toString();
	context.now = now
	
	
	def importedListExp = []
	
	stationList = delegator.findList("Station", null, null,null,null,false)
	
	stationList.each { station ->
	
		selectOrder = ["createdStamp DESC"];
		
	    selectEntityCondition = EntityCondition.makeCondition([
	     														 EntityCondition.makeCondition("stationId", EntityOperator.EQUALS, station.stationId.toLong()),
	                                                             EntityCondition.makeCondition("monthyear", EntityOperator.EQUALS, monthYear)],EntityOperator.AND);

		tempImportedListELI = delegator.findList("StationAccountTransaction", selectEntityCondition, null, selectOrder, null, false);
		
        stationCount = 0;
        tempImportedListELI.each { epr ->
            stationCount = stationCount + 1;
            if (stationCount==1) {
                def stationIdUse = epr.stationId.toString();
				eprBuilder = [
				            stationNumber : org.ofbiz.humanres.LeaveServices.getStationNumber(stationIdUse) ,
				            stationName : org.ofbiz.humanres.LeaveServices.getStation(stationIdUse),
				            createdBy : epr.createdBy,
				            dateImported : epr.createdStamp
				             ]            
		    	importedListExp.add(eprBuilder);
            }
        }
		
	}
	
	context.importedListExp = importedListExp
	
	
	
	
	/////////////////
	
	
	def importedListExpp = []
	
	stationListt = delegator.findList("Station", null, null,null,null,false)
	
	stationListt.each { stationn ->
	
		selectOrderr = ["createdStamp DESC"];
		
	    selectEntityConditionn = EntityCondition.makeCondition([
	     														 EntityCondition.makeCondition("stationId", EntityOperator.NOT_EQUAL, stationn.stationId.toLong()),
	                                                             EntityCondition.makeCondition("monthyear", EntityOperator.EQUALS, monthYear)],EntityOperator.AND);

		tempImportedListELII = delegator.findList("StationAccountTransaction", selectEntityConditionn, null, selectOrderr, null, false);
		
        stationCountt = 0;
        tempImportedListELII.each { eprr ->
            stationCountt = stationCountt + 1;
            if (stationCountt==0) {
                def stationIdUsee = eprr.stationId.toString();
				eprBuilderr = [
				            stationNumber : org.ofbiz.humanres.LeaveServices.getStationNumber(stationIdUsee) ,
				            stationName : org.ofbiz.humanres.LeaveServices.getStation(stationIdUsee),
				            createdBy : eprr.createdBy,
				            dateImported : eprr.createdStamp
				             ]            
		    	importedListExpp.add(eprBuilderr);
            }
        }
		
	}
	
	context.importedListExpp = importedListExpp
	

	
