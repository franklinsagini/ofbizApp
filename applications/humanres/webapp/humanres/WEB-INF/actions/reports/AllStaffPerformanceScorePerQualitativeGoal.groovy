import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

year = parameters.year
goal = parameters.perfGoalsId
Goal_Qualitative = "QNT_GOALS"



scorelist = [];

ReviewGroup = delegator.findList("StaffInPerfReviewGroup", null, null, null, null, false);

ReviewGroup.eachWithIndex { ReviewGroupItem, index ->

party = ReviewGroupItem.partyId

staff = delegator.findOne("Person", [partyId : party], false);
fName = staff.firstName
sName = staff.lastName

name = "${fName} ${sName}"
branchId = staff.branchId
depId = staff.departmentId

bran = delegator.findOne("PartyGroup", [partyId : branchId], false);
dept = delegator.findOne("department", [departmentId : depId], false);

branch = bran.groupName
department = dept.departmentName

	groupId = ReviewGroupItem.perfReviewDefId

	expr2 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsId: goal)
		}
Goal_QualitativeGroup = delegator.findList("PerfActionPlanIndicatorDefinition", expr2, null, null, null, false);
Goal_QualitativeGroup.eachWithIndex {Goal_QualitativeGroupItem, index2 ->

	
	Indicator = Goal_QualitativeGroupItem.PerfActionPlanIndicatorId
	IndicatorDescription = Goal_QualitativeGroupItem.actionPlanIndicatorDescription
	MaxPossibleScore = Goal_QualitativeGroupItem.percentage



QNT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(party, year, Indicator)
QNT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(party, year, Indicator)
QNT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(party, year, Indicator)
QNT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(party, year, Indicator)
QNT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(party, year, Indicator)


	scorelist.add([IndicatorDescription : IndicatorDescription, MaxPossibleScore : MaxPossibleScore, name : name,
		 branch : branch, department : department, Q1 : QNT_q1, Q2 : QNT_q2, Q3 : QNT_q3, Q4 : QNT_q4, Total : QNT_all4qstotalScore]);

	}



}

context.fName = fName;
context.sName = sName;
context.year = year;

context.scorelist = scorelist;
context.ReviewGroup = ReviewGroup;
IndicatorUppercased = IndicatorDescription.toUpperCase()
context.IndicatorUppercased = IndicatorUppercased;
