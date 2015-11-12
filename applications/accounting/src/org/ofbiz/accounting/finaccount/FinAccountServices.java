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

package org.ofbiz.accounting.finaccount;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.accounting.trialbalance.TrialBalanceServices;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.finaccount.FinAccountHelper;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import com.google.gson.Gson;
import com.ibm.icu.text.ChineseDateFormat.Field;

public class FinAccountServices {

	public static final String module = FinAccountServices.class.getName();
	public static final String resourceError = "AccountingErrorUiLabels";

	public static String updateFinAccount(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String finAccountTransId = (String) request.getParameter("finAccountTransId");
		String toggleVal = request.getParameter("toggle");

		System.out.println("######## finAccountTransId #### " + finAccountTransId + "############### toggleVal: " + toggleVal);

		GenericValue transaction = null;

		try {
			transaction = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId", finAccountTransId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		transaction.set("toggle", toggleVal);
		String toggleToSave = (String) transaction.get("toggle");
		System.out.println("#############################toggleToSave: " + toggleToSave);
		try {
			transaction.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	public static Map<String, Object> createAccountAndCredit(
			DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String finAccountTypeId = (String) context.get("finAccountTypeId");
		String accountName = (String) context.get("accountName");
		String finAccountId = (String) context.get("finAccountId");
		Locale locale = (Locale) context.get("locale");

		// check the type
		if (finAccountTypeId == null) {
			finAccountTypeId = "SVCCRED_ACCOUNT";
		}
		if (accountName == null) {
			if ("SVCCRED_ACCOUNT".equals(finAccountTypeId)) {
				accountName = "Customer Service Credit Account";
			} else {
				accountName = "Financial Account";
			}
		}

		GenericValue userLogin = (GenericValue) context.get("userLogin");
		try {
			// find the most recent (active) service credit account for the
			// specified party
			String partyId = (String) context.get("partyId");
			Map<String, String> lookupMap = UtilMisc.toMap("finAccountTypeId",
					finAccountTypeId, "ownerPartyId", partyId);

			// if a productStoreId is present, restrict the accounts returned
			// using the store's payToPartyId
			String productStoreId = (String) context.get("productStoreId");
			if (UtilValidate.isNotEmpty(productStoreId)) {
				String payToPartyId = ProductStoreWorker
						.getProductStorePayToPartyId(productStoreId, delegator);
				if (UtilValidate.isNotEmpty(payToPartyId)) {
					lookupMap.put("organizationPartyId", payToPartyId);
				}
			}

			// if a currencyUomId is present, use it to restrict the accounts
			// returned
			String currencyUomId = (String) context.get("currencyUomId");
			if (UtilValidate.isNotEmpty(currencyUomId)) {
				lookupMap.put("currencyUomId", currencyUomId);
			}

			// check for an existing account
			GenericValue creditAccount;
			if (finAccountId != null) {
				creditAccount = delegator.findOne("FinAccount",
						UtilMisc.toMap("finAccountId", finAccountId), false);
			} else {
				List<GenericValue> creditAccounts = delegator.findByAnd(
						"FinAccount", lookupMap, UtilMisc.toList("-fromDate"),
						false);
				creditAccount = EntityUtil.getFirst(EntityUtil
						.filterByDate(creditAccounts));
			}

			if (creditAccount == null) {
				// create a new service credit account
				String createAccountServiceName = "createFinAccount";
				if (UtilValidate.isNotEmpty(productStoreId)) {
					createAccountServiceName = "createFinAccountForStore";
				}
				// automatically set the parameters
				ModelService createAccountService = dctx
						.getModelService(createAccountServiceName);
				Map<String, Object> createAccountContext = createAccountService
						.makeValid(context, ModelService.IN_PARAM);
				createAccountContext.put("finAccountTypeId", finAccountTypeId);
				createAccountContext.put("finAccountName", accountName);
				createAccountContext.put("ownerPartyId", partyId);
				createAccountContext.put("userLogin", userLogin);

				Map<String, Object> createAccountResult = dispatcher.runSync(
						createAccountServiceName, createAccountContext);
				if (ServiceUtil.isError(createAccountResult)
						|| ServiceUtil.isFailure(createAccountResult)) {
					return createAccountResult;
				}

				if (createAccountResult != null) {
					String creditAccountId = (String) createAccountResult
							.get("finAccountId");
					if (UtilValidate.isNotEmpty(creditAccountId)) {
						creditAccount = delegator
								.findOne("FinAccount", UtilMisc.toMap(
										"finAccountId", creditAccountId), false);

						// create the owner role
						Map<String, Object> roleCtx = FastMap.newInstance();
						roleCtx.put("partyId", partyId);
						roleCtx.put("roleTypeId", "OWNER");
						roleCtx.put("finAccountId", creditAccountId);
						roleCtx.put("userLogin", userLogin);
						roleCtx.put("fromDate", UtilDateTime.nowTimestamp());
						Map<String, Object> roleResp;
						try {
							roleResp = dispatcher.runSync(
									"createFinAccountRole", roleCtx);
						} catch (GenericServiceException e) {
							return ServiceUtil.returnError(e.getMessage());
						}
						if (ServiceUtil.isError(roleResp)) {
							return roleResp;
						}
						finAccountId = creditAccountId; // update the
														// finAccountId for
														// return parameter
					}
				}
				if (creditAccount == null) {
					return ServiceUtil.returnError(UtilProperties.getMessage(
							resourceError,
							"AccountingFinAccountCannotCreditAccount", locale));
				}
			}

			// create the credit transaction
			Map<String, Object> transactionMap = FastMap.newInstance();
			transactionMap.put("finAccountTransTypeId", "ADJUSTMENT");
			transactionMap.put("finAccountId", creditAccount.getString("finAccountId"));
			transactionMap.put("partyId", partyId);
			transactionMap.put("amount", context.get("amount"));
			transactionMap.put("reasonEnumId", context.get("reasonEnumId"));
			transactionMap.put("comments", context.get("comments"));
			transactionMap.put("userLogin", userLogin);

			Map<String, Object> creditTransResult = dispatcher.runSync(
					"createFinAccountTrans", transactionMap);
			if (ServiceUtil.isError(creditTransResult)
					|| ServiceUtil.isFailure(creditTransResult)) {
				return creditTransResult;
			}
		} catch (GenericEntityException gee) {
			return ServiceUtil.returnError(gee.getMessage());
		} catch (GenericServiceException gse) {
			return ServiceUtil.returnError(gse.getMessage());
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("finAccountId", finAccountId);
		return result;
	}

	public static Map<String, Object> deleteBankRecon(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		String headerId = (String) context.get("headerId");

		GenericValue reconHeader = getReconHeader(delegator, headerId);

		// get all bankreconlines for this header id
		List<GenericValue> bankReconLines = null;
		try {
			bankReconLines = reconHeader.getRelated("BankReconLines", null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// foreach delete

		for (GenericValue reconLine : bankReconLines) {
			try {
				reconLine.remove();
			} catch (Exception e) {
				return ServiceUtil.returnError("COULD NOT DELETE TRANSACTION " + reconLine.getString("description"));
			}
		}

		// finally delete the header
		try {
			reconHeader.remove();
		} catch (Exception e) {
			return ServiceUtil.returnError("COULD NOT DELETE RECONCILIATION " + reconHeader.getString("name"));
		}
		return ServiceUtil.returnSuccess("RECONCILIATION DELETED SUCCESSFULLY");
	}

	public static Map<String, Object> createReconHeader(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		Timestamp reconDate = (Timestamp) context.get("reconDate");
		BigDecimal bankBalance = (BigDecimal) context.get("bankBalance");

		GenericValue finAccount = null;
		GenericValue acctgTrans = null;
		try {
			finAccount = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId", finAccountId), false);
			acctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId", "1"), false);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (finAccount == null) {
			return ServiceUtil.returnError("PROBLEMS IN RETRIEVEING BANK ACCOUNT DETAILS");
		}
		String glAccountId = finAccount.getString("postToGlAccountId");
		String organizationPartyId = finAccount.getString("organizationPartyId");
		Timestamp fromDate = acctgTrans.getTimestamp("postedDate");

		// TODO We need to get cash book balance here
		BigDecimal cashBookBalance = getOrganizationCashBookBalance(delegator, reconDate, organizationPartyId, glAccountId, fromDate);
		System.out.println("############################################## CASH BOOK BALANCE RETRIEVED: " + cashBookBalance);
		BigDecimal totalUnpresentedCheques = BigDecimal.ZERO;
		BigDecimal totalUncreditedBankings = BigDecimal.ZERO;
		BigDecimal totalUnreceiptedBankings = BigDecimal.ZERO;
		BigDecimal totalUnidentifiedDebits = BigDecimal.ZERO;

		String headerId = delegator.getNextSeqId("BankReconHeader");

		if (reconDate != null && bankBalance != null && cashBookBalance != null) {
			GenericValue bankReconHeader = delegator.makeValue("BankReconHeader");

			bankReconHeader.put("headerId", headerId);
			bankReconHeader.put("description", "RECON AS AT " + reconDate);
			bankReconHeader.put("reconDate", reconDate);
			bankReconHeader.put("statusName", "CAPTURED");
			bankReconHeader.put("finAccountId", finAccountId);
			bankReconHeader.put("partyId", userLogin.getString("userLoginId"));
			bankReconHeader.put("cashBookBalance", cashBookBalance);
			bankReconHeader.put("bankBalance", bankBalance);
			bankReconHeader.put("adjustedBankBalance", BigDecimal.ZERO);

			try {
				bankReconHeader.create();

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		} else {
			return ServiceUtil.returnError("Reconciliation Date and or Bank Balance is Missing, or even maybe the System is having issues in getting the Cash Book Balance");
		}

		// now get all transactions for this header
		List<GenericValue> transactions = null;
		List<GenericValue> unpresentedUnidentifiedTransactions = null;
		unpresentedUnidentifiedTransactions = getUnpresentedUnidentifiedForHeader(delegator, finAccountId);
		if (unpresentedUnidentifiedTransactions != null) {
			for (GenericValue trans : unpresentedUnidentifiedTransactions) {
				System.out.println("HEADER_ID: " + trans.getString("headerId") + " TRANSACTION " + trans.getString("finAccountTransTypeId"));
				GenericValue reconLines = delegator.makeValue("BankReconLines");
				String reconLinesId = delegator.getNextSeqId("BankReconLines");

				// save to BankReconLines
				reconLines.put("reconLineId", reconLinesId);

				reconLines.put("isUnpresentedCheques", trans.getString("isUnpresentedCheques"));
				reconLines.put("isUncreditedBankings", trans.getString("isUncreditedBankings"));
				reconLines.put("isUnidentifiedDebits", trans.getString("isUnidentifiedDebits"));
				reconLines.put("isUnreceiptedBankings", trans.getString("isUnreceiptedBankings"));
				reconLines.put("headerId", headerId);
				reconLines.put("finAccountTransId", trans.getString("finAccountTransId"));
				reconLines.put("finAccountTransTypeId", trans.getString("finAccountTransTypeId"));
				reconLines.put("finAccountId", trans.getString("finAccountId"));
				reconLines.put("transactionDate", trans.getTimestamp("transactionDate"));
				reconLines.put("amount", trans.getBigDecimal("amount"));
				reconLines.put("description", trans.getString("description"));
				reconLines.put("isReconciledItem", trans.getString("isReconciledItem"));

				try {
					reconLines.create();

				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		}

		transactions = getTransactionsForHeader(reconDate, finAccountId, delegator);
		if (transactions != null) {
			for (GenericValue transaction : transactions) {
				StringBuffer sb = new StringBuffer();
				GenericValue payment = null;

				try {
					payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", transaction.getString("paymentId")), false);
				} catch (GenericEntityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (transaction.getString("comments") == null) {

					// try to get account trans id
					String postingGlAccount = glAccountId;
					Timestamp transactionDate = transaction.getTimestamp("transactionDate");
					BigDecimal amount = transaction.getBigDecimal("amount");

					List<GenericValue> acctgTransEntries = null;
					List<GenericValue> stationAccountTransactions = null;
					GenericValue acctgTransEntry = null;
					GenericValue station = null;

					// AcctgTransEntry Conditions
					EntityConditionList<EntityExpr> acctgTransEntryCond = EntityCondition.makeCondition(UtilMisc.toList(
							EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, postingGlAccount),
							EntityCondition.makeCondition("amount", EntityOperator.EQUALS, amount),
							EntityCondition.makeCondition("createdTxStamp", EntityOperator.EQUALS, transactionDate)
							), EntityOperator.AND);

					try {
						acctgTransEntries = delegator.findList("AcctgTransEntry", acctgTransEntryCond, null, null, null, false);
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}

					if (acctgTransEntries != null) {
						if (acctgTransEntries.size() > 0) {
							acctgTransEntry = acctgTransEntries.get(0);
							// Station Account Transaction
							EntityConditionList<EntityExpr> stationAccountTransactionCond = EntityCondition.makeCondition(UtilMisc.toList(
									EntityCondition.makeCondition("transactionAmount", EntityOperator.EQUALS, acctgTransEntry.getBigDecimal("amount")),
									EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransEntry.getString("acctgTransId"))
									), EntityOperator.AND);

							try {
								stationAccountTransactions = delegator.findList("StationAccountTransaction", stationAccountTransactionCond, null, null, null, false);
								if (stationAccountTransactions.size() > 0) {
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
								}

							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}

					} else {
						System.out.println("WE WERE NO LUCKY IN GETTING ACCTRANS ID");
					}

				} else {
					sb.append(transaction.getString("comments"));
				}

				sb.append(" ");
				if (payment != null) {
					sb.append("Cheque No:");
					sb.append(" ");
					sb.append(payment.getString("paymentRefNum"));
				}
				sb.append(" ");

				GenericValue bankReconLines = delegator.makeValue("BankReconLines");
				String reconLineId = delegator.getNextSeqId("BankReconLines");
				if (transaction.getString("finAccountTransTypeId").equals("WITHDRAWAL")) {
					System.out.println("############################# WE ARE INSIDE " + transaction.getString("finAccountTransTypeId"));
					bankReconLines.put("isUnpresentedCheques", "Y");
					bankReconLines.put("isUncreditedBankings", "N");
					bankReconLines.put("isUnidentifiedDebits", "N");
					bankReconLines.put("isUnreceiptedBankings", "N");
					totalUnpresentedCheques = totalUnpresentedCheques.add(transaction.getBigDecimal("amount"));
				} else if (transaction.getString("finAccountTransTypeId").equals("DEPOSIT")) {
					System.out.println("############################# WE ARE INSIDE " + transaction.getString("finAccountTransTypeId"));
					// get total uncredited bankings deposits
					totalUncreditedBankings = totalUncreditedBankings.add(transaction.getBigDecimal("amount"));
					bankReconLines.put("isUncreditedBankings", "Y");
					bankReconLines.put("isUnidentifiedDebits", "N");
					bankReconLines.put("isUnreceiptedBankings", "N");
					bankReconLines.put("isUnpresentedCheques", "N");
				}

				// save to BankReconLines
				bankReconLines.put("reconLineId", reconLineId);
				bankReconLines.put("headerId", headerId);
				bankReconLines.put("finAccountTransId", transaction.getString("finAccountTransId"));
				bankReconLines.put("finAccountTransTypeId", transaction.getString("finAccountTransTypeId"));
				bankReconLines.put("finAccountId", transaction.getString("finAccountId"));
				bankReconLines.put("transactionDate", transaction.getTimestamp("transactionDate"));
				bankReconLines.put("amount", transaction.getBigDecimal("amount"));
				bankReconLines.put("description", sb.toString());
				bankReconLines.put("isReconciledItem", "N");

				try {
					bankReconLines.create();

				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
		} else {
			return ServiceUtil.returnError("No Transactions For this Recon. Check Dates and ensure there are transactions done on or Before that date");
		}
		return ServiceUtil.returnSuccess("CREATED SUCCESSFULLY");

	}

	private static BigDecimal getOrganizationCashBookBalance(Delegator delegator, Timestamp reconDate, String organizationPartyId, String glAccountId, Timestamp fromDate) {
		String debitFlag = "D";
		String creditFlag = "C";
		BigDecimal cashBookBalance = BigDecimal.ZERO;
		if (glAccountId == null) {
			return (BigDecimal) ServiceUtil.returnError("COULD NOT GET THE GL ACCOUNT FOR THIS CASH BOOK");

		} else if (organizationPartyId == null) {
			return (BigDecimal) ServiceUtil.returnError("MANAGED BY IS NOT SET FOR THIS BANK ACCOUNT");
		}
		BigDecimal totalDebits = getTotalDebitsForAccount(delegator, fromDate, reconDate, glAccountId, debitFlag, organizationPartyId);
		BigDecimal totalCredits = getTotalDebitsForAccount(delegator, fromDate, reconDate, glAccountId, creditFlag, organizationPartyId);

		System.out.println("############################################## totalDebits RETRIEVED: " + totalDebits + " For glAccountId: " + glAccountId);
		System.out.println("############################################## totalCredits RETRIEVED: " + totalCredits + " For glAccountId: " + glAccountId);
		Boolean isDebit = null;
		GenericValue glAccount = null;
		try {
			glAccount = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", glAccountId), false);
			isDebit = UtilAccounting.isDebitAccount(glAccount);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (isDebit) {
			cashBookBalance = totalDebits.subtract(totalCredits);
		} else {
			cashBookBalance = totalCredits.subtract(totalDebits);
		}
		return cashBookBalance;
	}

	private static BigDecimal getTotalDebitsForAccount(Delegator delegator, Timestamp fromDate, Timestamp reconDate, String glAccountId, String debitCreditFlag, String organizationPartyId) {
		System.out.println("################################ GETTING ACCOUNT BALANCE USING: ");
		System.out.println("################################ GETTING ACCOUNT BALANCE USING fromDate: " + fromDate);
		System.out.println("################################ GETTING ACCOUNT BALANCE USING reconDate: " + reconDate);
		System.out.println("################################ GETTING ACCOUNT BALANCE USING glAccountId: " + glAccountId);
		System.out.println("################################ GETTING ACCOUNT BALANCE USING debitCreditFlag: " + debitCreditFlag);
		System.out.println("################################ GETTING ACCOUNT BALANCE USING organizationPartyId: " + organizationPartyId);

		BigDecimal accountBalance = BigDecimal.ZERO;
		List<GenericValue> acctgTransEntry = null;
		EntityConditionList<EntityExpr> acctgTransEntrySumsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId),
				EntityCondition.makeCondition("debitCreditFlag", EntityOperator.EQUALS, debitCreditFlag),
				EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, reconDate),
				EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate)
				), EntityOperator.AND);

		try {
			acctgTransEntry = delegator.findList("AcctgTransEntry", acctgTransEntrySumsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		System.out.println("############################################## Number of Trans RETRIEVED: " + acctgTransEntry.size() + " For glAccountId: " + glAccountId);
		for (GenericValue entry : acctgTransEntry) {
			System.out.println("#################################### accountBalance " + accountBalance);
			System.out.println("#################################### AMOUNT " + entry.getBigDecimal("amount"));

			accountBalance = accountBalance.add(entry.getBigDecimal("amount"));
			System.out.println("#################################### TOTAL AMOUNT " + entry.getBigDecimal("amount"));
		}

		return accountBalance;
	}

	public static Map<String, Object> createUnidentifiedUpresented(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String headerId = (String) context.get("headerId");
		Timestamp transactionDate = (Timestamp) context.get("transactionDate");
		BigDecimal amount = (BigDecimal) context.get("amount");
		String description = (String) context.get("description");
		String finAccountTransTypeId = (String) context.get("finAccountTransTypeId");
		String referenceNumber = (String) context.get("referenceNumber");

		GenericValue bankReconLines = delegator.makeValue("BankReconLines");
		String reconLineId = delegator.getNextSeqId("BankReconLines");

		if (finAccountTransTypeId.equals("UD")) {
			bankReconLines.put("isUnidentifiedDebits", "Y");
		} else if (finAccountTransTypeId.equals("UB")) {
			bankReconLines.put("isUnreceiptedBankings", "Y");
		}

		// save to BankReconLines
		bankReconLines.put("reconLineId", reconLineId);
		bankReconLines.put("headerId", headerId);
		bankReconLines.put("finAccountTransId", delegator.getNextSeqId("FinAccountTrans"));
		bankReconLines.put("finAccountTransTypeId", finAccountTransTypeId);
		bankReconLines.put("transactionDate", transactionDate);
		bankReconLines.put("amount", amount);
		bankReconLines.put("description", description);
		bankReconLines.put("isReconciledItem", "N");
		bankReconLines.put("isUncreditedBankings", "N");
		bankReconLines.put("isUnpresentedCheques", "N");
		bankReconLines.put("referenceNumber", referenceNumber);
		bankReconLines.put("isManuallyCreated", "Y");

		try {
			bankReconLines.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		return result;
	}

	public static Map<String, Object> createUnreceiptedCheques(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String headerId = (String) context.get("headerId");
		Timestamp transactionDate = (Timestamp) context.get("transactionDate");
		BigDecimal amount = (BigDecimal) context.get("amount");
		String description = (String) context.get("description");
		String finAccountTransTypeId = (String) context.get("finAccountTransTypeId");
		String referenceNumber = (String) context.get("referenceNumber");

		GenericValue bankReconLines = delegator.makeValue("BankReconLines");
		String reconLineId = delegator.getNextSeqId("BankReconLines");

		// save to BankReconLines
		bankReconLines.put("isUnreceiptedBankings", "Y");
		bankReconLines.put("reconLineId", reconLineId);
		bankReconLines.put("headerId", headerId);
		bankReconLines.put("finAccountTransId", delegator.getNextSeqId("FinAccountTrans"));
		bankReconLines.put("finAccountTransTypeId", finAccountTransTypeId);
		bankReconLines.put("transactionDate", transactionDate);
		bankReconLines.put("amount", amount);
		bankReconLines.put("description", description);
		bankReconLines.put("isReconciledItem", "N");
		bankReconLines.put("isUncreditedBankings", "N");
		bankReconLines.put("isUnpresentedCheques", "N");
		bankReconLines.put("referenceNumber", referenceNumber);
		bankReconLines.put("isManuallyCreated", "Y");

		try {
			bankReconLines.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		return result;
	}

	public static Map<String, Object> createUnidentifiedDebits(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String headerId = (String) context.get("headerId");
		Timestamp transactionDate = (Timestamp) context.get("transactionDate");
		BigDecimal amount = (BigDecimal) context.get("amount");
		String description = (String) context.get("description");
		String finAccountTransTypeId = (String) context.get("finAccountTransTypeId");
		String referenceNumber = (String) context.get("referenceNumber");

		GenericValue bankReconLines = delegator.makeValue("BankReconLines");
		String reconLineId = delegator.getNextSeqId("BankReconLines");

		// save to BankReconLines
		bankReconLines.put("isUnidentifiedDebits", "Y");
		bankReconLines.put("reconLineId", reconLineId);
		bankReconLines.put("headerId", headerId);
		bankReconLines.put("finAccountTransId", delegator.getNextSeqId("FinAccountTrans"));
		bankReconLines.put("finAccountTransTypeId", finAccountTransTypeId);
		bankReconLines.put("transactionDate", transactionDate);
		bankReconLines.put("amount", amount);
		bankReconLines.put("description", description);
		bankReconLines.put("isReconciledItem", "N");
		bankReconLines.put("isUncreditedBankings", "N");
		bankReconLines.put("isUnpresentedCheques", "N");
		bankReconLines.put("referenceNumber", referenceNumber);
		bankReconLines.put("isManuallyCreated", "Y");

		try {
			bankReconLines.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		return result;
	}

	public static Map<String, Object> deleteUnidentifiedUnpresented(DispatchContext dctx, Map<String, Object> context) {
		String headerId = (String) context.get("headerId");
		String reconLineId = (String) context.get("reconLineId");
		Delegator delegator = dctx.getDelegator();
		Map<String, String> fields = UtilMisc.<String, String> toMap("headerId", headerId, "reconLineId", reconLineId);

		System.out.println("############################################# TRYING TO REMOVE RECON LINE: ");
		System.out.println("############################################# USING headerId: " + headerId);
		System.out.println("############################################# USING reconLineId: " + reconLineId);

		GenericValue bankReconLines = null;
		GenericValue finAccountTrans = null;
		try {
			bankReconLines = delegator.findOne("BankReconLines", fields, false);
			finAccountTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId", bankReconLines.getString("finAccountTransId")), false);

			System.out.println("############## BANK RECON GENERIC VALUE isManuallyCreated : " + bankReconLines.getString("isManuallyCreated"));

			if (finAccountTrans != null) {
				System.out.println("WE ARE HERE BECAUSE WE FOUND FIN ACCOUNT TRANS WITH ID " + bankReconLines.getString("finAccountTransId"));
				finAccountTrans.set("statusId", "FINACT_TRNS_APPROVED");
				finAccountTrans.store();
			} else {
				// get all bank recons with this finAccountTransId
				List<GenericValue> reconLines = null;
				EntityConditionList<EntityExpr> conditions = null;
				conditions = EntityCondition.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("finAccountTransId", EntityOperator.EQUALS, bankReconLines.getString("finAccountTransId"))
						));
				reconLines = delegator.findList("BankReconLines", conditions, null, null, null, false);
				for (GenericValue line : reconLines) {
					line.set("isManuallyCreated", "N");
					line.store();
				}

			}
			bankReconLines.remove();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);

		return result;
	}

	private static List<GenericValue> getTransactionsForHeader(Timestamp reconDate, String finAccountId, Delegator delegator) {
		System.out.println("############################################# TRYING TO GET TRANSACTIONS FOR: ");
		System.out.println("############################################# RECON DATE: " + reconDate);
		System.out.println("############################################# FIN ACCOUNT ID: " + finAccountId);
		List<GenericValue> transactions = null;
		// Get all transactions for this finAccount Id that have not been
		// reconciled and older than the recon date
		EntityConditionList<EntityExpr> cond = null;
		cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("finAccountId", EntityOperator.EQUALS, finAccountId),
				EntityCondition.makeCondition("transactionDate", EntityOperator.LESS_THAN_EQUAL_TO, reconDate),
				// EntityCondition.makeCondition("isReconcilled",
				// EntityOperator.NOT_EQUAL, "Y")
				EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "FINACT_TRNS_CREATED")
				));

		try {
			transactions = delegator.findList("FinAccountTrans", cond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		System.out.println("############################################# NUMBER OF TRANSACTIONS FETCHED: " + transactions.size());
		return transactions;
	}

	private static List<GenericValue> getUnpresentedUnidentifiedForHeader(Delegator delegator, String finAccountId) {
		List<GenericValue> unpresentedUnidentifiedTransactions = null;

		GenericValue lastSavedReconciliation = getLastSavedRecon(delegator, finAccountId);

		// lookup payment applications which took place before the asOfDateTime
		// for this invoice
		EntityConditionList<EntityExpr> dateCondition = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("isUnreceiptedBankings", EntityOperator.EQUALS, "Y"),
				EntityCondition.makeCondition("isUnidentifiedDebits", EntityOperator.EQUALS, "Y")), EntityOperator.OR);

		EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
				dateCondition,
				EntityCondition.makeCondition("headerId", EntityOperator.EQUALS, lastSavedReconciliation.getString("headerId"))),
				EntityOperator.AND);

		try {
			unpresentedUnidentifiedTransactions = delegator.findList("BankReconLines", conditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		System.out.println("############################################# NUMBER OF TRANSACTIONS FETCHED: " + unpresentedUnidentifiedTransactions.size());

		return unpresentedUnidentifiedTransactions;
	}

	public static BigDecimal getUncreditedBankings(Delegator delegator, String headerId) {
		BigDecimal uncreditedBankings = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);
		List<GenericValue> bankReconLines = null;
		try {
			bankReconLines = reconHeader.getRelated("BankReconLines", null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bankReconLines != null) {
			for (GenericValue line : bankReconLines) {
				if (line.getString("isUncreditedBankings") != null && line.getString("isUncreditedBankings").equals("Y")) {
					uncreditedBankings = uncreditedBankings.add(getLineAmount(line));
				}
			}
		} else {
			return (BigDecimal) ServiceUtil.returnError("COULF NOT FETCH BANK RECON LINES");
		}

		System.out.println("#############################################BANK RECON LINES COUNT: " + bankReconLines.size());
		System.out.println("#############################################getUncreditedBankings: " + uncreditedBankings);
		return uncreditedBankings;
	}

	public static BigDecimal getUnreceiptedBankings(Delegator delegator, String headerId) {
		BigDecimal unreceiptedBankings = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);
		List<GenericValue> bankReconLines = null;
		try {
			bankReconLines = reconHeader.getRelated("BankReconLines", null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bankReconLines != null) {
			for (GenericValue line : bankReconLines) {
				if (line.getString("isUnreceiptedBankings") != null && line.getString("isUnreceiptedBankings").equals("Y")) {
					unreceiptedBankings = unreceiptedBankings.add(getLineAmount(line));
				}
			}
		} else {
			return (BigDecimal) ServiceUtil.returnError("COULF NOT FETCH BANK RECON LINES");
		}
		System.out.println("#############################################BANK RECON LINES COUNT: " + bankReconLines.size());
		System.out.println("#############################################getUnreceiptedBankings: " + unreceiptedBankings);
		return unreceiptedBankings;
	}

	private static BigDecimal getLineAmount(GenericValue line) {
		BigDecimal lineAmount = BigDecimal.ZERO;
		lineAmount = line.getBigDecimal("amount");
		return lineAmount;
	}

	private static GenericValue getReconHeader(Delegator delegator, String headerId) {
		GenericValue reconHeader = null;

		try {
			reconHeader = delegator.findOne("BankReconHeader", UtilMisc.toMap("headerId", headerId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return reconHeader;
	}

	public static BigDecimal getUnidentifiedDebits(Delegator delegator, String headerId) {
		BigDecimal unidentifiedDebits = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);
		List<GenericValue> bankReconLines = null;
		try {
			bankReconLines = reconHeader.getRelated("BankReconLines", null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bankReconLines != null) {
			for (GenericValue line : bankReconLines) {
				if (line.getString("isUnidentifiedDebits") != null && line.getString("isUnidentifiedDebits").equals("Y")) {
					unidentifiedDebits = unidentifiedDebits.add(getLineAmount(line));
				}
			}
		} else {
			return (BigDecimal) ServiceUtil.returnError("COULF NOT FETCH BANK RECON LINES");
		}

		System.out.println("#############################################BANK RECON LINES COUNT: " + bankReconLines.size());
		System.out.println("#############################################getUnidentifiedDebits: " + unidentifiedDebits);
		return unidentifiedDebits;
	}

	public static BigDecimal getUnpresentedCheques(Delegator delegator, String headerId) {
		BigDecimal unpresentedCheques = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);
		List<GenericValue> bankReconLines = null;
		try {
			bankReconLines = reconHeader.getRelated("BankReconLines", null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (bankReconLines != null) {
			for (GenericValue line : bankReconLines) {
				if (line.getString("isUnpresentedCheques") != null && line.getString("isUnpresentedCheques").equals("Y")) {
					unpresentedCheques = unpresentedCheques.add(getLineAmount(line));
				}
			}
		} else {
			return (BigDecimal) ServiceUtil.returnError("COULF NOT FETCH BANK RECON LINES");
		}
		System.out.println("#############################################BANK RECON LINES COUNT: " + bankReconLines.size());
		System.out.println("#############################################getUnpresentedCheques: " + unpresentedCheques);
		return unpresentedCheques;
	}

	public static BigDecimal getAdjustedCashBookBalance(Delegator delegator, String headerId) {
		BigDecimal adjustedCashBookBalance = BigDecimal.ZERO;
		BigDecimal unreceiptedBankings = BigDecimal.ZERO;
		BigDecimal unidentifiedDebits = BigDecimal.ZERO;
		BigDecimal cashBookBalance = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);

		cashBookBalance = reconHeader.getBigDecimal("cashBookBalance");
		unreceiptedBankings = getUnreceiptedBankings(delegator, headerId);
		unidentifiedDebits = getUnidentifiedDebits(delegator, headerId);

		adjustedCashBookBalance = (cashBookBalance.add(unreceiptedBankings)).subtract(unidentifiedDebits);

		return adjustedCashBookBalance;
	}

	public static BigDecimal getAdjustedBankBalance(Delegator delegator, String headerId) {
		BigDecimal adjustedBankBalance = BigDecimal.ZERO;
		BigDecimal uncreditedBankings = BigDecimal.ZERO;
		BigDecimal unpresentedCheques = BigDecimal.ZERO;
		BigDecimal bankBalance = BigDecimal.ZERO;

		GenericValue reconHeader = getReconHeader(delegator, headerId);

		uncreditedBankings = getUncreditedBankings(delegator, headerId);
		unpresentedCheques = getUnpresentedCheques(delegator, headerId);
		bankBalance = reconHeader.getBigDecimal("bankBalance");

		adjustedBankBalance = (bankBalance.add(uncreditedBankings)).subtract(unpresentedCheques);

		return adjustedBankBalance;
	}

	public static Map<String, Object> saveReconciliation(DispatchContext dctx, Map<String, Object> context) {
		String headerId = (String) context.get("headerId");
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		BigDecimal adjustedBankBalance = BigDecimal.ZERO;
		BigDecimal adjustedCashBookBalance = BigDecimal.ZERO;

		adjustedBankBalance = getAdjustedBankBalance(delegator, headerId);
		adjustedCashBookBalance = getAdjustedCashBookBalance(delegator, headerId);

		GenericValue bankReconHeader = getReconHeader(delegator, headerId);

		if (bankReconHeader.getString("statusName").equals("RECONCILED")) {
			return ServiceUtil.returnError("THIS RECONCILIATION HAS ALREADY BEEN SAVED(RECONCILED)");
		}

		// compareDebitsToCredit(adjustedCashBookBalance, adjustedBankBalance,
		// creditAmount);
		int comparison = compareBigDecimals(adjustedBankBalance, adjustedCashBookBalance);
		if (comparison != 0) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					resourceError, "ReconciliationNotBalancingError", UtilMisc
							.toMap("headerId", headerId),
					locale));
		}

		// Replace is last recon here
		GenericValue lastRecon = getLastSavedRecon(delegator, bankReconHeader.getString("finAccountId"));

		try {
			lastRecon.set("isLastRecon", "N");
			lastRecon.store();
		} catch (Exception e) {
			// TODO: handle exception
		}

		bankReconHeader.set("statusName", "RECONCILED");
		bankReconHeader.set("adjustedCashBookBalance", adjustedCashBookBalance);
		bankReconHeader.set("adjustedBankBalance", adjustedBankBalance);
		bankReconHeader.set("isLastRecon", "Y");
		try {
			bankReconHeader.store();
			// Update FinAccountTrans statusId to FINACT_TRNS_APPROVED
			List<GenericValue> transactions = getReconciledItems(delegator, headerId);
			if (transactions.size() > 0) {
				for (GenericValue transaction : transactions) {
					GenericValue finAccountTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId", transaction.getString("finAccountTransId")), false);
					finAccountTrans.set("statusId", "FINACT_TRNS_APPROVED");
					finAccountTrans.store();
				}
			}

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		result.put("finAccountId", bankReconHeader.getString("finAccountId"));
		return result;
	}

	private static GenericValue getLastSavedRecon(Delegator delegator, String finAccountId) {
		List<GenericValue> recons = null;
		GenericValue lastSavedRecon = null;

		EntityConditionList<EntityExpr> lastReconCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("isLastRecon", EntityOperator.EQUALS, "Y"),
				EntityCondition.makeCondition("finAccountId", EntityOperator.EQUALS, finAccountId),
				EntityCondition.makeCondition("statusName", EntityOperator.EQUALS, "RECONCILED")
				), EntityOperator.AND);

		try {
			recons = delegator.findList("BankReconHeader", lastReconCond, null, null, null, false);
			System.out.println("##########################################################################WE GOT " + recons.size() + " LAST SAVED RECON HEADERS");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (recons != null && recons.size() > 0) {
			lastSavedRecon = recons.get(0);
		} else {
			System.out.println("##################################################################### IS THIS THE FIRST RECON OR WE MISSED IN GETTING THE LAST RECON");
		}
		System.out.println("##########################################################################WE GOT HEADER ID " + lastSavedRecon.getString("headerId") + " LAST SAVED RECON HEADERS");
		return lastSavedRecon;
	}

	private static List<GenericValue> getReconciledItems(Delegator delegator, String headerId) {
		List<GenericValue> transactions = null;

		EntityConditionList<EntityExpr> accountTransactionsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("isReconciledItem", EntityOperator.EQUALS, "Y"),
				EntityCondition.makeCondition("headerId", EntityOperator.EQUALS, headerId)
				), EntityOperator.AND);

		try {
			transactions = delegator.findList("BankReconLines", accountTransactionsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return transactions;
	}

	public static Map<String, Object> reconcileBankTrans(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		String finAccountTransId = (String) context.get("finAccountTransId");
		String reconDate = (String) context.get("reconDate");
		String headerId = (String) context.get("headerId");
		String reconLineId = (String) context.get("reconLineId");

		System.out.println("########### finAccountId: " + finAccountId);
		System.out.println("########### headerId: " + headerId);
		System.out.println("########### finAccountTransId: " + finAccountTransId);
		System.out.println("########### userLogin: " + userLogin.getString("userLoginId"));
		System.out.println("########### reconDate: " + reconDate);

		GenericValue bankReconLine = null;

		try {
			bankReconLine = delegator.findOne("BankReconLines", UtilMisc.toMap("reconLineId", reconLineId, "headerId", headerId), false);
			bankReconLine.set("isReconciledItem", "Y");
			if (bankReconLine.getString("finAccountTransTypeId").equals("DEPOSIT")) {
				bankReconLine.set("isUncreditedBankings", "N");
			} else if (bankReconLine.getString("finAccountTransTypeId").equals("WITHDRAWAL")) {
				bankReconLine.set("isUnpresentedCheques", "N");
			}
			bankReconLine.store();
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		return result;

	}

	public static Map<String, Object> unreconcileBankTrans(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		String finAccountTransId = (String) context.get("finAccountTransId");
		String reconDate = (String) context.get("reconDate");
		String headerId = (String) context.get("headerId");
		String reconLineId = (String) context.get("reconLineId");

		System.out.println("########### finAccountId: " + finAccountId);
		System.out.println("########### headerId: " + headerId);
		System.out.println("########### finAccountTransId: " + finAccountTransId);
		System.out.println("########### userLogin: " + userLogin.getString("userLoginId"));
		System.out.println("########### reconDate: " + reconDate);

		GenericValue bankReconLine = null;

		try {
			bankReconLine = delegator.findOne("BankReconLines", UtilMisc.toMap("reconLineId", reconLineId, "headerId", headerId), false);
			bankReconLine.set("isReconciledItem", "N");
			if (bankReconLine.getString("finAccountTransTypeId").equals("DEPOSIT")) {
				bankReconLine.set("isUncreditedBankings", "Y");
			} else if (bankReconLine.getString("finAccountTransTypeId").equals("WITHDRAWAL")) {
				bankReconLine.set("isUnpresentedCheques", "Y");
			}
			bankReconLine.store();
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("headerId", headerId);
		return result;

	}

	public static Map<String, Object> addCreditAccountPayment(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String paymentId = (String) context.get("paymentId");
		String comments = (String) context.get("comments");
		String glAccountId = (String) context.get("glAccountId");
		BigDecimal amount = (BigDecimal) context.get("amount");

		// Controlss

		// 1. Check that the credit amount is not exceeded by the Debit Lines
		// Get the MultiPayment Header
		GenericValue multiPayment = null;
		BigDecimal creditAmount = BigDecimal.ZERO;
		BigDecimal debitAmount = getMultiplePaymentDebitAmount(delegator, paymentId);
		try {
			multiPayment = delegator.findOne("MultiPayment", UtilMisc.toMap("paymentId", paymentId), false);
			if (multiPayment != null) {
				creditAmount = multiPayment.getBigDecimal("amount");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}

		int compare = compareDebitsToCredit(amount, debitAmount, creditAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount !");
		}

		GenericValue multiPaymentLines = delegator.makeValue("MultiPaymentLines");
		String lineId = delegator.getNextSeqId("lineId");

		multiPaymentLines.put("paymentId", paymentId);
		multiPaymentLines.put("comments", comments);
		multiPaymentLines.put("glAccountId", glAccountId);
		multiPaymentLines.put("amount", amount);
		multiPaymentLines.put("lineId", lineId);

		try {
			multiPaymentLines.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", paymentId);

		return result;
	}

	public static Map<String, Object> addDebitAccountPayment(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String paymentId = (String) context.get("paymentId");
		String comments = (String) context.get("comments");
		String glAccountId = (String) context.get("glAccountId");
		BigDecimal amount = (BigDecimal) context.get("amount");

		// Controlss

		// 1. Check that the credit amount is not exceeded by the Debit Lines
		// Get the MultiPayment Header
		GenericValue multiPayment = null;
		BigDecimal creditAmount = BigDecimal.ZERO;
		BigDecimal debitAmount = getMultiplePaymentDebitAmount(delegator, paymentId);
		try {
			multiPayment = delegator.findOne("MultiPayment", UtilMisc.toMap("paymentId", paymentId), false);
			if (multiPayment != null) {
				creditAmount = multiPayment.getBigDecimal("amount");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}

		int compare = compareDebitsToCredit(amount, debitAmount, creditAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount !");
		}

		GenericValue multiPaymentLines = delegator.makeValue("MultiPaymentLines");
		String lineId = delegator.getNextSeqId("lineId");

		multiPaymentLines.put("paymentId", paymentId);
		multiPaymentLines.put("comments", comments);
		multiPaymentLines.put("glAccountId", glAccountId);
		multiPaymentLines.put("amount", amount);
		multiPaymentLines.put("lineId", lineId);

		try {
			multiPaymentLines.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", paymentId);

		return result;
	}

	public static int compareDebitsToCredit(BigDecimal currentAmount, BigDecimal debitAmount, BigDecimal creditAmount) {
		System.out.println("######################### INITIAL DEBIT AMOUNT: " + debitAmount);
		System.out.println("######################### CREDIT AMOUNT: " + creditAmount);
		debitAmount = debitAmount.add(currentAmount);
		System.out.println("######################### DEBIT AMOUNT: " + debitAmount);
		int compare = debitAmount.compareTo(creditAmount);

		String str1 = "####################### Both values are equal ";
		String str2 = "####################### First Value is greater ";
		String str3 = "####################### Second value is greater";

		if (compare == 0)
			System.out.println(str1);
		else if (compare == 1)
			System.out.println(str2);
		else if (compare == -1)
			System.out.println(str3);

		return compare;
	}

	private static int compareBigDecimals(BigDecimal value1, BigDecimal value2) {
		int comparison = value1.compareTo(value2);

		String str1 = "####################### Both values are equal ";
		String str2 = "####################### First Value is greater ";
		String str3 = "####################### Second value is greater";

		if (comparison == 0)
			System.out.println(str1);
		else if (comparison == 1)
			System.out.println(str2);
		else if (comparison == -1)
			System.out.println(str3);

		return comparison;
	}

	public static Map<String, Object> updateDebitAccountPayment(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String paymentId = (String) context.get("paymentId");
		String lineId = (String) context.get("lineId");
		String comments = (String) context.get("comments");
		String glAccountId = (String) context.get("glAccountId");
		BigDecimal amount = (BigDecimal) context.get("amount");
		Map<String, String> fields = UtilMisc.<String, String> toMap("paymentId", paymentId, "lineId", lineId);

		GenericValue multiPayment = null;
		BigDecimal creditAmount = BigDecimal.ZERO;
		BigDecimal debitAmount = getMultiplePaymentDebitAmount(delegator, paymentId);
		try {
			multiPayment = delegator.findOne("MultiPayment", UtilMisc.toMap("paymentId", paymentId), false);
			if (multiPayment != null) {
				creditAmount = multiPayment.getBigDecimal("amount");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		int compare = compareDebitsToCredit(amount, debitAmount, creditAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Debit Can not be more than Credit Amount !");
		}

		GenericValue multiPaymentLine = null;

		try {
			multiPaymentLine = delegator.findOne("MultiPaymentLines", fields, false);
			multiPaymentLine.set("amount", amount);
			multiPaymentLine.set("comments", comments);
			multiPaymentLine.set("glAccountId", glAccountId);
			multiPaymentLine.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", paymentId);

		return result;
	}

	public static Map<String, Object> removeDebitAccountPayment(DispatchContext dctx, Map<String, Object> context) {
		String paymentId = (String) context.get("paymentId");
		String lineId = (String) context.get("lineId");
		Delegator delegator = dctx.getDelegator();
		Map<String, String> fields = UtilMisc.<String, String> toMap("paymentId", paymentId, "lineId", lineId);

		GenericValue multiPaymentLine = null;
		try {
			multiPaymentLine = delegator.findOne("MultiPaymentLines", fields, false);
			multiPaymentLine.remove();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", paymentId);

		return result;
	}

	public static BigDecimal getMultiplePaymentDebitAmount(Delegator delegator, String paymentId) {
		BigDecimal debitAmount = BigDecimal.ZERO;
		List<GenericValue> paymentLines = null;

		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId)
				));
		try {
			paymentLines = delegator.findList("MultiPaymentLines", cond, null, null, null, false);
			for (GenericValue line : paymentLines) {
				debitAmount = debitAmount.add(line.getBigDecimal("amount"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return debitAmount;
	}

	public static BigDecimal getMultiplePaymentCreditAmount(GenericValue payment) {
		BigDecimal creditAmount = BigDecimal.ZERO;

		creditAmount = payment.getBigDecimal("amount");

		return creditAmount;
	}

	public static int getTotalNumberOfDebitLines(Delegator delegator, String paymentId) {

		int totalDebitLines = 0;
		List<GenericValue> paymentLines = null;

		try {
			EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId)
					));
			paymentLines = delegator.findList("MultiPaymentLines", cond, null, null, null, false);
			totalDebitLines = paymentLines.size();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return totalDebitLines;
	}

	public static Map<String, Object> reverseTransaction(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String paymentId = (String) context.get("paymentId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// get payment using paymentId
		GenericValue payment = null;
		try {
			payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", paymentId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EntityConditionList<EntityExpr> cond = null;
		cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId)
				));
		List<GenericValue> acctgTransEntryItems = null;
		try {
			acctgTransEntryItems = delegator.findList("AcctgTransAndEntries", cond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// get financial transaction affected
		GenericValue finAccountTrans = null;
		try {
			finAccountTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId", payment.getString("finAccountTransId")), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// CREATE PAYMENT HEADER FOR THE REVERSED
		String reversedPaymentId = createPaymentHeaderForReversed(delegator, payment);

		// REVERSE ACCT TRANS
		// CREATE THE NEGATING ACCTG TRANS
		Boolean isAcctgTransSuccess = createReverseGlTransactions(delegator, reversedPaymentId, acctgTransEntryItems);

		// REVERSE Financial Account Transaction
		// CREATE THE NEGATING FIN ACCOUNT TRANS ID
		Boolean isFinancialAccountTransactionSuccess = createReverseBankTrans(delegator, reversedPaymentId, finAccountTrans);

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", reversedPaymentId);
		return result;
	}

	private static Boolean createReverseBankTrans(Delegator delegator, String reversedPaymentId, GenericValue finAccountTrans) {
		Boolean isSuccess = false;

		GenericValue oldFinAccountTrans = finAccountTrans;
		GenericValue newFinAccountTrans = null;

		newFinAccountTrans = delegator.makeValue("FinAccountTrans");
		String finAccountTransId = delegator.getNextSeqId("FinAccountTrans");

		String finAccountTransTypeId = null;
		if (oldFinAccountTrans.getString("finAccountTransTypeId").equals("WITHDRAWAL")) {
			finAccountTransTypeId = "DEPOSIT";
		} else if (oldFinAccountTrans.getString("finAccountTransTypeId").equals("DEPOSIT")) {
			finAccountTransTypeId = "WITHDRAWAL";
		}
		newFinAccountTrans.put("finAccountTransId", finAccountTransId);
		newFinAccountTrans.put("finAccountTransTypeId", finAccountTransTypeId);
		newFinAccountTrans.put("finAccountId", oldFinAccountTrans.getString("finAccountId"));
		newFinAccountTrans.put("comments", "REVERSE: " + oldFinAccountTrans.getString("comments"));
		newFinAccountTrans.put("transactionDate", UtilDateTime.nowTimestamp());
		newFinAccountTrans.put("amount", oldFinAccountTrans.getBigDecimal("amount"));
		newFinAccountTrans.put("paymentId", reversedPaymentId);
		newFinAccountTrans.put("statusId", "FINACT_TRNS_CREATED");
		newFinAccountTrans.put("toggle", "N");
		newFinAccountTrans.put("isReconcilled", "N");
		try {
			newFinAccountTrans.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	private static Boolean createReverseGlTransactions(Delegator delegator, String reversedPaymentId, List<GenericValue> acctgTransEntryItems) {
		Boolean isSuccess = false;
		GenericValue oldAcctgTransEntry = acctgTransEntryItems.get(0);
		String oldAcctgTransId = oldAcctgTransEntry.getString("acctgTransId");

		GenericValue oldAcctgTrans = null;
		String newAcctgTransId = delegator.getNextSeqId("AcctgTrans");
		try {
			oldAcctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId", oldAcctgTransId), false);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		GenericValue acctgTrans = null;
		acctgTrans = delegator.makeValue("AcctgTrans");
		acctgTrans.put("acctgTransId", newAcctgTransId);
		acctgTrans.put("acctgTransTypeId", oldAcctgTrans.getString("acctgTransTypeId"));
		acctgTrans.put("transactionDate", UtilDateTime.nowTimestamp());
		acctgTrans.put("isPosted", "Y");
		acctgTrans.put("isApproved", "Y");
		acctgTrans.put("postedDate", UtilDateTime.nowTimestamp());
		acctgTrans.put("glFiscalTypeId", "ACTUAL");
		acctgTrans.put("partyId", oldAcctgTrans.getString("partyId"));
		acctgTrans.put("roleTypeId", oldAcctgTrans.getString("roleTypeId"));
		acctgTrans.put("paymentId", reversedPaymentId);

		try {
			acctgTrans.create();

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (GenericValue item : acctgTransEntryItems) {

			GenericValue acctgTransEntry = null;
			String debitCreditFlag = null;
			if (item.getString("debitCreditFlag").equals("C")) {
				debitCreditFlag = "D";
			} else {
				debitCreditFlag = "C";
			}

			acctgTransEntry = delegator.makeValue("AcctgTransEntry");
			String acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
			acctgTransEntry.put("acctgTransId", newAcctgTransId);
			acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
			acctgTransEntry.put("acctgTransEntryTypeId", item.getString("acctgTransEntryTypeId"));
			acctgTransEntry.put("organizationPartyId", item.getString("organizationPartyId"));
			acctgTransEntry.put("amount", item.getBigDecimal("amount"));
			acctgTransEntry.put("origAmount", item.getBigDecimal("origAmount"));
			acctgTransEntry.put("currencyUomId", item.getString("currencyUomId"));
			acctgTransEntry.put("origCurrencyUomId", item.getString("origCurrencyUomId"));
			acctgTransEntry.put("debitCreditFlag", debitCreditFlag);
			acctgTransEntry.put("reconcileStatusId", item.getString("reconcileStatusId"));
			acctgTransEntry.put("partyId", item.getString("partyId"));
			acctgTransEntry.put("roleTypeId", item.getString("roleTypeId"));
			acctgTransEntry.put("glAccountId", item.getString("glAccountId"));

			try {
				acctgTransEntry.create();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		isSuccess = true;
		return isSuccess;
	}

	private static String createPaymentHeaderForReversed(Delegator delegator, GenericValue payment) {
		String reversedPaymentId = null;

		GenericValue confirmedPayment = null;

		if (payment != null) {
			confirmedPayment = delegator.makeValue("Payment");
			reversedPaymentId = delegator.getNextSeqId("Payment");
			confirmedPayment.put("paymentId", reversedPaymentId);
			confirmedPayment.put("paymentTypeId", payment.getString("paymentTypeId"));
			confirmedPayment.put("paymentMethodTypeId", payment.getString("paymentMethodTypeId"));
			confirmedPayment.put("paymentMethodId", payment.getString("paymentMethodId"));
			confirmedPayment.put("partyIdFrom", payment.getString("partyIdTo"));
			confirmedPayment.put("partyIdTo", payment.getString("partyIdFrom"));
			confirmedPayment.put("statusId", "PMNT_CONFIRMED");
			confirmedPayment.put("effectiveDate", payment.getTimestamp("effectiveDate"));
			confirmedPayment.put("amount", payment.getBigDecimal("amount"));
			confirmedPayment.put("comments", "REVERSAL: " + payment.getString("comments"));
			confirmedPayment.put("currencyUomId", payment.getString("currencyUomId"));

			try {
				confirmedPayment.create();
			} catch (Exception e) {

			}
		}

		return reversedPaymentId;
	}

	public static Map<String, Object> resendMsaccoPin(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Long msaccoApplicationId = (Long) context.get("msaccoApplicationId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// Get msaccoApplication using passed id
		GenericValue msaccoApplication = null;
		GenericValue msaccoLogs = null;
		String sent = "N";
		if (msaccoApplicationId != null) {
			try {
				msaccoApplication = delegator.findOne("MSaccoApplication", UtilMisc.toMap("msaccoApplicationId", msaccoApplicationId), false);

				msaccoApplication.set("sent", sent);
				msaccoApplication.store();
				// update logs here
				msaccoLogs = delegator.makeValue("MSaccoLog");
				Long msaccoLogId = delegator.getNextSeqIdLong("MSaccoLog");
				msaccoLogs.put("msaccoLogId", msaccoLogId);
				msaccoLogs.put("isActive", "Y");
				msaccoLogs.put("isActive", "1");
				msaccoLogs.put("cardStatusId", msaccoApplication.getLong("cardStatusId"));
				msaccoLogs.put("msaccoApplicationId", msaccoApplicationId);
				msaccoLogs.put("comment", "PIN RESEND REQUESTED");
				msaccoLogs.put("createdBy", userLogin.getString("userLoginId"));
				msaccoLogs.create();

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

		} else {
			return ServiceUtil.returnError("msaccoApplicationId Missing. Check and Try Again !");
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("msaccoApplicationId", msaccoApplicationId);
		return ServiceUtil.returnSuccess("PIN RESEND REQUEST SENT SUCCESSFULLY");

	}

	public static Map<String, Object> confirmMultiPayment(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String paymentId = (String) context.get("paymentId");
		String lineId = (String) context.get("lineId");
		String comments = (String) context.get("comments");
		String glAccountId = (String) context.get("glAccountId");
		BigDecimal amount = (BigDecimal) context.get("amount");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// Ensure a paymentId has been passed
		if (paymentId == null) {
			return ServiceUtil.returnError("Payment ID Missing Refresh Page and Try Again !");
		}

		GenericValue multiplePaymentHeader = null;
		try {
			multiplePaymentHeader = delegator.findOne("MultiPayment", UtilMisc.toMap("paymentId", paymentId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Check that the Debits and Credits are balancing

		BigDecimal creditAmount = getMultiplePaymentCreditAmount(multiplePaymentHeader);
		BigDecimal debitAmount = getMultiplePaymentDebitAmount(delegator, paymentId);

		int compare = creditAmount.compareTo(debitAmount);

		if (compare != 0) {
			return ServiceUtil.returnError("Ensure that the total Debits Amount is equal to Credit Amount before confirming payment! ");
		}

		// Create a payment

		String confirmedPaymentId = createConfirmedPayment(delegator, multiplePaymentHeader);

		// Create AcctgTransactions

		String acctgTransId = createConfirmedAcctgTrans(delegator, multiplePaymentHeader, confirmedPaymentId, userLogin);

		// Create AcctgTransEntry
		// start with the credit transaction
		Boolean isCreditSuccess = createcreateConfirmedTransEntry(delegator, acctgTransId, multiplePaymentHeader, "C");

		// Debit transactions
		List<GenericValue> debitLines = getPaymentDebitLines(multiplePaymentHeader);
		Boolean isDebitSuccess = null;
		for (GenericValue line : debitLines) {
			isDebitSuccess = createcreateConfirmedDebitTransEntry(delegator, acctgTransId, multiplePaymentHeader, line);
		}

		// Create FinAccountTrans Entry
		String finAccountTransId = null;

		if (isCreditSuccess && isDebitSuccess) {
			finAccountTransId = createFinAccountTranRecord(dctx, dispatcher, context, delegator, multiplePaymentHeader, userLogin, confirmedPaymentId);
		}

		try {
			GenericValue updatePayment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", confirmedPaymentId), false);
			updatePayment.set("finAccountTransId", finAccountTransId);
			multiplePaymentHeader.set("statusId", "PMNT_CONFIRMED");
			multiplePaymentHeader.store();
			updatePayment.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", confirmedPaymentId);
		return result;
	}

	public static Map<String, Object> confirmMultiReceipt(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String paymentId = (String) context.get("paymentId");
		String lineId = (String) context.get("lineId");
		String comments = (String) context.get("comments");
		String glAccountId = (String) context.get("glAccountId");
		BigDecimal amount = (BigDecimal) context.get("amount");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// Ensure a paymentId has been passed
		if (paymentId == null) {
			return ServiceUtil.returnError("Payment ID Missing Refresh Page and Try Again !");
		}

		GenericValue multiplePaymentHeader = null;
		try {
			multiplePaymentHeader = delegator.findOne("MultiPayment", UtilMisc.toMap("paymentId", paymentId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Check that the Debits and Credits are balancing

		BigDecimal creditAmount = getMultiplePaymentCreditAmount(multiplePaymentHeader);
		BigDecimal debitAmount = getMultiplePaymentDebitAmount(delegator, paymentId);

		int compare = creditAmount.compareTo(debitAmount);

		if (compare != 0) {
			return ServiceUtil.returnError("Ensure that the total Debits Amount is equal to Credit Amount before confirming payment! ");
		}

		// Create a payment

		String confirmedPaymentId = createConfirmedPayment(delegator, multiplePaymentHeader);

		// Create AcctgTransactions

		String acctgTransId = createConfirmedAcctgTransForReceipt(delegator, multiplePaymentHeader, confirmedPaymentId, userLogin);

		// Create AcctgTransEntry
		// start with the Debit transaction
		Boolean isDebitSuccess = createcreateConfirmedTransEntryForReceipt(delegator, acctgTransId, multiplePaymentHeader, "D");

		// Credit transactions
		List<GenericValue> debitLines = getPaymentDebitLines(multiplePaymentHeader);
		Boolean isCreditSuccess = null;
		for (GenericValue line : debitLines) {
			isCreditSuccess = createcreateConfirmedCreditTransEntry(delegator, acctgTransId, multiplePaymentHeader, line);
		}

		// Create FinAccountTrans Entry
		String finAccountTransId = null;

		if (isCreditSuccess && isDebitSuccess) {
			finAccountTransId = createDepositFinAccountTranRecord(dctx, dispatcher, context, delegator, multiplePaymentHeader, userLogin, confirmedPaymentId);
		}

		try {
			GenericValue updatePayment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", confirmedPaymentId), false);
			updatePayment.set("finAccountTransId", finAccountTransId);
			multiplePaymentHeader.set("statusId", "PMNT_CONFIRMED");
			multiplePaymentHeader.store();
			updatePayment.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("paymentId", confirmedPaymentId);
		return result;
	}

	private static String createFinAccountTranRecord(DispatchContext dctx, LocalDispatcher dispatcher, Map<String, Object> context, Delegator delegator,
			GenericValue multiplePaymentHeader, GenericValue userLogin, String paymentId) {
		String finAccountTransId = null;

		ModelService createFinAccountTrans = null;
		try {
			createFinAccountTrans = dctx.getModelService("createFinAccountTrans");
		} catch (GenericServiceException e1) {
			e1.printStackTrace();
		}

		Map<String, Object> inContext = createFinAccountTrans.makeValid(context, ModelService.IN_PARAM);
		Map<String, Object> createResult = null;
		inContext.put("finAccountTransTypeId", "WITHDRAWAL");
		inContext.put("finAccountId", getFinacialAccount(delegator, multiplePaymentHeader.getString("paymentMethodId")));
		inContext.put("comments", multiplePaymentHeader.getString("comments"));
		inContext.put("transactionDate", multiplePaymentHeader.getTimestamp("effectiveDate"));
		inContext.put("amount", multiplePaymentHeader.getBigDecimal("amount"));
		inContext.put("referenceNumber", multiplePaymentHeader.getString("paymentRefNum"));
		inContext.put("paymentId", paymentId);
		inContext.put("statusId", "FINACT_TRNS_CREATED");
		inContext.put("toggle", "N");
		inContext.put("isReconcilled", "N");
		try {
			createResult = dispatcher.runSync("createFinAccountTrans", inContext);
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		finAccountTransId = (String) createResult.get("finAccountTransId");
		return finAccountTransId;
	}

	private static String createDepositFinAccountTranRecord(DispatchContext dctx, LocalDispatcher dispatcher, Map<String, Object> context, Delegator delegator,
			GenericValue multiplePaymentHeader, GenericValue userLogin, String paymentId) {
		String finAccountTransId = null;

		ModelService createFinAccountTrans = null;
		try {
			createFinAccountTrans = dctx.getModelService("createFinAccountTrans");
		} catch (GenericServiceException e1) {
			e1.printStackTrace();
		}

		Map<String, Object> inContext = createFinAccountTrans.makeValid(context, ModelService.IN_PARAM);
		Map<String, Object> createResult = null;
		inContext.put("finAccountTransTypeId", "DEPOSIT");
		inContext.put("finAccountId", getFinacialAccount(delegator, multiplePaymentHeader.getString("paymentMethodId")));
		inContext.put("comments", multiplePaymentHeader.getString("comments"));
		inContext.put("transactionDate", multiplePaymentHeader.getTimestamp("effectiveDate"));
		inContext.put("amount", multiplePaymentHeader.getBigDecimal("amount"));
		inContext.put("referenceNumber", multiplePaymentHeader.getString("paymentRefNum"));
		inContext.put("paymentId", paymentId);
		inContext.put("statusId", "FINACT_TRNS_CREATED");
		inContext.put("toggle", "N");
		inContext.put("isReconcilled", "N");
		try {
			createResult = dispatcher.runSync("createFinAccountTrans", inContext);
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		finAccountTransId = (String) createResult.get("finAccountTransId");
		return finAccountTransId;
	}

	private static String getFinacialAccount(Delegator delegator, String paymentMethodId) {
		GenericValue paymentMethod = null;
		String finAccountId = null;
		try {
			paymentMethod = delegator.findOne("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId), false);
			finAccountId = paymentMethod.getString("finAccountId");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return finAccountId;
	}

	private static Boolean createcreateConfirmedDebitTransEntry(Delegator delegator, String acctgTransId, GenericValue multiplePaymentHeader, GenericValue line) {
		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		Boolean isSuccess = false;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", multiplePaymentHeader.getString("partyIdFrom"));
		acctgTransEntry.put("amount", line.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", multiplePaymentHeader.getBigDecimal("origAmount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", "D");
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("glAccountId", line.getString("glAccountId"));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private static Boolean createcreateConfirmedCreditTransEntry(Delegator delegator, String acctgTransId, GenericValue multiplePaymentHeader, GenericValue line) {
		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		Boolean isSuccess = false;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", multiplePaymentHeader.getString("partyIdTo"));
		acctgTransEntry.put("amount", line.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", multiplePaymentHeader.getBigDecimal("origAmount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", "C");
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("glAccountId", line.getString("glAccountId"));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private static List<GenericValue> getPaymentDebitLines(GenericValue multiplePaymentHeader) {
		List<GenericValue> paymentLines = null;

		try {
			paymentLines = multiplePaymentHeader.getRelated("MultiPaymentLines", null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return paymentLines;
	}

	private static Boolean createcreateConfirmedTransEntry(Delegator delegator, String acctgTransId, GenericValue multiplePaymentHeader, String debitCreditFlag) {

		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		Boolean isSuccess = false;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", multiplePaymentHeader.getString("partyIdFrom"));
		acctgTransEntry.put("amount", multiplePaymentHeader.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", multiplePaymentHeader.getBigDecimal("origAmount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", debitCreditFlag);
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("partyId", multiplePaymentHeader.getString("partyIdTo"));
		acctgTransEntry.put("roleTypeId", "BILL_TO_CUSTOMER");
		acctgTransEntry.put("glAccountId", getCreditGlAccount(delegator, multiplePaymentHeader.getString("paymentMethodId")));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private static Boolean createcreateConfirmedTransEntryForReceipt(Delegator delegator, String acctgTransId, GenericValue multiplePaymentHeader, String debitCreditFlag) {

		GenericValue acctgTransEntry = null;
		String acctgTransEntrySeqId = null;
		Boolean isSuccess = false;
		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("acctgTransEntryTypeId", "_NA_");
		acctgTransEntry.put("organizationPartyId", multiplePaymentHeader.getString("partyIdTo"));
		acctgTransEntry.put("amount", multiplePaymentHeader.getBigDecimal("amount"));
		acctgTransEntry.put("origAmount", multiplePaymentHeader.getBigDecimal("origAmount"));
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", debitCreditFlag);
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("partyId", multiplePaymentHeader.getString("partyIdFrom"));
		acctgTransEntry.put("roleTypeId", "BILL_TO_CUSTOMER");
		acctgTransEntry.put("glAccountId", getCreditGlAccount(delegator, multiplePaymentHeader.getString("paymentMethodId")));

		try {
			acctgTransEntry.create();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private static String getCreditGlAccount(Delegator delegator, String paymentMethodId) {
		GenericValue paymentMethod = null;
		String glAccount = null;
		try {
			paymentMethod = delegator.findOne("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId), false);
			glAccount = paymentMethod.getString("glAccountId");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return glAccount;
	}

	private static String createConfirmedAcctgTrans(Delegator delegator, GenericValue multiplePaymentHeader, String confirmedPaymentId, GenericValue userLogin) {

		GenericValue acctgTrans = null;
		String acctgTransId = null;

		if (multiplePaymentHeader != null) {
			acctgTrans = delegator.makeValue("AcctgTrans");
			acctgTransId = delegator.getNextSeqId("AcctgTrans");
			acctgTrans.put("acctgTransId", acctgTransId);
			acctgTrans.put("acctgTransTypeId", "OUTGOING_PAYMENT");
			acctgTrans.put("transactionDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("isPosted", "Y");
			acctgTrans.put("isApproved", "Y");
			acctgTrans.put("postedDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("glFiscalTypeId", "ACTUAL");
			acctgTrans.put("partyId", multiplePaymentHeader.getString("partyIdTo"));
			acctgTrans.put("roleTypeId", "BILL_FROM_VENDOR");
			acctgTrans.put("paymentId", confirmedPaymentId);
			acctgTrans.put("createdByUserLogin", userLogin.getString("userName"));

			try {
				acctgTrans.create();
			} catch (Exception e) {

			}
		}

		return acctgTransId;
	}

	private static String createConfirmedAcctgTransForReceipt(Delegator delegator, GenericValue multiplePaymentHeader, String confirmedPaymentId, GenericValue userLogin) {

		GenericValue acctgTrans = null;
		String acctgTransId = null;

		if (multiplePaymentHeader != null) {
			acctgTrans = delegator.makeValue("AcctgTrans");
			acctgTransId = delegator.getNextSeqId("AcctgTrans");
			acctgTrans.put("acctgTransId", acctgTransId);
			acctgTrans.put("acctgTransTypeId", "INCOMING_PAYMENT");
			acctgTrans.put("transactionDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("isPosted", "Y");
			acctgTrans.put("isApproved", "Y");
			acctgTrans.put("postedDate", UtilDateTime.nowTimestamp());
			acctgTrans.put("glFiscalTypeId", "ACTUAL");
			acctgTrans.put("partyId", multiplePaymentHeader.getString("partyIdTo"));
			acctgTrans.put("roleTypeId", "BILL_TO_CUSTOMER");
			acctgTrans.put("paymentId", confirmedPaymentId);
			acctgTrans.put("createdByUserLogin", userLogin.getString("userName"));

			try {
				acctgTrans.create();
			} catch (Exception e) {

			}
		}

		return acctgTransId;
	}

	private static String createConfirmedPayment(Delegator delegator, GenericValue multiplePaymentHeader) {
		GenericValue confirmedPayment = null;
		String paymentId = null;

		if (multiplePaymentHeader != null) {
			confirmedPayment = delegator.makeValue("Payment");
			paymentId = delegator.getNextSeqId("Payment");
			confirmedPayment.put("paymentId", paymentId);
			confirmedPayment.put("paymentTypeId", multiplePaymentHeader.getString("paymentTypeId"));
			confirmedPayment.put("paymentMethodTypeId", multiplePaymentHeader.getString("paymentMethodTypeId"));
			confirmedPayment.put("paymentMethodId", multiplePaymentHeader.getString("paymentMethodId"));
			confirmedPayment.put("partyIdFrom", multiplePaymentHeader.getString("partyIdFrom"));
			confirmedPayment.put("partyIdTo", multiplePaymentHeader.getString("partyIdTo"));
			confirmedPayment.put("statusId", "PMNT_CONFIRMED");
			confirmedPayment.put("effectiveDate", multiplePaymentHeader.getTimestamp("effectiveDate"));
			confirmedPayment.put("amount", multiplePaymentHeader.getBigDecimal("amount"));
			confirmedPayment.put("comments", multiplePaymentHeader.getString("comments"));
			confirmedPayment.put("currencyUomId", multiplePaymentHeader.getString("currencyUomId"));
			confirmedPayment.put("paymentRefNum", multiplePaymentHeader.getString("paymentRefNum"));

			try {
				confirmedPayment.create();
			} catch (Exception e) {

			}
		}

		return paymentId;
	}

	public static Map<String, Object> pullBankTrans(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");

		Timestamp generationDate = null;
		Long count = 0L;
		EntityConditionList<EntityExpr> cond = null;

		// using the passed finAccountId get the bank account
		GenericValue bankAccount = null;
		try {
			bankAccount = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// using the ReconPullLog pick only that have never been picked
		GenericValue reconPullLog = null;
		try {
			reconPullLog = delegator.findOne("ReconPullLog", UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (reconPullLog == null) {
			// create a new record here and pick date
			generationDate = UtilDateTime.nowTimestamp();
			GenericValue newReconPullLog = delegator.makeValue("ReconPullLog");
			// reconPullLog = delegator.makeValue("ReconPullLog");
			newReconPullLog.put("finAccountId", finAccountId);
			newReconPullLog.put("totalPulled", count);
			newReconPullLog.put("generationDate", generationDate);
			newReconPullLog.put("pulledBy", userLogin.getString("userLoginId"));
			try {
				newReconPullLog.create();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, bankAccount.getString("postToGlAccountId"))
					));
		} else {
			// pick date from here
			generationDate = reconPullLog.getTimestamp("generationDate");
			cond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, bankAccount.getString("postToGlAccountId")),
					EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN, generationDate)
					));
		}

		// get all transactions affecting the said account
		// fetch all transactions from account trans entry with this account id
		List<GenericValue> acctgTransEntryItems = null;

		try {
			acctgTransEntryItems = delegator.findList("AcctgTransEntry", cond, null, null, null, false);
			System.out.println("#################################### RETRIEVED  acctgTransEntryItems Successfully: ");
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ModelService createFinAccountTrans = null;
		try {
			createFinAccountTrans = dctx.getModelService("createFinAccountTrans");
		} catch (GenericServiceException e1) {
			e1.printStackTrace();
		}

		Map<String, Object> inContext = createFinAccountTrans.makeValid(context, ModelService.IN_PARAM);
		inContext.put("finAccountId", finAccountId);
		Map<String, Object> createResult = null;

		StringBuffer sb = new StringBuffer();

		for (GenericValue acctgTransEntry : acctgTransEntryItems) {

			// Get Transactions from AccountTransaction, ensure use amounts in
			// conditions
			List<GenericValue> accountTransactions = null;
			List<GenericValue> mSaccoApplication = null;
			List<GenericValue> cardApplication = null;

			EntityConditionList<EntityExpr> accountTransactionsCond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("transactionAmount", EntityOperator.EQUALS, acctgTransEntry.getBigDecimal("amount")),
					EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransEntry.getString("acctgTransId"))
					), EntityOperator.AND);

			try {
				accountTransactions = delegator.findList("AccountTransaction", accountTransactionsCond, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			String referenceNumber = null;
			for (GenericValue accountTransaction : accountTransactions) {
				GenericValue member = null;

				EntityConditionList<EntityExpr> msaccoApplCond = EntityCondition.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, accountTransaction.getLong("partyId"))
						));

				try {
					member = delegator.findOne("Member", UtilMisc.toMap("partyId", accountTransaction.getLong("partyId")), false);
					mSaccoApplication = delegator.findList("MSaccoApplication", msaccoApplCond, null, null, null, false);
					cardApplication = delegator.findList("CardApplication", msaccoApplCond, null, null, null, false);

				} catch (GenericEntityException e2) {
					e2.printStackTrace();
				}
				String cardNo = null;
				for (GenericValue card : cardApplication) {
					cardNo = card.getString("cardNumber");
				}
				String phoneNo = null;
				for (GenericValue phone : mSaccoApplication) {
					phoneNo = phone.getString("mobilePhoneNumber");
				}

				if (cardNo != null) {
					referenceNumber = cardNo;
				} else if (phoneNo != null) {
					referenceNumber = phoneNo;
				}

				if (member != null) {

					sb.append(accountTransaction.getString("transactionType"));
					sb.append("-");
					sb.append(acctgTransEntry.getString("acctgTransId"));
					if (cardNo != null) {
						sb.append(" ");
						sb.append(cardNo);
					}
					if (phoneNo != null) {
						sb.append(" ");
						sb.append(phoneNo);
					}
					if (accountTransaction.getString("chequeNo") != null) {
						sb.append(" ");
						sb.append("ChequeNo:");
						sb.append(accountTransaction.getString("chequeNo"));
					}
					sb.append(" ");
					sb.append(member.getString("firstName"));
					sb.append(" ");
					sb.append(member.getString("middleName"));
					sb.append(" ");
					sb.append(member.getString("lastName"));

				}
			}

			if (acctgTransEntry.getString("acctgTransTypeId") != null) {
				if (!acctgTransEntry.getString("acctgTransTypeId").equals("INCOMING_PAYMENT") || acctgTransEntry.getString("acctgTransTypeId").equals("OUTGOING_PAYMENT")) {
					if (acctgTransEntry.getString("debitCreditFlag").equals("D")) {
						System.out.println("#################################### ABOUT TO DO A DEPOSIT: ");
						// then do a deposit here
						inContext.put("finAccountTransTypeId", "DEPOSIT");
						inContext.put("comments", sb.toString());
						inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
						inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
						inContext.put("userLogin", userLogin);

						inContext.put("referenceNumber", referenceNumber);

					} else {
						System.out.println("#################################### ABOUT TO DO A WITHDRAWAL: ");
						// do a withdrawal
						inContext.put("finAccountTransTypeId", "WITHDRAWAL");
						inContext.put("comments", sb.toString());
						inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
						inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
						inContext.put("userLogin", userLogin);
						inContext.put("referenceNumber", referenceNumber);
					}
				}
			} else {
				if (acctgTransEntry.getString("debitCreditFlag").equals("D")) {
					System.out.println("#################################### ABOUT TO DO A DEPOSIT: ");
					// then do a deposit here
					inContext.put("finAccountTransTypeId", "DEPOSIT");
					inContext.put("comments", sb.toString());
					inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
					inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
					inContext.put("userLogin", userLogin);
					inContext.put("referenceNumber", referenceNumber);

				} else {
					System.out.println("#################################### ABOUT TO DO A WITHDRAWAL: ");
					// do a withdrawal
					inContext.put("finAccountTransTypeId", "WITHDRAWAL");
					inContext.put("comments", sb.toString());
					inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
					inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
					inContext.put("userLogin", userLogin);
					inContext.put("referenceNumber", referenceNumber);
				}
			}
			sb.setLength(0);
			try {
				createResult = dispatcher.runSync("createFinAccountTrans", inContext);
				System.out.println("#################################### TRYING TO CREATE FinAccountTrans : ");
			} catch (GenericServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (ServiceUtil.isError(createResult)) {
				return createResult;
			}
			count++;
		}
		// update Log
		// using the ReconPullLog pick only that have never been picked
		GenericValue reconPullLogUpdate = null;
		try {
			reconPullLogUpdate = delegator.findOne("ReconPullLog", UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		reconPullLogUpdate.set("generationDate", UtilDateTime.nowTimestamp());
		reconPullLogUpdate.set("totalPulled", count);
		reconPullLogUpdate.set("pulledBy", "admin");

		try {
			reconPullLogUpdate.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("finAccountId", finAccountId);
		return result;

	}

	public static Map<String, Object> pullBankTransOld(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		Map<String, Object> result = ServiceUtil.returnSuccess();

		System.out.println("#################################### finAccountId Passed: " + finAccountId);

		// using the passed finAccountId get the bank account
		GenericValue bankAccount = null;
		try {
			bankAccount = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		System.out.println("#################################### Bank Account Retrieved: " + bankAccount);
		// get all transactions from AccountTransaction
		List<GenericValue> accountTransactions = null;

		EntityConditionList<EntityExpr> accountTransactionsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS, "COMMISSION ON BANKERS CHEQUE"),
				EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS, "LOANCHEQUEPAY"),
				EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS, "Cheque Deposit Charge"),
				EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS, "Cheque Deposit Excise")
				), EntityOperator.AND);

		try {
			accountTransactions = delegator.findList("AccountTransaction", accountTransactionsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue transaction : accountTransactions) {
			String acctgTransId = transaction.getString("acctgTransId");
			System.out.println("#################################### Working with : " + acctgTransId + " acctgTransId");
			ModelService createFinAccountTrans = null;
			try {
				createFinAccountTrans = dctx.getModelService("createFinAccountTrans");
			} catch (GenericServiceException e1) {
				e1.printStackTrace();
			}

			Map<String, Object> inContext = createFinAccountTrans.makeValid(context, ModelService.IN_PARAM);
			inContext.put("finAccountId", finAccountId);
			Map<String, Object> createResult = null;

			// fetch all transactions from account trans entry with this account
			// id
			List<GenericValue> acctgTransEntryItems = null;

			EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, acctgTransId)
					));

			try {
				acctgTransEntryItems = delegator.findList("AcctgTransEntry", cond, null, null, null, false);
				System.out.println("#################################### RETRIEVED  acctgTransEntryItems Successfully: ");
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// check if it posted to any of the bank accounts
			for (GenericValue acctgTransEntry : acctgTransEntryItems) {
				System.out.println("#################################### WORKING WITH: " + acctgTransEntry.getString("glAccountId") + " glAccountId");
				System.out.println("#################################### WORKING WITH: " + bankAccount.getString("postToGlAccountId") + " postToGlAccountId");
				System.out.println("#################### COMPARING glAccountId: " + acctgTransEntry.getString("glAccountId") + " WITH postToGlAccountId " + bankAccount.getString("postToGlAccountId"));
				if (acctgTransEntry.getString("glAccountId").equals(bankAccount.getString("postToGlAccountId"))) {
					System.out.println("#################################### WORKING WITH: " + bankAccount.getString("postToGlAccountId") + " postToGlAccountId");
					if (acctgTransEntry.getString("debitCreditFlag").equals("D")) {
						System.out.println("#################################### ABOUT TO DO A DEPOSIT: ");
						// then do a deposit here
						inContext.put("finAccountTransTypeId", "DEPOSIT");
						inContext.put("comments", acctgTransEntry.getString("glAccountTypeId"));
						inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
						inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
						inContext.put("userLogin", userLogin);

					} else {
						System.out.println("#################################### ABOUT TO DO A WITHDRAWAL: ");
						// do a withdrawal
						inContext.put("finAccountTransTypeId", "WITHDRAWAL");
						inContext.put("comments", acctgTransEntry.getString("glAccountTypeId"));
						inContext.put("transactionDate", acctgTransEntry.getTimestamp("createdTxStamp"));
						inContext.put("amount", acctgTransEntry.getBigDecimal("origAmount"));
						inContext.put("userLogin", userLogin);
					}

					try {
						createResult = dispatcher.runSync("createFinAccountTrans", inContext);
						System.out.println("#################################### TRYING TO CREATE FinAccountTrans : ");
					} catch (GenericServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (ServiceUtil.isError(createResult)) {
						return createResult;
					}
					result.put("finAccountId", createResult.get("finAccountId"));
				}
			}
		}
		return result;
	}

	public static Map<String, Object> createFinAccountForStore(
			DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String productStoreId = (String) context.get("productStoreId");
		String finAccountTypeId = (String) context.get("finAccountTypeId");
		Locale locale = (Locale) context.get("locale");
		GenericValue productStore = ProductStoreWorker.getProductStore(
				productStoreId, delegator);

		try {
			// get the product store id and use it to generate a unique fin
			// account code
			GenericValue productStoreFinAccountSetting = delegator.findOne(
					"ProductStoreFinActSetting", UtilMisc.toMap(
							"productStoreId", productStoreId,
							"finAccountTypeId", finAccountTypeId), true);
			if (productStoreFinAccountSetting == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						resourceError, "AccountingFinAccountSetting", UtilMisc
								.toMap("productStoreId", productStoreId,
										"finAccountTypeId", finAccountTypeId),
						locale));
			}

			Long accountCodeLength = productStoreFinAccountSetting
					.getLong("accountCodeLength");
			Long accountValidDays = productStoreFinAccountSetting
					.getLong("accountValidDays");
			Long pinCodeLength = productStoreFinAccountSetting
					.getLong("pinCodeLength");
			String requirePinCode = productStoreFinAccountSetting
					.getString("requirePinCode");

			// automatically set the parameters for the create fin account
			// service
			ModelService createService = dctx
					.getModelService("createFinAccount");
			Map<String, Object> inContext = createService.makeValid(context,
					ModelService.IN_PARAM);
			Timestamp now = UtilDateTime.nowTimestamp();

			// now use our values
			String finAccountCode = FinAccountHelper.getNewFinAccountCode(
					accountCodeLength.intValue(), delegator);
			inContext.put("finAccountCode", finAccountCode);

			// with pin codes, the account code becomes the ID and the pin
			// becomes the code
			if ("Y".equalsIgnoreCase(requirePinCode)) {
				String pinCode = FinAccountHelper.getNewFinAccountCode(
						pinCodeLength.intValue(), delegator);
				inContext.put("finAccountPin", pinCode);
			}

			// set the dates/userlogin
			if (UtilValidate.isNotEmpty(accountValidDays)) {
				inContext.put("thruDate",
						UtilDateTime.getDayEnd(now, accountValidDays));
			}
			inContext.put("fromDate", now);
			inContext.put("userLogin", userLogin);

			// product store payToPartyId
			String payToPartyId = ProductStoreWorker
					.getProductStorePayToPartyId(productStoreId, delegator);
			inContext.put("organizationPartyId", payToPartyId);
			inContext.put("currencyUomId",
					productStore.get("defaultCurrencyUomId"));

			Map<String, Object> createResult = dispatcher.runSync(
					"createFinAccount", inContext);

			if (ServiceUtil.isError(createResult)) {
				return createResult;
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("finAccountId", createResult.get("finAccountId"));
			result.put("finAccountCode", finAccountCode);
			return result;
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		} catch (GenericServiceException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
	}

	public static Map<String, Object> checkFinAccountBalance(
			DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String finAccountId = (String) context.get("finAccountId");
		String finAccountCode = (String) context.get("finAccountCode");
		Locale locale = (Locale) context.get("locale");

		GenericValue finAccount;
		if (finAccountId == null) {
			try {
				finAccount = FinAccountHelper.getFinAccountFromCode(
						finAccountCode, delegator);
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			}
		} else {
			try {
				finAccount = delegator.findOne("FinAccount",
						UtilMisc.toMap("finAccountId", finAccountId), false);
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			}
		}
		if (finAccount == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					resourceError, "AccountingFinAccountNotFound",
					UtilMisc.toMap("finAccountId", finAccountId), locale));
		}

		// get the balance
		BigDecimal availableBalance = finAccount
				.getBigDecimal("availableBalance");
		BigDecimal balance = finAccount.getBigDecimal("actualBalance");
		if (availableBalance == null) {
			availableBalance = FinAccountHelper.ZERO;
		}
		if (balance == null) {
			balance = FinAccountHelper.ZERO;
		}

		String statusId = finAccount.getString("statusId");
		Debug.logInfo("FinAccount Balance [" + balance + "] Available ["
				+ availableBalance + "] - Status: " + statusId, module);

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("availableBalance", availableBalance);
		result.put("balance", balance);
		result.put("statusId", statusId);
		return result;
	}

	public static Map<String, Object> checkFinAccountStatus(
			DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String finAccountId = (String) context.get("finAccountId");
		Locale locale = (Locale) context.get("locale");

		if (finAccountId == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					resourceError, "AccountingFinAccountNotFound",
					UtilMisc.toMap("finAccountId", ""), locale));
		}

		GenericValue finAccount;
		try {
			finAccount = delegator.findOne("FinAccount",
					UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}

		if (finAccount != null) {
			String statusId = finAccount.getString("statusId");
			if (statusId == null)
				statusId = "FNACT_ACTIVE";

			BigDecimal balance = finAccount.getBigDecimal("actualBalance");
			if (balance == null) {
				balance = FinAccountHelper.ZERO;
			}

			Debug.logInfo("Account #" + finAccountId + " Balance: " + balance
					+ " Status: " + statusId, module);

			if ("FNACT_ACTIVE".equals(statusId)
					&& balance.compareTo(FinAccountHelper.ZERO) < 1) {
				finAccount.set("statusId", "FNACT_MANFROZEN");
				Debug.logInfo("Financial account [" + finAccountId
						+ "] has passed its threshold [" + balance
						+ "] (Frozen)", module);
			} else if ("FNACT_MANFROZEN".equals(statusId)
					&& balance.compareTo(FinAccountHelper.ZERO) > 0) {
				finAccount.set("statusId", "FNACT_ACTIVE");
				Debug.logInfo("Financial account [" + finAccountId
						+ "] has been made current [" + balance
						+ "] (Un-Frozen)", module);
			}
			try {
				finAccount.store();
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(e.getMessage());
			}
		}

		return ServiceUtil.returnSuccess();
	}

	public static Map<String, Object> refundFinAccount(DispatchContext dctx,
			Map<String, Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String finAccountId = (String) context.get("finAccountId");
		Map<String, Object> result = null;

		GenericValue finAccount;
		try {
			finAccount = delegator.findOne("FinAccount",
					UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}

		if (finAccount != null) {
			// check to make sure the account is refundable
			if (!"Y".equals(finAccount.getString("isRefundable"))) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						resourceError, "AccountingFinAccountIsNotRefundable",
						locale));
			}

			// get the actual and available balance
			BigDecimal availableBalance = finAccount
					.getBigDecimal("availableBalance");
			BigDecimal actualBalance = finAccount
					.getBigDecimal("actualBalance");

			// if they do not match, then there are outstanding authorizations
			// which need to be settled first
			if (actualBalance.compareTo(availableBalance) != 0) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						resourceError, "AccountingFinAccountCannotBeRefunded",
						locale));
			}

			// now we make sure there is something to refund
			if (actualBalance.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal remainingBalance = new BigDecimal(
						actualBalance.toString());
				BigDecimal refundAmount = BigDecimal.ZERO;

				List<EntityExpr> exprs = UtilMisc.toList(EntityCondition
						.makeCondition("finAccountTransTypeId",
								EntityOperator.EQUALS, "DEPOSIT"),
						EntityCondition.makeCondition("finAccountId",
								EntityOperator.EQUALS, finAccountId));
				EntityCondition condition = EntityCondition.makeCondition(
						exprs, EntityOperator.AND);

				EntityListIterator eli = null;
				try {
					eli = delegator.find("FinAccountTrans", condition, null,
							null, UtilMisc.toList("-transactionDate"), null);

					GenericValue trans;
					while (remainingBalance.compareTo(FinAccountHelper.ZERO) < 0
							&& (trans = eli.next()) != null) {
						String orderId = trans.getString("orderId");
						String orderItemSeqId = trans
								.getString("orderItemSeqId");

						// make sure there is an order available to refund
						if (orderId != null && orderItemSeqId != null) {
							GenericValue orderHeader = delegator.findOne(
									"OrderHeader",
									UtilMisc.toMap("orderId", orderId), false);
							GenericValue productStore = orderHeader
									.getRelatedOne("ProductStore", false);
							GenericValue orderItem = delegator.findOne(
									"OrderItem", UtilMisc.toMap("orderId",
											orderId, "orderItemSeqId",
											orderItemSeqId), false);
							if (!"ITEM_CANCELLED".equals(orderItem
									.getString("statusId"))) {

								// make sure the item hasn't already been
								// returned
								List<GenericValue> returnItems = orderItem
										.getRelated("ReturnItem", null, null,
												false);
								if (UtilValidate.isEmpty(returnItems)) {
									BigDecimal txAmt = trans
											.getBigDecimal("amount");
									BigDecimal refAmt = txAmt;
									if (remainingBalance.compareTo(txAmt) == -1) {
										refAmt = remainingBalance;
									}
									remainingBalance = remainingBalance
											.subtract(refAmt);
									refundAmount = refundAmount.add(refAmt);

									// create the return header
									Map<String, Object> rhCtx = UtilMisc.toMap(
											"returnHeaderTypeId",
											"CUSTOMER_RETURN", "fromPartyId",
											finAccount
													.getString("ownerPartyId"),
											"toPartyId", productStore
													.getString("payToPartyId"),
											"userLogin", userLogin);
									Map<String, Object> rhResp = dispatcher
											.runSync("createReturnHeader",
													rhCtx);
									if (ServiceUtil.isError(rhResp)) {
										throw new GeneralException(
												ServiceUtil
														.getErrorMessage(rhResp));
									}
									String returnId = (String) rhResp
											.get("returnId");

									// create the return item
									Map<String, Object> returnItemCtx = FastMap
											.newInstance();
									returnItemCtx.put("returnId", returnId);
									returnItemCtx.put("orderId", orderId);
									returnItemCtx.put("description", orderItem
											.getString("itemDescription"));
									returnItemCtx.put("orderItemSeqId",
											orderItemSeqId);
									returnItemCtx.put("returnQuantity",
											BigDecimal.ONE);
									returnItemCtx.put("receivedQuantity",
											BigDecimal.ONE);
									returnItemCtx.put("returnPrice", refAmt);
									returnItemCtx.put("returnReasonId",
											"RTN_NOT_WANT");
									returnItemCtx.put("returnTypeId",
											"RTN_REFUND"); // refund return
									returnItemCtx.put("returnItemTypeId",
											"RET_NPROD_ITEM");
									returnItemCtx.put("userLogin", userLogin);

									Map<String, Object> retItResp = dispatcher
											.runSync("createReturnItem",
													returnItemCtx);
									if (ServiceUtil.isError(retItResp)) {
										throw new GeneralException(
												ServiceUtil
														.getErrorMessage(retItResp));
									}
									String returnItemSeqId = (String) retItResp
											.get("returnItemSeqId");

									// approve the return
									Map<String, Object> appRet = UtilMisc
											.toMap("statusId",
													"RETURN_ACCEPTED",
													"returnId", returnId,
													"userLogin", userLogin);
									Map<String, Object> appResp = dispatcher
											.runSync("updateReturnHeader",
													appRet);
									if (ServiceUtil.isError(appResp)) {
										throw new GeneralException(
												ServiceUtil
														.getErrorMessage(appResp));
									}

									// "receive" the return - should trigger the
									// refund
									Map<String, Object> recRet = UtilMisc
											.toMap("statusId",
													"RETURN_RECEIVED",
													"returnId", returnId,
													"userLogin", userLogin);
									Map<String, Object> recResp = dispatcher
											.runSync("updateReturnHeader",
													recRet);
									if (ServiceUtil.isError(recResp)) {
										throw new GeneralException(
												ServiceUtil
														.getErrorMessage(recResp));
									}

									// get the return item
									GenericValue returnItem = delegator
											.findOne("ReturnItem", UtilMisc
													.toMap("returnId",
															returnId,
															"returnItemSeqId",
															returnItemSeqId),
													false);
									GenericValue response = returnItem
											.getRelatedOne(
													"ReturnItemResponse", false);
									if (response == null) {
										throw new GeneralException(
												"No return response found for: "
														+ returnItem
																.getPrimaryKey());
									}
									String paymentId = response
											.getString("paymentId");

									// create the adjustment transaction
									Map<String, Object> txCtx = FastMap
											.newInstance();
									txCtx.put("finAccountTransTypeId",
											"ADJUSTMENT");
									txCtx.put("finAccountId", finAccountId);
									txCtx.put("orderId", orderId);
									txCtx.put("orderItemSeqId", orderItemSeqId);
									txCtx.put("paymentId", paymentId);
									txCtx.put("amount", refAmt.negate());
									txCtx.put("partyId", finAccount
											.getString("ownerPartyId"));
									txCtx.put("userLogin", userLogin);

									Map<String, Object> txResp = dispatcher
											.runSync("createFinAccountTrans",
													txCtx);
									if (ServiceUtil.isError(txResp)) {
										throw new GeneralException(
												ServiceUtil
														.getErrorMessage(txResp));
									}
								}
							}
						}
					}
				} catch (GeneralException e) {
					Debug.logError(e, module);
					return ServiceUtil.returnError(e.getMessage());
				} finally {
					if (eli != null) {
						try {
							eli.close();
						} catch (GenericEntityException e) {
							Debug.logWarning(e, module);
						}
					}
				}

				// check to make sure we balanced out
				if (remainingBalance.compareTo(FinAccountHelper.ZERO) == 1) {
					result = ServiceUtil.returnSuccess(UtilProperties
							.getMessage(resourceError,
									"AccountingFinAccountPartiallyRefunded",
									locale));
				}
			}
		}

		if (result == null) {
			result = ServiceUtil.returnSuccess();
		}

		return result;
	}
}
