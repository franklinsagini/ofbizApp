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



memberStatusId = parameters.memberStatusId
employmentTypeId = parameters.employmentTypeId
genderId = parameters.genderId

println "####################### START DATE: "+ memberStatusId
println "####################### END DATE: "+ employmentTypeId
println "####################### END DATE: "+ genderId


summaryCondition = [];
memberStatusIdL = memberStatusId.toLong()
summaryCondition.add(EntityCondition.makeCondition("memberStatusId", EntityOperator.GREATER_THAN_EQUAL_TO, memberStatusIdL));
if (employmentTypeId) {
  employmentTypeIdL = employmentTypeId.toLong()
  summaryCondition.add(EntityCondition.makeCondition("employmentTypeId", EntityOperator.EQUALS, employmentTypeIdL));
}
if (genderId) {
  genderIdL = genderId.toLong()
  summaryCondition.add(EntityCondition.makeCondition("genderId", EntityOperator.EQUALS, genderIdL));
}

loanApps = delegator.findList('Member',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["createdStamp"],null,false)



context.loanApps = loanApps
