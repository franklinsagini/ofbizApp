import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 
	
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	
	stationId = parameters.stationId
	stationLong = stationId.toLong();
	
	def collectorResults = []
	
	def totalAmount = BigDecimal.ZERO;
	def totalAmountStr = "";
	
	  expre = exprBldr.AND() {
			EQUALS(stationId : stationId)
		}
	
	
	stationName = delegator.findList("Station", expre,null,null, null, false)
	stationName.eachWithIndex{ stationame ,index ->
	   nameOfStation = stationame.name
	   stationCode = stationame.employerCode
	   
	   context.nameOfStation = nameOfStation
	   context.stationCode = stationCode
	}
	
	
	
	
	
	
	
    BigDecimal zero = BigDecimal.ZERO

     express = exprBldr.AND() {
			EQUALS(stationId : stationLong)
			GREATER_THAN(transactionAmount : zero)
		}
		
	stationNames = delegator.findList("StationAccountTransaction", express,null,null, null, false)
	stationNames.eachWithIndex{stationlist, index ->
	         transactionId = stationlist.stationAccountTransactionId	
         	transactionDate = stationlist.createdStamp
         	chequeNo = stationlist.chequeNumber
         	descriptor = stationlist.monthyear
         	transactionAmount = stationlist.transactionAmount
         	description = "cheque for (month and Year) "+descriptor
  
         	collectorResults.add([transactionId : transactionId,transactionDate: transactionDate,chequeNo : chequeNo,description : description,
         	transactionAmount : transactionAmount]);
         	
         	 totalAmount = totalAmount + transactionAmount.toBigDecimal();
			totalAmountStr = totalAmount.toString();
         
	}
	
	context.collectorResults = collectorResults
	  context.totalAmountStr = totalAmountStr
