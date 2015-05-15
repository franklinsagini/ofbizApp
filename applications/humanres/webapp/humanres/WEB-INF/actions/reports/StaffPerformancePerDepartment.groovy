import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

year = parameters.year
dept = parameters.departmentId
bran = parameters.branchId
Goal_Quantitative = "QNT_GOALS"
Goal_Qualitative = "QTT_GOALS"






expr = exprBldr.AND() { EQUALS(departmentId: dept) }
ReviewGroup1 = delegator.findList("StaffInPerfReviewGroup", expr, null, null, null, false);

class QualitativeGoalScore{
	def name
	def maxScore
	def quarterOne
	def quarterTwo
	def quarterThree
	def quarterFour
	def total
}

class QuantitativeGoalScore{
	def name
	def objectiveName
	def actionPlanName
	def indicatorName
	def maxScore
	def quarterOne
	def quarterTwo
	def quarterThree
	def quarterFour
	def total
}

class IndividualGoalScore{

	def partyId

	//def quantitativeGoal
	def listQuantitativeGoalScore = []

	def quantitativeMaxScore
	def quantitativeQuarterOne
	def quantitativeQuarterTwo
	def quantitativeQuarterThree
	def quantitativeQuarterFour
	def quantitativeTotal

	def listQualitativeGoalScore = []

	def qualitativeMaxScore
	def qualitativeQuarterOne
	def qualitativeQuarterTwo
	def qualitativeQuarterThree
	def qualitativeQuarterFour
	def qualitativeTotal
	def all4qstotalScore
	

}

def listIndividualGoalScore = [];

ReviewGroup1.eachWithIndex { ReviewGroup1Item, index ->

	individual = new IndividualGoalScore()

	party = ReviewGroup1Item.partyId

	individual.partyId = party

	





	/*expr2 = exprBldr.AND() { EQUALS(partyId: party) }
	ReviewGroup = delegator.findList("StaffInPerfReviewGroup", expr2, null, null, null, false);

	ReviewGroup.eachWithIndex { ReviewGroupItem, index2 ->*/

	
	
		
        staffBranch = delegator.findOne("PartyGroup", [partyId : bran], false);
		staffdepartment = delegator.findOne("department", [departmentId : dept], false);
		staff = delegator.findOne("Person", [partyId : party], false);
		staffbran = staffBranch.groupName
		staffdept = staffdepartment.departmentName
		fName = staff.firstName
		sName = staff.lastName
		fNameUpperCased = fName.toUpperCase()
		sNameUpperCased = sName.toUpperCase()
		staffbranUppercase = staffbran.toUpperCase()
		staffdeptUppercase = staffdept.toUpperCase()
		Goal_QuantitativeScorelist = [];
		Goal_QualitativeScorelist = [];

		groupId = ReviewGroup1Item.perfReviewDefId

		expr3 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsDefId: Goal_Quantitative)
		}
		Goal_QuantitativeGroup = delegator.findList("PerfActionPlanIndicatorDefinition", expr3, null, null, null, false);
		Goal_QuantitativeGroup.eachWithIndex { Goal_QuantitativeGroupItem, index3 ->

			quantitativeGoal = new QuantitativeGoalScore()
			
			perspectiveId = Goal_QuantitativeGroupItem.perfGoalsId
			ObjectiveId = Goal_QuantitativeGroupItem.PerfReviewsGroupObjectiveDefinitionId
			ActionPlanId = Goal_QuantitativeGroupItem.PerfObjectiveActionPlanId
			Indicator = Goal_QuantitativeGroupItem.PerfActionPlanIndicatorId
			IndicatorDescription = Goal_QuantitativeGroupItem.actionPlanIndicatorDescription
			MaxPossibleScore = Goal_QuantitativeGroupItem.percentage

			perspective = delegator.findOne("PerfGoals", [perfGoalsId : perspectiveId], false);
			Objective = delegator.findOne("PerfReviewsGroupObjectiveDefinition", [PerfReviewsGroupObjectiveDefinitionId : ObjectiveId], false);
			ActionPlan = delegator.findOne("PerfObjectiveActionPlanDefinition", [PerfObjectiveActionPlanId : ActionPlanId], false);



			QNT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(party, year, Indicator)
			QNT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(party, year, Indicator)
			QNT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(party, year, Indicator)
			QNT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(party, year, Indicator)
			QNT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(party, year, Indicator)




			quantitativeGoal.name = perspective.goal
			quantitativeGoal.objectiveName = Objective.objectiveDescription
			quantitativeGoal.actionPlanName = ActionPlan.objectiveActionPlanDescription
			quantitativeGoal.indicatorName =IndicatorDescription
			quantitativeGoal.maxScore = MaxPossibleScore
			quantitativeGoal.quarterOne = QNT_q1
			quantitativeGoal.quarterTwo = QNT_q2
			quantitativeGoal.quarterThree = QNT_q3
			quantitativeGoal.quarterFour = QNT_q4
			quantitativeGoal.total = QNT_all4qstotalScore

			individual.listQuantitativeGoalScore << quantitativeGoal

			//	Goal_QuantitativeScorelist.add([perspective :perspective.goal, Objective :Objective.objectiveDescription,
			//		ActionPlan : ActionPlan.objectiveActionPlanDescription, IndicatorDescription : IndicatorDescription, MaxPossibleScore : MaxPossibleScore,
			//		 Q1 : QNT_q1, Q2 : QNT_q2, Q3 : QNT_q3, Q4 : QNT_q4, Total : QNT_all4qstotalScore]);


		}

		QNT_Totalq1 = org.ofbiz.humanres.HumanResServices.Q1StringSingle(party, year, Goal_Quantitative)
		QNT_Totalq2 = org.ofbiz.humanres.HumanResServices.Q2StringSingle(party, year, Goal_Quantitative)
		QNT_Totalq3 = org.ofbiz.humanres.HumanResServices.Q3StringSingle(party, year, Goal_Quantitative)
		QNT_Totalq4 = org.ofbiz.humanres.HumanResServices.Q4StringSingle(party, year, Goal_Quantitative)
		QNT_Totalall4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreStringSingle(party, year, Goal_Quantitative)
		QNT_Totalall4qstotalMaxScore = org.ofbiz.humanres.HumanResServices.getMaxTotalPartyPerformanceSingle(party, year, Goal_Quantitative)

		
		
		individual.quantitativeMaxScore = QNT_Totalall4qstotalMaxScore
		individual.quantitativeQuarterOne = QNT_Totalq1
		individual.quantitativeQuarterTwo = QNT_Totalq2
		individual.quantitativeQuarterThree = QNT_Totalq3
		individual.quantitativeQuarterFour = QNT_Totalq4
		individual.quantitativeTotal = QNT_Totalall4qstotalScore

		expr4 = exprBldr.AND() {
			EQUALS(perfReviewDefId: groupId)
			EQUALS(perfGoalsDefId: Goal_Qualitative)
		}
		Goal_QualitativeGroup = delegator.findList("PerfActionPlanIndicatorDefinition", expr4, null, null, null, false);
		Goal_QualitativeGroup.eachWithIndex { Goal_QualitativeGroupItem, index4 ->

			qualitative = new QualitativeGoalScore()

			Indicator2 = Goal_QualitativeGroupItem.PerfActionPlanIndicatorId
			IndicatorDescription2 = Goal_QualitativeGroupItem.actionPlanIndicatorDescription
			MaxPossibleScore2 = Goal_QualitativeGroupItem.percentage


			QTT_q1 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ1String(party, year, Indicator2)
			QTT_q2 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ2String(party, year, Indicator2)
			QTT_q3 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ3String(party, year, Indicator2)
			QTT_q4 = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorQ4String(party, year, Indicator2)
			QTT_all4qstotalScore = org.ofbiz.humanres.HumanResServices.PartyPerformancePerIndicatorTotalScoreString(party, year, Indicator2)

			Goal_QualitativeScorelist.add([IndicatorDescription2 :IndicatorDescription2, MaxPossibleScore2 :MaxPossibleScore2,
				Q1 : QTT_q1, Q2 : QTT_q2, Q3 : QTT_q3, Q4 : QTT_q4, Total : QTT_all4qstotalScore]);
			
			qualitative.name = IndicatorDescription2
			qualitative.maxScore = MaxPossibleScore2
			qualitative.quarterOne = QTT_q1
			qualitative.quarterTwo = QTT_q2
			qualitative.quarterThree = QTT_q3
			qualitative.quarterFour = QTT_q4
			qualitative.total = QTT_all4qstotalScore
			
			individual.listQualitativeGoalScore << qualitative

		}
		
		QTT_Totalq1 = org.ofbiz.humanres.HumanResServices.Q1StringSingle(party, year, Goal_Qualitative)
		QTT_Totalq2 = org.ofbiz.humanres.HumanResServices.Q2StringSingle(party, year, Goal_Qualitative)
		QTT_Totalq3 = org.ofbiz.humanres.HumanResServices.Q3StringSingle(party, year, Goal_Qualitative)
		QTT_Totalq4 = org.ofbiz.humanres.HumanResServices.Q4StringSingle(party, year, Goal_Qualitative)
		QTT_Totalall4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreStringSingle(party, year, Goal_Qualitative)
		QTT_Totalall4qstotalMaxScore = org.ofbiz.humanres.HumanResServices.getMaxTotalPartyPerformanceSingle(party, year, Goal_Qualitative)

		
		individual.qualitativeMaxScore = QTT_Totalall4qstotalMaxScore
		individual.qualitativeQuarterOne = QTT_Totalq1
		individual.qualitativeQuarterTwo = QTT_Totalq2
		individual.qualitativeQuarterThree = QTT_Totalq3
		individual.qualitativeQuarterFour = QTT_Totalq4
		individual.qualitativeTotal = QTT_Totalall4qstotalScore
		individual.all4qstotalScore = org.ofbiz.humanres.HumanResServices.getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoalsToString(party, year, Goal_Quantitative, Goal_Qualitative)

//		context.QNT_Totalq1 = QNT_Totalq1;
//		context.QNT_Totalq2 = QNT_Totalq2;
//		context.QNT_Totalq3 = QNT_Totalq3;
//		context.QNT_Totalq4 = QNT_Totalq4;
//		context.QNT_Totalall4qstotalScore = QNT_Totalall4qstotalScore;
//		context.QNT_Totalall4qstotalMaxScore = QNT_Totalall4qstotalMaxScore;
//		context.QTT_Totalq1 = QTT_Totalq1;
//		context.QTT_Totalq2 = QTT_Totalq2;
//		context.QTT_Totalq3 = QTT_Totalq3;
//		context.QTT_Totalq4 = QTT_Totalq4;
//		context.QTT_Totalall4qstotalScore = QTT_Totalall4qstotalScore;
//		context.QTT_Totalall4qstotalMaxScore = QTT_Totalall4qstotalMaxScore;
	//}
	//all4qstotalScore = org.ofbiz.humanres.HumanResServices.TotalScoreString(party, year)
	context.all4qstotalScore = org.ofbiz.humanres.HumanResServices.getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoalsToString(party, year, Goal_Quantitative, Goal_Qualitative)

	
	
	listIndividualGoalScore << individual
}


context.Goal_QuantitativeScorelist = Goal_QuantitativeScorelist;
context.Goal_QualitativeScorelist = Goal_QualitativeScorelist;
context.year = year;
context.ReviewGroup1 = ReviewGroup1;
context.fNameUpperCased = fNameUpperCased;
context.sNameUpperCased = sNameUpperCased;
context.staffbranUppercase = staffbranUppercase;
context.staffdeptUppercase = staffdeptUppercase;

context.listIndividualGoalScore = listIndividualGoalScore










