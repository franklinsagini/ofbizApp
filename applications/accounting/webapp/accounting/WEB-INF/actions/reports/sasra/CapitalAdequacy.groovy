import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;

balanceTotal = 0
componentList = [];
List mainAndExprs = FastList.newInstance();
mainAndExprs.add(EntityCondition.makeCondition("isCapitalComponent", EntityOperator.EQUALS, "Y"));
componentList = delegator.findList("CapitalAdequacyReport", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("code", "name", "amount"), UtilMisc.toList("code"), null, false);
if (componentList) {
    coreCapitalSubtotal = BigDecimal.ZERO
    totalDeductions = BigDecimal.ZERO
    coreCapitalSubtotal = BigDecimal.ZERO
    coreCapitalSubtotal = BigDecimal.ZERO
    componentMap = [];
    totalDeductionsMap = [];
    coreCapitalMap = [];

    componentList.each { component ->
      def codeString = component.code
      codeString = codeString.replace(".", "");

      if (codeString.toInteger() < 118) {
        if (component.amount) {

          coreCapitalSubtotal = coreCapitalSubtotal.add(component.amount)
        }
      }else if (codeString.toInteger() >= 118 && codeString.toInteger() < 200 ){
        if (component.amount) {
          totalDeductions = coreCapitalSubtotal.subtract(component.amount)
        }
      }
      if(codeString.toInteger() == 111){
        shareCapital = component.amount
      }


    }
    coreCapitalTotal = coreCapitalSubtotal - totalDeductions
    institutionalCapital = coreCapitalTotal - shareCapital

    componentMap = [code:"1.1.8", name:"Sub-Total(1.1.1 to 1.1.7)", amount:coreCapitalSubtotal]
    totalDeductionsMap = [code:"1.2.1", name:"TOTAL DEDUCTIONS(1.1.9 to 1.2.0)", amount:totalDeductions]
    coreCapitalMap = [code:"1.2.2", name:"CORE CAPITAL (1.1.8 to 1.2.1)", amount:coreCapitalTotal]
    institutionalCapitalMap = [code:"1.2.3", name:"INSTITUTIONAL CAPITAL (1.2.2 less 1.2.1)", amount:institutionalCapital]
    componentList.add(componentMap);
    componentList.add(totalDeductionsMap);
    componentList.add(coreCapitalMap);
    componentList.add(institutionalCapitalMap);
    componentList.sort {it.code}
}

context.capitalAdequacyComponentsList = componentList;


//On Balance Sheet List
onBalanceSheetList = [];
totalMap = [];
onBalanceSheetTotal = BigDecimal.ZERO
List onBalanceSheetConditions = FastList.newInstance();
onBalanceSheetConditions.add(EntityCondition.makeCondition("isOnBalanceSheetAsset", EntityOperator.EQUALS, "Y"));
onBalanceSheetList = delegator.findList("CapitalAdequacyReport", EntityCondition.makeCondition(onBalanceSheetConditions, EntityOperator.AND), UtilMisc.toSet("code", "name", "amount"), UtilMisc.toList("code"), null, false);

if(onBalanceSheetList){

  onBalanceSheetList.each { component ->
     if (component.amount) {
      onBalanceSheetTotal = onBalanceSheetTotal.add(component.amount)
     }
  }
  totalMap = [code:"2.0.8", name:"Total (2.0.1 TO 2.0.7)", amount:onBalanceSheetTotal]
  balanceSheetValue = 1988476482.00 //MAKE SURE THIS IS FETCHED FROM THE BALANCE SHEET
  balanceSheetValueMap = [code:"2.0.9", name:"Total Assets (Per Balance Sheet)", amount:balanceSheetValue] //MAKE SURE THIS IS FETCHED FROM THE BALANCE SHEET

  difference = onBalanceSheetTotal - balanceSheetValue //Difference of the totals with balance sheet value
  differenceMap = [code:"2.1.1", name:"Difference (2.0.8 Less 2.0.9)", amount:difference] //MAKE SURE THIS IS FETCHED FROM THE BALANCE SHEET
}

onBalanceSheetList.add(totalMap)
onBalanceSheetList.add(balanceSheetValueMap)
onBalanceSheetList.add(differenceMap)
context.capitalAdequacyOnBalanceList = onBalanceSheetList;

//Off Balance Sheet List
offBalanceSheetList = [];
totalMap = [];
offBalanceSheetTotal = BigDecimal.ZERO
List offBalanceSheetConditions = FastList.newInstance();
offBalanceSheetConditions.add(EntityCondition.makeCondition("isOffBalanceSheetAsset", EntityOperator.EQUALS, "Y"));
offBalanceSheetList = delegator.findList("CapitalAdequacyReport", EntityCondition.makeCondition(offBalanceSheetConditions, EntityOperator.AND), UtilMisc.toSet("code", "name", "amount"), UtilMisc.toList("code"), null, false);
offBalanceSheetList.each { component ->
   if (component.amount) {
    offBalanceSheetTotal = onBalanceSheetTotal.add(component.amount)
   }
}
context.capitalAdequacyOffBalanceList = offBalanceSheetList;

//Capital Ratio Calculations
capitalRatioCalcList = [];
totalAssests = BigDecimal.ZERO
totalDepositLiabilities = BigDecimal.ZERO
onBalanceSheetMap = [code:"4.0.1", name:"Total Asset value of ON-BALANCE Sheet Items", amount:onBalanceSheetTotal]
offBalanceSheetMap = [code:"4.0.2", name:"Total Asset value of OFF-BALANCE Sheet Items", amount:offBalanceSheetTotal]
totalAssests = onBalanceSheetTotal + offBalanceSheetTotal
totalAssestsMap = [code:"4.0.3", name:"Total Asset (4.0.1 + 4.0.2)", amount:totalAssests]
totalDepositLiabilities = 1269705398.00 //MAKES SURE THIS VALUE IS FETCHED FROM THE BALANCE SHEET
totalDepositLiabilitiesMap = [code:"4.0.4", name:"Total Deposits Liabilities (As per Balance Sheet)", amount:totalDepositLiabilities]

capitalRatioCalcList.add(onBalanceSheetMap)
capitalRatioCalcList.add(offBalanceSheetMap)
capitalRatioCalcList.add(totalAssestsMap)
capitalRatioCalcList.add(totalDepositLiabilitiesMap)
context.capitalRatioCalcList = capitalRatioCalcList



