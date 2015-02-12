
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import javolution.util.FastList;





assetsList = [];
liabilityList = [];
equityList = [];

reportId = "6"
List mainAndExprs = FastList.newInstance();

mainAndExprs.add(EntityCondition.makeCondition("reportId", EntityOperator.EQUALS, reportId));
	
 List<String> orderByList = new ArrayList<String>();
 orderByList.add("code");
 
 statement = delegator.findList("SasraReportItem", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), null, orderByList, null, false);
 
   statement.eachWithIndex { statementItem, index ->
      def codeString = statementItem.code
	   codeString = codeString.replace(".", "");

      if ((codeString.toInteger() > 0) && (codeString.toInteger() < 67)) {
		
		acode = statementItem.code;
        aname = statementItem.name;
		//abalance=0;
		abalance = org.ofbiz.accounting.ledger.SasraReportsService.getReportItemTotals(fromDate, thruDate, statementItem.reportId, statementItem.code);
		System.out.println(">>>>>>>from date>"+fromDate+ ">>To date>>"+thruDate+ ">>reportId>>"+statementItem.reportId+ ">>code>>" +statementItem.code);
		 assetMap = [code :acode, name : aname, balance :abalance]
		 assetsList.add(assetMap);
      }
	  
	   if ((codeString.toInteger() > 68) && (codeString.toInteger() < 109)) {
		lcode = statementItem.code;
          lname = statementItem.name;
 
 //lbalance=0;
          lbalance = org.ofbiz.accounting.ledger.SasraReportsService.getReportItemTotals(fromDate, thruDate, statementItem.reportId, statementItem.code);
		 liabilitiesMap = [code :lcode, name : lname, balance :lbalance]
		 liabilityList.add(liabilitiesMap);
      }

	   if ((codeString.toInteger() > 110) && (codeString.toInteger() < 220)) {
		ecode = statementItem.code;
       ename = statementItem.name;
 
 //ebalance=0;
        ebalance = org.ofbiz.accounting.ledger.SasraReportsService.getReportItemTotals(fromDate, thruDate, statementItem.reportId, statementItem.code);
		 equityMap = [code :ecode, name : ename, balance :ebalance]
		 equityList.add(equityMap);
      }



    }
	
 
 
context.assetsList = assetsList;
context.liabilityList = liabilityList;
context.equityList = equityList;
context.statement = statement;
context.fromDate = fromDate;
context.thruDate = thruDate;
	


