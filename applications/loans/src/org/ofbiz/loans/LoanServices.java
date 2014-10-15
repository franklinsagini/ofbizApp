package org.ofbiz.loans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

import com.google.gson.Gson;

public class LoanServices {
	public static Logger log = Logger.getLogger(LoanServices.class);

	public static String getLoanDetails(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanProductId = (String) request.getParameter("loanProductId");
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
		String loanProductId = (String) request.getParameter("loanProductId");

		// Locale locale = (Locale) request.getParameter("locale");
		GenericValue member = null;
		// String firstName, middleName, lastName, idNumber, memberType,
		// memberNumber, mobileNumber;
		String memberDetails = "";
		BigDecimal bdDepositamt = BigDecimal.ZERO;

		if ((loanProductId != null) && (!loanProductId.equals(""))) {
			bdDepositamt = totalSavings(partyId, loanProductId, delegator);
		}

		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
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

		// Get Total Savings for the Member (Get total for each of their savings
		// account)
		BigDecimal bdMaximumLoanAmt = BigDecimal.ZERO;
		BigDecimal bdExistingLoans;
		BigDecimal bdTotalSavings = BigDecimal.ZERO;
		// Get get Loan Product
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
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

			// Get Existing Loans
			
			
			//if this is a loan based on Multiples of Account Product then
			if (loanProduct.getString("multipleOfSavings").equals("Yes")){
				bdExistingLoans = calculateExistingLoansTotal(memberId,
						loanProductId, delegator);
				bdTotalSavings = totalSavings(memberId, loanProductId,
						delegator);
				bdMaximumLoanAmt = (bdTotalSavings.multiply(savingsMultiplier))
					.subtract(bdExistingLoans);
			} else{
				
				bdExistingLoans = calculateExistingAccountLessLoansTotal(memberId,
						loanProductId, delegator);
				bdMaximumLoanAmt = loanProduct.getBigDecimal("maximumAmt").subtract(bdExistingLoans);
			}
			
			//else if it is not based on multiples of Account Product the
			

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

	private static BigDecimal totalSavings(String memberId,
			String loanProductId, Delegator delegator) {
		// Get the multiplier account - the account being used to get the
		// maximum loan as defined in the
		// Loan Product Setup

		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// The Multiplier account id
		String accountProductId = loanProduct.getString("accountProductId");

		// Get Accounts for this member
		List<GenericValue> memberAccountELI = null;
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
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
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			// return ServiceUtil.returnError(UtilProperties.getMessage("",
			// "Cannot Get Member Details",
			// UtilMisc.toMap("errMessage", e.getMessage()), locale));
			// System.out.println(e.printStackTrace());
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
	private static BigDecimal calculateExistingLoansTotal(String memberId,
			String loanProductId, Delegator delegator) {
		BigDecimal existingLoansTotal = BigDecimal.ZERO;

		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// The Multiplier account id
		String accountProductId = loanProduct.getString("accountProductId");

		List<GenericValue> loanApplicationELI = null; // =

		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS, accountProductId)),
						EntityOperator.AND);
		
		//EntityOperator._emptyMap

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);
			existingLoansTotal = existingLoansTotal.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		return existingLoansTotal;
	}
	
	/***
	 * Total Loans for Loans without product
	 * 
	 * */
	/**
	 * Calculate Existing Loans Total
	 * **/
	private static BigDecimal calculateExistingAccountLessLoansTotal(String memberId,
			String loanProductId, Delegator delegator) {
		BigDecimal existingLoansTotal = BigDecimal.ZERO;

		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// The Multiplier account id
		//String accountProductId = loanProduct.getString("accountProductId");

		List<GenericValue> loanApplicationELI = null; // =

		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS, "NOACCOUNT")),
						EntityOperator.AND);
		
		//EntityOperator._emptyMap

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);
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
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
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
		EntityConditionList<EntityExpr> chargesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanProductId", EntityOperator.EQUALS, loanProductId),
						EntityCondition.makeCondition("chargedUpfront",
								EntityOperator.EQUALS, "Y")),
						EntityOperator.AND);

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
						"loanApplicationChargeId", loanApplicationChargeId,
						"isActive", "Y", "createdBy", partyId,
						"loanApplicationId",
						loanApplication.getString("loanApplicationId"),
						"productChargeId",
						loanProductCharge.getString("productChargeId"),
						"isFixed", loanProductCharge.getString("isFixed"),
						"chargedUpfront",
						loanProductCharge.getString("chargedUpfront"),

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

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		List<GenericValue> listToBeUpdatedGuarantors = new LinkedList<GenericValue>();
		for (GenericValue genericValue : loanGuarantorELI) {
			genericValue.set("guaranteedPercentage", bdGuaranteedPercentage);
			genericValue.set("guaranteedValue", bdGuaranteedValue);
			listToBeUpdatedGuarantors.add(genericValue);
		}

		// Save all the updated Guarantors
		try {
			delegator.storeAll(listToBeUpdatedGuarantors);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:27:20 PM Get the number of
	 *         guaranters for this Application TODO
	 * */
	private static Integer getNumberOfGuarantors(Delegator delegator,
			String loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =

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

	/***
	 * @author Japheth Odonya @when Aug 31, 2014 7:25:18 PM Get Loan Amount
	 *         given loanApplicationId
	 * */
	private static BigDecimal getLoanAmount(Delegator delegator,
			String loanApplicationId) {

		GenericValue loanApplication = null;

		// SaccoProduct
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

		try {
			loanApplicationCallateralELI = delegator.findList(
					"LoanApplicationCallateral", EntityCondition.makeCondition(
							"loanApplicationId", loanApplicationId), null,
					null, null, false);
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

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
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
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
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

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
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
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			// return "Cannot Get Product Details";
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

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
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

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
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

		// SaccoProduct
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		loanApplication.set("applicationStatus", "FORWAREDAPPROVAL");
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Create a Log
		GenericValue loanStatusLog;
		String loanStatusLogId = delegator.getNextSeqId("LoanStatusLog", 1);

		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				loanApplicationId, "applicationStatus", "FORWAREDAPPROVAL",
				"createdBy", userLoginId, "comment", "forwarded for approval"));

		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

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
			out.write("");
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

		return "";
	}
	
	/***
	 * Sent to Loans
	 * 
	 * */
	public static String forwardLoanApplicationToLoans(HttpServletRequest request,
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

		// SaccoProduct
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		loanApplication.set("applicationStatus", "FORWARDEDLOANS");
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Create a Log
		GenericValue loanStatusLog;
		String loanStatusLogId = delegator.getNextSeqId("LoanStatusLog", 1);

		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				loanApplicationId, "applicationStatus", "FORWARDEDLOANS",
				"createdBy", userLoginId, "comment", "forwarded to Loans"));

		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

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
			out.write("");
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

		return "";
	}

}
