import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;
import org.ofbiz.accounting.ledger.GeneralLedgerServices;
import org.ofbiz.accounting.branchreports.BranchUtilServices
exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

totalsBuilder = []
totalsList = [];

memberStatusListBuilder = []
memberStatusList = [];
runningBalance = 0
stationId = parameters.stationId

stationLong = stationId.toLong();

  expre = exprBldr.AND() {
	EQUALS(stationId : stationId)
	}



stationName = delegator.findList("Station", expre,null,null, null, false)

stationName.eachWithIndex{ stationame ,index ->

	nameOfStation = stationame.name
	stationCode = stationame.employerCode
	context.nameOfStation = nameOfStation
	context.stationCode = stationCode

}

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
glAccountId = '40000030'
transactions = GeneralLedgerServices.getTransactionsForGLByPeriod(delegator, glAccountId,startDate, endDate)

summaryCondition = [];
stationAccountTransactions = [];
accountTransactions = [];
transactions.each { transaction ->
	
	transCond = []
	transCond.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, transaction.acctgTransId));

	if (transaction.debitCreditFlag == "D") {
		//GET FROM ACCOUNT TRANSACTION
		accountTransactions = delegator.findList('AccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)
		if (accountTransactions != null) {
			singleAccountTransaction = accountTransactions[0]
			if (singleAccountTransaction != null) {
				if (singleAccountTransaction.partyId != null) {
					memberStationId = BranchUtilServices.getMembersStations(delegator, singleAccountTransaction.partyId)
					if (memberStationId != null) {
					if (memberStationId == stationId) {
					runningBalance = runningBalance + transaction.amount
					        memberStatusListBuilder = [
					        	transactionId : transaction.acctgTransId,	
					         	transactionDate : transaction.createdStamp,
					         	chequeNo : "-",
					         	descriptor : "",
					         	debitAmount : transaction.amount,
					         	description : "Total Amount disbursed to MPA",
					         	runningBalance : runningBalance,
					        ] 
					        memberStatusList.add(memberStatusListBuilder)

					}
				     }
					println "Member Station ID" + stationId;
				}
			}
		}
		
		


	}else if (transaction.debitCreditFlag == "C") {
		//GET FROM ACCOUNT TRANSACTION
		stationAccountTransactions = delegator.findList('StationAccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)

		if (stationAccountTransactions != null) {
			singleTransaction = stationAccountTransactions[0]
			
			if (singleTransaction != null) {
								if (singleTransaction.stationId != null) {
					if (singleTransaction.stationId == stationLong) {
						println ">>>>>>>>>>>>>>>>>>>>>>> OBJ "+singleTransaction.stationId;
			if (transaction.amount < 0) {
				runningBalance = runningBalance  + transaction.amount
			}else{
				runningBalance = runningBalance  - transaction.amount
			}
		
		        memberStatusListBuilder = [
		        	transactionId :transaction.acctgTransId,	
		         	transactionDate : singleTransaction.createdStamp,
		         	chequeNo : singleTransaction.chequeNumber,
		         	descriptor : singleTransaction.monthyear,
		         	creditAmount : transaction.amount,
		         	description : "cheque for (month and Year) "+singleTransaction.monthyear,
		         	runningBalance : runningBalance,
		        ] 
		        memberStatusList.add(memberStatusListBuilder)

					}
					
				}	
			}			
		}

			

		}

		


	}


context.collectorResults = memberStatusList






