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

reportId = "7"

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
  if (codeString.toInteger() < 30){
    System.out.println("FROOOOOOOOOOOOOOOOOOOOOOOOOM " + fromDate)
    System.out.println("THRUUUUUUUUUUUUUUUUUU " + thruDate)
    System.out.println("REEEEEEEEEEEEEEEEEEEEEEEEE" + item.reportId)
    System.out.println("COOOOOOOOODDDDDDDEEEEEE " + item.code)
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, reportId, item.code)
    finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }

  context.incomeList = finalReportItemList

  if (codeString.toInteger() > 29 && codeString.toInteger() < 40){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)

  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList



 if (codeString.toInteger() > 39 && codeString.toInteger() < 50){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() > 49 && codeString.toInteger() < 60){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() > 59 && codeString.toInteger() < 70){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() > 69 && codeString.toInteger() < 80){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() > 79 && codeString.toInteger() < 70){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() > 89 && codeString.toInteger() < 100){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    incomeFromInvestmentsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.incomeFromInvestmentsList = incomeFromInvestmentsList


 if (codeString.toInteger() == 100){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    netIncomeBeforeTaxesAndDonationsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.netIncomeBeforeTaxesAndDonationsList = netIncomeBeforeTaxesAndDonationsList


 if (codeString.toInteger() == 110){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    taxesList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.taxesList = taxesList



 if (codeString.toInteger() == 120){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    netIncomeAfterTaxesBeforeDonationsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.netIncomeAfterTaxesBeforeDonationsList = netIncomeAfterTaxesBeforeDonationsList




 if (codeString.toInteger() == 130){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    donationsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.donationsList = donationsList


 if (codeString.toInteger() == 140){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    netIncomeAfterTaxesAndDonationsList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.netIncomeAfterTaxesAndDonationsList = netIncomeAfterTaxesAndDonationsList


 if (codeString.toInteger() == 150){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    statutoryReservesList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.statutoryReservesList = statutoryReservesList


 if (codeString.toInteger() == 160){
    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, item.reportId, item.reportItemId)
    revenueReservesList.add("code" : item.code, "name" : item.name, "amount" : amount)
  }
  context.revenueReservesList = revenueReservesList





}//End of each






System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF " + finalReportItemList)
