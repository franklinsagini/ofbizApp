import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;

import freemarker.ext.xml.Navigator.TypeOp;

import java.text.SimpleDateFormat;

import javolution.util.FastList;

year = parameters.year
startDate = parameters.startDate
endDate = parameters.endDate


print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
	
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

print "formatted Date"
//println dateStartDate
//println dateEndDate


println "RRRRRRRRRRRRRR EAL DATES !!!!!!!!!!!!!"
println startDate
println endDate

context.sqlStartDate = sqlStartDate
context.sqlEndDate = sqlEndDate
context.year = year


class RiskClassItem{
	def no;
	def classification;
	def noOfAccounts;
	def outStandingLoanPortifolio;
	def requiredProvision;
	def requiredProfisionAmount
	
}



listRiskClassItem = [];
//Performing

riskClassItem = new RiskClassItem();
riskClassItem.no = 1;
riskClassItem.classification = "Performing"
riskClassItem.noOfAccounts = 20;
riskClassItem.outStandingLoanPortifolio = 1600800;
riskClassItem.requiredProvision = "0%";
riskClassItem.requiredProfisionAmount = 8000;


listRiskClassItem << riskClassItem;

//Watch Loan
riskClassItem = new RiskClassItem();
riskClassItem.no = 2;
riskClassItem.classification = "Watch Loan"
riskClassItem.noOfAccounts = 11;
riskClassItem.outStandingLoanPortifolio = 15000;
riskClassItem.requiredProvision = "5%";
riskClassItem.requiredProfisionAmount = 4500;

listRiskClassItem << riskClassItem;

//Substandard Loan
riskClassItem = new RiskClassItem();
riskClassItem.no = 3;
riskClassItem.classification = "Substandard Loan"
riskClassItem.noOfAccounts = 1400;
riskClassItem.outStandingLoanPortifolio = 15000;
riskClassItem.requiredProvision = "25%";
riskClassItem.requiredProfisionAmount = 1300;

listRiskClassItem << riskClassItem;

//Doubtful Loan

riskClassItem = new RiskClassItem();
riskClassItem.no = 4;
riskClassItem.classification = "Doubtful Loan"
riskClassItem.noOfAccounts = 345;
riskClassItem.outStandingLoanPortifolio = 15000;
riskClassItem.requiredProvision = "50%";
riskClassItem.requiredProfisionAmount = 1300;

listRiskClassItem << riskClassItem;

//Loan Loss


riskClassItem = new RiskClassItem();
riskClassItem.no = 5;
riskClassItem.classification = "Loan Loss"
riskClassItem.noOfAccounts = 345;
riskClassItem.outStandingLoanPortifolio = 15000;
riskClassItem.requiredProvision = "100%";
riskClassItem.requiredProfisionAmount = 1300;

listRiskClassItem << riskClassItem;



context.listRiskClassItem = listRiskClassItem

context.totalCount = 12344
context.totalAmount = 1440000




