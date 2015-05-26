
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
recordList = [];
expr = exprBldr.AND() {
	EQUALS(separated: "Y")
	EQUALS(status: "Separated")
}

	employeeList = delegator.findList("SeparationApplication", expr, null, null, null, false);
	
	employeeList.eachWithIndex { staffItem, index ->
	
		party = staffItem.partyId
		separationType = staffItem.separationTypeId
		
		staff = delegator.findOne("Person", [partyId : party], false);
		branches = delegator.findOne("PartyGroup", [partyId : staff.branchId], false);
		departments = delegator.findOne("department", [departmentId : staff.departmentId], false);
		typeOfSeparation = delegator.findOne("SeparationType", [separationTypeId : separationType], false);
		
		payroll = staff.employeeNumber;
		fname = staff.firstName;
		sname = staff.lastName;
		name = "${fname} ${sname}"
		bran = branches.groupName;
		depart = departments.departmentName;
		separateType = typeOfSeparation.name;
		separationReason = staffItem.reason;
		dateEffected = staffItem.effectiveDate;
		
		recordList.add([payroll :payroll, name :name, bran : bran, depart :depart, separateType : separateType, separationReason : separationReason, dateEffected : dateEffected]);
	 }
	
	context.recordList = recordList;
