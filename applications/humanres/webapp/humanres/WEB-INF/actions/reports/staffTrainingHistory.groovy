import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 



historylist = [];

now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());
party = parameters.partyId

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN(endDate: today)
			EQUALS(partyId: party)
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);

employees = delegator.findList("PartyAndTrainingEventsView", expr, null, ["endDate ASC"], findOptions, false);
employees.eachWithIndex { employeesItem, index ->




	traintypeid = employeesItem.trainingTypeId
	tr = delegator.findOne("TrainingTypes", [trainingTypeId : traintypeid], false);
	event = employeesItem.eventName
	trainingType = tr.trainingName
	start = employeesItem.startDate
	end = employeesItem.endDate
	
	
	historylist.add([event :event, trainingType : trainingType, start : start, end : end]);
		}
	
	context.historylist = historylist;
	context.employees = employees

	
	staff = delegator.findOne("Person", [partyId : party], false);
	fname = staff.firstName.toUpperCase()
	sname = staff.lastName.toUpperCase()
	context.fname = fname
	context.sname = sname