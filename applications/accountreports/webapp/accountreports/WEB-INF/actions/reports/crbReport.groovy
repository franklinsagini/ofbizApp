import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.entity.condition.*;
 import org.ofbiz.entity.util.*;
 import org.ofbiz.entity.*;
 import org.ofbiz.base.util.*;
 import javolution.util.FastList;
 import javolution.util.FastSet;
 import javolution.util.FastMap;
 import org.ofbiz.entity.transaction.TransactionUtil;
 import org.ofbiz.entity.util.EntityListIterator;
 import org.ofbiz.entity.GenericEntity;
 import org.ofbiz.entity.model.ModelField;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.model.ModelEntity;
 import org.ofbiz.entity.model.ModelReader;
 import  org.ofbiz.accounting.ledger.CrbReportServices

crbReportListBuilder = []
crbReportList = [];

//GET LOAN STATUS
loanStatus = delegator.findList('LoanStatus',  null, null,null,null,false)
loanStatusId = null;
loanStatus.each { status ->
    if (status.name == "DISBURSED") {
        loanStatusId = status.loanStatusId
    }
}

summaryCondition = [];
if (loanStatusId) {
  loanStatusIdL = loanStatusId.toLong()
  summaryCondition.add(EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, loanStatusIdL));
}


loanApps = delegator.findList('LoanApplication',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,null,null,false)

currentDate = UtilDateTime.nowTimestamp();



loanApps.each { obj ->
         //GET MEMBER
     member = delegator.findOne("Member", [partyId : obj.partyId], false);
     //GET SALUTATION 
     salutation = delegator.findOne("Salutation", [salutationId : member.salutationId], false);
     //GET GENDER 
     gender = delegator.findOne("Gender", [genderId : member.genderId], false); 
    //GET MARITAL STATUS
     maritalStatus = delegator.findOne("MaritalStatus", [maritalStatusId : member.maritalStatusId], false);
    //GET MARITAL STATUS
     station = delegator.findOne("Station", [stationId : (member.stationId).toString()], false);
    //GET LOAN DETAILS
     loanProduct = delegator.findOne("LoanProduct", [loanProductId : obj.loanProductId], false);
    //GET EMPLOYEMENT TYPE
     employementType = delegator.findOne("EmploymentType", [employmentTypeId : member.employmentTypeId], false);
    //GET BRANCH DETAILS
     branch = delegator.findOne("PartyGroup", [partyId : member.branchId], false);
    //GET Account DETAILS
     account = delegator.findOne("AccountProduct", [accountProductId : obj.accountProductId], false);
    //GET A List of Member Account DETAILS
    accountCondition = [];
    accountCondition.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, member.partyId));
    member_accounts = delegator.findList('MemberAccount',  EntityCondition.makeCondition(accountCondition, EntityOperator.AND), null,null,null,false)
    //GET ACTIVE LOAN ACCOUNT
    loanMemberAccount = null;
    member_accounts.each { ac ->
        if (account!=null) {
                    if (account.accountProductId == ac.accountProductId) {
            loanMemberAccount  = ac
        }
        }

    }
    //GET ACCOUNT STATUS DETAILS
    accountsStatus = null;
    dateAccountOpened  = null;
     accountStatus=null;
    if (loanMemberAccount!=null) {
        accountsStatus= delegator.findOne("AccountStatus", [accountStatusId : (loanMemberAccount.accountStatusId).toString()], false);
        dateAccountOpened = loanMemberAccount.createdStamp
         accountStatus=accountsStatus.name
    }
     


    //CHECK IF USING ID
    primaryIdentificationDocumentType=""
    primaryIdentificationDocNumber=""
    secondaryIdentificationDocumentType=""
    secondaryIdentificationDocumentNumber=""
    if (member.idNumber != null) {
        primaryIdentificationDocumentType = 1
        primaryIdentificationDocNumber =member.idNumber
    }else if (member.passportNumber != null) {
         secondaryIdentificationDocumentType = 2
         secondaryIdentificationDocumentNumber = member.passportNumber
    }

   loanRepaymentAmount =   CrbReportServices..getLastRepaymentAmount(delegator, obj.loanApplicationId)
   formatedloanRepaymentAmount = CrbReportServices.getCRBAmountFormat(loanRepaymentAmount)
   daysInArrears = CrbReportServices.lastRepaymentDurationToDateInDays(obj.loanApplicationId)
   lastRepaymentDate = org.ofbiz.loansprocessing.LoansProcessingServices.getLastRepaymentDate(obj.loanApplicationId)
   formatedLastRepaymentDate = CrbReportServices.getCRBDateFormat(lastRepaymentDate)
   currentLoanBalance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(obj.loanApplicationId)
   noInstalmentsInArrears = 0
    member_maritalStatus = ""
    
    if (daysInArrears.toInteger()>30) {
        daysInArrears = daysInArrears
        noInstalmentsInArrears = daysInArrears/30
    }else{
        daysInArrears = 0
        noInstalmentsInArrears = 0
    }

    performingNonPerforming = ""
    if (daysInArrears.toInteger()>90) {
        performingNonPerforming = "B"
    }else{
          performingNonPerforming = "A"
    }

    locationCountry = ""
    nationality = ""

    if (member.citizenship == "KEN") {
        locationCountry = "KENYA"
        nationality = "KENYAN"
    }


    if (maritalStatus.name == "Married") {
        member_maritalStatus = "M"
    }else if (maritalStatus.name == "Single") {
        member_maritalStatus = "S"
    }else if (maritalStatus.name == "Divorced") {
        member_maritalStatus = "D"
    }else  {
        member_maritalStatus = "U"
    }

    member_gender = ""
    if (gender.name == "Male") {
        member_gender = "M"
    }else if (gender.name == "Female") {
        member_gender = "F"
    }

     crbReportListBuilder = [
        surname:member.lastName,
        forename1: member.firstName,
        forename2:member.middleName,
        forename3:"",
        salutation:salutation.name,
        dateOfBirth:member.birthDate,
        clientNumber:member.memberNumber,
        loanNumber:obj.loanNo,
        gender:member_gender,
        nationality:nationality,
        maritalStatus:member_maritalStatus,
        primaryIdentificationDocumentType:primaryIdentificationDocumentType,
        primaryIdentificationDocNumber:primaryIdentificationDocNumber,
        secondaryIdentificationDocumentType:secondaryIdentificationDocumentType,
        secondaryIdentificationDocumentNumber:secondaryIdentificationDocumentNumber,
        otherIdentificationDocumentType:"",
        otherIdentificationDocumentNumber:"",
        mobileTelephoneNumber:member.mobileNumber,
        homeTelephoneNumber:"",
        workTelephoneNumber:"",
        postalAddress1:member.permanentAddress,
        postalAddress2:"",
        postalLocationTown:"",
        postalLocationCountry:"",
        postCode:"",
        physicalAddress1:"",
        physicalAddress2:"",
        plotNumber:"",
        locationTown:"",
        locationCountry:locationCountry,
        datePhysicalAddress:"",
        pinNumber:member.pinNumber,
        consumerWorkE:member.emailAddress,
        employerName:station.name,
        employerIndustryType:"",
        employmentDate:"",
        employeeType:employementType.name,
        salaryBand:"",
        lendersTradingName:"CHAI SACCO LTD",
        lendersRegisteredName:"CHAI SACCO LTD",
        lendersBranchName:branch.groupName,
        lendersBranchCode:"",
        accountJointSingleIndicator:"S",
        accountProductType:"H",
        instalmentDueDate:"",
        dateAccountOpened:dateAccountOpened,
        originalAmount:obj.approvedAmt,
        currencyFacility:"KES",
        amountKSH:obj.approvedAmt,
        currentBalance:currentLoanBalance,
        overdueBalance:"",
        overdueDate:obj.nextInstallmentDate,
        noDaysArrears: daysInArrears,
        noInstalmentsInArrears:noInstalmentsInArrears,
        performingNonPerforming:performingNonPerforming,
        accountStatus:accountStatus,
        accountStatusDate:currentDate,
        accountClosureReason:"",
        repaymentPeriod:obj.repaymentPeriod,
        deferredPaymentDate:"",
        deferredPaymentAmount:"",
        m:"",
        disbursementDate:obj.disbursementDate,
        instalmentAmount:formatedloanRepaymentAmount,
        lastPaymentDate:formatedLastRepaymentDate,
        lastLoanPayment:loanRepaymentAmount,
        typeofSecurity:"S"

    ]

    crbReportList.add(crbReportListBuilder);

}



context.crbReportList = crbReportList