/*******************************************************************************
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
 *******************************************************************************/
package org.ofbiz.accounting.ledger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.accounting.finaccount.FinAccountServices;

public class GeneralLedgerServices {

	public static final String module = GeneralLedgerServices.class.getName();

	private static BigDecimal ZERO = BigDecimal.ZERO;

	public static Map<String, Object> approveGLJV(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String manualGlJvId = (String) context.get("manualGlJvId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		//Get the Header Record
		GenericValue jvHeader = getJVHeader(delegator, manualGlJvId);
		
		//Check if total debit and credits are balancing
		BigDecimal linesAmount = getTotalDebitsForHeader(delegator, manualGlJvId);
		BigDecimal headerAmount = jvHeader.getBigDecimal("amount");
		
		int compare = linesAmount.compareTo(headerAmount);
		
		if (compare != 0) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount. Rule of double Entry being enforced ! Balance the Credits and Debit before approving");
		}
		
		//Update Header Record, Check who is posting and chnaged isposted flag
		try {
			jvHeader.set("approvedBy", userLogin.getString("userLoginId"));
			jvHeader.set("statusName", "APPROVED");
			jvHeader.set("isApproved", "Y");
			jvHeader.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("manualGlJvId", manualGlJvId);
		result.put("organizationPartyId", organizationPartyId);
		return result;
	}
	
	private static GenericValue getJVHeader(Delegator delegator, String manualGlJvId) {
		GenericValue jvHeader = null;
		try {
			jvHeader = delegator.findOne("ManualGLJVHeader", UtilMisc.toMap("manualGlJvId", manualGlJvId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return jvHeader;
	}

	public static Map<String, Object> postGLJV(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String manualGlJvId = (String) context.get("manualGlJvId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		//Get the Header Record
		GenericValue jvHeader = getJVHeader(delegator, manualGlJvId);
		
		//Create Account Trans Header
		String accgTransId = createTransactionHeaderForJV(delegator, jvHeader, userLogin);
		
		//Create JV Header AcctgTransEntry
		Boolean isHeaderSuccess = createJVAcctgTransEntry(delegator, jvHeader, userLogin, accgTransId);
		
		//Create JV Lines AcctgTransEntry
		Boolean isLineSuccess = true;
		List<GenericValue>jvLines = getJVLines(jvHeader);
		for (GenericValue line : jvLines) {
			if (isLineSuccess) {
				isLineSuccess = createJVAcctgTransEntryLine(delegator, line, jvHeader,userLogin, accgTransId);
			}
			
		}
		
		
		
		
		
		
		if (isHeaderSuccess && isLineSuccess) {
			try {
				jvHeader.set("postedBy", userLogin.getString("userLoginId"));
				jvHeader.set("statusName", "POSTED");
				jvHeader.set("isPosted", "Y");
				jvHeader.store();
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}else{
			return ServiceUtil.returnError("THERE WAS A PROBLEM IN GENERATING JV LINES. CALL ICT FOR HELP");
		}
		//Update Header Record, Check who is posting and chnaged isposted flag

		
		
		
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("manualGlJvId", manualGlJvId);
		result.put("organizationPartyId", organizationPartyId);
		return result;
	}
	
	private static List<GenericValue> getJVLines(GenericValue jvHeader) {
		List<GenericValue> jvLines = null;

		try {
			jvLines = jvHeader.getRelated("ManualGLJVLines", null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return jvLines;
	}
	
	public static Map<String, Object> deleteGLJV(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context.get("organizationPartyId");
		String manualGlJvId = (String) context.get("manualGlJvId");
		System.out.println("############### organizationPartyId "+organizationPartyId);
		System.out.println("############### manualGlJvId "+manualGlJvId);

		GenericValue jvHeader = getJVHeader(delegator, manualGlJvId);

		// get all bankreconlines for this header id
		List<GenericValue> jvLines = null;
		jvLines = getJVLines(jvHeader);
		// foreach delete

		for (GenericValue line : jvLines) {
			try {
				line.remove();
			} catch (Exception e) {
				return ServiceUtil.returnError("COULD NOT DELETE TRANSACTION " + line.getString("narration"));
			}
		}

		// finally delete the header
		try {
			jvHeader.remove();
		} catch (Exception e) {
			return ServiceUtil.returnError("COULD NOT DELETE RECONCILIATION " + jvHeader.getString("narration"));
		}
		Map<String, Object> result = ServiceUtil.returnSuccess("GL DELETED SUCCESSFULLY");
		result.put("manualGlJvId", manualGlJvId);
		result.put("organizationPartyId", organizationPartyId);
		return result;
	}
	
	public static Map<String, Object> deleteLoanClearItem(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Long loanClearId = (Long) context.get("loanClearId");
		Long loanClearItemId = (Long) context.get("loanClearItemId");
		System.out.println("############### loanClearId "+loanClearId);
		System.out.println("############### loanClearItemId "+loanClearItemId);

		GenericValue loanClearHeader = getLoanClearHeader(delegator, loanClearId);
		GenericValue loanClearItem = null;
		
		if (loanClearHeader.getString("isCleared").equals("N")) {
			try {
				loanClearItem = delegator.findOne("LoanClearItem", UtilMisc.toMap("loanClearItemId", loanClearItemId), false);
				//save log here
				GenericValue loanClearItemLog = null;
				String loanClearItemLogId = null;
				loanClearItemLog = delegator.makeValue("LoanClearItemLog");
				loanClearItemLogId = delegator.getNextSeqId("LoanClearItemLog");
				loanClearItemLog.put("loanClearItemLogId", loanClearItemLogId);
				loanClearItemLog.put("loanClearItemId", loanClearItem.getLong("loanClearItemId"));
				loanClearItemLog.put("loanClearId", loanClearItem.getLong("loanClearId"));
				loanClearItemLog.put("isActive", loanClearItem.getString("organizationPartyId"));
				loanClearItemLog.put("deletedBy", userLogin.getString("userLoginId"));
				loanClearItemLog.put("deletedOn", UtilDateTime.nowTimestamp());
				loanClearItemLog.put("createdBy", loanClearItem.getString("createdBy"));
				loanClearItemLog.put("updatedBy", loanClearItem.getString("updatedBy"));
				loanClearItemLog.put("loanApplicationId", loanClearItem.getLong("loanApplicationId"));
				loanClearItemLog.put("loanAmt", loanClearItem.getBigDecimal("loanAmt"));
				loanClearItemLog.create();
				
				// We can now safe remove loanClearItem				
				loanClearItem.remove();
				
				//Update Loan Record to display the button
				GenericValue loan = getLoan(delegator, loanClearItem.getLong("loanApplicationId"));
				System.out.println("########## LOAN RETRIVED "+loan.getString("loanNo"));
				System.out.println("########## LOAN BEFORE UPDATE "+loan.getString("isAddedToClear"));
				loan.set("isAddedToClear", null);
				loan.store();
				
				System.out.println("########## LOAN UPDATED "+loan.getString("isAddedToClear"));
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}else{
			Map<String, Object> errorResult = ServiceUtil.returnError("THIS LOAN HAS ALREADY BEEN CLEARED. YOU CAN NOT DELETE A CLEARED LOAN");
			errorResult.put("loanClearId", loanClearId);
			errorResult.put("loanClearItemId", loanClearItemId);
			return errorResult;
		}
		Map<String, Object> result = ServiceUtil.returnSuccess("DELETED SUCCESSFULLY");
		result.put("loanClearId", loanClearId);
		return result;
	}

	private static GenericValue getLoan(Delegator delegator, Long loanApplicationId) {
		GenericValue loan = null;
		try {
			loan = delegator.findOne("LoanApplication", UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loan;
	}

	private static GenericValue getLoanClearHeader(Delegator delegator, Long loanClearId) {
		GenericValue loanClearHeader = null;
		try {
			loanClearHeader = delegator.findOne("LoanClear", UtilMisc.toMap("loanClearId", loanClearId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loanClearHeader;
	}

	private static Boolean createJVAcctgTransEntryLine(Delegator delegator, GenericValue jvLine, GenericValue jvHeader,GenericValue userLogin, String acctgTransId) {
		Boolean isSuccess =  false;
		
		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", jvHeader.getString("organizationPartyId"));
		acctgTransEntry.put("amount", jvLine.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", jvLine.getBigDecimal("amount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", jvLine.getString("debitCreditFlag"));
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("partyId", userLogin.getString("partyId"));
		acctgTransEntry.put("glAccountId", jvLine.getString("glAccountId"));
		acctgTransEntry.put("description", "GL JV BY "+userLogin.getString("userLoginId")+".  "+jvLine.get("narration"));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return isSuccess;
	}
	
	private static Boolean createJVAcctgTransEntry(Delegator delegator, GenericValue jvHeader,GenericValue userLogin, String acctgTransId) {
		Boolean isSuccess =  false;
		
		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", jvHeader.getString("organizationPartyId"));
		acctgTransEntry.put("amount", jvHeader.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", jvHeader.getBigDecimal("amount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", jvHeader.getString("debitCreditFlag"));
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("partyId", userLogin.getString("partyId"));
		acctgTransEntry.put("glAccountId", jvHeader.getString("glAccountId"));
		acctgTransEntry.put("description", "GL JV BY "+userLogin.getString("userLoginId")+".  "+jvHeader.getString("narration"));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return isSuccess;
	}

	private static String createTransactionHeaderForJV(Delegator delegator, GenericValue jvHeader, GenericValue userLogin) {

		GenericValue acctgTrans = null;
		String acctgTransId = null;

		if (jvHeader != null) {
			acctgTrans = delegator.makeValue("AcctgTrans");
			acctgTransId = delegator.getNextSeqId("AcctgTrans");
			acctgTrans.put("acctgTransId", acctgTransId);
			acctgTrans.put("acctgTransTypeId", jvHeader.getString("acctgTransTypeId"));
			acctgTrans.put("description", "GL JV BY "+userLogin.getString("userLoginId")+".  "+jvHeader.getString("narration"));
			acctgTrans.put("transactionDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("isPosted", "Y");
			acctgTrans.put("isApproved", "Y");
			acctgTrans.put("postedDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("glFiscalTypeId", "ACTUAL");
			acctgTrans.put("partyId", userLogin.getString("partyId"));
			acctgTrans.put("createdByUserLogin", userLogin.getString("userLoginId"));

			try {
				acctgTrans.create();
			} catch (Exception e) {

			}
		}

		return acctgTransId;
	}
	
	
	public static Map<String, Object> addDebitTransaction(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String manualGlJvId = (String) context.get("manualGlJvId");
		String narration = (String) context.get("narration");
		String glAccountId = (String) context.get("glAccountId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		BigDecimal amount = (BigDecimal) context.get("amount");

		// Controlss

		// 1. Check that the credit amount is not exceeded by the Debit Lines
		// Get the MultiPayment Header
		GenericValue multiDebitHeader = null;
		BigDecimal creditAmount = BigDecimal.ZERO;
		BigDecimal debitAmount = getTotalDebitsForHeader(delegator, manualGlJvId);
		try {
			multiDebitHeader = delegator.findOne("ManualGLJVHeader", UtilMisc.toMap("manualGlJvId", manualGlJvId), false);
			if (multiDebitHeader != null) {
				creditAmount = multiDebitHeader.getBigDecimal("amount");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}

		int compare = FinAccountServices.compareDebitsToCredit(amount, debitAmount, creditAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount. Rule of double Entry being enforced !");
		}

		GenericValue debitLines = delegator.makeValue("ManualGLJVLines");
		String lineId = delegator.getNextSeqId("ManualGLJVLines");

		debitLines.put("manualGlJvId", manualGlJvId);
		debitLines.put("narration", narration);
		debitLines.put("glAccountId", glAccountId);
		debitLines.put("amount", amount);
		debitLines.put("lineId", lineId);
		debitLines.put("debitCreditFlag", "D");

		try {
			debitLines.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("manualGlJvId", manualGlJvId);
		result.put("organizationPartyId", organizationPartyId);

		return result;
	}

	public static Map<String, Object> addCreditTransaction(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String manualGlJvId = (String) context.get("manualGlJvId");
		String narration = (String) context.get("narration");
		String glAccountId = (String) context.get("glAccountId");
		String organizationPartyId = (String) context.get("organizationPartyId");
		BigDecimal amount = (BigDecimal) context.get("amount");

		// Controlss

		// 1. Check that the credit amount is not exceeded by the Debit Lines
		// Get the MultiPayment Header
		GenericValue multiDebitHeader = null;
		BigDecimal creditAmount = BigDecimal.ZERO;
		BigDecimal debitAmount = getTotalDebitsForHeader(delegator, manualGlJvId);
		try {
			multiDebitHeader = delegator.findOne("ManualGLJVHeader", UtilMisc.toMap("manualGlJvId", manualGlJvId), false);
			if (multiDebitHeader != null) {
				creditAmount = multiDebitHeader.getBigDecimal("amount");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}

		int compare = FinAccountServices.compareDebitsToCredit(amount, debitAmount, creditAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount. Rule of double Entry being enforced !");
		}

		GenericValue debitLines = delegator.makeValue("ManualGLJVLines");
		String lineId = delegator.getNextSeqId("lineId");

		debitLines.put("manualGlJvId", manualGlJvId);
		debitLines.put("narration", narration);
		debitLines.put("glAccountId", glAccountId);
		debitLines.put("amount", amount);
		debitLines.put("lineId", lineId);
		debitLines.put("debitCreditFlag", "C");

		try {
			debitLines.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("manualGlJvId", manualGlJvId);
		result.put("organizationPartyId", organizationPartyId);

		return result;
	}

	public static BigDecimal getTotalDebitsForHeader(Delegator delegator, String manualGlJvId) {
		BigDecimal debitAmount = BigDecimal.ZERO;
		List<GenericValue> debitLines = null;

		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("manualGlJvId", EntityOperator.EQUALS, manualGlJvId)
				));
		try {
			debitLines = delegator.findList("ManualGLJVLines", cond, null, null, null, false);
			for (GenericValue line : debitLines) {
				debitAmount = debitAmount.add(line.getBigDecimal("amount"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return debitAmount;
	}

	public static int getTotalNumberOfDebitLines(Delegator delegator, String manualGlJvId) {

		int totalDebitLines = 0;
		List<GenericValue> debitLines = null;

		try {
			EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("manualGlJvId", EntityOperator.EQUALS, manualGlJvId)
					));
			debitLines = delegator.findList("ManualGLJVLines", cond, null, null, null, false);
			totalDebitLines = debitLines.size();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return totalDebitLines;
	}

	public static Map<String, Object> createUpdateCostCenter(DispatchContext dctx, Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		BigDecimal totalAmountPercentage = ZERO;
		Map<String, Object> createGlAcctCatMemFromCostCentersMap = null;
		String glAccountId = (String) context.get("glAccountId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String, String> amountPercentageMap = UtilGenerics.checkMap(context.get("amountPercentageMap"));
		totalAmountPercentage = GeneralLedgerServices.calculateCostCenterTotal(amountPercentageMap);
		Map<String, Object> result = ServiceUtil.returnSuccess();
		for (String rowKey : amountPercentageMap.keySet()) {
			String rowValue = amountPercentageMap.get(rowKey);
			if (UtilValidate.isNotEmpty(rowValue)) {
				createGlAcctCatMemFromCostCentersMap = UtilMisc.toMap("glAccountId", glAccountId,
						"glAccountCategoryId", rowKey, "amountPercentage", new BigDecimal(rowValue),
						"userLogin", userLogin, "totalAmountPercentage", totalAmountPercentage);
			} else {
				createGlAcctCatMemFromCostCentersMap = UtilMisc.toMap("glAccountId", glAccountId,
						"glAccountCategoryId", rowKey, "amountPercentage", new BigDecimal(0),
						"userLogin", userLogin, "totalAmountPercentage", totalAmountPercentage);
			}
			try {
				result = dispatcher.runSync("createGlAcctCatMemFromCostCenters", createGlAcctCatMemFromCostCentersMap);
			} catch (GenericServiceException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			}
		}
		return result;
	}

	public static BigDecimal calculateCostCenterTotal(Map<String, String> amountPercentageMap) {
		BigDecimal totalAmountPercentage = ZERO;
		for (String rowKey : amountPercentageMap.keySet()) {
			if (UtilValidate.isNotEmpty(amountPercentageMap.get(rowKey))) {
				BigDecimal rowValue = new BigDecimal(amountPercentageMap.get(rowKey));
				if (rowValue != null)
					totalAmountPercentage = totalAmountPercentage.add(rowValue);
			}
		}
		return totalAmountPercentage;
	}

	public static String getGlNarration(Delegator delegator, GenericValue acctgTransEntry) {
		StringBuffer sb = new StringBuffer();

		List<GenericValue> accountTransactions = null;
		List<GenericValue> stationAccountTransactions = null;
		GenericValue member = null;
		GenericValue station = null;
		GenericValue acctgTrans = null;
		GenericValue payment = null;
		List<GenericValue> mSaccoApplication = null;
		List<GenericValue> cardApplication = null;

		// To Handle Non Member Payments
		try {
			acctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId", acctgTransEntry.getString("acctgTransId")), false);
			if (acctgTrans.getString("paymentId") != null) {
				payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", acctgTrans.getString("paymentId")), false);
			}
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Station Account Transaction
		EntityConditionList<EntityExpr> stationAccountTransactionCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("transactionAmount", EntityOperator.EQUALS, acctgTransEntry.getBigDecimal("amount")),
				EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransEntry.getString("acctgTransId"))
				), EntityOperator.AND);

		try {
			stationAccountTransactions = delegator.findList("StationAccountTransaction", stationAccountTransactionCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		EntityConditionList<EntityExpr> accountTransactionsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("transactionAmount", EntityOperator.EQUALS, acctgTransEntry.getBigDecimal("origAmount")),
				EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransEntry.getString("acctgTransId"))
				), EntityOperator.AND);

		try {
			accountTransactions = delegator.findList("AccountTransaction", accountTransactionsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (accountTransactions.size() > 0) {

			GenericValue accountTransaction = accountTransactions.get(0);
			try {
				member = delegator.findOne("Member", UtilMisc.toMap("partyId", accountTransaction.getLong("partyId")), false);
			} catch (GenericEntityException e) {

				e.printStackTrace();
			}

			sb.append(accountTransaction.getString("transactionType"));
			if (accountTransaction.getString("chequeNo") != null) {
				sb.append(" ");
				sb.append("ChequeNo: ");
				sb.append(accountTransaction.getString("chequeNo"));
			}
			sb.append(" ");
			sb.append(member.getString("firstName"));
			sb.append(" ");
			sb.append(member.getString("middleName"));
			sb.append(" ");
			sb.append(member.getString("lastName"));
			EntityConditionList<EntityExpr> msaccoApplCond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, accountTransaction.getLong("partyId"))
					));
			if (accountTransaction.getString("transactionType").equals("MSACCOWITHDRAWAL") || accountTransaction.getString("transactionType").equals("MSACCOENQUIRY")) {

				try {
					mSaccoApplication = delegator.findList("MSaccoApplication", msaccoApplCond, null, null, null, false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}

				String phoneNo = null;
				for (GenericValue phone : mSaccoApplication) {
					phoneNo = phone.getString("mobilePhoneNumber");
				}
				sb.append(" ");
				sb.append("Phone:");
				sb.append(" ");
				sb.append(phoneNo);
			}
			if (accountTransaction.getString("transactionType").equals("ATMWITHDRAWAL") || accountTransaction.getString("transactionType").equals("POSWITHDRAWAL") || accountTransaction.getString("transactionType").equals("VISAWITHDRAWAL")) {
				try {
					cardApplication = delegator.findList("CardApplication", msaccoApplCond, null, null, null, false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}

				String cardNo = null;
				for (GenericValue card : cardApplication) {
					cardNo = card.getString("cardNumber");
				}
				sb.append(" ");
				sb.append("Card No:");
				sb.append(" ");
				sb.append(cardNo);
			}
			

			
			

		} else if (payment != null) {
			sb.append(payment.getString("comments"));
			sb.append(" ");
			sb.append("ChequeNo: ");
			sb.append(payment.getString("paymentRefNum"));
		} else if (stationAccountTransactions.size() > 0) {
			GenericValue stationAccountTransaction = stationAccountTransactions.get(0);
			if (stationAccountTransaction.getLong("stationId") != null) {
				try {
					System.out.println("TRYING TO RETRIVE STATION USING " + stationAccountTransaction.getString("stationId"));
					station = delegator.findOne("Station", UtilMisc.toMap("stationId", stationAccountTransaction.getString("stationId")), false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				sb.append("Station Remitance");
				sb.append(" ");
				if (station != null) {
					sb.append(station.getString("name"));
				}
				if (stationAccountTransaction.getString("chequeNumber") != null) {
					sb.append(" ");
					sb.append("ChequeNo: ");
					sb.append(stationAccountTransaction.getString("chequeNumber"));
				}
				sb.append(" ");
				sb.append("Month Year");
				sb.append(" ");
				sb.append(stationAccountTransaction.getString("monthyear"));
			}
		} else if (acctgTransEntry.getString("glAccountTypeId") != null && acctgTransEntry.getString("glAccountTypeId").equals("TREASURY_TRANSFER")) {
			sb.append("TREASURY TRANSFER");
			GenericValue partyNameView = null;
			try {
				partyNameView = delegator.findOne("PartyNameView", UtilMisc.toMap("partyId", acctgTransEntry.getString("partyId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (partyNameView != null) {
				sb.append(" ");
				sb.append("by");
				sb.append(" ");
				if (partyNameView.getString("firstName") != null) {
					sb.append(partyNameView.getString("firstName"));
				}
				sb.append(" ");
				if (partyNameView.getString("middleName") != null) {
					sb.append(partyNameView.getString("middleName"));
				}
				sb.append(" ");
				if (partyNameView.getString("lastName") != null) {
					sb.append(partyNameView.getString("lastName"));
				}
			}
		} else if (acctgTransEntry.getString("glAccountTypeId") != null && acctgTransEntry.getString("glAccountTypeId").equals("TREASURY_TRANSFER")) {
			sb.append("TREASURY TRANSFER");
			GenericValue partyNameView = null;
			try {
				partyNameView = delegator.findOne("PartyNameView", UtilMisc.toMap("partyId", acctgTransEntry.getString("partyId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (partyNameView != null) {
				sb.append(" ");
				sb.append("by");
				sb.append(" ");
				if (partyNameView.getString("firstName") != null) {
					sb.append(partyNameView.getString("firstName"));
				}
				sb.append(" ");
				if (partyNameView.getString("middleName") != null) {
					sb.append(partyNameView.getString("middleName"));
				}
				sb.append(" ");
				if (partyNameView.getString("lastName") != null) {
					sb.append(partyNameView.getString("lastName"));
				}
			}

		} else if (acctgTransEntry.getString("glAccountTypeId") == null && acctgTrans.getString("description") != null) {
			sb.append(acctgTrans.getString("description"));
		}

		if (sb.length() < 1) {
			List<GenericValue>accountTransactionsNoAmtList = null;
			GenericValue accountTransactionsNoAmt = null;
			EntityConditionList<EntityExpr> accountTransactionsCondNoAmt = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransEntry.getString("acctgTransId"))
					), EntityOperator.AND);

			try {
				accountTransactionsNoAmtList = delegator.findList("AccountTransaction", accountTransactionsCondNoAmt, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			if (accountTransactionsNoAmtList.size() > 0) {
				accountTransactionsNoAmt = accountTransactionsNoAmtList.get(0);
			}
			
			
			EntityConditionList<EntityExpr> parentCond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("accountTransactionParentId", EntityOperator.EQUALS, accountTransactionsNoAmt.getString("accountTransactionParentId")),
					EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS, "CHEQUEDEPOSIT")
					), EntityOperator.AND);
			List<GenericValue> parentAccountTransactionList = null;
			try {
				parentAccountTransactionList = delegator.findList("AccountTransaction", parentCond, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			GenericValue parentAccountTransaction = null;
			
			if (parentAccountTransactionList.size() > 0) {
				parentAccountTransaction = parentAccountTransactionList.get(0);
				sb.append(" ");
				sb.append("ChequeNo: ");
				sb.append(" ");
				sb.append(parentAccountTransaction.getString("chequeNo"));
				sb.append(" ");
				sb.append("Drawer: ");
				sb.append(" ");
				sb.append(parentAccountTransaction.getString("drawer"));
			}
			
			
		
			if (sb.length() < 1) {
				if (acctgTransEntry.getString("glAccountTypeId") != null) {
					sb.append(acctgTransEntry.getString("glAccountTypeId"));
				}
			}
		}

		return sb.toString();
	}
}
