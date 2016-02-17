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
	
	def importedListExpp = []
	
	def importedListExppp = []
	
	stationList = delegator.findList("Station", null, null,null,null,false)
	
	stationList.each { station ->
	
		selectOrder = ["createdStamp DESC"];
		
	    selectEntityCondition = EntityCondition.makeCondition([
	     														 EntityCondition.makeCondition("stationNumber", EntityOperator.EQUALS, station.stationNumber),
	                                                             EntityCondition.makeCondition("month", EntityOperator.EQUALS, monthYear)],EntityOperator.AND);

		tempImportedListELI = delegator.findList("ExpectedPaymentReceived", selectEntityCondition, null, selectOrder, null, false);
		
           stationCount = 0;
               tempImportedListELI.each { epr ->
                 if(epr.stationNumber != station.stationNumber){
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
	}
	
	def  Ids = []
	
	Ids.add(importedListExp);
	
	println("*********************## my IDS  ##************"+Ids.stationNumber);

	   // selectEntityConditionLI = EntityCondition.makeCondition([EntityCondition.makeCondition("stationNumber", EntityOperator.NOT_EQUAL, id)]);
   
    stationListRwo = delegator.findList("Station",null,null,null,null,false)
                //stationCountt = 0;
	             stationListRwo.each{ stationCo ->
	                //if(Ids.contains(stationCo.stationNumber)){
	                //   stationCountt = stationCountt + 1;
	                             eprBuilderr = [
				              stationNumberr : stationCo.stationNumber,
				              stationNamee : stationCo.name
				            ]            
				    	importedListExpp.add(eprBuilderr);
	        // }
	  }
	
	def remList = []
	
	if(importedListExp.stationNumber != importedListExpp.stationNumberr ){
	       eprBuilderrr = [
				            stationNo : importedListExpp.stationNumberr,
				            stationN : importedListExpp.stationNamee
				          ]        
	                      remList.add(eprBuilderrr)       
	   }   
	
	println("*******************REM LIST************"+remList.stationNo);
	  	 
    context.importedListExp = importedListExp
   context.importedListExpp = remList
	
	