import groovy.time.*
import java.text.SimpleDateFormat
loanClearId = parameters.loanClearId

loanClearIdLong = loanClearId.toLong();

loanClear = delegator.findOne("LoanClear", [loanClearId : loanClearIdLong], false);

partyId = loanClear.partyId;


context.title = "Chai CO-OP. SAVINGS AND CREDIT SOCIETY LTD";

member = delegator.findOne("Member", [partyId : partyId], false);

context.member = member

def clearingList = []


class LoanClearCost{
	def loanNo
	def loanType
	def origAmount
	def loanBalance
	def accruedInterest
	def accruedInsurance
	def totalAmount
	def percentageCleared
	
}

listLoanClearCosting = delegator.findByAnd("LoanClearCosting",  [loanClearId : loanClearIdLong], null, false);
listLoanClearCosting.eachWithIndex { loanClearCostingItem, index ->
	
	loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : loanClearCostingItem.loanApplicationId], false);
	
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
	clearCost =  new LoanClearCost()
	clearCost.loanNo = loanApplication.loanNo;
	clearCost.loanType = loanProduct.name;
	
	clearCost.origAmount = loanApplication.loanAmt;
	clearCost.loanBalance = loanClearCostingItem.loanTotalAmt;
	
	clearCost.accruedInterest = loanClearCostingItem.totalAccruedInterest;
	clearCost.accruedInsurance = loanClearCostingItem.totalAccruedInsurance;
	
	totalAmount = loanClearCostingItem.loanTotalAmt + loanClearCostingItem.totalAccruedInterest + loanClearCostingItem.totalAccruedInsurance;
	
	clearCost.totalAmount = totalAmount;
	
	percentageRemaining = ((loanClearCostingItem.loanTotalAmt / loanApplication.loanAmt) * 100);
	percentageCleared = 100 - percentageRemaining;
	clearCost.percentageCleared = percentageCleared;
	
	clearingList.add(clearCost);
}

branch = delegator.findOne("PartyGroup", [partyId : member.branchId], false);
context.branch =  branch
context.clearingList = clearingList




def currentDate = new Date()
sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss")

context.currentDate = sdf.format(currentDate)

//sdf.format(currentDate)

