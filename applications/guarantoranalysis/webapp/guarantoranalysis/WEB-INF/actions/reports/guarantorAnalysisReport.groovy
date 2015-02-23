import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId

println(" PPP  ID IS "+partyId)
lpartyId = partyId.toLong();
println(" PPP  lpartyId IS "+lpartyId)

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;

//Get Loans Guaranteed By Member
class Loan{
	def loanNo;
	def loanType;
	def disbursedDate;
	def loanAmt;
	def balance;

	//Applicant Details
	def memberNumber;
	def memberName;
	def amountGuaranteed;
	def comment;

	def listOfGuarantors = []
}

class Guarantor{
	def memberNumber;
	def memberName;
	def amountGuaranteed;
	def comment;
}

//Get Loans Guaranteed By The Member
loansGuaranteedList = delegator.findByAnd("LoanGuarantor",  [guarantorId : lpartyId], null, false);

//Add the active loans from this to the new list

def loansGuaranteedByMemberList = [];

//Adding Items - the Loans
loansGuaranteedList.eachWithIndex { guarantorItem, guarantorindex ->
	loanGuaranteedItem = new Loan();

	Long loanApplicationId = guarantorItem.loanApplicationId;
	System.out.println(" The Application Id PPPPPPPPPPPPPPPP "+loanApplicationId);
	
	if (loanApplicationId != null){
	//Get Loan Product Name
	loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : loanApplicationId], false);

	loanGuaranteedItem.loanNo = loanApplication.loanNo;

	Long loanProductId = loanApplication.loanProductId;
	//Get Loan Product Name
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanProductId], false);

	loanGuaranteedItem.loanType = loanProduct.name;

	loanGuaranteedItem.disbursedDate = loanApplication.disbursementDate;
	loanGuaranteedItem.loanAmt = loanApplication.loanAmt;
	loanGuaranteedItem.balance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);

	Long applicantId = loanApplication.partyId;
	loanApplicant = delegator.findOne("Member", [partyId : applicantId], false);

	loanGuaranteedItem.memberNumber = loanApplicant.memberNumber;
	loanGuaranteedItem.memberName = loanApplicant.firstName+" "+loanApplicant.middleName+" "+loanApplicant.lastName;
	loanGuaranteedItem.amountGuaranteed = org.ofbiz.accountholdertransactions.LoanUtilities.getMyGuaranteedValue(loanApplicationId);
	loanGuaranteedItem.comment = "";

	//Add List of my guarantors


	loansGuaranteedByMemberList << loanGuaranteedItem;
	}
}

context.loansGuaranteedByMemberList = loansGuaranteedByMemberList;

def membersLoans = [];
myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId], null, false);

myLoansList.eachWithIndex { loanItem, loanIndex ->
	myLoanItem = new Loan();

	Long loanApplicationId = loanItem.loanApplicationId;
	System.out.println(" The Application Id PPPPPPPPPPPPPPPP "+loanApplicationId);
	//Get Loan Product Name
	//loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : loanApplicationId], false);

	myLoanItem.loanNo = loanItem.loanNo;

	Long loanProductId = loanItem.loanProductId;
	//Get Loan Product Name
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanProductId], false);

	myLoanItem.loanType = loanProduct.name;

	myLoanItem.disbursedDate = loanItem.disbursementDate;
	myLoanItem.loanAmt = loanItem.loanAmt;
	myLoanItem.balance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);


	//Add Guarantors for this Loan
	loanGuarantorsList = delegator.findByAnd("LoanGuarantor",  [loanApplicationId : loanApplicationId], null, false);
	loanGuarantorsList.eachWithIndex { loanGuarantorItem, loanGuarantorindex ->
		guarantor =  new Guarantor();

		//		def memberNumber;
		//		def memberName;
		//		def amountGuaranteed;
		//		def comment;
		guarantorId = loanGuarantorItem.guarantorId;
		guarantorLoanApplicationId = loanGuarantorItem.loanApplicationId;
		guarantorMember = delegator.findOne("Member", [partyId : guarantorId], false);
		guarantor.memberNumber = guarantorMember.memberNumber;
		guarantor.memberName = guarantorMember.firstName+" "+guarantorMember.middleName+" "+guarantorMember.lastName;
		guarantor.amountGuaranteed = org.ofbiz.accountholdertransactions.LoanUtilities.getMyGuaranteedValue(guarantorLoanApplicationId);
		guarantor.comment = "";
		
		myLoanItem.listOfGuarantors.add(guarantor);
		
		
	}
	
	
	membersLoans << myLoanItem;
}

context.membersLoans = membersLoans;
