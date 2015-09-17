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

//get all loan types
stations = delegator.findList('Station', null, null,null,null,false)
context.stations = stations
loanApps = delegator.findList('LoansByStations', null, null,null,null,false)
context.loanApps = loanApps
