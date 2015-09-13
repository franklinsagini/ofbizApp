import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;


context.startDate = startDate
context.endDate = endDate
loanProductId = parameters.loanProductId

println "####################### START DATE: "+ startDate
println "####################### END DATE: "+ endDate


//get all loan types
loanTypes = delegator.findList('LoanProduct', null, null,null,null,false)

//acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, ["createdTxStamp"], null, false)


//get all loans
summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate));
loanApps = delegator.findList('LoanApplication',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["createdStamp"],null,false)
context.loanApps = loanApps
//No Loan Product Selected
if (!loanProductId) {
context.loanTypes = loanTypes
}
