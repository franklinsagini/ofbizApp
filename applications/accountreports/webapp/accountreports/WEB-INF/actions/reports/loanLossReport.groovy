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




//All disbursed loans
myLoansList = BranchUtilServices.getLoansForPeriod(delegator, startDate, endDate,"6".toLong())

totalActive = 0
totalWatchful = 0
totalSubstandard = 0
totalDoubtFull = 0
totalLoss = 0

myLoansList.each { loan ->
    daysInArrears = 0
    currentLoanBalance = 0
    daysInArrears = CrbReportServices.lastRepaymentDurationToDateInDays(loan.loanApplicationId)
    currentLoanBalance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loan.loanApplicationId)

    if (daysInArrears.toInteger()<60) {
        //Watchful
        totalWatchful = totalWatchful + currentLoanBalance
        totalActive = totalActive + currentLoanBalance
    }else if( daysInArrears.toInteger()>60 && daysInArrears.toInteger() < 91) {
        //Substandard
        totalSubstandard = totalSubstandard + currentLoanBalance
    }else if( daysInArrears.toInteger()>90 && daysInArrears.toInteger() < 121) {
        //DOUBTFULL LOANS
        totalDoubtFull = totalDoubtFull + currentLoanBalance
    }else{
        //Loan Loss
        totalLoss = totalLoss + currentLoanBalance
    }

}

totalsList.add(UtilMisc.toMap("category", "PERFORMING", "active",totalActive ))
totalsList.add(UtilMisc.toMap("category", "WATCH LOANS", "watchLoans",totalWatchful))
totalsList.add(UtilMisc.toMap("category", "SUBSTANDARD LOANS", "substandardLoans",totalSubstandard))
totalsList.add(UtilMisc.toMap("category", "DOUBTFUL LOANS", "doubtfulLoans",totalDoubtFull))
totalsList.add(UtilMisc.toMap("category", "LOSS LOANS", "lossLoans",totalLoss))


context.startDate = parameters.startDate
context.endDate = parameters.endDate
context.loansLossList = totalsList

