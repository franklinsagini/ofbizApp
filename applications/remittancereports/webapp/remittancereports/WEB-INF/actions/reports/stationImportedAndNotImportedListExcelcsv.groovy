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
		
	    selectEntityCondition = EntityCondition.makeCondition([ EntityCondition.makeCondition("isActive", EntityOperator.EQUALS, "Y"),
	     														 EntityCondition.makeCondition("stationNumber", EntityOperator.EQUALS, station.stationNumber),
	                                                             EntityCondition.makeCondition("month", EntityOperator.EQUALS, monthYear)],EntityOperator.AND);

		tempImportedListELI = delegator.findList("ExpectedPaymentReceived", selectEntityCondition, null, selectOrder, null, false);
		
        stationCount = 0;
        tempImportedListELI.each { epr ->
            stationCount = stationCount + 1;
            if (stationCount==1) {
				eprBuilder = [
	            stationNumber : epr.stationNumber,
	            stationName : epr.stationName,
	            createdBy : epr.createdBy,
	            dateImported : epr.createdStamp
				                                    ]            
		    	importedListExp.add(eprBuilder);
            }
        }
		
	}
	
	context.importedListExp = importedListExp


    def importedListExpp = []

/// 	Not Imported
	stationListNImported = delegator.findList("Station", null, null,null,null,false)
	
	stationListNImported.each { stationNim ->
	
		selectOrderr = ["createdStamp DESC"];
		
	    selectEntityConditionOne= EntityCondition.makeCondition([ EntityCondition.makeCondition("isActive", EntityOperator.EQUALS, "Y"),
	     														 EntityCondition.makeCondition("stationNumber", EntityOperator.NOT_EQUAL, stationNim.stationNumber),
	                                                             EntityCondition.makeCondition("month", EntityOperator.EQUALS, monthYear)],EntityOperator.AND);

		tempImportedListELINImported = delegator.findList("ExpectedPaymentReceived", selectEntityConditionOne, null, selectOrderr, null, false);
		
        stationCountt = 0;
        tempImportedListELINImported.each { eprr ->
            stationCountt = stationCountt + 1;
            if (stationCountt==1) {
				eprBuilderr = [
	            stationNumberr : eprr.stationNumber,
	            stationNamee : eprr.stationName,
	            createdByy : eprr.createdBy,
	            dateImportedd : eprr.createdStamp
				]            
		    	importedListExpp.add(eprBuilderr);
            }
        }
		
	}
	
	context.importedListExpp = importedListExpp
	
	