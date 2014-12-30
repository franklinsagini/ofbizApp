import org.ofbiz.entity.Delegator;

import groovy.time.*

loanApplicationId = parameters.loanApplicationId
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

employmentType = delegator.findOne("EmploymentType", [employmentTypeId : member.employmentTypeId], false);
context.employmentType = employmentType; 
salutation = delegator.findOne("Salutation", [salutationId : member.salutationId], false);
context.salutation = salutation;

deductionEvaluationList = delegator.findByAnd("LoanDeductionEvaluation", [loanApplicationId : lloanApplicationId], null, false);
context.deductionEvaluation = deductionEvaluationList[0];


return
