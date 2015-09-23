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

public class GeneralLedgerServices {

	public static final String module = GeneralLedgerServices.class.getName();

	private static BigDecimal ZERO = BigDecimal.ZERO;

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
		List<GenericValue>stationAccountTransactions = null;
		GenericValue member = null;
		GenericValue station = null;
		GenericValue acctgTrans = null;
		GenericValue payment = null;
		List<GenericValue> mSaccoApplication = null;
		List<GenericValue> cardApplication = null;

		//To Handle Non Member Payments
		try {
			acctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId", acctgTransEntry.getString("acctgTransId")), false);
			if (acctgTrans.getString("paymentId")!= null) {
				payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId", acctgTrans.getString("paymentId")), false);
			}
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//Station Account Transaction
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
			if(accountTransaction.getString("chequeNo")!= null){
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
			if(accountTransaction.getString("transactionType").equals("MSACCOWITHDRAWAL") || accountTransaction.getString("transactionType").equals("MSACCOENQUIRY")){
				
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
			if(accountTransaction.getString("transactionType").equals("ATMWITHDRAWAL") || accountTransaction.getString("transactionType").equals("POSWITHDRAWAL") || accountTransaction.getString("transactionType").equals("VISAWITHDRAWAL")){
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

		}else if (payment!=null) {
			sb.append(payment.getString("comments"));
			sb.append(" ");
			sb.append("ChequeNo: ");
			sb.append(payment.getString("paymentRefNum"));
		}else if(stationAccountTransactions.size()>0){
			GenericValue stationAccountTransaction = stationAccountTransactions.get(0);
			if(stationAccountTransaction.getLong("stationId")!=null){
				try {
					station = delegator.findOne("Station", UtilMisc.toMap("stationId", stationAccountTransaction.getString("stationId")), false);
				} catch (GenericEntityException e) {
					
					e.printStackTrace();
				}
				
				sb.append("Station Remitance");
				sb.append(" ");
				sb.append(station.getString("name"));
				if(stationAccountTransaction.getString("chequeNumber")!= null){
					sb.append(" ");
					sb.append("ChequeNo: ");
					sb.append(stationAccountTransaction.getString("chequeNumber"));
				}
				sb.append(" ");
				sb.append("Month Year");
				sb.append(" ");
				sb.append(stationAccountTransaction.getString("monthyear"));
			}
		}else if(acctgTransEntry.getString("glAccountTypeId")!=null && acctgTransEntry.getString("glAccountTypeId").equals("TREASURY_TRANSFER")){
			sb.append("TREASURY TRANSFER");
			GenericValue partyNameView = null;
			try {
				partyNameView = delegator.findOne("PartyNameView", UtilMisc.toMap("partyId", acctgTransEntry.getString("partyId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (partyNameView!=null) {
				sb.append(" ");
				sb.append("by");
				sb.append(" ");
				if(partyNameView.getString("firstName")!=null){
					sb.append(partyNameView.getString("firstName"));
				}
				sb.append(" ");
				if(partyNameView.getString("middleName")!=null){
					sb.append(partyNameView.getString("middleName"));
				}
				sb.append(" ");
				if(partyNameView.getString("lastName")!=null){
					sb.append(partyNameView.getString("lastName"));
				}
			}
		}else if(acctgTransEntry.getString("glAccountTypeId")!=null && acctgTransEntry.getString("glAccountTypeId").equals("TREASURY_TRANSFER")){
			sb.append("TREASURY TRANSFER");
			GenericValue partyNameView = null;
			try {
				partyNameView = delegator.findOne("PartyNameView", UtilMisc.toMap("partyId", acctgTransEntry.getString("partyId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (partyNameView!=null) {
				sb.append(" ");
				sb.append("by");
				sb.append(" ");
				if(partyNameView.getString("firstName")!=null){
					sb.append(partyNameView.getString("firstName"));
				}
				sb.append(" ");
				if(partyNameView.getString("middleName")!=null){
					sb.append(partyNameView.getString("middleName"));
				}
				sb.append(" ");
				if(partyNameView.getString("lastName")!=null){
					sb.append(partyNameView.getString("lastName"));
				}
			}

		}else if(acctgTransEntry.getString("glAccountTypeId")==null && acctgTrans.getString("description")!=null){
			sb.append(acctgTrans.getString("description"));
		}
		
		if(sb.length()<1){
			if (acctgTransEntry.getString("glAccountTypeId")!=null) {
				sb.append(acctgTransEntry.getString("glAccountTypeId"));
			}
			
		}
		
		return sb.toString();
	}
}
