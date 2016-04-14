	import org.ofbiz.entity.util.EntityUtil;
	import org.ofbiz.entity.condition.EntityCondition;
	import org.ofbiz.entity.condition.EntityConditionBuilder;
	import org.ofbiz.entity.condition.EntityConditionList;
	import org.ofbiz.entity.condition.EntityExpr;
	import org.ofbiz.entity.condition.EntityOperator;
	import org.ofbiz.base.util.UtilDateTime;
	import org.ofbiz.entity.util.EntityFindOptions;
	import java.text.SimpleDateFormat; 
	
	
	
	startDate =parameters.fromDate
	noww = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(startDate);
	startsDateSql = new java.sql.Date(noww.getTime());
	startDateTimestamp = new Timestamp(startsDateSql.getTime());
	
	endDate =parameters.thruDate
	noww = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(endDate);
	endDateSQl= new java.sql.Date(noww.getTime());
	endDateTimestamp = new Timestamp(endDateSQl.getTime());
	
	context.fd = startDate
	context.ld = endDate
	
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	expr = exprBldr.AND() {
				
				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			}
			
     def freedGuarantorBetwween = []			
	//EntityFindOptions findOptions = new EntityFindOptions();
	//findOptions.setMaxRows(100);
	
	def loanee = null
	def guarantorName = null
	
	guarantorList = delegator.findList("LoanGuarantorRemoved", expr, null, ["createdStamp DESC"], null, false);
	
	guarantorList.eachWithIndex{guarantorList, index ->
            loanNo = guarantorList.loanApplicationId
            
            if(loanNo){
               loanee = org.ofbiz.guarantormanagement.GuarantorManagementServices.getLoaneeNameGivenLoanApplicationId(loanNo);
            }else{
              loanee = null;
            }
            
            loanGuarantorId = guarantorList.removedGuarantorId
            
             if(loanGuarantorId){
              guarantorName= org.ofbiz.guarantormanagement.GuarantorManagementServices.getLoanGuarantorNameGivenId(loanGuarantorId);
            }else{
              guarantorName = null;
            }
           
           
            dateFreed = guarantorList.createdStamp
            freedBy = guarantorList.createdBy
           
           freedGuarantorBetwween.add([loanNo:loanNo,dateFreed:dateFreed,loanee:loanee,guarantorName:guarantorName,freedBy:freedBy]);
	}
	
	context.freedGuarantorBetwween = freedGuarantorBetwween
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
