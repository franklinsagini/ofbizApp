import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 



onTraininglist = [];

now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN(startDate: today)
			GREATER_THAN(endDate: today)
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);

employees = delegator.findList("PartyAndTrainingEventsView", expr, null, ["startDate ASC"], findOptions, false);

employees.eachWithIndex { employeesItem, index ->
party = employeesItem.partyId
traintypeid = employeesItem.trainingTypeId
staff = delegator.findOne("Person", [partyId : party], false);
tr = delegator.findOne("TrainingTypes", [trainingTypeId : traintypeid], false);
fname = staff.firstName
sname = staff.lastName
name = "${staff.firstName} ${staff.lastName}";
event = employeesItem.eventName
trainingType = tr.trainingName
start = employeesItem.startDate
end = employeesItem.endDate
  
onTraininglist.add([name :name, event :event, trainingType : trainingType, start : start, end : end]);
}
	


context.onTraininglist = onTraininglist;
context.employees = employees