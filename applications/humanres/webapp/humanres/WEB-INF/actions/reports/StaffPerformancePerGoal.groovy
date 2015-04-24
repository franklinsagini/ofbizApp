import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

year = parameters.year
party = parameters.partyId
goal = parameters.perfGoalsId
Goal_Quantitative = "QNT_GOALS"

staff = delegator.findOne("Person", [partyId : party], false);
fName = staff.firstName
sName = staff.lastName
fNameUpperCased = fName.toUpperCase()
sNameUpperCased = sName.toUpperCase()

scorelist = [];

expr = exprBldr.AND() {
			EQUALS(partyId: party)
		}
ReviewGroup = delegator.findList("StaffInPerfReviewGroup", expr, null, null, null, false);

ReviewGroup.eachWithIndex { ReviewGroupItem, index ->

	groupId = ReviewGroupItem.perfReviewDefId

	expr2 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsId: goal)
		}
Goal_QuantitativeGroup = delegator.findList("PerfActionPlanIndicatorDefinition", expr2, null, null, null, false);
Goal_QuantitativeGroup.eachWithIndex { Goal_QuantitativeGroupItem, index2 ->

	perspectiveId = Goal_QuantitativeGroupItem.perfGoalsId
	ObjectiveId = Goal_QuantitativeGroupItem.PerfReviewsGroupObjectiveDefinitionId
	ActionPlanId = Goal_QuantitativeGroupItem.PerfObjectiveActionPlanId
	Indicator = Goal_QuantitativeGroupItem.PerfActionPlanIndicatorId
	IndicatorDescription = Goal_QuantitativeGroupItem.actionPlanIndicatorDescription
	MaxPossibleScore = Goal_QuantitativeGroupItem.percentage

	perspective = delegator.findOne("PerfGoals", [perfGoalsId : perspectiveId], false);
	Objective = delegator.findOne("PerfReviewsGroupObjectiveDefinition", [PerfReviewsGroupObjectiveDefinitionId : ObjectiveId], false);
	ActionPlan = delegator.findOne("PerfObjectiveActionPlanDefinition", [PerfObjectiveActionPlanId : ActionPlanId], false);
	perspectiveNormalCased = perspective.goal
    perspectiveUpperCased = perspectiveNormalCased.toUpperCase()
    context.perspectiveUpperCased = perspectiveUpperCased;

QNT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(party, year, Indicator)
QNT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(party, year, Indicator)
QNT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(party, year, Indicator)
QNT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(party, year, Indicator)
QNT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(party, year, Indicator)


	scorelist.add([Objective :Objective.objectiveDescription, 
		ActionPlan : ActionPlan.objectiveActionPlanDescription, IndicatorDescription : IndicatorDescription, MaxPossibleScore : MaxPossibleScore,
		 Q1 : QNT_q1, Q2 : QNT_q2, Q3 : QNT_q3, Q4 : QNT_q4, Total : QNT_all4qstotalScore]);

QNT_Totalq1 = org.ofbiz.humanres.HumanResServices.Q1StringSingle(party, year, Goal_Quantitative)
QNT_Totalq2 = org.ofbiz.humanres.HumanResServices.Q2StringSingle(party, year, Goal_Quantitative)
QNT_Totalq3 = org.ofbiz.humanres.HumanResServices.Q3StringSingle(party, year, Goal_Quantitative)
QNT_Totalq4 = org.ofbiz.humanres.HumanResServices.Q4StringSingle(party, year, Goal_Quantitative)
QNT_Totalall4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreStringSingle(party, year, Goal_Quantitative)
QNT_Totalall4qstotalMaxScore = org.ofbiz.humanres.HumanResServices.getMaxTotalPartyPerformanceSingle(party, year, Goal_Quantitative)

context.QNT_Totalq1 = QNT_Totalq1;
context.QNT_Totalq2 = QNT_Totalq2;
context.QNT_Totalq3 = QNT_Totalq3;
context.QNT_Totalq4 = QNT_Totalq4;
context.QNT_Totalall4qstotalScore = QNT_Totalall4qstotalScore;
context.QNT_Totalall4qstotalMaxScore = QNT_Totalall4qstotalMaxScore;

	}



}

context.fNameUpperCased = fNameUpperCased;
context.sNameUpperCased = sNameUpperCased;
context.year = year;

context.scorelist = scorelist;
context.ReviewGroup = ReviewGroup;
