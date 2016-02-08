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
	
	//exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	//expr = exprBldr.AND() {
	//			EQUALS(quarter : quarterSearch)
	//			EQUALS(stage : 'HOD')
	//		}
	
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
	
	//context.importedListELI = importedListELI
	context.importedListExp = importedListExp
