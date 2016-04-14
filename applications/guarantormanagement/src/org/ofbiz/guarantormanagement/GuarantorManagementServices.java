package org.ofbiz.guarantormanagement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/***
 * @author Japheth Odonya @when Jun 30, 2015 12:07:02 AM
 * 
 *         Guarantor Management Functions
 *
 */
public class GuarantorManagementServices {

	public static Logger log = Logger.getLogger(GuarantorManagementServices.class);

	public static String removeGuarantor(Long loanGuarantorId, Map<String, String> userLogin) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Check that if you remove this guarantor, the total loan balance is
		// still less than the
		// total deposits for the current members
		GenericValue loanGuarantor = LoanUtilities.getLoanGuarantorEntity(loanGuarantorId);

		Long loanApplicationId = loanGuarantor.getLong("loanApplicationId");
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);

		BigDecimal bdTotalGuarantorDepositsWithoutThisGuarantor = LoanUtilities
				.getTotalGuarantorDepositsWithoutThisGuarantor(loanGuarantorId, loanApplicationId);

		if (bdLoanBalance.compareTo(bdTotalGuarantorDepositsWithoutThisGuarantor) == 1)
			return "You cannot remove this Guarantor, the remaining guarantors deposits will be less than the loan balance, please add another Guarantor to this loan first!"
					+ " Loan Balance is " + bdLoanBalance + " While total Deposits remaining will be "
					+ bdTotalGuarantorDepositsWithoutThisGuarantor;

		/***
		 * @Modified by Ronald Langat @when Feb 15, 2016 12:15:02 PM
		 * 
		 *           Get Loanee details using the loan Number
		 *
		 */
		GenericValue getLoaneeDetails = null;
		try {
			getLoaneeDetails = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String firstname = null;
		String middlename = null;
		String lastname = null;
		String name = null;
		String idNumber = null;
		if (getLoaneeDetails.size() > 0) {
			firstname = getLoaneeDetails.getString("firstName");
			middlename = getLoaneeDetails.getString("middleName");
			lastname = getLoaneeDetails.getString("lastName");
			idNumber = getLoaneeDetails.getString("idNumber");
			name = firstname + "  " + middlename + "  " + lastname + " - " + idNumber;
		}

		// If successfull, create a removed / replaced guarantor record
		GenericValue loanGuarantorRemoved = null;
		Long loanGuarantorRemovedId = delegator.getNextSeqIdLong("LoanGuarantorRemoved");
		loanGuarantorRemoved = delegator.makeValue("LoanGuarantorRemoved",
				UtilMisc.toMap("loanGuarantorRemovedId", loanGuarantorRemovedId, "isActive", "Y", "createdBy",
						userLogin.get("userLoginId"), "loanee", name, "loanApplicationId", loanApplicationId,

		"removedGuarantorId", loanGuarantorId));

		log.info(" Added a removed Guarantor Log");

		try {
			TransactionUtil.begin();
			delegator.create(loanGuarantorRemoved);
			TransactionUtil.commit();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Remove the guarantor
		Map<String, Long> guarantorCondition = new HashMap<String, Long>();
		guarantorCondition.put("loanGuarantorId", loanGuarantorId);
		try {
			delegator.removeByAnd("LoanGuarantor", guarantorCondition);
			// delegator.removeByAnd("LoanGuarantor",
			// EntityCondition.makeCondition("loanGuarantorId",
			// loanGuarantorId));
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LoanServices.generateGuarantorPercentages(loanApplicationId);

		return "success";
	}

	/***
	 * @author Japheth Odonya @when Jun 30, 2015 1:27:16 AM
	 * 
	 *         Get Loan Application ID given loanGuarantorID
	 */
	public static Long getLoanApplicationIdGivenGuarantorId(Long loanGuarantorId) {
		GenericValue loanGuarantor = LoanUtilities.getLoanGuarantorEntity(loanGuarantorId);
		Long loanApplicationId = loanGuarantor.getLong("loanApplicationId");
		return loanApplicationId;
	}

	public static String getLoaneeNameGivenLoanApplicationId(long loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue getLoaneeDetails = null;
		try {
			getLoaneeDetails = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String firstname = null;
		String middlename = null;
		String lastname = null;
		String name = null;
		String idNumber = null;
		if (getLoaneeDetails.size() > 0) {
			firstname = getLoaneeDetails.getString("firstName");
			middlename = getLoaneeDetails.getString("middleName");
			lastname = getLoaneeDetails.getString("lastName");
			idNumber = getLoaneeDetails.getString("idNumber");
			name = firstname + "  " + middlename + "  " + lastname + " - " + idNumber;
		}else{
			name = "  ";
		}

		return name;

	}

	public static String getLoanGuarantorNameGivenId(long guarantorId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> getGuarantorDetails = null;
		List<GenericValue> getGuarantorName = null;
		EntityConditionList<EntityExpr> guartConditions1 = EntityCondition.makeCondition(UtilMisc
				.toList(EntityCondition.makeCondition("guarantorId", EntityOperator.EQUALS, guarantorId)));
		
		try {
			getGuarantorDetails = delegator.findList("LoanGuarantor",guartConditions1, null,null,null,false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}

		String memberNumber = null;
		String firstname = null;
		String middlename = null;
		String lastname = null;
		String name = null;
		for (GenericValue gener : getGuarantorDetails) {
			memberNumber = gener.getString("memberNo");

			EntityConditionList<EntityExpr> guartConditions = EntityCondition.makeCondition(UtilMisc
					.toList(EntityCondition.makeCondition("memberNumber", EntityOperator.EQUALS, memberNumber)));

			try {
				getGuarantorName = delegator.findList("Member", guartConditions, null, null, null, false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			for (GenericValue genericValue : getGuarantorName) {
				firstname = genericValue.getString("firstName");
				middlename = genericValue.getString("middleName");
				lastname = genericValue.getString("lastName");
				name = firstname + "  " + middlename + "  " + lastname + " - " + lastname;
			}

		}
		System.out.println("------------name-----" + name);
		return name;

	}

}
