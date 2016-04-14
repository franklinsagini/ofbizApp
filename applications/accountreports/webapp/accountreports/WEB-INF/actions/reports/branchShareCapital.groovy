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


shareCapital = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10040".toLong())
shareCapitalTotaling = BranchUtilServices.getBranchAccountProductTotalsTotal(delegator, startDate, "10040".toLong())

shareCapitalBroughForw = 0
closingBalance= 0
totalShareCapital = 0
shareCapitalBroughForward = 0
shareCapitalBroughForwardTotali = 0
totalRepaid = 0
branches.each { branch ->
    branchShareCapitalAmount = 0
    branchShareCapitalAmountToThatDate = 0
    branchRepaid = 0

      shareCapitalTotaling.each { branchTotalB4StartDate ->

        if (branchTotalB4StartDate.branchId == branch.partyId) {
            branchShareCapitalAmountToThatDate = branchShareCapitalAmountToThatDate + branchTotalB4StartDate.transactionAmount
        }
    }
 
    shareCapital.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchShareCapitalAmount = branchShareCapitalAmount + contribution.transactionAmount
        }
    }
    
    totalShareCapital = totalShareCapital+branchShareCapitalAmount
    totalRepaid = totalRepaid+branchRepaid
    
    branchShareCapitalAmountToThatDateCal = branchShareCapitalAmountToThatDate
    branchShareCapitalAmountCal = branchShareCapitalAmount
    shareCapitalBroughForward = branchShareCapitalAmountToThatDateCal - branchShareCapitalAmountCal
    shareCapitalBroughForwardTotali = shareCapitalBroughForwardTotali + branchShareCapitalAmountToThatDateCal
    shareCapitalBroughForw = shareCapitalBroughForw + branchShareCapitalAmountToThatDate
    closingBalance = shareCapitalBroughForw +  totalShareCapital

     totalsBuilder = [
            branchName : branch.groupName,
            shareCapitalAmount : branchShareCapitalAmount,
            balanceBroughtForward : branchShareCapitalAmountToThatDate,
            closingBalance: branchShareCapitalAmount + branchShareCapitalAmountToThatDate,
            repaidTotal : branchRepaid
        ]
    totalsList.add(totalsBuilder)

}


totalsList.add(UtilMisc.toMap("branchName", "GRAND TOTALS","balanceBroughtForward", shareCapitalBroughForw, "shareCapitalAmount",  totalShareCapital, "closingBalance", closingBalance, "repaidTotal",  totalRepaid))
context.startDate = parameters.startDate
context.endDate = parameters.endDate
context.totalsList = totalsList
