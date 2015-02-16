package org.ofbiz.loans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class LoanServices {
	public static Logger log = Logger.getLogger(LoanServices.class);

	public static String getLoanDetails(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		Long loanProductId = Long.valueOf(request.getParameter("loanProductId")
				.replaceAll(",", ""));
		GenericValue loanProduct = null;

		// SaccoProduct
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Product Details";
		}

		if (loanProduct != null) {
			result.put("interestRatePM", loanProduct.get("interestRatePM"));
			result.put("maxRepaymentPeriod",
					loanProduct.get("maxRepaymentPeriod"));
			result.put("maximumAmt", loanProduct.get("maximumAmt"));
			result.put("multipleOfSavingsAmt",
					loanProduct.get("multipleOfSavingsAmt"));

			// result.put("selectedRepaymentPeriod",
			// saccoProduct.get("selectedRepaymentPeriod"));
		} else {
			System.out.println("######## Product details not found #### ");
		}
		// return JSONBuilder.class.
		// JSONObject root = new JSONObject();

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	public static String getMember(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		// Delegator delegator = dctx.getDelegator();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// request.getParameter(arg0)
		String partyId = (String) request.getParameter("memberId");
		// String loanProductId = (String)
		// request.getParameter("loanProductId");

		// Locale locale = (Locale) request.getParameter("locale");
		GenericValue member = null;
		// String firstName, middleName, lastName, idNumber, memberType,
		// memberNumber, mobileNumber;
		String memberDetails = "";
		BigDecimal bdDepositamt = BigDecimal.ZERO;

		// bdDepositamt = totalSavings(partyId, loanProductId, delegator);
		bdDepositamt = totalMemberDepositSavings(partyId, delegator);
		partyId = partyId.replaceAll(",", "");
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			// return ServiceUtil.returnError(UtilProperties.getMessage("",
			// "Cannot Get Member Details",
			// UtilMisc.toMap("errMessage", e.getMessage()), locale));
			return "Cannot Get Member Details";
		}

		if (member != null) {
			result.put("firstName", member.get("firstName"));
			result.put("middleName", member.get("middleName"));
			result.put("lastName", member.get("lastName"));
			result.put("idNumber", member.get("idNumber"));
			result.put("memberType", member.get("memberType"));
			result.put("memberNumber", member.get("memberNumber"));
			result.put("mobileNumber", member.get("mobileNumber"));

			result.put("payrollNo", member.get("payrollNumber"));
			result.put("memberNo", member.get("memberNumber"));
			result.put("memberClass", member.get("memberClass"));

			result.put("payrolNo", member.get("payrollNumber"));
			result.put("currentStationId", member.get("stationId"));
			result.put("depositamt", bdDepositamt);

			Date joinDate = member.getDate("joinDate");
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			String strJoinDate = format1.format(joinDate);
			result.put("joinDate", strJoinDate);

			SimpleDateFormat formattedDateInput = new SimpleDateFormat(
					"yyyy-MM-dd");
			String dateInputJoinDate = formattedDateInput.format(joinDate);
			result.put("inputDate", dateInputJoinDate);
			// SimpleDateFormat

			// Calculate Date Duration from Join Date to Now
			int membershipDuration = getMemberDurations(joinDate);

			result.put("membershipDuration", membershipDuration);
		} else {
			System.out.println("######## Member details not found #### ");
		}
		// return JSONBuilder.class.
		// JSONObject root = new JSONObject();

		Gson gson = new Gson();
		String json = gson.toJson(result);

		System.out.println("json = " + json);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;

	}

	/**
	 * Get Loan Max Amount
	 * */
	public static String getLoanMaxAmount(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanProductId = (String) request.getParameter("loanProductId");
		String memberId = (String) request.getParameter("memberId");
		GenericValue loanProduct = null;
		loanProductId = loanProductId.replaceAll(",", "");

		// Get Total Savings for the Member (Get total for each of their savings
		// account)
		BigDecimal bdMaximumLoanAmt = BigDecimal.ZERO;
		BigDecimal bdExistingLoans;
		BigDecimal bdTotalSavings = BigDecimal.ZERO;
		// Get get Loan Product
		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Product Details";
		}
		BigDecimal savingsMultiplier = BigDecimal.ZERO;
		result.put("maxLoanAmt", BigDecimal.ZERO);
		if (loanProduct != null) {

			if (loanProduct.getBigDecimal("multipleOfSavingsAmt") != null) {
				savingsMultiplier = loanProduct
						.getBigDecimal("multipleOfSavingsAmt");
			}

			memberId = memberId.replaceAll(",", "");
			Long partyId = Long.valueOf(memberId);
			bdExistingLoans = calculateExistingLoansTotal(partyId);

			// Get Existing Loans
			// if this is a loan based on Multiples of Account Product then
			if (loanProduct.getString("multipleOfSavings").equals("Yes")) {
				// bdExistingLoans = calculateExistingLoansTotal(memberId,
				// loanProductId, delegator);
				bdTotalSavings = totalSavings(memberId, loanProductId,
						delegator);
				// bdMaximumLoanAmt =
				// (bdTotalSavings.multiply(savingsMultiplier))
				// .subtract(bdExistingLoans);
				Long accountProductId = loanProduct.getLong("accountProductId");
				bdMaximumLoanAmt = calculateMaximumAmount(partyId,
						accountProductId, savingsMultiplier, bdTotalSavings);
			} else {

				// bdExistingLoans = calculateExistingAccountLessLoansTotal(
				// memberId, loanProductId, delegator);
				BigDecimal bdTotalLoanBalanceThisProduct = getTotalLoansOfThisType(
						partyId, loanProductId);
				bdMaximumLoanAmt = loanProduct.getBigDecimal("maximumAmt")
						.subtract(bdTotalLoanBalanceThisProduct);
			}

			// BigDecimal dbTotalPrincipalRepaid = LoanRepayments
			// .getTotalPrincipalPaid(memberId);
			// bdMaximumLoanAmt = bdMaximumLoanAmt.add(dbTotalPrincipalRepaid);
			// else if it is not based on multiples of Account Product the

			// Check if account has retainer

			log.info("############ The total Savings are ####### "
					+ bdTotalSavings);
			log.info("############ The Existing Loans are ####### "
					+ bdExistingLoans);
			log.info("############ The Maximum Amount is ####### "
					+ bdMaximumLoanAmt);
			// bdMaximumLoanAmt = bdMaximumLoanAmt.subtract(bdExistingLoans);
			if (bdMaximumLoanAmt.compareTo(new BigDecimal(0)) == -1) {
				bdMaximumLoanAmt = BigDecimal.ZERO;
			}
			result.put("maxLoanAmt", bdMaximumLoanAmt);
			result.put("existingLoans", bdExistingLoans);

		} else {
			System.out.println("######## Product details not found #### ");

			result.put("maxLoanAmt", bdMaximumLoanAmt);
		}

		// Get the multiplier for the Loan Savings

		// Multiple the Total Savings by the Multiplier

		// Return the Multiple as the Maximum of the Loan/Product

		// SaccoProduct

		// return JSONBuilder.class.
		// JSONObject root = new JSONObject();

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	/***
	 * Calculate total balance for Loans of this type
	 * */

	private static BigDecimal getTotalLoansOfThisType(Long partyId,
			String loanProductId) {

		BigDecimal bdTotal = BigDecimal.ZERO;
		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, loanStatusId),

				EntityCondition.makeCondition("loanProductId",
						EntityOperator.EQUALS, Long.valueOf(loanProductId))

				), EntityOperator.AND);

		// EntityOperator._emptyMap
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);

			BigDecimal bdLoanRepaid = getLoansRepaidByLoanApplicationId(genericValue
					.getLong("loanApplicationId"));
			BigDecimal bdLoanBalance = genericValue.getBigDecimal("loanAmt")
					.subtract(bdLoanRepaid);
			bdTotal = bdTotal.add(bdLoanBalance);
		}

		return bdTotal;
	}

	private static BigDecimal calculateMaximumAmount(Long partyId,
			Long accountProductId, BigDecimal savingsMultiplier,
			BigDecimal bdTotalSavings) {
		// TODO Auto-generated method stub

		BigDecimal bdMaxAmount = BigDecimal.ZERO;

		BigDecimal bdHeldDepositsTotalAmount = BigDecimal.ZERO;
		// Get all the loans

		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		// if (loanApplicationELI != null)
		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);
			GenericValue localLoanProduct = getLoanProduct(genericValue
					.getLong("loanProductId"));

			// if (genericValue.get)
			if ((localLoanProduct != null)
					&& (localLoanProduct.getLong("accountProductId") != null)
					&& (localLoanProduct.getLong("accountProductId")
							.equals(accountProductId))) {

				// GenericValue localAccountProduct =
				// getAccountProduct(localLoanProduct.getLong("accountProductId"));

				if ((localLoanProduct.getBigDecimal("multipleOfSavingsAmt") != null)
						&& (localLoanProduct.getBigDecimal(
								"multipleOfSavingsAmt").compareTo(
								BigDecimal.ZERO) == 1)) {
					BigDecimal bdLoanRepaid = getLoansRepaidByLoanApplicationId(genericValue
							.getLong("loanApplicationId"));
					BigDecimal bdLoanBalance = genericValue.getBigDecimal(
							"loanAmt").subtract(bdLoanRepaid);
					BigDecimal bdHeldAmount = BigDecimal.ZERO;
					bdHeldAmount = bdLoanBalance.divide(localLoanProduct
							.getBigDecimal("multipleOfSavingsAmt"), 4,
							RoundingMode.HALF_UP);
					bdHeldDepositsTotalAmount = bdHeldDepositsTotalAmount
							.add(bdHeldAmount);
				}
			}

			// existingLoansTotal = existingLoansTotal.add(bdLoanBalance);
		}

		BigDecimal bdFreeDepositsTotalAmt = bdTotalSavings
				.subtract(bdHeldDepositsTotalAmount);
		bdMaxAmount = bdFreeDepositsTotalAmt.multiply(savingsMultiplier);

		bdMaxAmount = bdMaxAmount.setScale(4, RoundingMode.HALF_UP);

		return bdMaxAmount;
	}

	private static GenericValue getAccountProduct(Long accountProductId) {
		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return accountProduct;
	}

	private static GenericValue getLoanProduct(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return loanProduct;
	}

	private static GenericValue getLoanApplication(Long loanApplicationId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return loanApplication;
	}

	private static BigDecimal totalSavings(String memberId,
			String loanProductId, Delegator delegator) {
		// Get the multiplier account - the account being used to get the
		// maximum loan as defined in the
		// Loan Product Setup
		loanProductId = loanProductId.replaceAll(",", "");
		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// The Multiplier account id
		String accountProductId = loanProduct.getString("accountProductId");

		// Get Accounts for this member
		List<GenericValue> memberAccountELI = null;
		accountProductId = accountProductId.replaceAll(",", "");

		memberId = memberId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)), EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						Long.valueOf(accountProductId))), EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					accountsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (memberAccountELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdTotalSavings = BigDecimal.ZERO;
		for (GenericValue genericValue : memberAccountELI) {
			// result.put(genericValue.get("bankBranchId").toString(),
			// genericValue.get("branchName"));
			// Compute the total Savings for this account
			bdTotalSavings = bdTotalSavings.add(AccHolderTransactionServices
					.getTotalSavings(genericValue.get("memberAccountId")
							.toString(), delegator));
		}
		// sum up all the savings
		return bdTotalSavings;
	}

	public static BigDecimal getTotalMemberDeposits(Long partyId) {
		BigDecimal bdTotalDeposit = BigDecimal.ZERO;
		// Delegator delegator =
		// bdTotalDeposit =
		// memberId = memberId.replaceAll(",", "");
		String memberId = String.valueOf(partyId);
		memberId = memberId.replaceAll(",", "");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		bdTotalDeposit = totalMemberDepositSavings(memberId, delegator);
		return bdTotalDeposit;
	}

	/***
	 * Get total deposit savings
	 * 
	 * */
	private static BigDecimal totalMemberDepositSavings(String memberId,
			Delegator delegator) {
		// Get the multiplier account - the account being used to get the
		// maximum loan as defined in the
		// Loan Product Setup
		memberId = memberId.replaceAll(",", "");
		// Get Accounts for this member
		List<GenericValue> memberAccountELI = null;
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)), EntityCondition.makeCondition(
						"withdrawable", EntityOperator.EQUALS, "No")),
						EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					accountsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (memberAccountELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdTotalSavings = BigDecimal.ZERO;
		// BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : memberAccountELI) {
			// result.put(genericValue.get("bankBranchId").toString(),
			// genericValue.get("branchName"));
			// Compute the total Savings for this account
			bdTotalSavings = bdTotalSavings.add(AccHolderTransactionServices
					.getTotalSavings(genericValue.get("memberAccountId")
							.toString(), delegator));

			// bdOpeningBalance =
			// bdOpeningBalance.add(getTotalOpeningBalance(memberId,
			// genericValue.get("memberAccountId")
			// .toString(), delegator));
		}
		// sum up all the savings
		// bdTotalSavings = bdTotalSavings.add(bdOpeningBalance);
		return bdTotalSavings;
	}

	private static BigDecimal getTotalOpeningBalance(String memberId,
			String memberAccountId, Delegator delegator) {
		List<GenericValue> memberAccountDetailsELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("memberAccountId",
								EntityOperator.EQUALS,
								Long.valueOf(memberAccountId))),
						EntityOperator.AND);

		try {
			memberAccountDetailsELI = delegator.findList(
					"MemberAccountDetails", accountsConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (memberAccountDetailsELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdTotalOpeningBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : memberAccountDetailsELI) {
			bdTotalOpeningBalance = bdTotalOpeningBalance.add(genericValue
					.getBigDecimal("savingsOpeningBalance"));

		}
		return bdTotalOpeningBalance;
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

	public static int getMemberDuration(String partyId) {
		int durationInMonths = 0;

		// Get Member
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Date joinDate = member.getDate("joinDate");
		// Date dateJoinDate = new Date(joinDate.getTime());
		durationInMonths = getMemberDurations(joinDate);

		return durationInMonths;
	}

	/**
	 * Calculate Existing Loans Total
	 * **/
	public static BigDecimal calculateExistingLoansTotal(String memberId,
			String loanProductId, Delegator delegator) {
		BigDecimal existingLoansTotal = BigDecimal.ZERO;

		if ((loanProductId != null)
				&& (!loanProductId.equals(null))
				&& (!loanProductId.equals(""))
				&& (!loanProductId.equals("NULL") && (!loanProductId
						.equals("null")))) {
			log.info("PPPPPPPPP  The product ID is ### " + loanProductId);

		}

		// else {
		// accountProductId = getShareDepositAccountId("901");
		// }

		// EntityCondition.makeCondition(
		// "accountProductId", EntityOperator.EQUALS,
		// Long.valueOf(accountProductId)),

		memberId = memberId.replaceAll(",", "");
		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		// accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		if (loanApplicationELI != null)
			for (GenericValue genericValue : loanApplicationELI) {
				// toDeleteList.add(genericValue);

				BigDecimal bdLoanRepaid = getLoansRepaidByLoanApplicationId(genericValue
						.getLong("loanApplicationId"));
				BigDecimal bdLoanBalance = genericValue
						.getBigDecimal("loanAmt").subtract(bdLoanRepaid);
				existingLoansTotal = existingLoansTotal.add(bdLoanBalance);
			}
		log.info("##########MMMMMMMMMMM Member #######" + memberId);
		// log.info("##########AAAAAAAAAAA Account #######" + accountProductId);
		log.info("##########SSSSSSSSSSS Status ID #######" + loanStatusId);
		log.info("########## Counting Existing Loans #######"
				+ loanApplicationELI.size());
		return existingLoansTotal;
	}

	/***
	 * Calculate Existing Loans Totals - knowing memberId alone
	 * 
	 * */
	public static BigDecimal calculateExistingLoansTotal(Long partyId) {
		BigDecimal existingLoansTotal = BigDecimal.ZERO;

		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		if (loanApplicationELI != null)
			for (GenericValue genericValue : loanApplicationELI) {
				// toDeleteList.add(genericValue);

				BigDecimal bdLoanRepaid = getLoansRepaidByLoanApplicationId(genericValue
						.getLong("loanApplicationId"));
				BigDecimal bdLoanBalance = genericValue
						.getBigDecimal("loanAmt").subtract(bdLoanRepaid);
				existingLoansTotal = existingLoansTotal.add(bdLoanBalance);
			}
		log.info("##########MMMMMMMMMMM Member #######" + partyId);
		log.info("##########SSSSSSSSSSS Status ID #######" + loanStatusId);
		log.info("########## Counting Existing Loans #######"
				+ loanApplicationELI.size());
		return existingLoansTotal;
	}

	public static BigDecimal getTotalDisbursedLoans(Long partyId) {
		BigDecimal bdDisbursedLoansTotal = BigDecimal.ZERO;

		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {

			bdDisbursedLoansTotal = bdDisbursedLoansTotal.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		return bdDisbursedLoansTotal;
	}

	public static String getShareDepositAccountId(String code) {
		// TODO Auto-generated method stub
		List<GenericValue> accountProductELI = null; // =
		String accountProductId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : accountProductELI) {
			accountProductId = String.valueOf(genericValue
					.getLong("accountProductId"));
		}

		return accountProductId;
	}

	/***
	 * Total Loans for Loans without product
	 * 
	 * */
	/**
	 * Calculate Existing Loans Total
	 * **/
	public static BigDecimal calculateExistingAccountLessLoansTotal(
			String memberId, String loanProductId, Delegator delegator) {
		BigDecimal existingLoansTotal = BigDecimal.ZERO;

		// The Multiplier account id
		// String accountProductId = loanProduct.getString("accountProductId");

		// EntityCondition.makeCondition(
		// "accountProductId", EntityOperator.EQUALS, null),
		Long loanStatusId = getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		memberId = memberId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)), EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS, loanStatusId)),
						EntityOperator.AND);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);
			System.out.println("GGGGGGGGG Got value GGGGGGGG "
					+ genericValue.getBigDecimal("loanAmt"));
			existingLoansTotal = existingLoansTotal.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		return existingLoansTotal;
	}

	public static Timestamp calculateLoanRepaymentStartDate(
			GenericValue loanApplication) {

		Map<String, Object> result = FastMap.newInstance();
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");// (String)context.get("loanApplicationId");
		log.info("What we got is ############ " + loanApplicationId);

		Delegator delegator;
		// delegator = D
		// ctx.getDelegator();

		// delegator = DelegatorFactoryImpl.getDelegator("delegator");
		delegator = loanApplication.getDelegator();
		// GenericValue accountTransaction = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get Salary processing day
		Timestamp repaymentStartDate = getProcessingDate(delegator);

		// loanApplication.set("monthlyRepayment", paymentAmount);
		loanApplication.set("repaymentStartDate", repaymentStartDate);
		log.info("##### End Date is ######## " + repaymentStartDate);
		log.info("##### ID is  ######## " + loanApplicationId);
		loanApplication.set("repaymentStartDate", repaymentStartDate);

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("repaymentStartDate", repaymentStartDate);
		return repaymentStartDate;
	}

	private static Timestamp getProcessingDate(Delegator delegator) {
		List<GenericValue> salaryProcessingDateELI = null; // =
		Long processingDay = 0l;
		try {
			salaryProcessingDateELI = delegator.findList(
					"SalaryProcessingDate", null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : salaryProcessingDateELI) {
			processingDay = genericValue.getLong("processingDay");
		}

		log.info("##### Salary Processing Day is ######## " + processingDay);

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		Timestamp repaymentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		;
		LocalDateTime localDateCurrent = new LocalDateTime(
				currentDate.getTime());
		LocalDateTime localDateRepaymentDate = new LocalDateTime(
				repaymentDate.getTime());

		if (localDateCurrent.getDayOfMonth() < processingDay.intValue()) {
			// Repayment Date is Beginning of Next Month
			localDateRepaymentDate = localDateRepaymentDate.plusMonths(1);
			// localDateRepaymentDate = localDateRepaymentDate.getD
			// DateMidnight firstDay = new DateMidnight().withDayOfMonth(1);
			DateTime startOfTheMonth = new DateTime().dayOfMonth()
					.withMinimumValue().withTimeAtStartOfDay();
			DateTime startOfNextMonth = startOfTheMonth.plusMonths(1)
					.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			repaymentDate = new Timestamp(startOfNextMonth.toLocalDate()
					.toDate().getTime());
		} else {
			// from 15th and beyond then start paying two months later
			DateTime startOfTheMonth = new DateTime().dayOfMonth()
					.withMinimumValue().withTimeAtStartOfDay();
			DateTime startOfNextMonth = startOfTheMonth.plusMonths(2)
					.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			repaymentDate = new Timestamp(startOfNextMonth.toLocalDate()
					.toDate().getTime());
		}

		return repaymentDate;

	}

	/**
	 * @author Japheth Odonya @when Aug 20, 2014 7:07:07 PM Add Charges for the
	 *         Product to the Product on application
	 * **/
	public static String addCharges(GenericValue loanApplication,
			Map<String, String> context) {

		// Map<String, Object> result = FastMap.newInstance();
		String loanProductId = loanApplication.getString("loanProductId");
		String partyId = (String) context.get("userLoginId");
		log.info("The Loan Product ID is ############ " + loanProductId);
		log.info("The Party ID is ############ " + partyId);
		log.info("CCC Creating Charges ############ for "
				+ loanApplication.getBigDecimal("loanAmt"));

		Delegator delegator;
		delegator = loanApplication.getDelegator();

		// Get the Charges attached to the LoanProduct
		// GenericValue loanApplicationCharge = null;
		List<GenericValue> loanProductChargeELI = null;
		// List<GenericValue> listLoanApplicationCharge = new
		// ArrayList<GenericValue>();

		// Get only Upfront like negotiation fee, the other charges like
		// insurance will be part of the
		// amortization schedule
		loanProductId = loanProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> chargesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanProductId", EntityOperator.EQUALS,
						Long.valueOf(loanProductId)), EntityCondition
						.makeCondition("chargedUpfront", EntityOperator.EQUALS,
								"Y")), EntityOperator.AND);

		try {
			loanProductChargeELI = delegator.findList("LoanProductCharge",
					chargesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanProductCharge : loanProductChargeELI) {
			log.info("AAAAAA Added a charge ############ for "
					+ loanApplication.getBigDecimal("loanAmt"));
			createLoanApplicationCharge(loanProductCharge, loanApplication,
					partyId);
		}
		return "";
	}

	/**
	 * Given a LoanApplication and LoanProductCharge add a LoanApplicationCharge
	 * to the Application.
	 * **/
	private static void createLoanApplicationCharge(
			GenericValue loanProductCharge, GenericValue loanApplication,
			String partyId) {
		// Create a Loan Application Charge
		String isFixed = loanProductCharge.getString("isFixed");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		BigDecimal bdRateAmount = loanProductCharge.getBigDecimal("rateAmount");
		BigDecimal bdFixedAmount = loanProductCharge
				.getBigDecimal("fixedAmount");
		Delegator delegator = loanApplication.getDelegator();
		log.info(" LLLLLLLLLLLLL Loan Amount " + bdLoanAmt);
		if (isFixed.equals("N")) {
			// Compute Fixed Amount

			bdFixedAmount = (bdLoanAmt.setScale(6)).multiply(
					bdRateAmount.setScale(6)).divide(new BigDecimal(100), 6,
					RoundingMode.HALF_UP);

			log.info("FFFFFFFFFF Fixed Amount " + bdFixedAmount);
		}

		GenericValue loanApplicationCharge;
		String loanApplicationChargeId;
		loanApplicationChargeId = delegator
				.getNextSeqId("LoanApplicationCharge");

		loanApplicationCharge = delegator.makeValidValue(
				"LoanApplicationCharge", UtilMisc.toMap(
						"loanApplicationChargeId", Long
								.valueOf(loanApplicationChargeId), "isActive",
						"Y", "createdBy", partyId, "loanApplicationId", Long
								.valueOf(loanApplication
										.getString("loanApplicationId")),
						"productChargeId", Long.valueOf(loanProductCharge
								.getString("productChargeId")), "isFixed",
						loanProductCharge.getString("isFixed"),
						"chargedUpfront", loanProductCharge
								.getString("chargedUpfront"),

						"rateAmount", bdRateAmount, "fixedAmount",
						bdFixedAmount));
		try {
			delegator.createOrStore(loanApplicationCharge);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:19:07 PM Generates Percentage
	 *         of the Total Loan for each Gurantor (Equally Distributed)
	 *         guaranteedPercentage guaranteedValue
	 * */
	public static String generateGuarantorPercentages(GenericValue loanGuarantor) {
		String loanApplicationId = loanGuarantor.getString("loanApplicationId");
		Delegator delegator = loanGuarantor.getDelegator();
		// Get Loan Amount
		BigDecimal bdLoanAmt = getLoanAmount(delegator, loanApplicationId);
		// Get Number of Guarantors
		Integer guarantersCount = getNumberOfGuarantors(delegator,
				loanApplicationId);

		// Computer, Percentage and Value for each Gurantor
		BigDecimal bdGuaranteedPercentage = new BigDecimal(100).divide(
				new BigDecimal(guarantersCount), 6, RoundingMode.HALF_UP);
		BigDecimal bdGuaranteedValue = bdLoanAmt.divide(new BigDecimal(
				guarantersCount), 6, RoundingMode.HALF_UP);

		// Update Gurantors with the Value of bdGuaranteedPercentage and
		// bdGuaranteedValue

		updateGuarantors(bdGuaranteedPercentage, bdGuaranteedValue,
				loanApplicationId, delegator);

		return "";
	}

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:39:28 PM Update All the
	 *         Guarantor Records for this Application
	 * 
	 *         The Percentages are changing as Gauarantors increase
	 * */
	private static void updateGuarantors(BigDecimal bdGuaranteedPercentage,
			BigDecimal bdGuaranteedValue, String loanApplicationId,
			Delegator delegator) {
		List<GenericValue> loanGuarantorELI = null; // =
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdDepositamt = BigDecimal.ZERO;

		// bdDepositamt = totalSavings(partyId, loanProductId, delegator);

		List<GenericValue> listToBeUpdatedGuarantors = new LinkedList<GenericValue>();
		for (GenericValue genericValue : loanGuarantorELI) {
			bdDepositamt = totalMemberDepositSavings(
					genericValue.getLong("guarantorId").toString(), delegator);
			Long memberStationId = getMemberStationId(genericValue.getLong(
					"guarantorId").toString());
			genericValue.set("guaranteedPercentage", bdGuaranteedPercentage);
			genericValue.set("guaranteedValue", bdGuaranteedValue);
			genericValue.set("depositamt", bdDepositamt);
			genericValue.set("currentStationId", memberStationId);

			listToBeUpdatedGuarantors.add(genericValue);
		}

		// Save all the updated Guarantors
		try {
			delegator.storeAll(listToBeUpdatedGuarantors);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}

	private static Long getMemberStationId(String partyId) {
		// TODO Auto-generated method stub
		GenericValue member = null; // =
		partyId = partyId.replaceAll(",", "");

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Find Member");
			// return "Cannot Get Product Details";
		}

		if (member != null) {
			return member.getLong("stationId");
		}

		return 0L;
	}

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:27:20 PM Get the number of
	 *         guaranters for this Application TODO
	 * */
	private static Integer getNumberOfGuarantors(Delegator delegator,
			String loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Integer count = loanGuarantorELI.size();
		// for (GenericValue genericValue : loanGuarantorELI) {
		// count = count + 1;
		// }

		return count;
	}

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:25:18 PM Get Loan Amount
	 *         given loanApplicationId
	 * */
	private static BigDecimal getLoanAmount(Delegator delegator,
			String loanApplicationId) {

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
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

	/**
	 * @author Japheth Odonya @when Sep 4, 2014 6:39:44 PM Check if Loan has
	 *         Collateral
	 * **/
	public static String loanHasCollateral(String loanApplicationId) {
		// Map<String, Object> result = FastMap.newInstance();

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// GenericValue loanApplication;
		// try {
		// loanApplication = delegator.findOne("LoanApplication",
		// UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
		// } catch (GenericEntityException e) {
		// e.printStackTrace();
		// //return "Cannot Get Product Details";
		// }

		// Delegator delegator = loanApplication.getDelegator();
		// String loanApplicationId =
		// loanApplication.getString("loanApplicationId");

		List<GenericValue> loanApplicationCallateralELI = null; // =
		loanApplicationId = loanApplicationId.replaceAll(",", "");

		try {
			loanApplicationCallateralELI = delegator.findList(
					"LoanApplicationCallateral",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String collateralsAvailable = "";
		if ((loanApplicationCallateralELI == null)
				|| (loanApplicationCallateralELI.size() <= 0)) {
			collateralsAvailable = "N";
		} else {
			collateralsAvailable = "Y";
		}
		return collateralsAvailable;
	}

	/**
	 * @author Japheth Odonya @when Sep 4, 2014 12:45:52 AM
	 * 
	 *         Has Guarantors
	 * **/
	public static String loanHasGuarantors(String loanApplicationId) {
		// Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// String loanApplicationId =
		// loanApplication.getString("loanApplicationId");
		//
		List<GenericValue> loanGuarantorELI = null; // =
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String guarantorsAvailable = "";
		if ((loanGuarantorELI == null) || (loanGuarantorELI.size() <= 0)) {
			guarantorsAvailable = "N";
		} else {
			guarantorsAvailable = "Y";
		}
		return guarantorsAvailable;
	}

	/***
	 * @author Japheth Odonya @when Sep 4, 2014 6:43:32 PM Guarantor Totals
	 *         Equal or Greater than Loan Amount
	 * */
	public static String guarantorTotalsEqualLoanTotal(String loanApplicationId) {

		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			// return "Cannot Get Product Details";
		}

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		BigDecimal bdTotalDeposits = getGuarantorTotalDeposits(loanApplication);

		String guarantorsTotalDepositsEnough = "";

		if (bdTotalDeposits.compareTo(bdLoanAmt) == -1) {
			guarantorsTotalDepositsEnough = "N";
		} else {
			guarantorsTotalDepositsEnough = "Y";
		}
		return guarantorsTotalDepositsEnough;
	}

	/***
	 * Get Total Deposits for all Guarantors
	 * 
	 * */
	private static BigDecimal getGuarantorTotalDeposits(
			GenericValue loanApplication) {
		BigDecimal bdTotalDeposits = BigDecimal.ZERO;
		List<GenericValue> loanGuarantorELI = null;
		Delegator delegator = loanApplication.getDelegator();
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");

		loanApplicationId = loanApplicationId.replaceAll(",", "");

		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdDepositamt = null;
		for (GenericValue genericValue : loanGuarantorELI) {

			bdDepositamt = genericValue.getBigDecimal("depositamt");

			if (bdDepositamt != null) {
				bdTotalDeposits = bdTotalDeposits.add(bdDepositamt);
			}

		}

		return bdTotalDeposits;
	}

	/**
	 * @author Japheth Odonya @when Sep 4, 2014 7:15:54 PM Check that Each
	 *         Guarantor Deposit is greater than average loan amount (loan
	 *         amount / number of guarantors)
	 * **/
	public static String checkEachGuarantorDepositGreaterThanAverage(
			String loanApplicationId) {
		String eacherGuarantorGreaterThanAverage = "Y";

		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Integer noOfGuarantors = getNumberOfGuarantors(loanApplication);
		BigDecimal loanAmt = loanApplication.getBigDecimal("loanAmt");

		BigDecimal bdAverageLoanAmt = BigDecimal.ZERO;

		if (noOfGuarantors > 0) {
			bdAverageLoanAmt = loanAmt.divide(new BigDecimal(noOfGuarantors),
					6, RoundingMode.HALF_UP);
		} else {
			eacherGuarantorGreaterThanAverage = "N";
		}

		List<GenericValue> loanGuarantorELI = null;
		// Delegator delegator = loanApplication.getDelegator();
		// String loanApplicationId =
		// loanApplication.getString("loanApplicationId");
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdDepositAmt = null;

		for (GenericValue genericValue : loanGuarantorELI) {

			bdDepositAmt = genericValue.getBigDecimal("depositamt");

			// If Deposit Amount is not provided or is less that average of
			// loanAmount
			if ((bdDepositAmt == null)
					|| (bdDepositAmt.compareTo(bdAverageLoanAmt) == -1)) {
				eacherGuarantorGreaterThanAverage = "N";
			}

		}
		return eacherGuarantorGreaterThanAverage;
	}

	/***
	 * Count Guarantors
	 * */
	private static Integer getNumberOfGuarantors(GenericValue loanApplication) {
		List<GenericValue> loanGuarantorELI = null;
		Delegator delegator = loanApplication.getDelegator();
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");
		loanApplicationId = loanApplicationId.replaceAll(",", "");

		try {
			loanGuarantorELI = delegator.findList(
					"LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							Long.valueOf(loanApplicationId)), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanGuarantorELI != null) {
			return loanGuarantorELI.size();
		} else {
			return 0;
		}

	}

	/***
	 * @author Japheth Odonya @when Sep 5, 2014 12:10:03 AM
	 * 
	 *         collateralsAvailable guarantorsAvailable
	 *         guarantorsTotalDepositsEnough eacherGuarantorGreaterThanAverage
	 * 
	 * */
	public static String validateApplicationForm(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");

		String collateralsAvailable = loanHasCollateral(loanApplicationId);
		String guarantorsAvailable = loanHasGuarantors(loanApplicationId);
		String guarantorsTotalDepositsEnough = guarantorTotalsEqualLoanTotal(loanApplicationId);
		String eacherGuarantorGreaterThanAverage = checkEachGuarantorDepositGreaterThanAverage(loanApplicationId);

		result.put("collateralsAvailable", collateralsAvailable);
		result.put("guarantorsAvailable", guarantorsAvailable);
		result.put("guarantorsTotalDepositsEnough",
				guarantorsTotalDepositsEnough);
		result.put("eacherGuarantorGreaterThanAverage",
				eacherGuarantorGreaterThanAverage);

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	/***
	 * @author Japheth Odonya @when Sep 5, 2014 12:57:17 AM
	 *         forwardLoanApplication
	 * 
	 *         Forward Loan Application to Next Stage - to Review
	 * 
	 * */
	public static String forwardLoanApplication(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		// Map<String, String> userLogin = (Map<String,
		// String>)request.getAttribute("userLogin");
		// request.get
		// String userLoginId = userLogin.get("userLoginId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long oldStatusId = loanApplication.getLong("loanStatusId");
		Long newStatusId;
		String oldStatus = loanApplication.getString("applicationStatus");
		String newStatus, commentName;

		Long forwardedApprovalId = getLoanStatusId("FORWAREDAPPROVAL");
		Long approvedId = getLoanStatusId("APPROVED");

		if (oldStatusId.equals(forwardedApprovalId)) {
			newStatusId = approvedId;
			commentName = "Loan Approved by " + userLoginId;
		} else {
			newStatusId = forwardedApprovalId;
			commentName = "forwarded by " + userLoginId + " for approval ";
		}

		loanApplication.set("loanStatusId", newStatusId);
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Create a Log
		GenericValue loanStatusLog;
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", newStatusId,
				"createdBy", userLoginId, "comment", commentName));

		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return "success";
	}

	/***
	 * Sent to Loans
	 * 
	 * */
	public static String forwardLoanApplicationToLoans(
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String statusName = "FORWARDEDLOANS";
		Long loanStatusId = getLoanStatusId(statusName);

		loanApplication.set("loanStatusId", loanStatusId);
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// Create a Log
		GenericValue loanStatusLog;
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "forwarded to Loans"));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return "success";
	}

	// rejectLoanApplication
	public static String rejectLoanApplication(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String statusName = "REJECTED";
		Long loanStatusId = getLoanStatusId(statusName);

		loanApplication.set("loanStatusId", loanStatusId);
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// Create a Log
		GenericValue loanStatusLog;
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "forwarded to Loans"));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return "success";
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

	public static BigDecimal getLoansRepaid(Long memberId) {
		BigDecimal bdTotalRepaid = BigDecimal.ZERO;

		// Get total repayments opening repayments
		BigDecimal bdOpeningRepayment = getTotalOpeningRepayments(memberId);

		// get total repayments from repayments table
		BigDecimal bdTotalRepayment = getTotalRepaymentFromRunningRepayments(memberId);

		// sum up the repayments
		bdTotalRepaid = bdOpeningRepayment.add(bdTotalRepayment);
		return bdTotalRepaid;
	}

	/**
	 * Total Repaid By Loan ApplicationId
	 * */
	public static BigDecimal getLoansRepaidByLoanApplicationId(
			Long loanApplicationId) {
		BigDecimal bdTotalRepaid = BigDecimal.ZERO;

		// Get total repayments opening repayments
		BigDecimal bdOpeningRepayment = getTotalOpeningRepaymentsByLoanApplicationId(loanApplicationId);

		// get total repayments from repayments table
		BigDecimal bdTotalRepayment = getTotalRepaymentFromRunningRepaymentsByLoanApplicationId(loanApplicationId);

		// sum up the repayments
		bdTotalRepaid = bdOpeningRepayment.add(bdTotalRepayment);
		return bdTotalRepaid;
	}

	/***
	 * @author Japheth Odonya @when Nov 8, 2014 4:19:04 PM
	 * 
	 *         Get total days in repayment table
	 * */
	private static BigDecimal getTotalRepaymentFromRunningRepayments(
			Long memberId) {

		// Get the disbursed loans for this member
		List<Long> loanApplicationIdList = getDisbursedLoansIds(memberId);

		// get the repayments for the disbursed loans
		BigDecimal bdDisbursedLoansRunningRepaymentTotal = getDisbursedLoansRunningRepayment(loanApplicationIdList);

		// TODO Auto-generated method stub
		return bdDisbursedLoansRunningRepaymentTotal;
	}

	/****
	 * 
	 * Get Total Repayment from Running Repayments By Loan Application ID
	 * */
	private static BigDecimal getTotalRepaymentFromRunningRepaymentsByLoanApplicationId(
			Long loanApplicationId) {

		// Get the disbursed loans for this member
		List<Long> loanApplicationIdList = getDisbursedLoansIdsByLoanApplicationId(loanApplicationId);

		// get the repayments for the disbursed loans
		BigDecimal bdDisbursedLoansRunningRepaymentTotal = getDisbursedLoansRunningRepayment(loanApplicationIdList);

		// TODO Auto-generated method stub
		return bdDisbursedLoansRunningRepaymentTotal;
	}

	private static BigDecimal getDisbursedLoansRunningRepayment(
			List<Long> loanApplicationIdList) {

		BigDecimal bdRepaymentTotal = BigDecimal.ZERO;

		for (Long loanApplicationId : loanApplicationIdList) {
			bdRepaymentTotal = bdRepaymentTotal
					.add(getRepaymentTotalForLoanApplication(loanApplicationId));
		}

		return bdRepaymentTotal;
	}

	private static BigDecimal getRepaymentTotalForLoanApplication(
			Long loanApplicationId) {
		List<GenericValue> loanRepaymentELI = null; // =
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		BigDecimal bdTotalRepayment = BigDecimal.ZERO;
		for (GenericValue genericValue : loanRepaymentELI) {

			if (genericValue.getBigDecimal("principalAmount") != null) {
				bdTotalRepayment = bdTotalRepayment.add(genericValue
						.getBigDecimal("principalAmount"));
			}
		}

		return bdTotalRepayment;
	}

	private static List<Long> getDisbursedLoansIds(Long memberId) {
		Long loanStatusId = getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS,
								Long.valueOf(loanStatusId))),
						EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI == null) {
			return null;
		}
		List<Long> loansDisbursedIdList = new ArrayList<Long>();
		for (GenericValue genericValue : loanApplicationELI) {
			loansDisbursedIdList.add(genericValue.getLong("loanApplicationId"));
		}

		return loansDisbursedIdList;
	}

	/****
	 * Disbursed Loan IDs by Loan Application Id
	 * getDisbursedLoansIdsByLoanApplicationId
	 * */
	private static List<Long> getDisbursedLoansIdsByLoanApplicationId(
			Long loanApplicationId) {
		Long loanStatusId = getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId), EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						Long.valueOf(loanStatusId))), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI == null) {
			return null;
		}
		List<Long> loansDisbursedIdList = new ArrayList<Long>();
		for (GenericValue genericValue : loanApplicationELI) {
			loansDisbursedIdList.add(genericValue.getLong("loanApplicationId"));
		}

		return loansDisbursedIdList;
	}

	/***
	 * @author Japheth Odonya @when Nov 8, 2014 4:16:09 PM Get total opening
	 *         loans repayments for this employee
	 * */
	private static BigDecimal getTotalOpeningRepayments(Long memberId) {
		Long loanStatusId = getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS,
								Long.valueOf(loanStatusId))),
						EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdTotalRepayments = BigDecimal.ZERO;
		for (GenericValue genericValue : loanApplicationELI) {
			// if (genericValue.getBigDecimal("totalRepayment") != null) {
			// bdTotalRepayments = bdTotalRepayments.add(genericValue
			// .getBigDecimal("totalRepayment"));
			// }
			if (genericValue.getBigDecimal("outstandingBalance") != null) {
				// if
				// (genericValue.getBigDecimal("outstandingBalance").compareTo(BigDecimal.ZERO)
				// != -1){
				bdTotalRepayments = bdTotalRepayments.add(genericValue
						.getBigDecimal("loanAmt").subtract(
								genericValue
										.getBigDecimal("outstandingBalance")));
				// }
				log.info("GOT SUBTRACTED " + bdTotalRepayments
						+ "AAAAAAAAAAAAALLLLLLLLLLLLLLLLLLLL");
			}
		}
		return bdTotalRepayments;
	}

	/***
	 * Total Repayments By Loan Application Id
	 * */
	private static BigDecimal getTotalOpeningRepaymentsByLoanApplicationId(
			Long loanApplicationId) {
		Long loanStatusId = getLoanStatusId("DISBURSED");

		// , EntityCondition.makeCondition(
		// "loanStatusId", EntityOperator.EQUALS,
		// Long.valueOf(loanStatusId))

		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdTotalRepayments = BigDecimal.ZERO;
		for (GenericValue genericValue : loanApplicationELI) {

			if (genericValue.getBigDecimal("outstandingBalance") != null) {
				bdTotalRepayments = bdTotalRepayments.add(genericValue
						.getBigDecimal("loanAmt").subtract(
								genericValue
										.getBigDecimal("outstandingBalance")));
				log.info("MUST HAVE SUBTRACTED "
						+ bdTotalRepayments
						+ "SSSSSSSSSSSSSSSSSSSSSSSLLLLLLLLLLLLLLIIIIIIIIIIIDDDDDDD"
						+ genericValue.getBigDecimal("loanAmt"));
			}
		}
		return bdTotalRepayments;
	}

	/***
	 * @author Japheth Odonya @when Nov 17, 2014 8:17:44 PM
	 * 
	 *         Count Loans By Member
	 * 
	 * */

	public static Long countRunningLoansByMember(Long memberId) {

		Long disbursedLoanStatusId = getLoanStatusId("DISBURSED");
		Long guarantorLoanStatusId = getLoanStatusId("GUARANTORLOAN");

		Long disbursedLoansCount = getLoansCount(memberId,
				disbursedLoanStatusId);
		Long guarantorLoansCount = getLoansCount(memberId,
				guarantorLoanStatusId);
		Long totalLoansCount = disbursedLoansCount + guarantorLoansCount;

		return totalLoansCount;
	}

	private static Long getLoansCount(Long memberId, Long loanStatusId) {
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS,
								Long.valueOf(loanStatusId))),
						EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplicationELI == null) {
			return null;
		}

		return new Long(loanApplicationELI.size());
	}

	/***
	 * @author Japheth Odonya @when Nov 17, 2014 10:08:46 PM
	 * 
	 *         Get Remaining Loan Balance
	 * */
	public static BigDecimal getLoanRemainingBalance(Long loanApplicationId) {
		BigDecimal bdRemainingBalance = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		bdRemainingBalance = getLoanAmount(delegator,
				String.valueOf(loanApplicationId)).subtract(
				getLoansRepaidByLoanApplicationId(loanApplicationId));

		return bdRemainingBalance;

	}

	public static String getLoanPercentageRepaid(Long loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal repaidPercent = (getLoansRepaidByLoanApplicationId(loanApplicationId)
				.divide(getLoanAmount(delegator,
						String.valueOf(loanApplicationId)), 2,
						RoundingMode.HALF_UP)).multiply(new BigDecimal(100));

		String percentageRepaid = String.valueOf(repaidPercent) + " %";
		return percentageRepaid;
	}

	private static BigDecimal getLoanPercentageRepaidValue(
			Long loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal repaidPercent = (getLoansRepaidByLoanApplicationId(loanApplicationId)
				.divide(getLoanAmount(delegator,
						String.valueOf(loanApplicationId)), 2,
						RoundingMode.HALF_UP)).multiply(new BigDecimal(100));

		return repaidPercent;
	}

	public static BigDecimal getAllowedPercentageLimitForClearance() {
		BigDecimal bdPercentage = new BigDecimal(100);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanClearLimitELI = null;
		try {
			loanClearLimitELI = delegator.findList("LoanClearLimit", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanClearLimitELI == null) {
			return new BigDecimal(100);
		}
		for (GenericValue genericValue : loanClearLimitELI) {
			bdPercentage = genericValue.getBigDecimal("limitPercent");
		}

		return bdPercentage;

	}

	public static String alloweToClear(Long loanApplicationId) {
		String allowed = "N";

		if (getLoanPercentageRepaidValue(loanApplicationId).compareTo(
				getAllowedPercentageLimitForClearance()) >= 0) {
			allowed = "Y";
		}
		return allowed;
	}

	public static String addToClearList(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Create LoanClear and add loan to it
		GenericValue loanClear;
		Long loanClearId = null;
		if (openLoanClearanceExists(loanApplication.getLong("partyId"))) {
			loanClearId = getExistingLoanClearance(loanApplication
					.getLong("partyId"));
		} else {
			loanClearId = delegator.getNextSeqIdLong("LoanClear", 1);
			loanApplicationId = loanApplicationId.replaceAll(",", "");
			loanClear = delegator.makeValue("LoanClear",
					UtilMisc.toMap("loanClearId", loanClearId, "partyId",
							loanApplication.getLong("partyId"), "loanTotalAmt",
							getLoanRemainingBalance(Long
									.valueOf(loanApplicationId)), "createdBy",
							userLoginId, "isActive", "Y", "isCleared", "N"));
			try {
				delegator.createOrStore(loanClear);
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
		}

		// Save LoanClearItem

		GenericValue loanClearItem;
		Long loanClearItemId = delegator.getNextSeqIdLong("LoanClearItem", 1);
		// loanApplicationId = loanApplicationId.replaceAll(",", "");
		loanClearItem = delegator.makeValue("LoanClearItem", UtilMisc.toMap(
				"loanClearId", loanClearId, "loanClearItemId", loanClearItemId,
				"loanApplicationId", Long.valueOf(loanApplicationId),
				"loanAmt",
				getLoanRemainingBalance(Long.valueOf(loanApplicationId)),
				"createdBy", userLoginId, "isActive", "Y"));
		try {
			delegator.createOrStore(loanClearItem);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		loanApplication.set("isAddedToClear", "Y");
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return "success";
	}

	private static Long getExistingLoanClearance(Long partyId) {
		EntityConditionList<EntityExpr> clearanceConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("isCleared",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);
		List<GenericValue> loanClearELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", clearanceConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue loanClear = null;
		for (GenericValue genericValue : loanClearELI) {
			loanClear = genericValue;
		}
		if (loanClear != null)
			return loanClear.getLong("loanClearId");

		return null;
	}

	private static boolean openLoanClearanceExists(Long partyId) {
		EntityConditionList<EntityExpr> clearanceConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("isCleared",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);
		List<GenericValue> loanClearELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", clearanceConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (loanClearELI.size() > 0)
			return true;

		return false;
	}

	/**
	 * clearAll
	 * */
	public static String clearAll(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanClearId = (String) request.getParameter("loanClearId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanClear = null;

		loanClearId = loanClearId.replaceAll(",", "");
		try {
			loanClear = delegator.findOne("LoanClear",
					UtilMisc.toMap("loanClearId", Long.valueOf(loanClearId)),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanClear.getLong("loanApplicationId") == null) {
			return "error";
		}

		Map<String, String> userLoginMap = new HashMap<String, String>();
		userLoginMap.put("userLoginId", userLoginId);
		userLoginMap.put("partyId",
				String.valueOf(loanClear.getLong("partyId")));
		// Get all applications under this clearance and set their status to
		// cleared
		Long loanClearedStatusId = getLoanStatusId("CLEARED");
		List<GenericValue> loanClearItemELI = new ArrayList<GenericValue>();
		try {
			loanClearItemELI = delegator
					.findList(
							"LoanClearItem",
							EntityCondition.makeCondition("loanClearId",
									Long.valueOf(loanClearId)), null, null,
							null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdClearAmount = BigDecimal.ZERO;

		for (GenericValue genericValue : loanClearItemELI) {
			// genericValue.set(name, value);
			updateLoanToCleared(genericValue.getLong("loanApplicationId"),
					loanClearedStatusId);
			bdClearAmount = bdClearAmount.add(genericValue
					.getBigDecimal("loanAmt"));
		}
		// Update the LoanClearance by setting isCleared to 'Y'
		loanClear.set("isCleared", "Y");
		try {
			delegator.createOrStore(loanClear);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Post the Loan Clearance

		// Cr Loan Receivable
		// Dr Loan Clearance
		// AccHolderTransactionServices.
		// AccHolderTransactionServices.postTransactionEntry(delegator,
		// bdLoanAmount, partyId, loanReceivableAccount, postingType,
		// acctgTransId, acctgTransType, entrySequenceId);
		// POST Charge
		String acctgTransType = "LOAN_CLEARANCE";

		// Create the Account Trans Record
		String acctgTransId = AccHolderTransactionServices
				.createAccountingTransaction(null, acctgTransType, userLoginMap);
		// Debit Loan Clearance
		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String partyId = String.valueOf(loanClear.getLong("partyId"));
		String loanClearanceAccountId = AccHolderTransactionServices
				.getCashAccount(null, "LOANCLEARANCE");
		String postingType = "D";
		String entrySequenceId = "00001";
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdClearAmount, partyId, loanClearanceAccountId, postingType,
				acctgTransId, acctgTransType, entrySequenceId);

		// Credit Loan Receivables
		String loanReceivablesAccountId = AccHolderTransactionServices
				.getMemberDepositAccount(null, "LOANCLEARANCE");
		postingType = "C";
		entrySequenceId = "00002";
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdClearAmount, partyId, loanReceivablesAccountId, postingType,
				acctgTransId, acctgTransType, entrySequenceId);

		// Update loan applied, set its amount eligible
		// loanApplicationId

		Long loanAppliedForId = loanClear.getLong("loanApplicationId");
		// Get Maximum amount for this loan application
		GenericValue loanApplication = getLoanApplication(loanAppliedForId);
		BigDecimal bdTotalSavings = totalSavings(
				loanApplication.getLong("partyId").toString(), loanApplication
						.getLong("loanProductId").toString(), delegator);
		// bdMaximumLoanAmt =
		// (bdTotalSavings.multiply(savingsMultiplier))
		// .subtract(bdExistingLoans);
		GenericValue loanProduct = getLoanProduct(loanApplication
				.getLong("loanProductId"));
		Long accountProductId = loanProduct.getLong("accountProductId");
		BigDecimal bdMaximumLoanAmt = calculateMaximumAmount(
				loanApplication.getLong("partyId"), accountProductId,
				loanProduct.getBigDecimal("multipleOfSavingsAmt"),
				bdTotalSavings);

		BigDecimal bdExistingLoans = calculateExistingLoansTotal(loanApplication
				.getLong("partyId"));
		loanApplication.set("maxLoanAmt", bdMaximumLoanAmt);
		loanApplication.set("existingLoans", bdExistingLoans);

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return "success";
	}

	/***
	 * Update Loan Application to Cleared
	 * */
	private static void updateLoanToCleared(Long loanApplicationId,
			Long loanClearedStatusId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		loanApplication.set("loanStatusId", loanClearedStatusId);
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BigDecimal getTotalGuaranteedValueByMember(Long partyId) {
		BigDecimal bdTotalGuaranteed = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> myGuaranteedLoanELI = null;
		EntityConditionList<EntityExpr> guarantorConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"guarantorId", EntityOperator.EQUALS, partyId)),
						EntityOperator.AND);

		try {
			myGuaranteedLoanELI = delegator.findList("MyGuaranteedLoan",
					guarantorConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : myGuaranteedLoanELI) {

			BigDecimal bdGuarantedValue = getMyGuaranteedValue(genericValue
					.getLong("loanApplicationId"));

			bdTotalGuaranteed = bdTotalGuaranteed.add(bdGuarantedValue);

			// genericValue
			// .getBigDecimal("guaranteedValue")
		}
		return bdTotalGuaranteed;
	}

	private static BigDecimal getMyGuaranteedValue(Long loanApplicationId) {
		// TODO Auto-generated method stub
		BigDecimal bdLoanBalanceAmt = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		Long noOfGuarantors = LoanUtilities
				.getGuarantorsCount(loanApplicationId);
		return bdLoanBalanceAmt.divide(new BigDecimal(noOfGuarantors), 4,
				RoundingMode.HALF_UP);
	}

	public static BigDecimal getTotalAvailableValueByMember(Long partyId) {
		return getTotalMemberDeposits(partyId).subtract(
				getTotalGuaranteedValueByMember(partyId));
	}

	public static String updateLoanApplicationToSelfGuaranteed(
			String loanApplicationId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		loanApplicationId = loanApplicationId.replaceAll("", ",");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Loan Application";
		}

		loanApplication.set("isSelfGuaranteed", "Y");
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static String hasSavingsAccount(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Long partyId = Long.valueOf((String) request.getParameter("partyId"));

		result.put("hasSavingsAccount", hasSavingsAccount(partyId));

		// int membershipDuration = getMemberDurations(joinDate);

		// result.put("membershipDuration", membershipDuration);

		result.put("isOldEnough", LoanUtilities.isOldEnough(partyId.toString()));
		result.put("isFromAnotherSacco",
				LoanUtilities.isFromAnotherSacco(partyId.toString()));

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	/***
	 * otherExistingLoans
	 * 
	 * Validating on other existing loans
	 * */
	public static String otherExistingLoans(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Long loanApplicationId = Long.valueOf((String) request
				.getParameter("loanApplicationId"));

		result.put("otherLoansProcessing",
				otherLoansInProcessing(loanApplicationId));
		result.put("otherLoanNoRepayment",
				otherLoanWithoutRepayment(loanApplicationId));
		result.put("anotherRunningLoanOfSameType",
				otherRunningLoanOfSameType(loanApplicationId));

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	private static boolean otherRunningLoanOfSameType(Long loanApplicationId) {
		//Get if there is a disbursed loan that has not started repayment
				List<GenericValue> loanApplicationELI = null;
				GenericValue loanApplication = getLoanApplication(loanApplicationId);
				Long loanDisbursedStatusId = getLoanStatusId("DISBURSED");
				EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
						.makeCondition(
								UtilMisc.toList(EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										loanDisbursedStatusId),
										
										EntityCondition.makeCondition(
												"loanApplicationId", EntityOperator.NOT_EQUAL,
												loanApplicationId),
												
												EntityCondition.makeCondition(
														"loanProductId", EntityOperator.EQUALS,
														loanApplication.getLong("loanProductId")),
												
										EntityCondition
												.makeCondition("partyId",
														EntityOperator.EQUALS,
														loanApplication.getLong("partyId"))), EntityOperator.AND);
				Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
				try {
					loanApplicationELI = delegator.findList("LoanApplication",
							loanApplicationConditions, null, null, null, false);
				} catch (GenericEntityException e2) {
					e2.printStackTrace();
				}
				
				Boolean anotherLoanOfSameType = false;
				
				if ((loanApplicationELI != null) && (loanApplicationELI.size() > 0)){
					anotherLoanOfSameType = true;
				}
				
				return anotherLoanOfSameType;
	}

	//True means repayment not started
	private static boolean otherLoanWithoutRepayment(Long loanApplicationId) {
		//Get if there is a disbursed loan that has not started repayment
		List<GenericValue> loanApplicationELI = null;
		GenericValue loanApplication = getLoanApplication(loanApplicationId);
		Long loanDisbursedStatusId = getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"loanStatusId", EntityOperator.EQUALS,
								loanDisbursedStatusId),
								EntityCondition
										.makeCondition("partyId",
												EntityOperator.EQUALS,
												loanApplication.getLong("partyId"))), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		Boolean repaymentNotStarted = false;
		for (GenericValue genericValue : loanApplicationELI) {
			
			if (repaymentNotStarted(genericValue.getLong("loanApplicationId"))){
				repaymentNotStarted = true;
			}
		}
		
		
		return repaymentNotStarted;
	}

	private static boolean repaymentNotStarted(Long loanApplicationId) {
		// TODO Check that loan repayment has not started
		BigDecimal bdTotalRepayment = getLoansRepaidByLoanApplicationId(loanApplicationId);
		
		if ((bdTotalRepayment == null) || (bdTotalRepayment.compareTo(BigDecimal.ZERO) != 1))
		{
			return true;
		}
		
		return false;
	}

	private static boolean otherLoansInProcessing(Long loanApplicationId) {
		List<GenericValue> loanApplicationELI = null;
		
		GenericValue loanApplication = getLoanApplication(loanApplicationId);
		
		Long loanForwardedStatusId = getLoanStatusId("FORWARDEDLOANS");
		Long loanApprovedStatusId = getLoanStatusId("APPROVED");
		Long loanAppraisedStatusId = getLoanStatusId("APPRAISED");
		Long loanReturnedAppraisalStatusId = getLoanStatusId("RETUREDTOAPPRAISAL");
		Long loanForwardedForAppraisalStatusId = getLoanStatusId("FORWARDEDFORAPPRAISAL");

		EntityConditionList<EntityConditionList<EntityCondition>> statusConditions =
				EntityCondition.makeCondition(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS, loanForwardedStatusId),
						
						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS, loanApprovedStatusId),
						
						EntityCondition.makeCondition("loanStatusId",
										EntityOperator.EQUALS, loanAppraisedStatusId),		

						EntityCondition.makeCondition("loanStatusId",
												EntityOperator.EQUALS, loanReturnedAppraisalStatusId),		

						EntityCondition.makeCondition("loanStatusId",
														EntityOperator.EQUALS, loanForwardedForAppraisalStatusId)), EntityOperator.OR), EntityCondition.makeCondition( "partyId", EntityOperator.EQUALS, loanApplication.getLong("partyId")), EntityCondition.makeCondition( "loanApplicationId", EntityOperator.NOT_EQUAL, loanApplicationId)), EntityOperator.AND));
		
		//EntityConditionList.makeCondition(
		
		//EntityConditionList<EntityExpr> partyConditions = EntityConditionList.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		//		"partyId", EntityOperator.EQUALS, loanApplication.getLong("partyId"))), EntityOperator.AND);
				
		//EntityConditionList<EntityExpr> loanApplicationConditions = statusConditions.makeCondition(partyConditions);
	
		Delegator delagator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delagator.findList("LoanApplication",
					statusConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if ((loanApplicationELI != null) && (loanApplicationELI.size() > 0)){
			return true;
		}
		
		return false;
	}

	/***
	 * Determine if a member has a savings account product
	 * */
	public static Boolean hasSavingsAccount(Long partyId) {
		List<GenericValue> accountProductELI = null; // =
		Long accountProductId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("isSavings", "Y"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : accountProductELI) {
			accountProductId = genericValue.getLong("accountProductId");
		}

		if (accountProductId == null)
			return false;

		// Get the memberAccountId given accountProductId and PartyId
		List<GenericValue> memberAccountELI = null;
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS, accountProductId)),
						EntityOperator.AND);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					accountsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (memberAccountELI == null) {
			return false;
		} else {
			if (memberAccountELI.size() > 0) {
				return true;
			}
		}

		return false;

	}

	public static Boolean hasMemberDepositAccount(Long partyId) {
		return false;
	}

	public static Boolean hasBeenMemberLongEnough(Long partyId) {
		return false;
	}

	/**
	 * IsSelfGuarantee
	 * */
	public static String isSelfGuarantee(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		String loanSecurityId = (String) request.getParameter("loanSecurityId");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		log.info("Loan Security ID i is ##### " + loanSecurityId);
		if (loanSecurityId == null)
			return null;

		loanSecurityId = loanSecurityId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceAll(",", "");

		// isSelfGuarantee
		Boolean isSelfGuarantee = isSelfGuarantee(
				Long.valueOf(loanSecurityId.trim()),
				Long.valueOf(loanApplicationId.trim()));
		log.info("Self Guarantee is ##### " + isSelfGuarantee);
		result.put("isSelfGuarantee", isSelfGuarantee);

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return json;
	}

	private static Boolean isSelfGuarantee(Long loanSecurityId,
			Long loanApplicationId) {
		GenericValue loanSecurity = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanSecurity = delegator.findOne("LoanSecurity",
					UtilMisc.toMap("loanSecurityId", loanSecurityId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanSecurity == null)
			return false;

		if (loanSecurity.getString("description").equals("Self")) {
			// Update Loan Application to is Self Guaranteed
			updateSelfGuaranteed(loanSecurityId, loanApplicationId, "Self");
			return true;
		}

		if (loanSecurity.getString("description").equals("Guarantors")) {
			// Update Loan Application to Guarantor Loan
			updateSelfGuaranteed(loanSecurityId, loanApplicationId,
					"Guarantors");
			return false;
		}

		if (loanSecurity.getString("description").equals("Collateral")) {
			// Update Loan Application to Collateral Loan
			updateSelfGuaranteed(loanSecurityId, loanApplicationId,
					"Collateral");
			return false;
		}

		return false;
	}

	private static void updateSelfGuaranteed(Long loanSecurityId,
			Long loanApplicationId, String securityType) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Update to self guaranteed
		if (securityType.equals("Self")) {
			loanApplication.set("isSelfGuaranteed", "Y");

			// Update Member to Self Guaranteed
			updateMemberToSelfGuaranteed(loanApplication.getLong("partyId"));

		} else if (securityType.equals("Guarantors")) {
			loanApplication.set("isGuarantorLoan", "Y");
		} else if (securityType.equals("Collateral")) {
			loanApplication.set("isCollateralLoan", "Y");
		}

		loanApplication.set("loanSecurityId", loanSecurityId);
		// Set the Loan Security ID

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	private static void updateMemberToSelfGuaranteed(Long partyId) {
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		member.set("isSelfGuaranteed", "Y");

		try {
			delegator.createOrStore(member);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	public static GenericValue getLoan(String loanNo) {
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanNo", EntityOperator.EQUALS, loanNo)),
						EntityOperator.AND);

		List<GenericValue> loanApplicationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue loan = null;

		for (GenericValue genericValue : loanApplicationELI) {
			loan = genericValue;
		}

		return loan;

	}

	public static BigDecimal getLoanBalanceAmount(String loanApplicationId) {
		BigDecimal bdLoanBalance = BigDecimal.ZERO;

		BigDecimal bdLoanAmt = getLoanAmount(
				DelegatorFactoryImpl.getDelegator(null), loanApplicationId);

		String loanApplicationIdStr = loanApplicationId.replaceAll(",", "");
		Long loanApplicationIdLong = Long.valueOf(loanApplicationIdStr.trim());

		BigDecimal bdLoanRepaidAmt = getLoansRepaidByLoanApplicationId(loanApplicationIdLong);

		bdLoanBalance = bdLoanAmt.subtract(bdLoanRepaidAmt);
		return bdLoanBalance;

	}

}
