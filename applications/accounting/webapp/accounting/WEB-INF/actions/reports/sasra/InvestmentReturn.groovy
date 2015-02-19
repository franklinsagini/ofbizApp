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

minimumRequiredLandBuildingToAsset = 5.00
landBuildingAssetRatio = BigDecimal.ZERO;

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

    if(codeString.toInteger() == 15){
    //get land and building code
    financialAssetCode = item.code
    }

    if(codeString.toInteger() == 11){
    //get land and building code
    coreCapitalCode = item.code
    }


    //calculate land building to total asset ratio and update the map
    if (codeString.toInteger() == 20) {
      landBuildingAssetRatio = SasraReportsService.getReportItemRatioPercentage(fromDate, thruDate, reportId, landBuildingCode, totalAssetsCode)
      finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
    }else if(codeString.toInteger() == 21){
      amount = minimumRequiredLandBuildingToAsset
      finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)

    }else if(codeString.toInteger() == 22){
    //calculate excess deficiency
      amount = landBuildingAssetRatio - minimumRequiredLandBuildingToAsset

      finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
    }else if(codeString.toInteger() == 30){
    //calculate excess deficiency
      finacialCapitalToCoreCapitalRatio = SasraReportsService.getReportItemRatio(fromDate, thruDate, reportId, financialAssetCode, coreCapitalCode)

      finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
    }else{
      //else just get the totals as usual and update the map
      amount = SasraReportsService.getReportItemTotals(fromDate, thruDate, reportId, item.code)
      finalReportItemList.add("code" : item.code, "name" : item.name, "amount" : amount)
    }
}
 context.investmentReturnList = finalReportItemList;
