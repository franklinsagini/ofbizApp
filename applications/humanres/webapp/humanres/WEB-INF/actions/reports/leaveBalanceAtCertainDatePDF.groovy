import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

asFromDate = parameters.fromDate
partyId = parameters.partyId
leaveType = parameters.leaveType
context.leaveType = leaveType
context.title = "Chai Sacco"

java.sql.Date sqlFromDate = null;
dateFromDate = null;

def accruedBal = BigDecimal.ZERO;
def forwardedBal = BigDecimal.ZERO;
def usedLeaveBal = BigDecimal.ZERO;
def totalDays = BigDecimal.ZERO;
def balances = BigDecimal.ZERO;

dateFromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(asFromDate);
sqlFromDate = new java.sql.Date(dateFromDate.getTime());

fromDateTimestamp = new Timestamp(sqlFromDate.getTime());

if (partyId && asFromDate) { 
    employee = delegator.findOne("LeavesBalanceView", [partyId : partyId], false);
    
    accruedBal = org.ofbiz.humanres.LeaveServices.getAccruedLeavesAtThisdate(sqlFromDate, partyId);
    forwardedBal = org.ofbiz.humanres.LeaveServices.getForwardedLeaves(partyId);
    usedLeaveBal = org.ofbiz.humanres.LeaveServices.getTotalUsedDays(sqlFromDate, partyId);
    totalDays = org.ofbiz.humanres.LeaveServices.totalLeaveDays(forwardedBal, accruedBal);
    balances = org.ofbiz.humanres.LeaveServices.balanceLeave(totalDays, usedLeaveBal);
    
    context.employee = employee;
    context.accruedBal = accruedBal
    context.forwardedBal = forwardedBal
    context.usedLeaveBal = usedLeaveBal
    context.totalDays = totalDays
    context.balances = balances
    
    context.sqlFromDate = sqlFromDate
    
   return
   
}


   expr = exprBldr.AND() {
			NOT_EQUAL(employmentStatusEnumId: "15")
		}
		
    EntityFindOptions findOptions = new EntityFindOptions();
	findOptions.setMaxRows(100);
	leaveBalancelist = [];
	leaveBalances = delegator.findList("LeavesBalanceView", expr, null, null, findOptions, false);

    leaveBalances.eachWithIndex{leaveB , index ->
    
    leaveItem = delegator.makeValue("LeaveReportItem",null);
    
            personDetail =  delegator.findOne("Person",[partyId : leaveB.partyId ],false);
            firstname =  personDetail.firstName
            lastname =  personDetail.lastName
           
            name = firstname+"  "+lastname
            
            
    
		    accruedBal = org.ofbiz.humanres.LeaveServices.getAccruedLeavesAtThisdate(sqlFromDate, leaveB.partyId);
		    forwardedBal = org.ofbiz.humanres.LeaveServices.getForwardedLeaves(leaveB.partyId);
		    usedLeaveBal = org.ofbiz.humanres.LeaveServices.getTotalUsedDays(sqlFromDate, leaveB.partyId);
		    totalDays = org.ofbiz.humanres.LeaveServices.totalLeaveDays(forwardedBal, accruedBal);
		    balances = org.ofbiz.humanres.LeaveServices.balanceLeave(totalDays, usedLeaveBal);
     
            println("###########  inside Groovy NAMES##"+name)
            println("###########  inside Accrued##"+accruedBal)
            println("###########  inside Groovy SQL Date"+sqlFromDate)
            println("########## inside Groovy Party Id"+leaveB.partyId)
      
            leaveListBuilder = [
              names :name,
              accrued : accruedBal,
              forwarded :forwardedBal,
              usedLeave : usedLeaveBal,
              totalDays : totalDays,
              balances : balances
              ]
          
            leaveBalancelist.add(leaveListBuilder);
             println("#######leaveListBuilder#####"+leaveListBuilder.accrued);
       }
 
       context.leaveBalances = leaveBalances;
       context.leaveBalancelist = leaveBalancelist;
       context.sqlFromDate = sqlFromDate
    




















