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
package org.ofbiz.accounting.payment;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.webapp.event.EventHandlerException;


/**
 * Worker methods for Payments
 */
public class PaymentWorker {

	public static final String module = PaymentWorker.class.getName();
	public static Logger log = Logger.getLogger(PaymentWorker.class);
	private static int decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
	private static int rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");

	// to be able to use in minilanguage where Boolean cannot be used
	public static List<Map<String, GenericValue>> getPartyPaymentMethodValueMaps(Delegator delegator, String partyId) {
		return (getPartyPaymentMethodValueMaps(delegator, partyId, false));
	}

	public static List<Map<String, GenericValue>> getPartyPaymentMethodValueMaps(Delegator delegator, String partyId,
			Boolean showOld) {
		List<Map<String, GenericValue>> paymentMethodValueMaps = FastList.newInstance();
		try {
			List<GenericValue> paymentMethods = delegator.findByAnd("PaymentMethod",
					UtilMisc.toMap("partyId", partyId), null, false);

			if (!showOld)
				paymentMethods = EntityUtil.filterByDate(paymentMethods, true);

			for (GenericValue paymentMethod : paymentMethods) {
				Map<String, GenericValue> valueMap = FastMap.newInstance();

				paymentMethodValueMaps.add(valueMap);
				valueMap.put("paymentMethod", paymentMethod);
				if ("CREDIT_CARD".equals(paymentMethod.getString("paymentMethodTypeId"))) {
					GenericValue creditCard = paymentMethod.getRelatedOne("CreditCard", false);
					if (creditCard != null)
						valueMap.put("creditCard", creditCard);
				} else if ("GIFT_CARD".equals(paymentMethod.getString("paymentMethodTypeId"))) {
					GenericValue giftCard = paymentMethod.getRelatedOne("GiftCard", false);
					if (giftCard != null)
						valueMap.put("giftCard", giftCard);
				} else if ("EFT_ACCOUNT".equals(paymentMethod.getString("paymentMethodTypeId"))) {
					GenericValue eftAccount = paymentMethod.getRelatedOne("EftAccount", false);
					if (eftAccount != null)
						valueMap.put("eftAccount", eftAccount);
				}
			}
		} catch (GenericEntityException e) {
			Debug.logWarning(e, module);
		}
		return paymentMethodValueMaps;
	}

	public static Map<String, Object> getPaymentMethodAndRelated(ServletRequest request, String partyId) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Map<String, Object> results = FastMap.newInstance();

		Boolean tryEntity = true;
		if (request.getAttribute("_ERROR_MESSAGE_") != null)
			tryEntity = false;

		String donePage = request.getParameter("DONE_PAGE");
		if (donePage == null || donePage.length() <= 0)
			donePage = "viewprofile";
		results.put("donePage", donePage);

		String paymentMethodId = request.getParameter("paymentMethodId");

		// check for a create
		if (request.getAttribute("paymentMethodId") != null) {
			paymentMethodId = (String) request.getAttribute("paymentMethodId");
		}

		results.put("paymentMethodId", paymentMethodId);

		GenericValue paymentMethod = null;
		GenericValue creditCard = null;
		GenericValue giftCard = null;
		GenericValue eftAccount = null;

		if (UtilValidate.isNotEmpty(paymentMethodId)) {
			try {
				paymentMethod = delegator.findOne("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId),
						false);
				creditCard = delegator.findOne("CreditCard", UtilMisc.toMap("paymentMethodId", paymentMethodId), false);
				giftCard = delegator.findOne("GiftCard", UtilMisc.toMap("paymentMethodId", paymentMethodId), false);
				eftAccount = delegator.findOne("EftAccount", UtilMisc.toMap("paymentMethodId", paymentMethodId), false);
			} catch (GenericEntityException e) {
				Debug.logWarning(e, module);
			}
		}
		if (paymentMethod != null) {
			results.put("paymentMethod", paymentMethod);
		} else {
			tryEntity = false;
		}

		if (creditCard != null) {
			results.put("creditCard", creditCard);
		}
		if (giftCard != null) {
			results.put("giftCard", giftCard);
		}
		if (eftAccount != null) {
			results.put("eftAccount", eftAccount);
		}

		String curContactMechId = null;

		if (creditCard != null) {
			curContactMechId = UtilFormatOut.checkNull(tryEntity ? creditCard.getString("contactMechId") : request
					.getParameter("contactMechId"));
		} else if (giftCard != null) {
			curContactMechId = UtilFormatOut.checkNull(tryEntity ? giftCard.getString("contactMechId") : request
					.getParameter("contactMechId"));
		} else if (eftAccount != null) {
			curContactMechId = UtilFormatOut.checkNull(tryEntity ? eftAccount.getString("contactMechId") : request
					.getParameter("contactMechId"));
		}
		if (curContactMechId != null) {
			results.put("curContactMechId", curContactMechId);
		}

		results.put("tryEntity", tryEntity);

		return results;
	}

	public static GenericValue getPaymentAddress(Delegator delegator, String partyId) {
		List<GenericValue> paymentAddresses = null;
		try {
			paymentAddresses = delegator.findByAnd("PartyContactMechPurpose",
					UtilMisc.toMap("partyId", partyId, "contactMechPurposeTypeId", "PAYMENT_LOCATION"),
					UtilMisc.toList("-fromDate"), false);
			paymentAddresses = EntityUtil.filterByDate(paymentAddresses);
		} catch (GenericEntityException e) {
			Debug.logError(e, "Trouble getting PartyContactMechPurpose entity list", module);
		}

		// get the address for the primary contact mech
		GenericValue purpose = EntityUtil.getFirst(paymentAddresses);
		GenericValue postalAddress = null;
		if (purpose != null) {
			try {
				postalAddress = delegator.findOne("PostalAddress",
						UtilMisc.toMap("contactMechId", purpose.getString("contactMechId")), false);
			} catch (GenericEntityException e) {
				Debug.logError(
						e,
						"Trouble getting PostalAddress record for contactMechId: " + purpose.getString("contactMechId"),
						module);
			}
		}

		return postalAddress;
	}

	/**
	 * Returns the total from a list of Payment entities
	 *
	 * @param payments
	 *            List of Payment GenericValue items
	 * @return total payments as BigDecimal
	 */

	public static BigDecimal getPaymentsTotal(List<GenericValue> payments) {
		if (payments == null) {
			throw new IllegalArgumentException("Payment list cannot be null");
		}

		BigDecimal paymentsTotal = BigDecimal.ZERO;
		for (GenericValue payment : payments) {
			paymentsTotal = paymentsTotal.add(payment.getBigDecimal("amount")).setScale(decimals, rounding);
		}
		return paymentsTotal;
	}

	/**
	 * Method to return the total amount of an payment which is applied to a
	 * payment
	 *
	 * @param delegator
	 *            the delegator
	 * @param paymentId
	 *            paymentId of the Payment
	 * @return the applied total as BigDecimal
	 */
	public static BigDecimal getPaymentApplied(Delegator delegator, String paymentId) {
		return getPaymentApplied(delegator, paymentId, false);
	}

	public static BigDecimal getPaymentApplied(Delegator delegator, String paymentId, Boolean actual) {
		if (delegator == null) {
			throw new IllegalArgumentException("Null delegator is not allowed in this method");
		}

		GenericValue payment = null;
		try {
			payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", paymentId), false);
		} catch (GenericEntityException e) {
			Debug.logError(e, "Problem getting Payment", module);
		}

		if (payment == null) {
			throw new IllegalArgumentException("The paymentId passed does not match an existing payment");
		}

		return getPaymentApplied(payment, actual);
	}

	/**
	 * Method to return the amount applied converted to the currency of payment
	 *
	 * @param paymentApplicationId
	 *            the payment application id
	 * @return appliedAmount the applied amount as BigDecimal
	 */
	public static BigDecimal getPaymentAppliedAmount(Delegator delegator, String paymentApplicationId) {
		GenericValue paymentApplication = null;
		BigDecimal appliedAmount = BigDecimal.ZERO;
		try {
			paymentApplication = delegator.findOne("PaymentApplication",
					UtilMisc.toMap("paymentApplicationId", paymentApplicationId), false);
			appliedAmount = paymentApplication.getBigDecimal("amountApplied");
			if (paymentApplication.get("paymentId") != null) {
				GenericValue payment = paymentApplication.getRelatedOne("Payment", false);
				if (paymentApplication.get("invoiceId") != null && payment.get("actualCurrencyAmount") != null
						&& payment.get("actualCurrencyUomId") != null) {
					GenericValue invoice = paymentApplication.getRelatedOne("Invoice", false);
					if (payment.getString("actualCurrencyUomId").equals(invoice.getString("currencyUomId"))) {
						appliedAmount = appliedAmount.multiply(payment.getBigDecimal("amount")).divide(
								payment.getBigDecimal("actualCurrencyAmount"), new MathContext(100));
					}
				}
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Problem getting Payment", module);
		}
		return appliedAmount;
	}

	/**
	 * Method to return the total amount of an payment which is applied to a
	 * payment
	 *
	 * @param payment
	 *            GenericValue object of the Payment
	 * @return the applied total as BigDecimal in the currency of the payment
	 */
	public static BigDecimal getPaymentApplied(GenericValue payment) {
		return getPaymentApplied(payment, false);
	}

	/**
	 * Method to return the total amount of a payment which is applied to a
	 * payment
	 *
	 * @param payment
	 *            GenericValue object of the Payment
	 * @param actual
	 *            false for currency of the payment, true for the actual
	 *            currency
	 * @return the applied total as BigDecimal in the currency of the payment
	 */
	public static BigDecimal getPaymentApplied(GenericValue payment, Boolean actual) {
		BigDecimal paymentApplied = BigDecimal.ZERO;
		List<GenericValue> paymentApplications = null;
		try {
			List<EntityExpr> cond = UtilMisc.toList(EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, payment.getString("paymentId")),
																							EntityCondition.makeCondition("toPaymentId", EntityOperator.EQUALS, payment.getString("paymentId"))
																							);
																							EntityCondition partyCond = EntityCondition.makeCondition(cond, EntityOperator.OR);
			paymentApplications = payment.getDelegator().findList("PaymentApplication", partyCond, null,
					UtilMisc.toList("invoiceId", "billingAccountId"), null, false);
			if (UtilValidate.isNotEmpty(paymentApplications)) {
				for (GenericValue paymentApplication : paymentApplications) {
					BigDecimal amountApplied = paymentApplication.getBigDecimal("amountApplied");
					// check currency invoice and if different convert amount
					// applied for display
					if (actual.equals(Boolean.FALSE) && paymentApplication.get("invoiceId") != null
							&& payment.get("actualCurrencyAmount") != null
							&& payment.get("actualCurrencyUomId") != null) {
						GenericValue invoice = paymentApplication.getRelatedOne("Invoice", false);
						if (payment.getString("actualCurrencyUomId").equals(invoice.getString("currencyUomId"))) {
							amountApplied = amountApplied.multiply(payment.getBigDecimal("amount")).divide(
									payment.getBigDecimal("actualCurrencyAmount"), new MathContext(100));
						}
					}
					paymentApplied = paymentApplied.add(amountApplied).setScale(decimals, rounding);
				}
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Trouble getting entities", module);
		}
		return paymentApplied;
	}

	public static BigDecimal getPaymentNotApplied(GenericValue payment) {
		if (payment != null) {
			return payment.getBigDecimal("amount").subtract(getPaymentApplied(payment)).setScale(decimals, rounding);
		}
		return BigDecimal.ZERO;
	}

	public static BigDecimal getPaymentNotApplied(GenericValue payment, Boolean actual) {
		if (actual.equals(Boolean.TRUE) && UtilValidate.isNotEmpty(payment.getBigDecimal("actualCurrencyAmount"))) {
			return payment.getBigDecimal("actualCurrencyAmount").subtract(getPaymentApplied(payment, actual))
					.setScale(decimals, rounding);
		}
		return payment.getBigDecimal("amount").subtract(getPaymentApplied(payment)).setScale(decimals, rounding);
	}

	public static BigDecimal getPaymentNotApplied(Delegator delegator, String paymentId) {
		return getPaymentNotApplied(delegator, paymentId, false);
	}

	public static BigDecimal getPaymentNotApplied(Delegator delegator, String paymentId, Boolean actual) {
		if (delegator == null) {
			throw new IllegalArgumentException("Null delegator is not allowed in this method");
		}

		GenericValue payment = null;
		try {
			payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", paymentId), false);
		} catch (GenericEntityException e) {
			Debug.logError(e, "Problem getting Payment", module);
		}

		if (payment == null) {
			throw new IllegalArgumentException("The paymentId passed does not match an existing payment");
		}
		return payment.getBigDecimal("amount").subtract(getPaymentApplied(delegator, paymentId, actual))
				.setScale(decimals, rounding);
	}



	
	public static String createPayment(Delegator delegator, String branchId, String fromPartyId, String toPartyId, BigDecimal amount, String desc){
		String paymentId = null;
		String transactionType = "WITHDRAWAL";
		String statusId = "FINACT_TRNS_CREATED";
		Timestamp transactionDate = UtilDateTime.nowTimestamp();
		String finAccountId=null;
		String creditGlAccount = "";
		
		finAccountId = getFinAccountIdGivenBranchId(delegator, fromPartyId);
		creditGlAccount = getPostingGlAccount(delegator, finAccountId);
		
		//create payment
		paymentId = createConfirmedPayment(delegator,fromPartyId,toPartyId,amount,desc,transactionDate);
		
		//create fin_account
		String finAccountTransId = createFinAccountTrans(delegator, amount, desc, transactionType,statusId,transactionDate,finAccountId);
				

		
		
		//Create accounting transactions
		String acctgTransId = createAccountTransHeader(delegator,transactionDate, paymentId, fromPartyId);
		
		createDebitEntry(delegator, transactionDate, fromPartyId,amount,acctgTransId);
		createCreditEntry(delegator, transactionDate, fromPartyId,amount,acctgTransId, creditGlAccount);
		
		
		return paymentId;
	}

	private static String getPostingGlAccount(Delegator delegator, String finAccountId) {
		String creditGlAccount = "";
		GenericValue finAccount = null;
		try {
			finAccount = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId", finAccountId), false);
			creditGlAccount = finAccount.getString("postToGlAccountId");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return creditGlAccount;
	}

	private static String getFinAccountIdGivenBranchId(Delegator delegator, String fromPartyId) {
		String finAccountId=null;
		List<GenericValue>finAccounts = null;
		
		EntityConditionList<EntityExpr> finAccountCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, fromPartyId)
				), EntityOperator.AND);

		try {
			finAccounts = delegator.findList("FinAccount", finAccountCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue account : finAccounts) {
			if (account.getString("finAccountId").equals("20")) {
				//HQ
				finAccountId = account.getString("finAccountId");
			}else if (account.getString("finAccountId").equals("21")) {
				//LITEIN
				finAccountId = account.getString("finAccountId");
			}else if (account.getString("finAccountId").equals("22")) {
				//NAIROBI
				finAccountId = account.getString("finAccountId");
			}
		}
		
		return finAccountId;
	}

	private static void createDebitEntry(Delegator delegator, Timestamp transactionDate, String fromPartyId, BigDecimal amount, String acctgTransId) {
		String debitCreditFlag = "D";
		String glAccountId = "33000001";
		
		GenericValue acctgTransEntry = null;
		

		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		String acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("organizationPartyId", fromPartyId);
		acctgTransEntry.put("amount", amount);
		acctgTransEntry.put("origAmount", amount);
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", debitCreditFlag);
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("glAccountId", glAccountId);
		acctgTransEntry.put("createdStamp", transactionDate);

		try {
			acctgTransEntry.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	private static void createCreditEntry(Delegator delegator, Timestamp transactionDate, String fromPartyId, BigDecimal amount, String acctgTransId, String creditGlAccount) {
		String debitCreditFlag = "C";
		
		GenericValue acctgTransEntry = null;

		acctgTransEntry = delegator.makeValue("AcctgTransEntry");
		String acctgTransEntrySeqId = delegator.getNextSeqId("AcctgTransEntry");
		acctgTransEntry.put("acctgTransId", acctgTransId);
		acctgTransEntry.put("acctgTransEntrySeqId", acctgTransEntrySeqId);
		acctgTransEntry.put("organizationPartyId", fromPartyId);
		acctgTransEntry.put("amount", amount);
		acctgTransEntry.put("origAmount", amount);
		acctgTransEntry.put("currencyUomId", "KES");
		acctgTransEntry.put("origCurrencyUomId", "KES");
		acctgTransEntry.put("debitCreditFlag", debitCreditFlag);
		acctgTransEntry.put("reconcileStatusId", "AES_NOT_RECONCILED");
		acctgTransEntry.put("glAccountId", creditGlAccount);
		acctgTransEntry.put("createdStamp", transactionDate);

		try {
			acctgTransEntry.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	
	private static String createAccountTransHeader(Delegator delegator, Timestamp transactionDate, String paymentId, String fromPartyId) {
		String acctgTransTypeId = "OUTGOING_PAYMENT";
		String newAcctgTransId = delegator.getNextSeqId("AcctgTrans");
		GenericValue acctgTrans = null;
		acctgTrans = delegator.makeValue("AcctgTrans");
		acctgTrans.put("acctgTransId", newAcctgTransId);
		acctgTrans.put("acctgTransTypeId", acctgTransTypeId);
		acctgTrans.put("transactionDate", transactionDate);
		acctgTrans.put("isPosted", "Y");
		acctgTrans.put("isApproved", "Y");
		acctgTrans.put("postedDate", UtilDateTime.nowTimestamp());
		acctgTrans.put("glFiscalTypeId", "ACTUAL");
		acctgTrans.put("partyId", fromPartyId);
		acctgTrans.put("paymentId", paymentId);
		
		
		try {
			acctgTrans.create();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return newAcctgTransId;
	}

	private static String createFinAccountTrans(Delegator delegator, BigDecimal amount, String desc, String transactionType, String statusId, Timestamp transactionDate, String finAccountId) {
		String finAccountTransId = null;
		GenericValue finAccountTrans = null;
		finAccountTrans = delegator.makeValue("FinAccountTrans");
		finAccountTransId = delegator.getNextSeqId("FinAccountTrans");
		finAccountTrans.put("finAccountTransId", finAccountTransId);
		finAccountTrans.put("amount", amount);
		finAccountTrans.put("comments", desc);
		finAccountTrans.put("finAccountId", finAccountId);
		finAccountTrans.put("finAccountTransTypeId", transactionType);
		finAccountTrans.put("statusId", statusId);
		finAccountTrans.put("transactionDate", transactionDate);
		
		try {
			finAccountTrans.create();
		} catch (Exception e) {

		}
		
		return finAccountTransId;
	}

	private static String createConfirmedPayment(Delegator delegator, String fromPartyId, String toPartyId, BigDecimal amount, String desc, Timestamp date) {
		String paymentId = null;
		GenericValue confirmedPayment = null;
		
		confirmedPayment = delegator.makeValue("Payment");
		paymentId = delegator.getNextSeqId("Payment");
		confirmedPayment.put("paymentId", paymentId);
		confirmedPayment.put("paymentTypeId", "FUM");
		confirmedPayment.put("paymentMethodTypeId", "COMPANY_CHECK");
		confirmedPayment.put("paymentMethodId", "CHEQUE_FOSA_ACCOUNT");
		confirmedPayment.put("partyIdFrom", fromPartyId);
		confirmedPayment.put("partyIdTo", toPartyId);
		confirmedPayment.put("statusId", "PMNT_CONFIRMED");
		confirmedPayment.put("effectiveDate", UtilDateTime.nowTimestamp());
		confirmedPayment.put("amount", amount);
		confirmedPayment.put("comments",desc);
		confirmedPayment.put("currencyUomId", "KES");
		confirmedPayment.put("paymentRefNum", desc);

		try {
			confirmedPayment.create();
		} catch (Exception e) {

		}
		
		
		return paymentId;
	}
	

	
}
