import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.ledger.SasraReportsService;
import javolution.util.FastList;


if (!fromDate) {
    return;
}
if (!thruDate) {
    thruDate = UtilDateTime.nowTimestamp();
}
if (!glFiscalTypeId) {
    return;
}

reportId = "2"

reportItemList = [];
List finalReportItemList = [];
List incomeFromInvestmentsList = [];
List revenueReservesList = [];
List statutoryReservesList = [];
List netIncomeAfterTaxesAndDonationsList = [];
List donationsList = [];
List netIncomeAfterTaxesBeforeDonationsList = [];
List netIncomeBeforeTaxesAndDonationsList = [];
List taxesList = [];
List newReportItemList = [];
List mainAndExprs = FastList.newInstance();

mainAndExprs.add(EntityCondition.makeCondition("reportId", EntityOperator.EQUALS, reportId));
reportItemList = delegator.findList("SasraReportItem", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("code", "name"), UtilMisc.toList("code"), null, false);
amount = 56.23
System.out.println("VALUEEEEEEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSS" + reportItemList)
reportItemList.each { item ->

  //get code
  def codeString = item.code
  codeString = codeString.replace(".", "");

    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, reportId, item.code)
    finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeList = finalReportItemList
