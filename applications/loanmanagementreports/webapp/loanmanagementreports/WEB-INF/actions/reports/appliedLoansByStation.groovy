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
stationId = parameters.stationId

println "####################### START DATE: "+ startDate
println "####################### END DATE: "+ endDate
finalTransList = []
finalTransListBuilder = []

//acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, ["createdTxStamp"], null, false)


//get all loans
summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate));
loanApps = delegator.findList('LoanApplication',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["createdStamp"],null,false)
context.loanApps = loanApps
//No Loan Product Selected
if (!stationId) {
//get all loan types
stations = delegator.findList('Station', null, null,null,null,false)
context.stations = stations
}else{
//get all loan types
stationId = stationId.toLong()
loanTypeCondition = [];
loanTypeCondition.add(EntityCondition.makeCondition("stationId", EntityOperator.EQUALS, stationId));
stations = delegator.findList('Station', EntityCondition.makeCondition(loanTypeCondition, EntityOperator.AND), null,null,null,false)
context.stations = stations
}

stations.each { station ->
  loanApps.each { loanApp ->
    //get stationId
    member = loanApp.getRelated("Member", null, null, false)
    finalTransListBuilder = [
      loanNo:loanApp.loanNo,
      createdStamp:loanApp.createdStamp,
      memberNumber:loanApp.memberNumber,
      firstName:loanApp.firstName,
      lastName:loanApp.lastName,
      appliedAmt:loanApp.appliedAmt,
      loanStatusId:loanApp.loanStatusId,
      stationId:member.stationId.toString()
    ]

    finalTransList.add(finalTransListBuilder);

  }
}
context.finalTransList = finalTransList
