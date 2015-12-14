import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;


import freemarker.ext.xml.Navigator.TypeOp;

import java.text.SimpleDateFormat;

import javolution.util.FastList;

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

loanApplicationIdString = parameters.loanApplicationId;
loanApplicationIdLong = loanApplicationIdString.toLong();

 rejectedLoan = delegator.findOne("LoanApplication", [loanApplicationId : loanApplicationIdLong], false);

   loanStatus = rejectedLoan.loanStatusId;
         
   partyId =   rejectedLoan.partyId;     
         
    // Get Date For Formatting
    
    dateCreated =  rejectedLoan.createdStamp;
    
    // Get loan Product 
    loanProductId = rejectedLoan.loanProductId;
    
    ///////
         
   loanStatusIdLong = loanStatus.toLong();
         
       expr = exprBldr.AND() {
				EQUALS(loanApplicationId : loanApplicationIdLong)
				EQUALS(loanStatusId : loanStatusIdLong)
		}
		
	EntityFindOptions findOptions = new EntityFindOptions();
	findOptions.setMaxRows(100);
         
    loanStatusRejection = delegator.findList("LoanStatusLog", expr, null, null, findOptions, false);

    loanStatusRejection.eachWithIndex{loanstatusLogItem , index ->
    
        rejComment = loanstatusLogItem.comment
    
    }
       
     context.loanStatusRejection = rejComment ;
         
     context.rejectedLoan = rejectedLoan;
  
  // get today and convert the request date
now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

java.sql.Date  todayy      = new java.sql.Date(dateCreated.getTime());
   
   context.today = today;
   context.todayy = todayy;
   
   
   stationOfMember = delegator.findOne("Member", [partyId : partyId], false); 
   
   getStationOfMember =  delegator.findOne("Station", [stationId : stationOfMember.stationId.toString()], false);
     
    context.getStationOfMember = getStationOfMember;   
    
  ////// Get The Loan Type
  
   productLoanType = delegator.findOne("LoanProduct", [loanProductId : loanProductId], false);
  
  context.loanType = productLoanType.name
  
  
  
    
     