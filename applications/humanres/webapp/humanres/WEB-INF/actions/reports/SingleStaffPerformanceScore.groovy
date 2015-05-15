import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

year = parameters.year
partyId = parameters.partyId
Goal_Quantitative = "QNT_GOALS"
Goal_Qualitative = "QTT_GOALS"

staff = delegator.findOne("Person", [partyId : partyId], false);
fName = staff.firstName
sName = staff.lastName
fNameUpperCased = fName.toUpperCase()
sNameUpperCased = sName.toUpperCase()
Goal_QuantitativeScorelist = [];
Goal_QualitativeScorelist = [];

expr = exprBldr.AND() {
			EQUALS(partyId: partyId)
		}
ReviewGroup = delegator.findList("StaffInPerfReviewGroup", expr, null, null, null, false);

ReviewGroup.eachWithIndex { ReviewGroupItem, index ->

	groupId = ReviewGroupItem.perfReviewDefId

	expr2 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsDefId: Goal_Quantitative)
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
	


QNT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(partyId, year, Indicator)
QNT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(partyId, year, Indicator)
QNT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(partyId, year, Indicator)
QNT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(partyId, year, Indicator)
QNT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(partyId, year, Indicator)


	Goal_QuantitativeScorelist.add([perspective :perspective.goal, Objective :Objective.objectiveDescription, 
		ActionPlan : ActionPlan.objectiveActionPlanDescription, IndicatorDescription : IndicatorDescription, MaxPossibleScore : MaxPossibleScore,
		 Q1 : QNT_q1, Q2 : QNT_q2, Q3 : QNT_q3, Q4 : QNT_q4, Total : QNT_all4qstotalScore]);

QNT_Totalq1 = org.ofbiz.humanres.HumanResServices.Q1StringSingle(partyId, year, Goal_Quantitative)
QNT_Totalq2 = org.ofbiz.humanres.HumanResServices.Q2StringSingle(partyId, year, Goal_Quantitative)
QNT_Totalq3 = org.ofbiz.humanres.HumanResServices.Q3StringSingle(partyId, year, Goal_Quantitative)
QNT_Totalq4 = org.ofbiz.humanres.HumanResServices.Q4StringSingle(partyId, year, Goal_Quantitative)
QNT_Totalall4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreStringSingle(partyId, year, Goal_Quantitative)
QNT_Totalall4qstotalMaxScore = org.ofbiz.humanres.HumanResServices.getMaxTotalPartyPerformanceSingle(partyId, year, Goal_Quantitative)

	}

	expr3 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsDefId: Goal_Qualitative)
		}
Goal_QualitativeGroup = delegator.findList("PerfActionPlanIndicatorDefinition", expr3, null, null, null, false);
Goal_QualitativeGroup.eachWithIndex { Goal_QualitativeGroupItem, index3 ->

	
	Indicator2 = Goal_QualitativeGroupItem.PerfActionPlanIndicatorId
	IndicatorDescription2 = Goal_QualitativeGroupItem.actionPlanIndicatorDescription
	MaxPossibleScore2 = Goal_QualitativeGroupItem.percentage


QTT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(partyId, year, Indicator2)
QTT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(partyId, year, Indicator2)
QTT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(partyId, year, Indicator2)
QTT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(partyId, year, Indicator2)
QTT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(partyId, year, Indicator2)

	Goal_QualitativeScorelist.add([IndicatorDescription2 :IndicatorDescription2, MaxPossibleScore2 :MaxPossibleScore2,
	 Q1 : QTT_q1, Q2 : QTT_q2, Q3 : QTT_q3, Q4 : QTT_q4, Total : QTT_all4qstotalScore]);

QTT_Totalq1 = org.ofbiz.humanres.HumanResServices.Q1StringSingle(partyId, year, Goal_Qualitative)
QTT_Totalq2 = org.ofbiz.humanres.HumanResServices.Q2StringSingle(partyId, year, Goal_Qualitative)
QTT_Totalq3 = org.ofbiz.humanres.HumanResServices.Q3StringSingle(partyId, year, Goal_Qualitative)
QTT_Totalq4 = org.ofbiz.humanres.HumanResServices.Q4StringSingle(partyId, year, Goal_Qualitative)
QTT_Totalall4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreStringSingle(partyId, year, Goal_Qualitative)
QTT_Totalall4qstotalMaxScore = org.ofbiz.humanres.HumanResServices.getMaxTotalPartyPerformanceSingle(partyId, year, Goal_Qualitative)

	}

context.QNT_Totalq1 = QNT_Totalq1;
context.QNT_Totalq2 = QNT_Totalq2;
context.QNT_Totalq3 = QNT_Totalq3;
context.QNT_Totalq4 = QNT_Totalq4;
context.QNT_Totalall4qstotalScore = QNT_Totalall4qstotalScore;
context.QNT_Totalall4qstotalMaxScore = QNT_Totalall4qstotalMaxScore;
context.QTT_Totalq1 = QTT_Totalq1;
context.QTT_Totalq2 = QTT_Totalq2;
context.QTT_Totalq3 = QTT_Totalq3;
context.QTT_Totalq4 = QTT_Totalq4;
context.QTT_Totalall4qstotalScore = QTT_Totalall4qstotalScore;
context.QTT_Totalall4qstotalMaxScore = QTT_Totalall4qstotalMaxScore;

}
//all4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreString(partyId, year)
all4qstotalScore = org.ofbiz.humanres.HumanResServices.getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoalsToString(partyId, year, Goal_Quantitative, Goal_Qualitative)
 
context.Goal_QuantitativeScorelist = Goal_QuantitativeScorelist;
context.Goal_QualitativeScorelist = Goal_QualitativeScorelist;
context.year = year;
context.ReviewGroup = ReviewGroup;
context.fNameUpperCased = fNameUpperCased;
context.sNameUpperCased = sNameUpperCased;
context.all4qstotalScore = all4qstotalScore;