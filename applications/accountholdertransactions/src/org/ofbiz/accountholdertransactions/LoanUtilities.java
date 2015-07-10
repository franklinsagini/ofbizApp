package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

public class LoanUtilities {

	public static Logger log = Logger.getLogger(LoanUtilities.class);
	public static String MEMBER_DEPOSIT_CODE = "901";
	public static String SAVINGS_ACCOUNT_CODE = "999";

	public static Long getMemberId(String payrollNo) {
		Long memberId = null;

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : memberELI) {
			memberId = genericValue.getLong("partyId");
		}

		return memberId;
	}

	/***
	 * Get Member Status given payroll Number
	 * */
	public static String getMemberStatusGivenPayrollNo(String payrollNo) {
		Long memberStatusId = null;

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : memberELI) {
			memberStatusId = genericValue.getLong("memberStatusId");
		}

		String status = getMemberStatusName(memberStatusId);

		return status;
	}

	/***
	 * @author Japheth Odonya @when Jun 7, 2015 8:24:46 PM
	 * 
	 *         Get Member Employment Type given payroll number
	 * */
	public static String getMemberEmploymentTypeGivenPayrollNo(String payrollNo) {
		Long employmentTypeId = null;

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : memberELI) {
			employmentTypeId = genericValue.getLong("employmentTypeId");
		}

		String employmentType = getEmploymentTypeName(employmentTypeId);

		return employmentType;
	}

	public static String getMemberStatusName(Long memberStatusId) {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("memberStatusId",
							memberStatusId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String status = "";
		for (GenericValue genericValue : memberStatusELI) {
			status = genericValue.getString("name");
		}

		return status;
	}

	/****
	 * @author Japheth Odonya @when Jun 7, 2015 8:28:13 PM Get Employment Type
	 *         name given employmentTypeId
	 * 
	 * */
	public static String getEmploymentTypeName(Long employmentTypeId) {
		List<GenericValue> employmentTypeELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			employmentTypeELI = delegator.findList("EmploymentType",
					EntityCondition.makeCondition("employmentTypeId",
							employmentTypeId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String type = "";
		for (GenericValue genericValue : employmentTypeELI) {
			type = genericValue.getString("name");
		}

		return type;
	}

	public static List<Long> getLoanApplicationIds(Long memberId) {
		List<Long> loanApplicationIds = new ArrayList<Long>();

		Long disbursedLoansStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						disbursedLoansStatusId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						memberId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			loanApplicationIds.add(genericValue.getLong("loanApplicationId"));
		}

		return loanApplicationIds;
	}

	public static String getLoanProductCode(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct.getString("code");
	}

	public static GenericValue getLoanApplicationEntity(Long loanApplicationId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanApplication;
	}

	/***
	 * @author Japheth Odonya @when Jun 1, 2015 11:06:53 PM
	 * 
	 *         Get the loanApplication entity given the loanNo
	 * */
	public static GenericValue getLoanApplicationEntityGivenLoanNo(String loanNo) {
		GenericValue loanApplication = null;
		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			loanApplication = genericValue;
		}

		return loanApplication;
	}

	/***
	 * Get Product Name given loanNo
	 * 
	 * Return loanProduct Name
	 * */
	public static String getLoanProductNameGivenLoanNo(String loanNo) {
		GenericValue loanApplication = null;
		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			loanApplication = genericValue;
		}

		Long loanProductId = loanApplication.getLong("loanProductId");

		GenericValue loanProduct = getLoanProduct(loanProductId);
		String name = "";

		if (loanProduct != null) {
			name = loanProduct.getString("name");
		}
		return name;
	}

	public static GenericValue getLoanProduct(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct;
	}

	/***
	 * Get Loan Product given Code
	 * */
	public static GenericValue getLoanProductGivenCode(String code) {
		GenericValue loanProduct = null;
		List<GenericValue> loanProductELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProductELI = delegator.findList("LoanProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanProductELI) {
			loanProduct = genericValue;
		}

		return loanProduct;
	}

	/***
	 * Get AccountProduct
	 * */
	public static GenericValue getAccountProduct(Long accountProductId) {
		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct;
	}

	public static GenericValue getAccountProductGivenMemberAccountId(
			Long memberAccountId) {

		GenericValue memberAccount = getMemberAccount(memberAccountId);
		Long accountProductId = memberAccount.getLong("accountProductId");

		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct;
	}

	public static GenericValue getMemberAccount(Long memberAccountId) {
		GenericValue memberAccount = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return memberAccount;
	}
	
	/***
	 * @author Japheth Odonya  @when Jun 25, 2015 5:00:34 PM
	 * Get Member Account No given memberAccountId
	 * */
	public static String getMemberAccountNumber(Long memberAccountId) {
		GenericValue memberAccount = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		String accountNo = "";
		accountNo = memberAccount.getString("accountNo");
		return accountNo;
	}

	/***
	 * @author Japheth Odonya  @when Jun 25, 2015 5:00:23 PM
	 * 
	 * Member Account Name
	 * */
	public static String getMemberAccountName(Long memberAccountId) {
		GenericValue memberAccount = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		String accountName = "";
		accountName = memberAccount.getString("accountName");
		return accountName;
	}


	/***
	 * Get Loan Status
	 * */

	public static Long getLoanStatusId(String name) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long loanStatusId = 0L;
		for (GenericValue genericValue : loanStatusELI) {
			loanStatusId = genericValue.getLong("loanStatusId");
		}

		String statusIdString = String.valueOf(loanStatusId);
		statusIdString = statusIdString.replaceAll(",", "");
		loanStatusId = Long.valueOf(statusIdString);
		return loanStatusId;
	}

	// Count Loan Guarantors

	public static Long getGuarantorsCount(Long loanApplicationId) {
		List<GenericValue> guaranteedLoanELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			guaranteedLoanELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Long guarantorCount = 0L;

		guarantorCount = new Long(guaranteedLoanELI.size());

		return guarantorCount;
	}

	public static GenericValue getMember(String partyId) {

		partyId = partyId.replaceAll(",", "");
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return member;
	}

	public static String getMemberBranchId(String partyId) {

		partyId = partyId.replaceAll(",", "");
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (member != null)
			return member.getString("branchId").toString();

		return null;
	}

	public static GenericValue getMember(Long partyId) {

		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return member;
	}

	public static int getMemberDurations(Date joinDate) {

		LocalDateTime stJoinDate = new LocalDateTime(joinDate.getTime());
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());

		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(stJoinDate, stCurrentDate, monthDay);

		int months = difference.getMonths();

		return months;

	}

	public static int getMemberDurationYears(Date deathOfBirth) {

		LocalDateTime stDeathOfBirth = new LocalDateTime(deathOfBirth.getTime());
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());

		PeriodType years = PeriodType.years();

		Period difference = new Period(stDeathOfBirth, stCurrentDate, years);

		int yearDifference = difference.getYears();

		return yearDifference;

	}

	public static boolean isOldEnough(String partyId) {

		Boolean oldEnough = false;

		GenericValue member = getMember(partyId);

		int duration = getMemberDurations(member.getDate("joinDate"));

		if (duration < 3) {
			oldEnough = false;
		} else {
			oldEnough = true;
		}

		return oldEnough;
	}

	public static boolean isFromAnotherSacco(String partyId) {

		Boolean fromAnotherSacco = false;

		GenericValue member = getMember(partyId);

		String membershipofOtherSacco = member
				.getString("membershipofOtherSacco");

		if ((membershipofOtherSacco != null)
				&& (membershipofOtherSacco.equals("Y"))) {
			fromAnotherSacco = true;
		} else {
			fromAnotherSacco = false;
		}

		return fromAnotherSacco;
	}

	public static GenericValue getAccountProductGivenCodeId(String code) {

		List<GenericValue> accountProductELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> accountProductConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"code", EntityOperator.EQUALS, code)),
						EntityOperator.AND);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					accountProductConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue accountProduct = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProduct = genericValue;
		}

		return accountProduct;
	}
	
	//getAccountProductGivenCodeId
	public static Long getAccountProductIdGivenCodeId(String code) {

		List<GenericValue> accountProductELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> accountProductConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"code", EntityOperator.EQUALS, code)),
						EntityOperator.AND);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					accountProductConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue accountProduct = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProduct = genericValue;
		}
		
		if (accountProduct == null)
			return null;

		return accountProduct.getLong("accountProductId");
	}

	// org.ofbiz.accountholdertransactions.LoanUtilities.getRecommendedAmount(BigDecimal
	// bdMaxLoanAmt, BigDecimal bdAppliedAmt)
	public static BigDecimal getRecommendedAmount(BigDecimal bdMaxLoanAmt,
			BigDecimal bdAppliedAmt) {
		BigDecimal bdRecommendeAmt = bdAppliedAmt;

		if (bdAppliedAmt.compareTo(bdMaxLoanAmt) == 1) {
			bdRecommendeAmt = bdMaxLoanAmt;
		}

		return bdRecommendeAmt;
	}

	public static Long getMemberAge(Long partyId) {
		Long memberAge = 0L;

		GenericValue member = getMember(partyId);
		Date dateOfBirth = member.getDate("birthDate");

		memberAge = new Long(getMemberDurationYears(dateOfBirth));

		return memberAge;
	}

	/***
	 * Get Last Member Deposit Contribution Date
	 * */
	public static Date getLastMemberDepositContributionDate(Long partyId) {
		Timestamp lastDate = null;

		// Get MemberDeposit AccountProductID
		GenericValue accountProduct = getAccountProductGivenCodeId(MEMBER_DEPOSIT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");

		// Get MemberAccount for the Account Product and party ID
		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		lastDate = memberAccount.getTimestamp("lastContributionDate");
		return lastDate;
	}

	/****
	 * Get Last Member Deposit Last Contribution Amount
	 * */
	public static BigDecimal getLastMemberDepositContributionAmount(Long partyId) {
		BigDecimal bdLastAmount = null;
		// Get MemberDeposit AccountProductID
		GenericValue accountProduct = getAccountProductGivenCodeId(MEMBER_DEPOSIT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");

		// Get MemberAccount for the Account Product and party ID
		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		bdLastAmount = memberAccount.getBigDecimal("contributingAmount");
		return bdLastAmount;
	}

	/***
	 * Total Defaulted Loans
	 * */
	public static BigDecimal getTotalDefaultedLoans(Long partyId) {
		BigDecimal bdTotalLoans = BigDecimal.ZERO;

		Long loanDefaulterProductId = getLoanProductGivenCode("D330").getLong(
				"loanProductId");
		System.out
				.println("DDDDDDD DEFFFFFFFFFF Calculate Defaulted Prod ID is "
						+ loanDefaulterProductId);
		// Long defaultedLoanStatusId =
		// LoanServices.getLoanStatusId("DEFAULTED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanProductId", EntityOperator.EQUALS,
						loanDefaulterProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			System.out.println("DDDDDDD DEFFFFFFFFFF The Amount is "
					+ genericValue.getBigDecimal("loanAmt"));
			bdTotalLoans = bdTotalLoans.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		System.out.println("DDDDDDD DEFFFFFFFFFF total is " + bdTotalLoans);
		System.out.println("DDDDDDD DEFFFFFFFFFF count is "
				+ loanApplicationELI.size());
		System.out.println("DDDDDDD DEFFFFFFFFFF party ID is " + partyId);

		return bdTotalLoans;
	}

	/***
	 * Get Loan Default/Transfer Date
	 * */
	public static Date getDefaultTransferDate(Long partyId) {
		Timestamp lastDate = null;

		Long defaultedLoanStatusId = LoanServices.getLoanStatusId("DEFAULTED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						defaultedLoanStatusId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			// bdTotalLoans.add(genericValue.getBigDecimal("loanAmt"));
			lastDate = genericValue.getTimestamp("defaultTransferDate");
		}

		return lastDate;
	}

	public static String getDefaultedLoansComment(Long partyId) {
		Long loanDefaultedProductId = getLoanProductGivenCode("D330").getLong(
				"loanProductId");
		// Long defaultedLoanStatusId =
		// LoanServices.getLoanStatusId("DEFAULTED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanProductId", EntityOperator.EQUALS,
						loanDefaultedProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanApplicationELI.size() > 0) {
			return "Has attached Loan(s) ";
		} else
			return "No attached Loan(s)";
	}

	public static String getDefaultedLoansTotalsWithComment(Long partyId) {
		System.out
				.println(" LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL DDDDDDDDDDDDDDD D330 Looking for Defaulted Loans ..... "
						+ partyId);
		// D330
		Long loanDefaulterProductId = getLoanProductGivenCode("D330").getLong(
				"loanProductId");

		BigDecimal bdTotalDefaulted = getTotalDefaultedLoans(partyId);
		BigDecimal bdTotalDefaultBalance = BigDecimal.ZERO;
		// Long defaultedLoanStatusId =
		// LoanServices.getLoanStatusId("DEFAULTED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanProductId", EntityOperator.EQUALS,
						loanDefaulterProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			bdTotalDefaultBalance = bdTotalDefaultBalance.add(LoanServices
					.getLoanRemainingBalance(genericValue
							.getLong("loanApplicationId")));
		}

		if (bdTotalDefaulted.compareTo(BigDecimal.ZERO) == 1) {
			return " Org Amount = " + bdTotalDefaulted + " Balance "
					+ bdTotalDefaultBalance;
		} else {
			return "No Defaulted Loan";
		}

		// if (loanApplicationELI.size() > 0){
		// return "Has defaulted before";
		// } else
		// return "No Defaulted Loan";
	}

	// result.put("hasLoans", LoanUtilities.hasLoans(partyId));
	// result.put("hasGuaranteed", LoanUtilities.hasGuaranteed(partyId));
	// result.put("shareCapitalBelowMinimum",
	// LoanUtilities.shareCapitalBelowMinimum(partyId));
	// result.put("memberDepositsLessThanLoans",
	// LoanUtilities.memberDepositsLessThanLoans(partyId));

	public static boolean hasLoans(Long partyId) {
		Long defaultedLoanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						defaultedLoanStatusId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanApplicationELI.size() > 0) {
			return true;
		} else
			return false;
	}

	public static boolean hasGuaranteed(Long partyId) {
		Long defaultedLoanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						defaultedLoanStatusId),

				EntityCondition.makeCondition("guarantorId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanGuarantorDisbursedLoan",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanApplicationELI.size() > 0) {
			return true;
		} else
			return false;
	}

	public static boolean shareCapitalBelowMinimum(Long partyId) {
		BigDecimal bdShareCapitalAmount = LoanUtilities.getShareCapitalAccountBalance(partyId);
		
		BigDecimal bdShareCapitalMinimum = LoanUtilities.getShareCapitalLimit(partyId);
		
		if (bdShareCapitalAmount.compareTo(bdShareCapitalMinimum) == -1)
			return true;
		
		return false;
	}

	public static boolean memberDepositsLessThanLoans(Long partyId) {
		return false;
	}

	/***
	 * Check that Loan Application Has Guarantors (Must have Guarantors) returns
	 * true of loanApplication has guarantors
	 * */
	public static boolean hasGuarantors(Long loanApplicationId) {
		EntityConditionList<EntityExpr> guarantorConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)

				), EntityOperator.AND);

		List<GenericValue> loanGuarantorELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					guarantorConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		System.out.println(" OOOOOOOOOOOOOOOOOOOOOO The Sizzzzzzzzzzzze "
				+ loanGuarantorELI.size());
		if (loanGuarantorELI.size() > 0) {
			return true;
		} else
			return false;
	}

	/***
	 * Returns True if total Deposits for guarantors is less that the appraised
	 * or recommended or entitlement amount
	 * */
	public static boolean guarantorTotalEnough(Long loanApplicationId,
			BigDecimal bdLoanAmt) {
		// TODO Auto-generated method stub

		// depositAmt
		BigDecimal bdTotalDepositAmt = getTotalDepositAmount(loanApplicationId);

		if (bdTotalDepositAmt.compareTo(bdLoanAmt) >= 0)
			return true;

		return false;
	}

	private static BigDecimal getTotalDepositAmount(Long loanApplicationId) {
		EntityConditionList<EntityExpr> guarantorConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)

				), EntityOperator.AND);

		List<GenericValue> loanGuarantorELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					guarantorConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdTotalDeposit = BigDecimal.ZERO;

		for (GenericValue genericValue : loanGuarantorELI) {
			bdTotalDeposit = bdTotalDeposit.add(genericValue
					.getBigDecimal("depositamt"));
		}

		return bdTotalDeposit;
	}

	public static boolean minimumOK(Long loanApplicationId, BigDecimal loanAmt) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue loanProduct = getLoanProduct(loanApplication
				.getLong("loanProductId"));

		BigDecimal minimumAmt = null;
		minimumAmt = loanProduct.getBigDecimal("minimumAmt");

		if ((minimumAmt == null)
				|| (minimumAmt.compareTo(BigDecimal.ZERO) == 0)) {
			return true;
		}

		if (minimumAmt.compareTo(loanAmt) == 1) {
			return false;
		}

		return true;
	}

	public static boolean maximumOk(Long loanApplicationId, BigDecimal loanAmt) {
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue loanProduct = getLoanProduct(loanApplication
				.getLong("loanProductId"));

		BigDecimal maximumAmt = null;
		maximumAmt = loanProduct.getBigDecimal("maximumAmt");

		if ((maximumAmt == null)
				|| (maximumAmt.compareTo(BigDecimal.ZERO) == 0)) {
			return true;
		}

		if (maximumAmt.compareTo(loanAmt) == -1) {
			return false;
		}

		return true;
	}

	public static boolean isNotSixMonthOldNotFromOtherSacco(
			Long loanApplicationId) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);
		Long partyId = loanApplication.getLong("partyId");

		if ((!isOldEnough(partyId.toString()))
				&& (!isFromAnotherSacco(partyId.toString()))) {
			return false;
		}

		return true;
	}

	public static boolean isAppraisedAmounNotMoreEntitlement(
			Long loanApplicationId, BigDecimal bdLoanAmt) {

		// BigDecimal bdLoan
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		BigDecimal bdEntitlementAmount = loanApplication
				.getBigDecimal("maxLoanAmt");

		if (bdLoanAmt.compareTo(bdEntitlementAmount) == 1) {
			return false;
		}

		return true;
	}

	public static boolean repaymentPeriodNotMoreThanMaximum(
			Long loanApplicationId) {

		// BigDecimal bdLoan
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		Long repaymentPeriod = loanApplication.getLong("repaymentPeriod");
		Long maxRepaymentPeriod = loanApplication.getLong("maxRepaymentPeriod");

		if (repaymentPeriod > maxRepaymentPeriod) {
			return false;
		}

		return true;
	}

	public static boolean addedDeductions(Long loanApplicationId) {

		EntityConditionList<EntityExpr> deductionsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)

				), EntityOperator.AND);

		List<GenericValue> loanDeductionEvaluationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanDeductionEvaluationELI = delegator.findList(
					"LoanDeductionEvaluation", deductionsConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanDeductionEvaluationELI.size() > 0) {
			return true;
		} else
			return false;

	}

	public static Integer getNumberOfGuarantors(Long loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =
		// loanApplicationId = loanApplicationId.replaceAll(",", "");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Integer count = loanGuarantorELI.size();
		// for (GenericValue genericValue : loanGuarantorELI) {
		// count = count + 1;
		// }

		return count;
	}

	public static BigDecimal getLoanAmount(Long loanApplicationId) {

		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Fing LoanApplication");
			// return "Cannot Get Product Details";
		}

		if (loanApplication != null) {
			return loanApplication.getBigDecimal("loanAmt");
		}

		return null;
	}

	public static BigDecimal getMyGuaranteedValue(Long loanApplicationId) {
		// TODO Auto-generated method stub
		BigDecimal bdLoanBalanceAmt = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		Long noOfGuarantors = LoanUtilities
				.getGuarantorsCount(loanApplicationId);
		return bdLoanBalanceAmt.divide(new BigDecimal(noOfGuarantors), 4,
				RoundingMode.HALF_UP);
	}

	public static String getLoanProductCodeGivenLoanNo(String loanNo) {
		// TODO Auto-generated method stub
		// Get Loan Product Code given loanNo

		List<GenericValue> loanApplicationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI.size() < 1) {
			return null;
		}

		Long loanProductId = null;

		for (GenericValue genericValue : loanApplicationELI) {
			loanProductId = genericValue.getLong("loanProductId");
		}

		String productCode = getLoanProductCode(loanProductId);

		return productCode.trim();
	}

	public static String getStationEmployerCode(String stationId) {
		GenericValue station = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			station = delegator.findOne("Station",
					UtilMisc.toMap("stationId", stationId.trim()), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (station == null)
			return null;

		String employerCode = station.getString("employerCode");

		if (employerCode != null)
			employerCode = employerCode.trim();
		return employerCode;
	}

	public static String getStationId(String employerCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator
					.findList("Station", EntityCondition.makeCondition(
							"employerCode", employerCode), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String stationId = null;
		if (stationELI.size() > 0) {
			stationId = stationELI.get(0).getString("stationId");
		}

		return stationId;

	}

	public static String getStationName(String employerCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator
					.findList("Station", EntityCondition.makeCondition(
							"employerCode", employerCode), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String stationName = "";
		if (stationELI.size() > 0) {
			stationName = stationELI.get(0).getString("name");
		}

		return stationName;

	}

	public static String getCurrentYear() {
		LocalDate localDate = new LocalDate();

		return String.valueOf(localDate.getYear());
	}

	public static String getCurrentMonth() {
		LocalDate localDate = new LocalDate();

		return String.valueOf(localDate.getMonthOfYear());
	}

	public static GenericValue getStation(String stationId) {
		GenericValue station = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			station = delegator.findOne("Station",
					UtilMisc.toMap("stationId", stationId.trim()), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (station == null)
			return null;

		return station;
	}

	public static List<String> getStationIds(String employerCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator
					.findList("Station", EntityCondition.makeCondition(
							"employerCode", employerCode), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> stationIdList = new ArrayList<String>();

		for (GenericValue genericValue : stationELI) {
			// memberId = genericValue.getLong("partyId");
			stationIdList.add(genericValue.getString("stationId"));
		}
		return stationIdList;
	}

	public static GenericValue getStationGivenOnlineCode(String onlineCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator.findList(
					"Station",
					EntityCondition.makeCondition("Onlinecode",
							onlineCode.trim()), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue station = null;

		for (GenericValue genericValue : stationELI) {
			station = genericValue;
		}

		return station;
	}

	public static String getBranchName(String fromBranchId) {

		// groupName
		GenericValue partyGroup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			partyGroup = delegator.findOne("PartyGroup",
					UtilMisc.toMap("partyId", fromBranchId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return partyGroup.getString("groupName");
	}

	/**
	 * Strips String of values
	 * **/
	public static String stripStringName(String id) {
		id = id.replaceAll(",", "");
		return id;
	}

	public static String getMemberName(String payrollNo) {
		String memberNames = null;

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : memberELI) {
			memberNames = genericValue.getString("firstName") + " "
					+ genericValue.getString("middleName") + " "
					+ genericValue.getString("lastName");
		}

		return memberNames;
	}

	// public static GenericValue getMemberGivenEmployeeNumber(String
	// employeeNumber) {
	// GenericValue member = null;
	//
	// List<GenericValue> memberELI = null; // =
	// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
	// try {
	// memberELI = delegator.findList("Member",
	// EntityCondition.makeCondition("employeeNumber", employeeNumber),
	// null, null, null, false);
	// } catch (GenericEntityException e) {
	// e.printStackTrace();
	// }
	//
	// for (GenericValue genericValue : memberELI) {
	// member = genericValue;
	// }
	//
	// return member;
	// }

	public static GenericValue getSalaryMonthYear(Long salaryMonthIdLong) {
		GenericValue salaryMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			salaryMonthYear = delegator.findOne("SalaryMonthYear",
					UtilMisc.toMap("salaryMonthYearId", salaryMonthIdLong),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// return loanProduct.getString("code");
		return salaryMonthYear;
	}

	public static Boolean payrollNumberExists(String payrollNo) {

		/***
		 * Check if Payroll Number exists
		 * */
		List<GenericValue> memberELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"payrollNumber", EntityOperator.EQUALS,
						payrollNo.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member", memberConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (memberELI.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static List<GenericValue> getAccountProductChargeList(
			String transactionType, String productCode) {

		// Get the account Product ID
		GenericValue accountProduct = getAccountProductGivenCodeId(productCode);
		Long accountProductId = null;
		if (accountProduct != null)
			accountProductId = accountProduct.getLong("accountProductId");

		EntityConditionList<EntityExpr> accountProductChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						transactionType),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		List<GenericValue> accountProductChargeELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductChargeELI = delegator.findList(
					"AccountProductCharge", accountProductChargeConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return accountProductChargeELI;
	}

	public static GenericValue getProductCharge(Long productChargeId) {
		GenericValue productCharge = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			productCharge = delegator.findOne("ProductCharge",
					UtilMisc.toMap("productChargeId", productChargeId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return productCharge;
	}

	// ## Get the list of contributing accounts
	public static List<GenericValue> getMemberContributingAccounts(Long memberId) {
		// Get from MemberAccount - accounts that are contributing and belong to
		// this member
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();

		List<String> orderByList = new LinkedList<String>();
		orderByList.add("accountProductId");
		// String accountProductId = getShareDepositAccountId("901");
		// accountProductId = accountProductId.replaceAll(",", "");
		// Long accountProductIdLong = Long.valueOf(accountProductId);
		// And accountProductId not equal to memberDeposit, not equal to share
		// capital and not equal to
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"contributing", EntityOperator.EQUALS, "YES"),
				//
				// EntityCondition.makeCondition(
				// "accountProductId", EntityOperator.NOT_EQUAL,
				// accountProductIdLong),

						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, memberId)

				), EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberAccountELI;

	}

	// ### Get Contributing Amount
	/****
	 * Create Expectation
	 * **/
	public static BigDecimal getContributionAmount(GenericValue memberAccount,
			Long memberId) {

		GenericValue accountProduct = RemittanceServices
				.findAccountProduct(memberAccount.getLong("accountProductId")
						.toString());

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}

		// Get Contributing Amount
		BigDecimal bdContributingAmt = BigDecimal.ZERO;

		if (accountProduct.getString("code").equals(MEMBER_DEPOSIT_CODE)) {
			// Calculate Contribution based on graduated scale this is for
			// Member Deposits
			bdContributingAmt = LoansProcessingServices
					.getLoanCurrentContributionAmount(memberId);

			BigDecimal bdSpecifiedAmount = memberAccount
					.getBigDecimal("contributingAmount");

			if ((bdSpecifiedAmount != null)
					&& (bdSpecifiedAmount.compareTo(bdContributingAmt) == 1)) {
				bdContributingAmt = bdSpecifiedAmount;
			}

		} else {
			if (memberAccount.getBigDecimal("contributingAmount") != null) {
				bdContributingAmt = memberAccount
						.getBigDecimal("contributingAmount");
			} else {
				bdContributingAmt = accountProduct
						.getBigDecimal("minSavingsAmt");
			}
		}
		return bdContributingAmt;
	}

	public static Long getSavingsMemberAccountId(Long theLoanApplicationId) {
		// TODO Auto-generated method stub
		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(SAVINGS_ACCOUNT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");

		GenericValue loanApplication = getLoanApplicationEntity(theLoanApplicationId);

		GenericValue member = getMemberGivenPartyId(loanApplication
				.getLong("partyId"));
		Long partyId = member.getLong("partyId");

		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		Long memberAccountId = null;

		if (memberAccount != null)
			memberAccountId = memberAccount.getLong("memberAccountId");

		return memberAccountId;
	}

	public static GenericValue getMemberGivenPartyId(Long partyId) {
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return member;
	}

	public static String getMemberPartyIdFromMemberAccountId(
			Long memberAccountId) {
		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("memberAccountId",
						EntityOperator.EQUALS, memberAccountId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		if (memberAccount != null)
			return memberAccount.getLong("partyId").toString();

		return null;

	}

	/***
	 * Get memberAccountId
	 * 
	 * Get Member Account ID given accountProductId and partyId
	 * **/
	public static Long getMemberAccountIdFromMemberAccount(Long partyId,
			Long accountProductId) {

		// Long memberAccountId
		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		if (memberAccount != null)
			return memberAccount.getLong("memberAccountId");

		return null;
	}

	public static GenericValue getMemberGiveLoanApplicationId(
			Long loanApplicationId) {

		// Get Loan Application
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long partyId = loanApplication.getLong("partyId");

		GenericValue member = getMember(partyId);

		return member;
	}

	public static Long getMemberDepositsAccountId(String code) {
		List<GenericValue> accountProductELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long accountProductId = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProductId = genericValue.getLong("accountProductId");
		}
		return accountProductId;
	}

	public static Boolean netPayMoreThanZero(Long loanApplicationId) {
		EntityConditionList<EntityExpr> deductionsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)

				), EntityOperator.AND);

		List<GenericValue> loanDeductionEvaluationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> orderList = new ArrayList<String>();
		orderList.add("-loanDeductionEvaluationId");
		try {
			loanDeductionEvaluationELI = delegator.findList(
					"LoanDeductionEvaluation", deductionsConditions, null,
					orderList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanDeductionEvaluationELI == null)
			return false;

		if (loanDeductionEvaluationELI.size() <= 0)
			return false;

		GenericValue loanDeductionEvaluation = loanDeductionEvaluationELI
				.get(0);

		if (loanDeductionEvaluation
				.getBigDecimal("grossPayMinusTotalDeduction") == null)
			return false;

		if (loanDeductionEvaluation
				.getBigDecimal("grossPayMinusTotalDeduction").compareTo(
						BigDecimal.ZERO) == 1)
			return true;

		return false;

	}

	public static GenericValue getMemberGivenPayrollNumber(String payrollNo) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		payrollNo = payrollNo.trim();
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue member = null;

		for (GenericValue genericValue : memberELI) {
			member = genericValue;
		}

		return member;

	}

	public static GenericValue getMemberGivenEmployeeNumber(
			String employeeNumber) {
		// TODO Auto-generated method stub
		// employeeNumber

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		employeeNumber = employeeNumber.trim();
		try {
			memberELI = delegator.findList("Member", EntityCondition
					.makeCondition("employeeNumber", employeeNumber), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue member = null;

		for (GenericValue genericValue : memberELI) {
			member = genericValue;
		}

		return member;
	}

	/***
	 * Get Member given memberNumber
	 * 
	 * */
	public static GenericValue getMemberGivenMemberNumber(String memberNumber) {
		// TODO Auto-generated method stub
		// employeeNumber

		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		memberNumber = memberNumber.trim();
		try {
			memberELI = delegator
					.findList("Member", EntityCondition.makeCondition(
							"memberNumber", memberNumber), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue member = null;

		for (GenericValue genericValue : memberELI) {
			member = genericValue;
		}

		return member;
	}

	public static GenericValue getMemberGivenEmployeeNumber(
			String employeeNumber, String onlineCode) {
		// TODO Auto-generated method stub
		// employeeNumber
		// Get all station ids where online code is online code
		List<String> listStationId = getListStationId(onlineCode);

		// Get Member whose employer number is employerNumber and station Id is
		// one of the IDs on the list
		GenericValue member = null;

		GenericValue tempMember = null;
		for (String stationId : listStationId) {
			stationId = stationId.trim();
			tempMember = getMember(employeeNumber, stationId);

			if (tempMember != null) {
				member = tempMember;
				log.info("PPPPPPP Successful Assignment !!!!");
			}

		}

		return member;
	}

	private static GenericValue getMember(String employeeNumber,
			String stationId) {
		Long stationIdLong = Long.valueOf(stationId);

		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationId", EntityOperator.EQUALS, stationIdLong),

				EntityCondition.makeCondition("employeeNumber",
						EntityOperator.EQUALS, employeeNumber.trim())

				), EntityOperator.AND);

		log.info("WWWWWWWW will compare Station for member");
		log.info("WWWWWWWWW Station ID " + stationId);
		log.info("WWWWWWWWW Employee Number " + employeeNumber);

		List<GenericValue> listMemberELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			listMemberELI = delegator.findList("Member", memberConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		log.info("WWWWWWWWW List Size " + listMemberELI.size());
		GenericValue member = null;
		for (GenericValue genericValue : listMemberELI) {
			member = genericValue;
		}

		return member;
	}

	private static List<String> getListStationId(String onlineCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		onlineCode = onlineCode.trim();
		try {
			stationELI = delegator.findList("Station",
					EntityCondition.makeCondition("Onlinecode", onlineCode),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> listStationId = new ArrayList<String>();

		for (GenericValue genericValue : stationELI) {
			listStationId.add(genericValue.getString("stationId"));
		}

		log.info(" SSSSSSSS station IDs " + listStationId.size());

		return listStationId;
	}

	/****
	 * @author Japheth Odonya @when Jun 3, 2015 9:56:25 PM
	 * 
	 *         Check if account is mapped to a branch
	 * 
	 *         returns false if not returns true is mapped
	 * */
	public static boolean organizationAccountMapped(String treasuryAccountId,
			String branchId) {

		List<GenericValue> glAccountOrganizationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> glAccountOrganizationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("glAccountId", EntityOperator.EQUALS,
								treasuryAccountId), EntityCondition
						.makeCondition("organizationPartyId",
								EntityOperator.EQUALS, branchId)

				), EntityOperator.AND);

		try {
			glAccountOrganizationELI = delegator.findList(
					"GlAccountOrganization", glAccountOrganizationConditions,
					null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((glAccountOrganizationELI == null)
				|| (!(glAccountOrganizationELI.size() > 0)))
			return false;

		return true;
	}

	/***
	 * Get Branch ID given employer code
	 * */
	public static String getBranchId(String employerCode) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		employerCode = employerCode.trim();
		try {
			stationELI = delegator
					.findList("Station", EntityCondition.makeCondition(
							"employerCode", employerCode), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue station = null;
		for (GenericValue genericValue : stationELI) {
			station = genericValue;
		}

		if (station != null)
			return station.getString("branchId");

		return null;
	}

	/***
	 * @author Japheth Odonya @when Jun 4, 2015 10:41:12 AM
	 * 
	 *         Check that an account product actually does have an account set
	 *         in the setup
	 * 
	 *         (Will need to credit this account)
	 * 
	 *         Returns true is missing, false if set already
	 * */
	public static boolean missingGLAccount(String code) {

		GenericValue accountProduct = null;
		accountProduct = getAccountProductGivenCodeId(code.trim());

		if (accountProduct == null)
			return true;

		String glAccountId = accountProduct.getString("glAccountId");

		if (glAccountId == null)
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when Jun 4, 2015 10:46:14 AM
	 * 
	 *         Get if an account product is already mapped to the branch
	 *         provided
	 * */
	public static boolean notAccountsNotMapped(String branchId, String code) {
		code = code.trim();

		GenericValue accountProduct = null;
		accountProduct = getAccountProductGivenCodeId(code.trim());

		String glAccountId = accountProduct.getString("glAccountId");

		if (glAccountId == null)
			return true;

		if (!organizationAccountMapped(glAccountId, branchId))
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when Jun 4, 2015 12:44:23 PM
	 * 
	 *         Get GL Account ID
	 * */

	public static String getGLAccountIDForAccountProduct(String code) {

		GenericValue accountProduct = null;
		accountProduct = getAccountProductGivenCodeId(code.trim());

		if (accountProduct == null)
			return null;

		String glAccountId = null;

		glAccountId = accountProduct.getString("glAccountId");

		return glAccountId;
	}

	/****
	 * @author Japheth Odonya @when Jun 7, 2015 8:36:12 PM Get Loan Time
	 *         Difference in months from last repaid to today
	 * */
	public static Long loanDefaultTimeDiff(String loanNo) {

		GenericValue loanApplication = getLoanApplicationEntityGivenLoanNo(loanNo);

		Timestamp fromDate = loanApplication.getTimestamp("lastRepaymentDate");
		Timestamp toDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		Long timeDiff = getTimeDifferenceInMonths(fromDate, toDate);
		return timeDiff;
	}

	/***
	 * @author Japheth Odonya @when Jun 7, 2015 8:30:29 PM
	 * 
	 *         Get the time difference in Months given start and end date
	 * */
	public static Long getTimeDifferenceInMonths(Timestamp fromDate,
			Timestamp toDate) {
		int timeTiff = 0;

		if (fromDate != null) {
			timeTiff = Months.monthsBetween(new LocalDate(fromDate.getTime()),
					new LocalDate(toDate.getTime())).getMonths();
			return new Long(timeTiff);
		} else {
			return null;
		}

	}

	/****
	 * @author Japheth Odonya @when Jun 7, 2015 8:44:08 PM
	 * 
	 *         Get Member's Member Deposits Balance
	 * */
	public static BigDecimal getMemberDepositsBalance(String payrollNo) {
		BigDecimal bdBalance = BigDecimal.ZERO;

		Long memberAccountId = getMemberDepositMemberAccountId(payrollNo);

		bdBalance = AccHolderTransactionServices
				.getBookBalanceNow(memberAccountId.toString());

		return bdBalance;
	}

	public static Long getMemberDepositMemberAccountId(String payrollNo) {
		// TODO Auto-generated method stub
		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(MEMBER_DEPOSIT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");

		// GenericValue loanApplication =
		// getLoanApplicationEntity(theLoanApplicationId);

		GenericValue member = getMemberGivenPayrollNumber(payrollNo);
		Long partyId = member.getLong("partyId");

		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		Long memberAccountId = null;

		if (memberAccount != null)
			memberAccountId = memberAccount.getLong("memberAccountId");

		return memberAccountId;
	}

	public static Timestamp getLoanLastRepaymentDate(String loanNo) {
		Timestamp lastRepaymentDate = null;
		GenericValue loanApplication = getLoanApplicationEntityGivenLoanNo(loanNo);

		lastRepaymentDate = loanApplication.getTimestamp("lastRepaymentDate");

		Timestamp lastRepayFromTransaction = getLastRepaymentDateFromTransaction(loanNo);

		if (lastRepayFromTransaction == null)
			return lastRepaymentDate;

		return lastRepaymentDate;
	}

	/***
	 * Get last repayment date from LoanRepayment
	 * */
	private static Timestamp getLastRepaymentDateFromTransaction(String loanNo) {
		// TODO Auto-generated method stub
		return null;
	}

	/***
	 * @author Japheth Odonya @when Jun 11, 2015 10:56:34 AM
	 * 
	 *         Return false if ATM does notexists Return true if ATM exist
	 * 
	 * */
	public static Boolean checkATMExists(Long partyId) {

		// find list CardApplication
		List<GenericValue> cardApplicationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardApplicationELI = delegator.findList("CardApplication",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((cardApplicationELI != null) && (cardApplicationELI.size() > 0)) {
			return true;
		}

		return false;
	}

	/****
	 * @author Japheth Odonya @when Jun 11, 2015 10:57:12 AM
	 * 
	 *         Return false if Msacco does not exists Return true if Msacco
	 *         exist
	 * */
	public static Boolean checkMsaccoExists(Long partyId) {
		// find list MSaccoApplication
		List<GenericValue> msaccoApplicationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			msaccoApplicationELI = delegator.findList("MSaccoApplication",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((msaccoApplicationELI != null) && (msaccoApplicationELI.size() > 0)) {
			return true;
		}

		return false;
	}

	public static void addNewATMApplication(Long partyId,
			Map<String, String> userLogin) {

		GenericValue member = getMember(partyId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		Long accountProductId = getAccountProductGivenCodeId(
				SAVINGS_ACCOUNT_CODE).getLong("accountProductId");

		Long memberAccountId = getMemberAccountIdFromMemberAccount(partyId,
				accountProductId);

		Long cardStatusId = getCardStatusId("NEW");

		GenericValue cardApplication = null;
		String userLoginId = (String) userLogin.get("userLoginId");
		Long cardApplicationId = delegator.getNextSeqIdLong("CardApplication",
				1);
		cardApplication = delegator.makeValue("CardApplication", UtilMisc
				.toMap("cardApplicationId", cardApplicationId, "isActive", "Y",
						"createdBy", userLoginId,

						"partyId", partyId,

						"idNumber", member.getString("idNumber"),
						"payrollNumber", member.getString("payrollNumber"),
						"memberAccountId", memberAccountId,
						"mobilePhoneNumber", member.getString("mobileNumber"),
						"firstName", member.getString("firstName"),
						"middleName", member.getString("middleName"),
						"lastName", member.getString("lastName"),

						"cardStatusId", cardStatusId));
		try {
			delegator.createOrStore(cardApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/****
	 * @author Japheth Odonya @when Jun 11, 2015 5:03:31 PM
	 * 
	 *         Create New MSacco Application
	 * */
	public static void addNewMSaccoApplication(Long partyId,
			Map<String, String> userLogin) {

		GenericValue member = getMember(partyId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		Long accountProductId = getAccountProductGivenCodeId(
				SAVINGS_ACCOUNT_CODE).getLong("accountProductId");

		Long memberAccountId = getMemberAccountIdFromMemberAccount(partyId,
				accountProductId);

		Long cardStatusId = getCardStatusId("NEW");

		GenericValue msaccoApplication = null;
		String userLoginId = (String) userLogin.get("userLoginId");
		Long msaccoApplicationId = delegator.getNextSeqIdLong(
				"MSaccoApplication", 1);
		msaccoApplication = delegator.makeValue("MSaccoApplication", UtilMisc
				.toMap("msaccoApplicationId", msaccoApplicationId, "isActive",
						"Y", "createdBy", userLoginId, "formNumber",
						msaccoApplicationId, "partyId", partyId,
						"mobilePhoneNumber", member.getString("mobileNumber"),

						"memberAccountId", memberAccountId,

						"cardStatusId", cardStatusId));
		try {
			delegator.createOrStore(msaccoApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}

	/***
	 * Check if Member has savings account
	 * 
	 * return false if does not have
	 * */
	public static Boolean memberHasSavingsAccount(Long partyId) {
		GenericValue accountProduct = getAccountProductGivenCodeId(SAVINGS_ACCOUNT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");
		Long memberAccountId = null;
		memberAccountId = getMemberAccountIdFromMemberAccount(partyId,
				accountProductId);

		if (memberAccountId != null)
			return true;

		return false;
	}

	public static boolean hasAccount(String accountCode, String payrollNo) {
		// TODO Check of a member , given payroll number has an account of the
		// product given
		Long accountProductId = LoanUtilities.getAccountProductGivenCodeId(
				accountCode).getLong("accountProductId");

		Long partyId = LoanUtilities.getMemberId(payrollNo);

		Long memberAccountId = LoanUtilities
				.getMemberAccountIdFromMemberAccount(partyId, accountProductId);

		if (memberAccountId != null)
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when Jun 11, 2015 2:31:10 PM
	 * 
	 *         Get Card Status given Card Name
	 * */
	public static Long getCardStatusId(String name) {
		List<GenericValue> cardStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardStatusELI = delegator.findList("CardStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long cardStatusId = 0L;
		for (GenericValue genericValue : cardStatusELI) {
			cardStatusId = genericValue.getLong("cardStatusId");
		}

		return cardStatusId;
	}

	public static String getCardName(Long cardStatusId) {
		GenericValue cardStatus = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardStatus = delegator.findOne("CardStatus",
					UtilMisc.toMap("cardStatusId", cardStatusId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return cardStatus.getString("name");
	}

	public static BigDecimal getMinimumBalance(Long memberAccountId) {

		GenericValue accountProduct = getAccountProductGivenMemberAccountId(memberAccountId);

		BigDecimal mimimumBalance = BigDecimal.ZERO;

		if (accountProduct.getBigDecimal("minBalanceAmt") != null)
			mimimumBalance = accountProduct.getBigDecimal("minBalanceAmt");

		return mimimumBalance;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 9:26:04 AM Get Member Status ID
	 *         given name
	 * */
	public static Long getMemberStatusId(String statusName) {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name", statusName), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberStatusId = null;
		for (GenericValue genericValue : memberStatusELI) {
			memberStatusId = genericValue.getLong("memberStatusId");
		}

		return memberStatusId;
	}

	/****
	 * @author Japheth Odonya  @when Jun 19, 2015 12:45:29 PM
	 * Member Names
	 * */
	public static String getMemberNameGivenLoanApplicationId(
			Long loanApplicationId) {

		String memberNames = "";

		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue member = getMember(loanApplication.getLong("partyId"));

		if (member == null)
			return "";

		if ((member.getString("firstName") != null)
				&& (!member.getString("firstName").equals(""))) {
			memberNames = memberNames + member.getString("firstName") + " ";
		}

		if ((member.getString("middleName") != null)
				&& (!member.getString("middleName").equals(""))) {
			memberNames = memberNames + member.getString("middleName") + " ";
		}

		if ((member.getString("lastName") != null)
				&& (!member.getString("lastName").equals(""))) {
			memberNames = memberNames + member.getString("lastName") + " ";
		}
		return memberNames;
	}

	/****
	 * @author Japheth Odonya  @when Jun 19, 2015 12:45:10 PM
	 * 
	 * Member Number
	 * **/
	public static String getMemberNumberGivenLoanApplicationId(
			Long loanApplicationId) {
		String memberNumber = "";

		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue member = getMember(loanApplication.getLong("partyId"));

		if (member == null)
			return "";

		if ((member.getString("memberNumber") != null) && (!member.getString("memberNumber").equals(""))) {
			memberNumber = member.getString("memberNumber");
		}
		
		return memberNumber;
	}

	
	/***
	 * @author Japheth Odonya  @when Jun 19, 2015 12:44:55 PM
	 * 
	 * Payroll Number
	 * */
	public static String getPayrollNumberGivenLoanApplicationId(
			Long loanApplicationId) {
		String payrollNumber = "";

		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue member = getMember(loanApplication.getLong("partyId"));

		if (member == null)
			return "";

		if ((member.getString("payrollNumber") != null) && (!member.getString("payrollNumber").equals(""))) {
			payrollNumber = member.getString("payrollNumber");
		}
		
		return payrollNumber;
	}
	
	/***
	 * @author Japheth Odonya  @when Jun 19, 2015 12:44:40 PM
	 * 
	 * Mobile Number
	 * */
	public static String getMobileNumberGivenLoanApplicationId(
			Long loanApplicationId) {
		String mobileNumber = "";

		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue member = getMember(loanApplication.getLong("partyId"));

		if (member == null)
			return "";

		if ((member.getString("mobileNumber") != null) && (!member.getString("mobileNumber").equals(""))) {
			mobileNumber = member.getString("mobileNumber");
		}
		
		return mobileNumber;
	}

	public static String getMemberStationName(Long loanApplicationId) {
		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);

		GenericValue member = getMember(loanApplication.getLong("partyId"));

		if (member == null)
			return "";
		
		Long stationId = member.getLong("stationId");
		
		return getStationName(getStation(stationId.toString()).getString("employerCode"));
		
	}

	public static BigDecimal getLoanAmountGivenLoanApplicationId(
			Long loanApplicationId) {
		return getLoanAmount(loanApplicationId);
	}

	public static BigDecimal getShareCapitalAmount(Long partyId) {
		Long accountProductId = getAccountProductGivenCodeId(AccHolderTransactionServices.SHARE_CAPITAL_CODE).getLong("accountProductId");
		BigDecimal bdShareCapital = AccHolderTransactionServices.getAccountTotalBalance(accountProductId, partyId);
		bdShareCapital = bdShareCapital.setScale(2, RoundingMode.HALF_DOWN);
		return bdShareCapital;
	}

	public static BigDecimal getMemberDepositAmount(Long partyId) {
		Long accountProductId = getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getLong("accountProductId");
		BigDecimal bdMemberDeposit = AccHolderTransactionServices.getAccountTotalBalance(accountProductId, partyId);
		bdMemberDeposit = bdMemberDeposit.setScale(2, RoundingMode.HALF_DOWN);
		return bdMemberDeposit;
	}

	/****
	 * @author Japheth Odonya  @when Jun 23, 2015 10:56:41 PM
	 * 
	 * Given an ATM Card find the current status ID
	 * */
	public static Long getCurrentCardStatusId(Long cardApplicationId) {
		// TODO Auto-generated method stub
		
		GenericValue cardApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardApplication = delegator.findOne("CardApplication",
					UtilMisc.toMap("cardApplicationId", cardApplicationId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (cardApplication == null)
			return null;
		
		return cardApplication.getLong("cardStatusId");
	}

	
	/****
	 * @author Japheth Odonya  @when Jun 24, 2015 1:09:27 AM
	 * 
	 * Check that transactionType for accountProductId does exist
	 * */
	public static boolean existsCharges(String transactionType, Long accountProductId) {
		
		EntityConditionList<EntityExpr> accountProductChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("transactionType", EntityOperator.EQUALS,
						transactionType)

				), EntityOperator.AND);

		List<GenericValue> accountProductChargeELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductChargeELI = delegator.findList("AccountProductCharge",
					accountProductChargeConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((accountProductChargeELI != null) && (accountProductChargeELI.size() > 0)){
			return true;
		}
		
		return false;
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jun 28, 2015 1:05:25 PM
	 * 
	 * Get the Bank GL account ID given the finAccountId
	 * */
	public static String getBankglAccountId(String finAccountId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue finAccount = null;
		try {
			finAccount = delegator.findOne("FinAccount",
					UtilMisc.toMap("finAccountId", finAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (finAccount == null)
			return null;
		
		return finAccount.getString("postToGlAccountId");
	}

	/***
	 * @author Japheth Odonya  @when Jun 28, 2015 7:08:59 PM
	 * 
	 * Returns the Member Class ID given the class name
	 * 
	 * */
	public static Long getMemberClassId(String name) {
		List<GenericValue> memberClassELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberClassELI = delegator.findList("MemberClass",
					EntityCondition.makeCondition("name", name), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberClassId = null;
		for (GenericValue genericValue : memberClassELI) {
			memberClassId = genericValue.getLong("memberClassId");
		}

		return memberClassId;
	}

	/**
	 * @author Japheth Odonya  @when Jun 28, 2015 7:08:34 PM
	 * 
	 * Returns the Share Minimum Entity
	 * 
	 * **/
	public static GenericValue getShareMinimumEntity(Long memberClassId) {
		List<GenericValue> shareMinimumELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			shareMinimumELI = delegator.findList("ShareMinimum",
					EntityCondition.makeCondition("memberClassId", memberClassId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue shareMinimum = null;
		for (GenericValue genericValue : shareMinimumELI) {
			shareMinimum = genericValue;
		}
		
		return shareMinimum;
	}

	/***
	 * @author Japheth Odonya  @when Jun 30, 2015 12:16:18 AM
	 * 
	 * Get Loan Guarantor Entity
	 * */
	public static GenericValue getLoanGuarantorEntity(Long loanGuarantorId) {
		GenericValue loanGuarantor = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantor = delegator.findOne("LoanGuarantor",
					UtilMisc.toMap("loanGuarantorId", loanGuarantorId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanGuarantor;
	}

	/**
	 * @author Japheth Odonya  @when Jun 30, 2015 12:28:37 AM
	 * 
	 * 
	 * Get total deposits this loan guarantors less this guarantor attempting to be deleted
	 * */
	public static BigDecimal getTotalGuarantorDepositsWithoutThisGuarantor(
			Long loanGuarantorId, Long loanApplicationId) {
		
		EntityConditionList<EntityExpr> loanGuarantorConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId),

				EntityCondition.makeCondition("loanGuarantorId", EntityOperator.NOT_EQUAL,
						loanGuarantorId)

				), EntityOperator.AND);

		List<GenericValue> loanGuarantorELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					loanGuarantorConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal bdTotalDepositAmount = BigDecimal.ZERO;
		Long accountProductId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getLong("accountProductId");
		
		for (GenericValue genericValue : loanGuarantorELI) {
			Long memberAccountId = LoanUtilities.getMemberAccountIdFromMemberAccount(genericValue.getLong("guarantorId"), accountProductId);
			
			bdTotalDepositAmount = bdTotalDepositAmount.add(AccHolderTransactionServices.getAvailableBalanceVer3(memberAccountId.toString()));
		}

		
		return bdTotalDepositAmount;
	}

	public static BigDecimal getShareCapitalAccountBalance(Long partyId) {
		
		BigDecimal bdShareCapitalBalance = BigDecimal.ZERO;
		Long accountProductId = getAccountProductIdGivenCodeId(AccHolderTransactionServices.SHARE_CAPITAL_CODE);
		
		Long memberAccountId = getMemberAccountIdFromMemberAccount(partyId, accountProductId);
		
		bdShareCapitalBalance = AccHolderTransactionServices.getBookBalanceNow(memberAccountId.toString());
		return bdShareCapitalBalance;
	}

	public static BigDecimal getShareCapitalLimit(Long partyId) {
		GenericValue member = LoanUtilities.getMember(partyId);
		
		Long memberClassId = member.getLong("memberClassId");
		
		if (memberClassId == null){
			memberClassId = LoanUtilities.getMemberClassId("Class A");
		}
		
		GenericValue shareMinimum = getShareMinimumEntity(memberClassId);
		
		if (shareMinimum == null)
			return null;
		
		
		return shareMinimum.getBigDecimal("minShareCapital");
	}

	public static String getMemberAccountIdGivenMemberAndAccountCode(
			Long partyId, String code) {
		Long accountProductId = getAccountProductIdGivenCodeId(code);
		
		Long memberAccountId = getMemberAccountIdFromMemberAccount(partyId, accountProductId);

		return memberAccountId.toString();
	}

	public static GenericValue getProductChargeEntity(String name) {
		GenericValue productCharge = null;

		List<GenericValue> productChargeELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			productChargeELI = delegator.findList("ProductCharge",
					EntityCondition.makeCondition("name", name),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : productChargeELI) {
			productCharge = genericValue;
		}

		return productCharge;
	}

	public static String getMemberDepositsGLAccountId() {
		String glAccountId = getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getString("glAccountId");
		return glAccountId;
	}

	public static String getLoanReceivableAccountId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String getTransactionGLAccountGivenTransactionName(
			String transactionName) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							transactionName), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (accountHolderTransactionSetup == null)
			return null;
		return accountHolderTransactionSetup.getString("memberDepositAccId");
	}

	public static String getSavingsAccountglAccountId() {
		String glAccountId = getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE).getString("glAccountId");
		return glAccountId;
	}
	
	public static GenericValue getEntityValue(String entityName, String primaryKeyName, Long primaryKeyValue){
		GenericValue entity = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			entity = delegator.findOne(entityName,
					UtilMisc.toMap(primaryKeyName, primaryKeyValue), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return entity;
	}
	
	public static GenericValue getEntityValue(String entityName, String primaryKeyName, String primaryKeyValue){
		GenericValue entity = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			entity = delegator.findOne(entityName,
					UtilMisc.toMap(primaryKeyName, primaryKeyValue), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return entity;
	}
	
	
	/****
	 * @author Japheth Odonya  @when Jul 10, 2015 2:59:35 PM
	 * Get loan balance remittance code
	 * */
	public static String getLoanBalanceRemittanceCode(String loanNo)
	{
		String remitanceCode = "";
		if (loanNo == null)
			return "";
		
		if (loanNo.equals(""))
			return "";
				
		if (loanNo.equals("0"))
			return "";
		GenericValue loanApplication = getLoanApplicationEntityGivenLoanNo(loanNo);
		
		Long loanProductId = loanApplication.getLong("loanProductId");
		
		GenericValue loanProduct = getLoanProduct(loanProductId);
		
		remitanceCode = loanProduct.getString("code");
		remitanceCode = remitanceCode+"D";
		
		log.info("RRRRRRRRRRRRRRREEEEEEEEEEEEEMMMMMMMMMMMM LOAN NO is "+loanNo);
		log.info("RRRRRRRRRRRRRRREEEEEEEEEEEEEMMMMMMMMMMMM code is "+remitanceCode);
		return remitanceCode;
	}


}
