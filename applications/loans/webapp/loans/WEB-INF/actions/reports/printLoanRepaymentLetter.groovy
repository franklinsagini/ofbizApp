import groovy.time.*

loanApplicationId = parameters.loanApplicationId
lloanApplicationId = loanApplicationId.toLong();

guarantorId = parameters.guarantorId
context.title = "Chai Sacco"

if (loanApplicationId) {
	loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : lloanApplicationId], false);
   context.loanApplication = loanApplication;
}

//Get the LoanProduct

loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
context.loanProduct = loanProduct;

member = delegator.findOne("Member", [partyId : loanApplication.partyId], false);
context.member = member;


guarantorIdLong = guarantorId.toLong();

guarantor = delegator.findOne("Member", [partyId : guarantorIdLong], false);
context.guarantor = guarantor;

guaranteedAmt = org.ofbiz.accountholdertransactions.LoanUtilities.getMyGuaranteedValue(lloanApplicationId);
context.guaranteedAmt = guaranteedAmt


salutation = delegator.findOne("Salutation", [salutationId : member.salutationId], false);
context.salutation = salutation;

startDate = loanApplication.repaymentStartDate;
duration = loanApplication.repaymentPeriod;
use(TimeCategory) {
	lastDate = startDate  + duration.toInteger().month;
}

context.startDate = startDate;
context.lastDate = lastDate;

currentContributionAmt = 0
newMemberDepostContributionAmt = 0
loanDeductionAmt = 0
context.currentContributionAmt = currentContributionAmt;
context.minimumShareContribution = newMemberDepostContributionAmt;
context.loanRepayment = loanDeductionAmt;
totalDeduction = newMemberDepostContributionAmt + loanDeductionAmt;
context.totalDeduction = totalDeduction;

return
