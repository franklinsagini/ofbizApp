import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 


quarterSearch = parameters.quarter

quarterSearchU= quarterSearch.toUpperCase();
context.quarterSearchU = quarterSearchU


now = Calendar.getInstance().getTime().toString();
context.now = now

//exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

//expr = exprBldr.AND() {
//			EQUALS(quarter : quarterSearch)
//			EQUALS(stage : 'FORWARDED')
//		}


ecl = EntityCondition.makeCondition([
                                    EntityCondition.makeCondition("quarter", EntityOperator.EQUALS, quarterSearch),
                                    EntityCondition.makeCondition("stage", EntityOperator.EQUALS, "FORWARDED")],
                                EntityOperator.AND);
 osisFindOptions = new EntityFindOptions();
 osisOrder = ["partyId"];	
 osisFields = ["partyId", "quarter", "stage", "hod", "lastUpdatedTxStamp"] as Set;
 osisFindOptions.setDistinct(true);
orderPaymentPreferences = delegator.findList("PerfPartyReview", ecl, osisFields, osisOrder, osisFindOptions, false);
context.activities = orderPaymentPreferences
//context.activities = delegator.findList("PerfPartyReview", expr, null, null, null, false);
 
 //DateDoneOfPartyId = orderPaymentPreferences.partyId
 
 //DateDoneContext = delegator.findList("PerfPartyReview", [partyId : DateDoneOfPartyId],null,null,null, false);
 //context.DateDoneContext = DateDoneContext
 