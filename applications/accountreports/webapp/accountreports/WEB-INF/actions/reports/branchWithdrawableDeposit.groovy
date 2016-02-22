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



savingsAccount = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10010".toLong())
chaiJunior = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10011".toLong())
chaiPremierHoliday = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10012".toLong())
fixedDeposit = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10013".toLong())
shareCapital = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10041".toLong())
medical = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10042".toLong())
biashara = BranchUtilServices.getBranchAccountProductTotals(delegator, startDate, endDate, "10043".toLong())

totalMemberDeposits = 0
totalRepaid = 0
branches.each { branch ->
    branchSavingsAccountAmount = 0
    branchChaiJuniorAmount = 0
    branchChaiPremierHolidayAmount = 0
    branchFixedDepositAmount = 0
    branchMedicalAccountAmount = 0
    branchBiasharaAccountAmount = 0
    branchRepaid = 0

 
    savingsAccount.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchSavingsAccountAmount = branchSavingsAccountAmount + contribution.transactionAmount
        }
    }    
    chaiJunior.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchChaiJuniorAmount = branchChaiJuniorAmount + contribution.transactionAmount
        }
    }    
    chaiPremierHoliday.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchChaiPremierHolidayAmount = branchChaiPremierHolidayAmount + contribution.transactionAmount
        }
    }    
    fixedDeposit.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchFixedDepositAmount = branchFixedDepositAmount + contribution.transactionAmount
        }
    }

    medical.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchMedicalAccountAmount = branchMedicalAccountAmount + contribution.transactionAmount
        }
    }

    biashara.each { contribution ->

        if (contribution.branchId == branch.partyId) {
            branchBiasharaAccountAmount = branchBiasharaAccountAmount + contribution.transactionAmount
        }
    }

//    totalMemberDeposits = totalMemberDeposits+branchMemberDepositAmount
//    totalRepaid = totalRepaid+branchRepaid

     totalsBuilder = [
            branchName : branch.groupName,
            savingsAccount : branchSavingsAccountAmount,
            chaiAngelsJuniorAccount : branchChaiJuniorAmount,
            chaiPremierHolidayAccount : branchChaiPremierHolidayAmount,
            fixedDeposit : branchFixedDepositAmount,
            medical : branchMedicalAccountAmount,
            biashara : branchBiasharaAccountAmount
        ]
    totalsList.add(totalsBuilder)

}


context.startDate = parameters.startDate
context.endDate = parameters.endDate
context.totalsList = totalsList
