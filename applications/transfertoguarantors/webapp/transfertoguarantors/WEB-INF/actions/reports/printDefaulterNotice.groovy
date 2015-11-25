import groovy.time.*
import static java.math.RoundingMode.HALF_UP

loanApplicationId = parameters.loanApplicationId

System.out.println('Loan Application ID is '+loanApplicationId);
lloanApplicationId = loanApplicationId.toLong();
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

branchId = member.branchId;
branch = delegator.findOne("PartyGroup", [partyId : branchId], false);
context.branchName = branch.groupName;

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


myGuarantorList = delegator.findByAnd("LoanGuarantor",  [loanApplicationId : lloanApplicationId], null, false);
context.myGuarantorList = myGuarantorList

//Total Loan Balance - adds interest and insuran
bdTotalLoanBalance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalacePlusInterestPlusInsurace(lloanApplicationId);
context.bdTotalLoanBalance = bdTotalLoanBalance

int numberOfGuarantors = myGuarantorList.size();
//
bdAmountPerGuarantor = bdTotalLoanBalance.divide(numberOfGuarantors, 2, HALF_UP);
context.bdAmountPerGuarantor = bdAmountPerGuarantor

periodFromLastRepay = org.ofbiz.loansprocessing.LoansProcessingServices.lastRepaymentDurationToDateVersion2(lloanApplicationId);
context.periodFromLastRepay = periodFromLastRepay


return