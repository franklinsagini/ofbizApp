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
loanStatusId = parameters.loanStatusId
stationId = parameters.stationId

println "####################### START DATE: "+ startDate
println "####################### END DATE: "+ endDate


summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
summaryCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate));
if (loanStatusId) {
  loanStatusIdL = loanStatusId.toLong()
  summaryCondition.add(EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, loanStatusIdL));
}
if (loanProductId) {
  loanProductIdL = loanProductId.toLong()
  summaryCondition.add(EntityCondition.makeCondition("loanProductId", EntityOperator.EQUALS, loanProductIdL));
}
if (stationId) {
  summaryCondition.add(EntityCondition.makeCondition("stationId", EntityOperator.EQUALS, stationId));
}
loanApps = delegator.findList('LoansByStations',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["createdStamp"],null,false)



context.loanApps = loanApps
