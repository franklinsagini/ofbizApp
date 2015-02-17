import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.ledger.SasraReportsService;
import javolution.util.FastList;



reportItemList = [];
finalReportItemList = [];
reportId = "5"
landBuildingCode = null
totalAssets = null

List mainAndExprs = FastList.newInstance();


//get the passed date
if (!fromDate) {
    return;
}
if (!thruDate) {
    thruDate = UtilDateTime.nowTimestamp();
}
if (!glFiscalTypeId) {
    return;
}


mainAndExprs.add(EntityCondition.makeCondition("reportId", EntityOperator.EQUALS, reportId));
reportItemList = delegator.findList("SasraReportItem", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("code", "name"), UtilMisc.toList("code"), null, false);

reportItemList.each { item ->


    amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, reportId, item.code)
    finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)



    def codeString = item.code
    codeString = codeString.replace(".", "");

    if(codeString.toInteger() == 12){
      //get total assets
      totalAssetsCode = item.code
    }

    if(codeString.toInteger() == 16){
      //get land and building code
      landBuildingCode = item.code
    }

    //calculate land building to total asset ratio and update the map
    if (codeString.toInteger() == 20) {
      System.out.println("System landBuildingCodeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee " + landBuildingCode)
      System.out.println("System totalAssetsssssssssssssssssssssssssssssssssssssssss " + totalAssetsCode)
      System.out.println("System totalAssetsssssssssssssssssssssssssssssssssssssssss " + fromDate)
      System.out.println("System totalAssetsssssssssssssssssssssssssssssssssssssssss " + thruDate)
      System.out.println("System totalAssetsssssssssssssssssssssssssssssssssssssssss " + reportId)

      amount = SasraReportsService.getReportItemRatio(fromDate, thruDate, reportId, landBuildingCode, totalAssetsCode)
      System.out.println("System AMMMMMMMMMMMMMMMMMMMMMMMMMMMMM " + amount)
    }


    if(codeString.toInteger() == 16){
      //get land and building code
      landBuildingCode = item.code
    }



}


 context.investmentReturnList = finalReportItemList;
