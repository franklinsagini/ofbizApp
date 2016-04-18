import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;


startDate = parameters.startDate
endDate = parameters.endDate
stationId = parameters.stationId

println "####################### STATION ID: "+ stationId

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

//get all loan types
//stations = delegator.findList('Station', null, null,null,null,false)
//context.stations = stations
//loanApps = delegator.findList('LoansByStations', null, null,null,null,false)
//context.loanApps = loanApps

stationLoansList = delegator.findByAnd("LoansByStations",  [stationId : stationId], null, false)



combinedList = [];
def loanItem;

def loanBalance = BigDecimal.ZERO;

stationLoansList.eachWithIndex { loan, index ->
	loanItem = loan.loanNo
	
	member = delegator.findOne("Member", [partyId : loan.partyId], false);
	names = member.firstName+" "+member.middleName+" "+member.lastName;
	
	payrollNumber = member.payrollNumber.trim();
	memberNumber = member.memberNumber.trim();
	idNumber = member.idNumber.trim();
		print " --------payrollNumber"+payrollNumber
	
	
	loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : loan.loanApplicationId], false);
	disbursementDate = loanApplication.disbursementDate;
	loanBalance = loanApplication.loanAmt - org.ofbiz.loans.LoanServices.getLoansRepaidByLoanApplicationId(loanApplication.loanApplicationId);
	loanBalance = loanBalance;
	
	
	
	
combinedList.add([loanNo :loanItem, fullName :names, payrollNumber : payrollNumber,
 memberNumber :memberNumber, idNumber : idNumber, disbursementDate : disbursementDate, loanBalance : loanBalance]);
}

context.combinedList = combinedList;