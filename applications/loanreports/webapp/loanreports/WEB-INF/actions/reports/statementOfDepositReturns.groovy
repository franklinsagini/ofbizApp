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

class DepositReturnItem{
	def no;
	def range;
	
	def listDepositType = [];
}

class TypeOfDeposit{
	def name;
	def noOfAccount;
	def amount;
}


listDepositReturnItem = [];
//1 to 50000

depositReturnItem = new DepositReturnItem();
depositReturnItem.no = 1;
depositReturnItem.range = "1 to 50000"

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Fosa Savings";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Non Withdrawal";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

listDepositReturnItem << depositReturnItem;

//50001 to 100000

depositReturnItem = new DepositReturnItem();
depositReturnItem.no = 2;
depositReturnItem.range = "50001 to 100000"

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Fosa Savings";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Non Withdrawal";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

listDepositReturnItem << depositReturnItem;

//100001 to 300000

depositReturnItem = new DepositReturnItem();
depositReturnItem.no = 3;
depositReturnItem.range = "100001 to 300000"

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Fosa Savings";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Non Withdrawal";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

listDepositReturnItem << depositReturnItem;


//300001 to 1000000

depositReturnItem = new DepositReturnItem();
depositReturnItem.no = 4;
depositReturnItem.range = "300001 to 1000000"

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Fosa Savings";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Non Withdrawal";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

listDepositReturnItem << depositReturnItem;

//1000001 to 1000,000,000

depositReturnItem = new DepositReturnItem();
depositReturnItem.no = 5;
depositReturnItem.range = "1000001 to 1000,000,000"

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Fosa Savings";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

typeOfDeposit = new TypeOfDeposit();
typeOfDeposit.name = "Non Withdrawal";
typeOfDeposit.noOfAccount = 1;
typeOfDeposit.amount = 0.0;

depositReturnItem.listDepositType << typeOfDeposit;

listDepositReturnItem << depositReturnItem;

context.listDepositReturnItem = listDepositReturnItem

context.totalCount = 10
context.totalAmount = 15




