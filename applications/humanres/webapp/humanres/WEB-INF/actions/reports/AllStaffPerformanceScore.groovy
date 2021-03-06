import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

year = parameters.year
Goal_Quantitative = "QNT_GOALS"
Goal_Qualitative = "QTT_GOALS"
scorelist = [];

allStaffUnderReview = delegator.findList("StaffInPerfReviewGroup", null, null, null, null, false);
   
allStaffUnderReview.eachWithIndex { allStaffUnderReviewItem, index ->

party = allStaffUnderReviewItem.partyId
//totalScore = org.ofbiz.humanres.HumanResServices.getTotalPartyPerformance(party, year)
q1 = org.ofbiz.humanres.HumanResServices.Q1String(party, year)
q2 = org.ofbiz.humanres.HumanResServices.Q2String(party, year)
q3 = org.ofbiz.humanres.HumanResServices.Q3String(party, year)
q4 = org.ofbiz.humanres.HumanResServices.Q4String(party, year)
//all4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreString(party, year)

all4qstotalScore = org.ofbiz.humanres.HumanResServices.getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoalsToString(party, year, Goal_Quantitative, Goal_Qualitative)

bonus = org.ofbiz.humanres.HumanResServices.StaffBonusOnSalary(party, year)
increment = org.ofbiz.humanres.HumanResServices.StaffSalaryIncrement(party, year)




staff = delegator.findOne("Person", [partyId : party], false);
//dept = delegator.findOne("department", [departmentId : staff.departmentId], false);
bran = delegator.findOne("PartyGroup", [partyId : staff.branchId], false);
name = "${staff.firstName} ${staff.lastName}";
branch = bran.groupName
//department = dept.departmentName
Q1 = q1
Q2 = q2
Q3 = q3
Q4 = q4
Total = all4qstotalScore

//}
scorelist.add([name :name, branch :branch, Q1 : Q1,
 Q2 : Q2, Q3 : Q3, Q4 : Q4, Total : Total, bonus : bonus, increment : increment]);
 }
 
 // department : department, 
 
context.scorelist = scorelist;
context.year = year;
context.allStaffUnderReview = allStaffUnderReview;
