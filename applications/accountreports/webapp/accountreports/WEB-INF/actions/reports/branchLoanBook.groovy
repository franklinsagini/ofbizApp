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
 import  org.ofbiz.accounting.ledger.CrbReportServices
 import java.text.SimpleDateFormat;
 import org.ofbiz.accounting.branchreports.BranchUtilServices

totalsBuilder = []
totalsList = [];

memberStatusListBuilder = []
memberStatusList = [];


java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

if ((parameters.startDate?.trim())){
    dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parameters.startDate);

    sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((parameters.endDate?.trim())){
    dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parameters.endDate);
    sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

startDateTimestamp = new Timestamp(sqlStartDate.getTime());
endDateTimestamp = new Timestamp(sqlEndDate.getTime());

startDate = UtilDateTime.getDayStart(startDateTimestamp)
endDate = UtilDateTime.getDayEnd(endDateTimestamp)

// GET THE BRANCHES
summaryCondition = []
summaryCondition.add(EntityCondition.makeCondition("isBranch", EntityOperator.EQUALS, "Y"));
summaryCondition.add(EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, "Company"));
branches = delegator.findList('PartyGroup',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,null,null,false)

// GET THE LOAN GRANTED
//loansDisbursedConditions = []
//loansDisbursedConditions.add(EntityCondition.makeCondition("disbursementDate", EntityOperator.GREATER_THAN_EQUAL_TO, startDate))
//loansDisbursedConditions.add(EntityCondition.makeCondition("disbursementDate", EntityOperator.LESS_THAN_EQUAL_TO, endDate))
//loansDisbursedConditions.add(EntityCondition.makeCondition("loanStatusId", EntityOperator.LESS_THAN_EQUAL_TO, "6".toLong()))

//myLoansList = delegator.findList("LoanApplication", EntityCondition.makeCondition(loansDisbursedConditions, EntityOperator.AND), null, ["disbursementDate ASC"], null, false)

myLoansList = BranchUtilServices.getLoansForPeriod(delegator, startDate, endDate,"6".toLong())
myRepaymentsList = BranchUtilServices.getRepaymentForPeriod(delegator, startDate, endDate)
broughtForwardGrantedList = BranchUtilServices.getLoansForPeriod(delegator, startDate,"6".toLong())
broughtForwardRepaidList = BranchUtilServices.getRepaymentForPeriod(delegator, startDate)

totalGranted = 0
totalRepaid = 0
totalbranchGrantedCL = 0
totalBranchGrantedBF = 0
totalbranchRepaidBF = 0
totalBranchRepaidCL = 0
branches.each { branch ->
    branchGranted = 0
    branchGrantedBF = 0
    branchRepaid = 0
    branchRepaidBF = 0

    myRepaymentsList.each { repayment ->
        //get members branch
        branchId = BranchUtilServices.getMembersBranch(delegator, repayment.partyId)
        if (branchId == branch.partyId) {
            branchRepaid = branchRepaid + repayment.totalPrincipalDue
        }
    }

    broughtForwardRepaidList.each { repayment ->
        //get members branch
        branchId = BranchUtilServices.getMembersBranch(delegator, repayment.partyId)
        if (branchId == branch.partyId) {
            branchRepaidBF = branchRepaidBF + repayment.totalPrincipalDue
        }
    }

    myLoansList.each { loan ->
        //get members branch
        branchId = BranchUtilServices.getMembersBranch(delegator, loan.partyId)
        if (branchId == branch.partyId) {
            branchGranted = branchGranted + loan.loanAmt
        }
    }


    broughtForwardGrantedList.each { loan ->
        //get members branch
        branchId = BranchUtilServices.getMembersBranch(delegator, loan.partyId)
        if (branchId == branch.partyId) {
            branchGrantedBF = branchGrantedBF + loan.loanAmt
        }
    }

    totalGranted = totalGranted+branchGranted
    totalRepaid = totalRepaid+branchRepaid
    branchGrantedCL = branchGrantedBF + branchGranted
    totalBranchGrantedBF = totalBranchGrantedBF + branchGrantedBF
    totalbranchGrantedCL = totalbranchGrantedCL + branchGrantedCL
    branchRepaidCL = branchRepaidBF + branchRepaid
    totalbranchRepaidBF = totalbranchRepaidBF + branchRepaidBF
    totalBranchRepaidCL = totalBranchRepaidCL + branchRepaidCL

    totalsBuilder = [
            branchName : branch.groupName,
            broughtForwardGranted : branchGrantedBF,
            grantedTotal : branchGranted,
            closingBalanceGranted : branchGrantedCL,
            broughtForwardRepaid : branchRepaidBF,
            repaidTotal : branchRepaid,
            closingBalanceRepaid : branchRepaidCL
        ]

    totalsList.add(totalsBuilder)



}


totalsList.add(UtilMisc.toMap("branchName", "GRAND TOTALS",
                                            "broughtForwardGranted",  totalBranchGrantedBF,
                                            "grantedTotal",  totalGranted,
                                            "closingBalanceGranted",  totalbranchGrantedCL,
                                            "broughtForwardRepaid",  totalbranchRepaidBF,
                                            "repaidTotal",  totalRepaid,
                                            "closingBalanceRepaid",  totalBranchRepaidCL
                                            )
                    )
context.startDate = parameters.startDate
context.endDate = parameters.endDate
context.totalsList = totalsList

