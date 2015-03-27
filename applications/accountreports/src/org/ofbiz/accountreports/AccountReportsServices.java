package org.ofbiz.accountreports;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.RemittanceServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.webapp.event.EventHandlerException;

public class AccountReportsServices {

	public static Logger log = Logger.getLogger(AccountReportsServices.class);
	public static String generateTransactionAudit(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");

		// Get Account Trans IDs from the AcctTrans

		// AcctgTrans

		// Long memberStatusId = getMemberStatusId("ACTIVE");
		// EntityConditionList<EntityExpr> memberConditions = EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition
		// .makeCondition("memberStatusId", EntityOperator.EQUALS,
		// memberStatusId)
		//
		// ), EntityOperator.AND);
		//
		// try {
		// memberELI = delegator.findList("Member", memberConditions, null,
		// null, null, false);
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		// //MemberStationList
		// try {
		// memberStationELI = delegator.findList("MemberStationList", null,
		// null,
		// null, null, false);
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		List<GenericValue> listAcctgTrans = null;
		// MemberStationList
		try {
			listAcctgTrans = delegator.findList("AcctgTrans", null, null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		log.info(" TTTTTTTTTTTTTTT Working on  "+listAcctgTrans.size()+" Transactions");
		int count = 0;
		for (GenericValue genericValue : listAcctgTrans) {
			// if the acctgTransId has correct double entries (Cs total equals
			// to Ds totals) then we are good
			// otherwise add the account trans to the audit list
			count++;
			if (!isCorrectlyEntered(genericValue.getString("acctgTransId"))) {
				// Add the AcctgTrans to the transactions audit
				log.info(" $$$$$$$$$$$$ Transaction ID "+genericValue.getString("acctgTransId")+" IS NOT Balanced");
				addToAudit(genericValue, delegator);
			} 
			
			if ((count % 100) == 0){
				log.info(" ############# DONE  "+count+" Records  now on "+genericValue.getString("acctgTransId"));
			}
		}

		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}

	private static void addToAudit(GenericValue acctgTrans, Delegator delegator) {
		// TODO Add Transaction ID to Audit
		GenericValue acctTransAudit = null;
		
		Long acctTransAuditId = delegator.getNextSeqIdLong("AcctTransAudit", 1);
		
		acctTransAudit = delegator.makeValue("AcctTransAudit", UtilMisc.toMap(
				"acctTransAuditId", acctTransAuditId, "isActive", "Y",
				"createdBy", "admin", "acctgTransId", acctgTrans.getString("acctgTransId"), "glAccountId",
				acctgTrans.getString("glAccountId")));
		try {
			delegator.createOrStore(acctTransAudit);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}

	private static boolean isCorrectlyEntered(String acctgTransId) {
		// TODO Check if acctgTransId has correct entries (Debits and Credits
		// are equal)
		BigDecimal bdTotalDebits = getTotalEntries(acctgTransId, "D");
		BigDecimal bdTotalCredits = getTotalEntries(acctgTransId, "C");
		
		if (bdTotalDebits.compareTo(bdTotalCredits) == 0)
		{
			return true;
		}
		
		return false;
	}

	private static BigDecimal getTotalEntries(String acctgTransId, String debitCreditFlag) {
		BigDecimal bdTotalEntryAmt = BigDecimal.ZERO;
		List<GenericValue> listEntries = null;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> entriesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("acctgTransId", EntityOperator.EQUALS,
								acctgTransId),
						EntityCondition
								.makeCondition("debitCreditFlag", EntityOperator.EQUALS,
										debitCreditFlag)
								

				), EntityOperator.AND);

		try {
			listEntries = delegator.findList("AcctgTransEntry", entriesConditions, null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : listEntries) {
			bdTotalEntryAmt = bdTotalEntryAmt.add(genericValue.getBigDecimal("amount"));
		}
		
		//bdTotalEntryAmt = bdTotalEntryAmt.
		return bdTotalEntryAmt.setScale(4, RoundingMode.HALF_UP);
	}

}
