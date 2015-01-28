/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;


import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;

import java.sql.Date;
import java.sql.Timestamp;

import javolution.util.FastList;

/**
 * Just pasted all the code in IncomeStatement groovy ...
 * 
 * This is evil but I didnt have a choice
 * */
//if (!fromDateIncome) {
//	return;
//}
if (!thruDate) {
	thruDate = UtilDateTime.nowTimestamp();
}

def fromDateIncome = thruDate - 365;

if (!glFiscalTypeId) {
	return;
}

// Setup the divisions for which the report is executed

uiLabelMap = UtilProperties.getResourceBundleMap("AccountingUiLabels", locale);
List partyIdsIncome = PartyWorker.getAssociatedPartyIdsByRelationshipType(delegator, organizationPartyId, 'GROUP_ROLLUP');
								  
//List partyIdsIncome = PartyWorker.getAssociatedpartyIdsByRelationshipType(delegator, organizationPartyId, 'GROUP_ROLLUP');
partyIdsIncome.add(organizationPartyId);

// Get the group of account classes that will be used to position accounts in the proper section of the financial statement
GenericValue revenueGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "REVENUE"), true);
List revenueAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(revenueGlAccountClass);
GenericValue contraRevenueGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "CONTRA_REVENUE"), true);
List contraRevenueAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(contraRevenueGlAccountClass);
GenericValue incomeGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "INCOME"), true);
List incomeAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(incomeGlAccountClass);
GenericValue expenseGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "EXPENSE"), true);
List expenseAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(expenseGlAccountClass);
GenericValue cogsExpenseGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "COGS_EXPENSE"), true);
List cogsExpenseAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(cogsExpenseGlAccountClass);
GenericValue sgaExpenseGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "SGA_EXPENSE"), true);
List sgaExpenseAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(sgaExpenseGlAccountClass);
GenericValue depreciationGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "DEPRECIATION"), true);
List depreciationAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(depreciationGlAccountClass);

List mainAndExprsIncome = FastList.newInstance();
mainAndExprsIncome.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIdsIncome));
mainAndExprsIncome.add(EntityCondition.makeCondition("isPosted", EntityOperator.EQUALS, "Y"));
mainAndExprsIncome.add(EntityCondition.makeCondition("glFiscalTypeId", EntityOperator.EQUALS, glFiscalTypeId));
mainAndExprsIncome.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.NOT_EQUAL, "PERIOD_CLOSING"));
mainAndExprsIncome.add(EntityCondition.makeCondition("transactionDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDateIncome));
mainAndExprsIncome.add(EntityCondition.makeCondition("transactionDate", EntityOperator.LESS_THAN, thruDate));

List balanceTotalListIncome= [];

// REVENUE
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List revenueAndExprs = FastList.newInstance(mainAndExprsIncome);
revenueAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, revenueAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(revenueAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// revenues are accounts of class CREDIT: the balance is given by credits minus debits
		BigDecimal balance = creditAmount.subtract(debitAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// revenues are accounts of class CREDIT: the balance is given by credits minus debits
	balanceTotal = balanceTotalCredit.subtract(balanceTotalDebit);
}
context.revenueAccountBalanceList = accountBalanceList;
context.revenueAccountBalanceList.add(UtilMisc.toMap("accountName", "TOTAL REVENUES", "balance", balanceTotal));
context.revenueBalanceTotal = balanceTotal;

// CONTRA REVENUE
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List contraRevenueAndExprs = FastList.newInstance(mainAndExprsIncome);
contraRevenueAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, contraRevenueAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(contraRevenueAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// contra revenues are accounts of class DEBIT: the balance is given by debits minus credits
		BigDecimal balance = debitAmount.subtract(creditAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// contra revenues are accounts of class DEBIT: the balance is given by debits minus credits
	balanceTotal = balanceTotalDebit.subtract(balanceTotalCredit);
}
context.contraRevenueBalanceTotal = balanceTotal;
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "TOTAL CONTRA REVENUE", "balance", balanceTotal));

// EXPENSE
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List expenseAndExprs = FastList.newInstance(mainAndExprsIncome);
expenseAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, expenseAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(expenseAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// expenses are accounts of class DEBIT: the balance is given by debits minus credits
		BigDecimal balance = debitAmount.subtract(creditAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// expenses are accounts of class DEBIT: the balance is given by debits minus credits
	balanceTotal = balanceTotalDebit.subtract(balanceTotalCredit);
}
context.expenseAccountBalanceList = accountBalanceList;
context.expenseAccountBalanceList.add(UtilMisc.toMap("accountName", "TOTAL EXPENSES", "balance", balanceTotal));
context.expenseBalanceTotal = balanceTotal;

// COST OF GOODS SOLD (COGS_EXPENSE)
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List cogsExpenseAndExprs = FastList.newInstance(mainAndExprsIncome);
cogsExpenseAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, cogsExpenseAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(cogsExpenseAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// expenses are accounts of class DEBIT: the balance is given by debits minus credits
		BigDecimal balance = debitAmount.subtract(creditAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// expenses are accounts of class DEBIT: the balance is given by debits minus credits
	balanceTotal = balanceTotalDebit.subtract(balanceTotalCredit);
}
context.cogsExpense = balanceTotal;
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingCostOfGoodsSold", "balance", balanceTotal));

// OPERATING EXPENSES (SGA_EXPENSE)
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List sgaExpenseAndExprs = FastList.newInstance(mainAndExprsIncome);
sgaExpenseAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, sgaExpenseAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(sgaExpenseAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// expenses are accounts of class DEBIT: the balance is given by debits minus credits
		BigDecimal balance = debitAmount.subtract(creditAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// expenses are accounts of class DEBIT: the balance is given by debits minus credits
	balanceTotal = balanceTotalDebit.subtract(balanceTotalCredit);
}
sgaExpense = balanceTotal;

//DEPRECIATION (DEPRECIATION)
//account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List depreciationAndExprs = FastList.newInstance(mainAndExprsIncome);
depreciationAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, depreciationAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(depreciationAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
Map transactionTotalsMap = [:];
balanceTotalCredit = BigDecimal.ZERO;
balanceTotalDebit = BigDecimal.ZERO;
transactionTotals.each { transactionTotal ->
   Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
   if (!accountMap) {
	   accountMap = UtilMisc.makeMapWritable(transactionTotal);
	   accountMap.remove("debitCreditFlag");
	   accountMap.remove("amount");
	   accountMap.put("D", BigDecimal.ZERO);
	   accountMap.put("C", BigDecimal.ZERO);
	   accountMap.put("balance", BigDecimal.ZERO);
   }
   UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
   if ("D".equals(transactionTotal.debitCreditFlag)) {
	   balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
   } else {
	   balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
   }
   BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
   BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
   // expenses are accounts of class DEBIT: the balance is given by debits minus credits
   BigDecimal balance = debitAmount.subtract(creditAmount);
   accountMap.put("balance", balance);
   transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
// expenses are accounts of class DEBIT: the balance is given by debits minus credits
balanceTotal = balanceTotalDebit.subtract(balanceTotalCredit);
}
depreciation = balanceTotal;

// INCOME
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List incomeAndExprs = FastList.newInstance(mainAndExprsIncome);
incomeAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, incomeAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(incomeAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
if (transactionTotals) {
	Map transactionTotalsMap = [:];
	balanceTotalCredit = BigDecimal.ZERO;
	balanceTotalDebit = BigDecimal.ZERO;
	transactionTotals.each { transactionTotal ->
		Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
		if (!accountMap) {
			accountMap = UtilMisc.makeMapWritable(transactionTotal);
			accountMap.remove("debitCreditFlag");
			accountMap.remove("amount");
			accountMap.put("D", BigDecimal.ZERO);
			accountMap.put("C", BigDecimal.ZERO);
			accountMap.put("balance", BigDecimal.ZERO);
		}
		UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
		if ("D".equals(transactionTotal.debitCreditFlag)) {
			balanceTotalDebit = balanceTotalDebit.add(transactionTotal.amount);
		} else {
			balanceTotalCredit = balanceTotalCredit.add(transactionTotal.amount);
		}
		BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
		BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
		// income are accounts of class CREDIT: the balance is given by credits minus debits
		BigDecimal balance = creditAmount.subtract(debitAmount);
		accountMap.put("balance", balance);
		transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
	}
	accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
	// incomes are accounts of class CREDIT: the balance is given by credits minus debits
	balanceTotal = balanceTotalCredit.subtract(balanceTotalDebit);
}
context.incomeAccountBalanceList = accountBalanceList;
context.incomeAccountBalanceList.add(UtilMisc.toMap("accountName", "TOTAL INCOME", "balance", balanceTotal));
context.incomeBalanceTotal = balanceTotal;

// NET SALES = REVENUES - CONTRA REVENUES
context.netSales = (context.revenueBalanceTotal).subtract(context.contraRevenueBalanceTotal);
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingTotalNetSales", "balance", context.netSales));
// GROSS MARGIN = NET SALES - COSTS OF GOODS SOLD
context.grossMargin = (context.netSales).subtract(context.cogsExpense);
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingGrossMargin", "balance", context.grossMargin));
// OPERATING EXPENSES
context.sgaExpense = sgaExpense;
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingOperatingExpenses", "balance", context.sgaExpense));
// DEPRECIATION
context.depreciation = depreciation;
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingDepreciation", "balance", context.depreciation));
// INCOME FROM OPERATIONS = GROSS MARGIN - OPERATING EXPENSES
context.incomeFromOperations = (context.grossMargin).subtract(context.sgaExpense);
balanceTotalListIncome.add(UtilMisc.toMap("totalName", "AccountingIncomeFromOperations", "balance", context.incomeFromOperations));
// NET INCOME
context.netIncome = (context.netSales).add(context.incomeBalanceTotal).subtract(context.expenseBalanceTotal);

/**
 * 
 * End of madness
 * 
 * **/



uiLabelMap = UtilProperties.getResourceBundleMap("AccountingUiLabels", locale);

if (!thruDate) {
    thruDate = UtilDateTime.nowTimestamp();
}
if (!glFiscalTypeId) {
    return;
}

// Setup the divisions for which the report is executed
//List partyIds = partyIdsIncome;
//PartyWorker.getAssociatedPartyIdsByRelationshipType(delegator, organizationPartyId, 'GROUP_ROLLUP');

//List partyIds = PartyWorker.getAssociatedpartyIdsByRelationshipType(delegator, organizationPartyId, 'GROUP_ROLLUP');
List partyIds = PartyWorker.getAssociatedPartyIdsByRelationshipType(delegator, organizationPartyId, 'GROUP_ROLLUP');

partyIds.add(organizationPartyId);

// Get the group of account classes that will be used to position accounts in the proper section of the financial statement
GenericValue assetGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "ASSET"), true);
List assetAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(assetGlAccountClass);
GenericValue contraAssetGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "CONTRA_ASSET"), true);
List contraAssetAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(contraAssetGlAccountClass);
GenericValue liabilityGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "LIABILITY"), true);
List liabilityAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(liabilityGlAccountClass);
GenericValue equityGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "EQUITY"), true);
List equityAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(equityGlAccountClass);
GenericValue currentAssetGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "CURRENT_ASSET"), true);
List currentAssetAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(currentAssetGlAccountClass);
GenericValue longtermAssetGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "LONGTERM_ASSET"), true);
List longtermAssetAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(longtermAssetGlAccountClass);

GenericValue currentLiabilityGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "CURRENT_LIABILITY"), true);
List currentLiabilityAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(currentLiabilityGlAccountClass);

GenericValue longtermLiabilityGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "LONGTERM_LIABILITY"), true);
List longtermLiabilityAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(longtermLiabilityGlAccountClass);


GenericValue accumDepreciationGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "ACCUM_DEPRECIATION"), true);
List accumDepreciationAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(accumDepreciationGlAccountClass);
GenericValue accumAmortizationGlAccountClass = delegator.findOne("GlAccountClass", UtilMisc.toMap("glAccountClassId", "ACCUM_AMORTIZATION"), true);
List accumAmortizationAccountClassIds = UtilAccounting.getDescendantGlAccountClassIds(accumAmortizationGlAccountClass);

// Find the last closed time period to get the fromDate for the transactions in the current period and the ending balances of the last closed period
Map lastClosedTimePeriodResult = dispatcher.runSync("findLastClosedDate", UtilMisc.toMap("organizationPartyId", organizationPartyId, "findDate", new Date(thruDate.getTime()),"userLogin", userLogin));
Timestamp fromDate = (Timestamp)lastClosedTimePeriodResult.lastClosedDate;
if (!fromDate) {
    return;
}
GenericValue lastClosedTimePeriod = (GenericValue)lastClosedTimePeriodResult.lastClosedTimePeriod;
// Get the opening balances of all the accounts
Map assetOpeningBalances = [:];
Map contraAssetOpeningBalances = [:];
Map currentAssetOpeningBalances = [:];
Map longtermAssetOpeningBalances = [:];
Map liabilityOpeningBalances = [:];
Map currentLiabilityOpeningBalances = [:];
Map longtermLiabilityOpeningBalances = [:];
Map equityOpeningBalances = [:];
if (lastClosedTimePeriod) {
    List timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, assetAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    List lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        assetOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, contraAssetAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        contraAssetOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, liabilityAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        liabilityOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, equityAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        equityOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, currentAssetAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        currentAssetOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, longtermAssetAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        longtermAssetOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
    timePeriodAndExprs = FastList.newInstance();
    timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, currentLiabilityAccountClassIds));
    timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
    timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
    lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
    lastTimePeriodHistories.each { lastTimePeriodHistory ->
        Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
        currentLiabilityOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
    }
	
	//longtermLiability
	timePeriodAndExprs = FastList.newInstance();
	timePeriodAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
	timePeriodAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, longtermLiabilityGlAccountClass));
	timePeriodAndExprs.add(EntityCondition.makeCondition("endingBalance", EntityOperator.NOT_EQUAL, BigDecimal.ZERO));
	timePeriodAndExprs.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, lastClosedTimePeriod.customTimePeriodId));
	lastTimePeriodHistories = delegator.findList("GlAccountAndHistory", EntityCondition.makeCondition(timePeriodAndExprs, EntityOperator.AND), null, null, null, false);
	lastTimePeriodHistories.each { lastTimePeriodHistory ->
		Map accountMap = UtilMisc.toMap("glAccountId", lastTimePeriodHistory.glAccountId, "accountCode", lastTimePeriodHistory.accountCode, "accountName", lastTimePeriodHistory.accountName, "balance", lastTimePeriodHistory.getBigDecimal("endingBalance"), "D", lastTimePeriodHistory.getBigDecimal("postedDebits"), "C", lastTimePeriodHistory.getBigDecimal("postedCredits"));
		longtermLiabilityOpeningBalances.put(lastTimePeriodHistory.glAccountId, accountMap);
	}

}

List balanceTotalList = [];

List mainAndExprs = FastList.newInstance();
mainAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.IN, partyIds));
mainAndExprs.add(EntityCondition.makeCondition("isPosted", EntityOperator.EQUALS, "Y"));
mainAndExprs.add(EntityCondition.makeCondition("glFiscalTypeId", EntityOperator.EQUALS, glFiscalTypeId));
mainAndExprs.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.NOT_EQUAL, "PERIOD_CLOSING"));
mainAndExprs.add(EntityCondition.makeCondition("transactionDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
mainAndExprs.add(EntityCondition.makeCondition("transactionDate", EntityOperator.LESS_THAN, thruDate));

// ASSETS
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List assetAndExprs = FastList.newInstance(mainAndExprs);
assetAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, assetAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(assetAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);

transactionTotalsMap = [:];
transactionTotalsMap.putAll(assetOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // assets are accounts of class DEBIT: the balance is given by debits minus credits
    BigDecimal balance = debitAmount.subtract(creditAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.assetAccountBalanceList = accountBalanceList;
context.assetAccountBalanceList.add(UtilMisc.toMap("accountName", uiLabelMap.AccountingTotalAssets, "balance", balanceTotal));
context.assetBalanceTotal = balanceTotal;

// CURRENT ASSETS
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List currentAssetAndExprs = FastList.newInstance(mainAndExprs);
currentAssetAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, currentAssetAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(currentAssetAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(currentAssetOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // assets are accounts of class DEBIT: the balance is given by debits minus credits
    BigDecimal balance = debitAmount.subtract(creditAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.currentAssetBalanceTotal = balanceTotal;
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingCurrentAssets", "balance", balanceTotal));

// LONGTERM ASSETS
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List longtermAssetAndExprs = FastList.newInstance(mainAndExprs);
longtermAssetAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, longtermAssetAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(longtermAssetAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(longtermAssetOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // assets are accounts of class DEBIT: the balance is given by debits minus credits
    BigDecimal balance = debitAmount.subtract(creditAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.longtermAssetBalanceTotal = balanceTotal;
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingLongTermAssets", "balance", balanceTotal));

// CONTRA ASSETS
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List contraAssetAndExprs = FastList.newInstance(mainAndExprs);
contraAssetAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, contraAssetAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(contraAssetAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);

transactionTotalsMap = [:];
transactionTotalsMap.putAll(contraAssetOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // contra assets are accounts of class CREDIT: the balance is given by credits minus debits
    BigDecimal balance = debitAmount.subtract(creditAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
//context.contraAssetAccountBalanceList = accountBalanceList;
context.assetAccountBalanceList.addAll(accountBalanceList);
context.assetAccountBalanceList.add(UtilMisc.toMap("accountName", uiLabelMap.AccountingTotalAccumulatedDepreciation, "balance", balanceTotal));
context.contraAssetBalanceTotal = balanceTotal;
//balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingLongTermAssetsAtCost", "balance", (context.longtermAssetBalanceTotal - context.contraAssetBalanceTotal)));
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingTotalAccumulatedDepreciation", "balance", balanceTotal));
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingTotalAssets", "balance", (context.currentAssetBalanceTotal + context.longtermAssetBalanceTotal + balanceTotal)));

// LIABILITY
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List liabilityAndExprs = FastList.newInstance(mainAndExprs);
liabilityAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, liabilityAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(liabilityAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(liabilityOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // liabilities are accounts of class CREDIT: the balance is given by credits minus debits
    BigDecimal balance = creditAmount.subtract(debitAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.liabilityAccountBalanceList = accountBalanceList;
context.liabilityAccountBalanceList.add(UtilMisc.toMap("accountName", uiLabelMap.AccountingTotalLiabilities, "balance", balanceTotal));
context.liabilityBalanceTotal = balanceTotal;

// CURRENT LIABILITY
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List currentLiabilityAndExprs = FastList.newInstance(mainAndExprs);
currentLiabilityAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, currentLiabilityAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(currentLiabilityAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(currentLiabilityOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // liabilities are accounts of class CREDIT: the balance is given by credits minus debits
    BigDecimal balance = creditAmount.subtract(debitAmount);
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.currentLiabilityBalanceTotal = balanceTotal;
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingCurrentLiabilities", "balance", balanceTotal));

//Longterm Liabilities

accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List longtermLiabilityAndExprs = FastList.newInstance(mainAndExprs);
longtermLiabilityAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, longtermLiabilityAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(longtermLiabilityAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(longtermLiabilityOpeningBalances);
transactionTotals.each { transactionTotal ->
	Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
	if (!accountMap) {
		accountMap = UtilMisc.makeMapWritable(transactionTotal);
		accountMap.remove("debitCreditFlag");
		accountMap.remove("amount");
		accountMap.put("D", BigDecimal.ZERO);
		accountMap.put("C", BigDecimal.ZERO);
		accountMap.put("balance", BigDecimal.ZERO);
	}
	UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
	BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
	BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
	// liabilities are accounts of class CREDIT: the balance is given by credits minus debits
	BigDecimal balance = creditAmount.subtract(debitAmount);
	accountMap.put("balance", balance);
	transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
	balanceTotal = balanceTotal + accountBalance.balance;
}
context.longtermLiabilityBalanceTotal = balanceTotal;
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingLongtermLiabilities", "balance", balanceTotal));
// EQUITY
// account balances
accountBalanceList = [];
transactionTotals = [];
balanceTotal = BigDecimal.ZERO;
List equityAndExprs = FastList.newInstance(mainAndExprs);
equityAndExprs.add(EntityCondition.makeCondition("glAccountClassId", EntityOperator.IN, equityAccountClassIds));
transactionTotals = delegator.findList("AcctgTransEntrySums", EntityCondition.makeCondition(equityAndExprs, EntityOperator.AND), UtilMisc.toSet("glAccountId", "accountName", "accountCode", "debitCreditFlag", "amount"), UtilMisc.toList("glAccountId"), null, false);
transactionTotalsMap = [:];
transactionTotalsMap.putAll(equityOpeningBalances);
transactionTotals.each { transactionTotal ->
    Map accountMap = (Map)transactionTotalsMap.get(transactionTotal.glAccountId);
    if (!accountMap) {
        accountMap = UtilMisc.makeMapWritable(transactionTotal);
        accountMap.remove("debitCreditFlag");
        accountMap.remove("amount");
        accountMap.put("D", BigDecimal.ZERO);
        accountMap.put("C", BigDecimal.ZERO);
        accountMap.put("balance", BigDecimal.ZERO);
    }
    UtilMisc.addToBigDecimalInMap(accountMap, transactionTotal.debitCreditFlag, transactionTotal.amount);
    BigDecimal debitAmount = (BigDecimal)accountMap.get("D");
    BigDecimal creditAmount = (BigDecimal)accountMap.get("C");
    // equities are accounts of class CREDIT: the balance is given by credits minus debits
    BigDecimal balance = creditAmount.subtract(debitAmount);
	
	//if (accountMap){
		if (transactionTotal.accountCode.equals("42000007")){
			balance = balance + context.netIncome;
		}
	//}
	
    accountMap.put("balance", balance);
    transactionTotalsMap.put(transactionTotal.glAccountId, accountMap);
}
// Add the "retained earnings" account
Map netIncomeResult = dispatcher.runSync("prepareIncomeStatement", UtilMisc.toMap("organizationPartyId", organizationPartyId, "glFiscalTypeId", glFiscalTypeId, "fromDate", fromDate, "thruDate", thruDate,"userLogin", userLogin));
BigDecimal netIncome = (BigDecimal)netIncomeResult.totalNetIncome;
GenericValue retainedEarningsAccount = delegator.findOne("GlAccountTypeDefault", UtilMisc.toMap("glAccountTypeId", "RETAINED_EARNINGS", "organizationPartyId", organizationPartyId), true);
if (retainedEarningsAccount) {
    GenericValue retainedEarningsGlAccount = retainedEarningsAccount.getRelatedOne("GlAccount", false);
    transactionTotalsMap.put(retainedEarningsGlAccount.glAccountId, UtilMisc.toMap("glAccountId", retainedEarningsGlAccount.glAccountId,"accountName", retainedEarningsGlAccount.accountName, "accountCode", retainedEarningsGlAccount.accountCode, "balance", netIncome));
}
accountBalanceList = UtilMisc.sortMaps(transactionTotalsMap.values().asList(), UtilMisc.toList("accountCode"));
accountBalanceList.each { accountBalance ->
    balanceTotal = balanceTotal + accountBalance.balance;
}
context.equityAccountBalanceList = accountBalanceList;
context.equityAccountBalanceList.add(UtilMisc.toMap("accountName", uiLabelMap.AccountingTotalEquities, "balance", balanceTotal));
context.equityBalanceTotal = balanceTotal;

context.liabilityEquityBalanceTotal = context.liabilityBalanceTotal + context.equityBalanceTotal
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingEquities", "balance", context.equityBalanceTotal));
balanceTotalList.add(UtilMisc.toMap("totalName", "AccountingTotalLiabilitiesAndEquities", "balance", context.liabilityEquityBalanceTotal));

context.balanceTotalList = balanceTotalList;
