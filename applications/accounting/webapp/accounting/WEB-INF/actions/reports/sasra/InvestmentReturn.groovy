import org.ofbiz.base.util.UtilMisc;
investmentReturnList = [];
componentMap = []
landBuildingAssetRatio = BigDecimal.ZERO
landBuildingAmount = BigDecimal.ZERO
totalAssetAmount = BigDecimal.ZERO
financialAssets = BigDecimal.ZERO
coreCapital = BigDecimal.ZERO
totalDeposit = BigDecimal.ZERO
nonEarningAssets = BigDecimal.ZERO

investmentReturnList = delegator.findList("InvestmentReturnReport", null, UtilMisc.toSet("code", "attribName", "amount"), UtilMisc.toList("code"), null, false);
if (investmentReturnList) {
  investmentReturnList.each { investment ->
    def codeString = investment.code
    codeString = codeString.replace(".", "");

      if (codeString.toInteger() == 16) {
        if (investment.amount > 0) {
          landBuildingAmount = investment.amount
        }
      }

      if (codeString.toInteger() == 15) {
        if (investment.amount > 0) {
          financialAssets = investment.amount
        }
      }
      if (codeString.toInteger() == 14) {
        if (investment.amount > 0) {
          nonEarningAssets = investment.amount
        }
      }
      if (codeString.toInteger() == 13) {
        if (investment.amount > 0) {
          totalDeposit = investment.amount
        }
      }

      if (codeString.toInteger() == 11) {
        if (investment.amount > 0) {
          coreCapital = investment.amount
        }
      }

      if (codeString.toInteger() == 12) {
        if (investment.amount > 0) {
          totalAssetAmount = investment.amount
        }
      }
  }//END OF INTERATION
  if (totalAssetAmount > 0 ) {
    landBuildingAssetRatio = (landBuildingAmount/totalAssetAmount)*100
  }


}//END OUTER-MOST IF
 requiredLandAssetRatio = 5.00
 institutionalCapitalMap = [code:"2.0", attribName:"Land and Building to Total Assets Ratio (1.6/1.2)%", amount:landBuildingAssetRatio]
 reqLandBuildingToTotallMap = [code:"2.1", attribName:"Required Minimum Land Bulding to Total Asset Ratio", amount:requiredLandAssetRatio]

 landAssetDeficiancy = landBuildingAssetRatio - requiredLandAssetRatio
 landAssetDeficiancyMap = [code:"2.2", attribName:"Excess Deficiancy (2.0 less 2.1)", amount:landAssetDeficiancy]
 investmentReturnList.add(institutionalCapitalMap)
 investmentReturnList.add(reqLandBuildingToTotallMap)
 investmentReturnList.add(landAssetDeficiancyMap)

  if (coreCapital > 0 ) {
    finInvestToCoreRatio = (financialAssets/coreCapital)*100
  }

 finInvestToCoreCapMap = [code:"3.0", attribName:"Financial Investment to Core Capital (1.5/1.1)", amount:finInvestToCoreRatio]
 investmentReturnList.add(finInvestToCoreCapMap)

 requiredFinInvestToCoreRatio = 4.00
 requiredFinInvestToCoreRatioMap = [code:"3.1", attribName:"Minimum Financial Investment to Core Capital", amount:requiredFinInvestToCoreRatio]
 investmentReturnList.add(requiredFinInvestToCoreRatioMap)

 //Excess Deficiency
 diffFinInvestToCore = finInvestToCoreRatio - requiredFinInvestToCoreRatio
 diffFinInvestToCoreMap = [code:"3.2", attribName:"Excess/Deficiency (3.0-3.1)", amount:diffFinInvestToCore]
 investmentReturnList.add(diffFinInvestToCoreMap)


 finIvestToDepo = (financialAssets/totalDeposit)*100
 diffFinInvestToCoreMap = [code:"4.0", attribName:"Financial Investment to Total Deposits Liability Ratios (1.5/1.3)", amount:finIvestToDepo]
 investmentReturnList.add(diffFinInvestToCoreMap)

 requiredFinInvestToDepo = 5.00
 requiredFinInvestToDepoMap = [code:"4.1", attribName:"Minimum Financial Investment to Total Deposits and Liabilities", amount:requiredFinInvestToDepo]
 investmentReturnList.add(requiredFinInvestToDepoMap)



 //Excess Deficiency
 diffFinInvestToDepo = finIvestToDepo - requiredFinInvestToDepo
 diffFinInvestToDepoMap = [code:"4.2", attribName:"Excess/Deficiency (4.0 - 4.1)", amount:diffFinInvestToDepo]
 investmentReturnList.add(diffFinInvestToDepoMap)


nonEarningAssetsToAssetsRatio = (nonEarningAssets/totalAssetAmount)*100
nonEarningAssetsToAssetsMap = [code:"5.0", attribName:"Non-Earning Assets to Total Assets Ratio (1.4/1.2)", amount:nonEarningAssetsToAssetsRatio]
investmentReturnList.add(nonEarningAssetsToAssetsMap)


 maxNonEarningAssetsRatio = 5.00
 maxNonEarningAssetsMap = [code:"5.1", attribName:"Maximum Non Earning Assets Ratio % ", amount:maxNonEarningAssetsRatio]
 investmentReturnList.add(maxNonEarningAssetsMap)


 //Excess Deficiency
 maxNonEarningAssets = nonEarningAssetsToAssetsRatio - maxNonEarningAssetsRatio
 diffFinInvestToDepoMap = [code:"5.2", attribName:"Excess/Deficiency (5.0 - 5.1)", amount:maxNonEarningAssets]
 investmentReturnList.add(diffFinInvestToDepoMap)

 context.investmentReturnList = investmentReturnList;
