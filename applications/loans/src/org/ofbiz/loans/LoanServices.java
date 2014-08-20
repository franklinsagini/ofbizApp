package org.ofbiz.loans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
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
			result.put("multipleOfSavingsAmt", loanProduct.get("multipleOfSavingsAmt"));
			
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
		// Locale locale = (Locale) request.getParameter("locale");
		GenericValue member = null;
		// String firstName, middleName, lastName, idNumber, memberType,
		// memberNumber, mobileNumber;
		String memberDetails = "";

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

			Date joinDate = member.getDate("joinDate");
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			String strJoinDate = format1.format(joinDate);
			result.put("joinDate", strJoinDate);
			
			SimpleDateFormat formattedDateInput = new SimpleDateFormat("yyyy-MM-dd");
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
		BigDecimal bdTotalSavings = totalSavings(memberId, delegator);
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

			// result.put("interestRatePM", loanProduct.get("interestRatePM"));
			// result.put("maxRepaymentPeriod",
			// loanProduct.get("maxRepaymentPeriod"));
			// result.put("maximumAmt", loanProduct.get("maximumAmt"));
			// result.put("selectedRepaymentPeriod",
			// saccoProduct.get("selectedRepaymentPeriod"));
			if (loanProduct.getBigDecimal("multipleOfSavingsAmt") != null) {
				savingsMultiplier = loanProduct
						.getBigDecimal("multipleOfSavingsAmt");
			}

			bdMaximumLoanAmt = bdTotalSavings.multiply(savingsMultiplier);
			
			//Get Existing Loans
			bdExistingLoans = calculateExistingLoansTotal(memberId, delegator);
			bdMaximumLoanAmt = bdMaximumLoanAmt.subtract(bdExistingLoans);
			result.put("maxLoanAmt", bdMaximumLoanAmt);
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

	private static BigDecimal totalSavings(String memberId, Delegator delegator) {
		// Get Accounts for this member
		List<GenericValue> memberAccountELI = null; // =
													// delegator.findListIteratorByCondition("MemberAccount",
													// new EntityExpr("partyId",
													// EntityOperator.EQUALS,
													// memberId),
													// UtilMisc.toList("productId"),
													// null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", memberId), null,
					null, null, false);
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

	private static int getMemberDurations(Date joinDate) {

		LocalDateTime stJoinDate = new LocalDateTime(joinDate.getTime());
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());

		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(stJoinDate, stCurrentDate, monthDay);

		int months = difference.getMonths();

		return months;

	}
	/**
	 * Calculate Existing Loans Total
	 * **/
	private static BigDecimal calculateExistingLoansTotal(String memberId, Delegator delegator){
		BigDecimal existingLoansTotal = BigDecimal.ZERO;
		
		List<GenericValue> loanApplicationELI = null; // =

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("partyId",
							memberId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		//List<GenericValue> loansList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanApplicationELI) {
			//toDeleteList.add(genericValue);
			existingLoansTotal = existingLoansTotal.add(genericValue.getBigDecimal("loanAmt"));
		}

		return existingLoansTotal;
	}
	
	public static Timestamp calculateLoanRepaymentStartDate(GenericValue loanApplication) {

		Map<String, Object> result = FastMap.newInstance();
		String loanApplicationId = loanApplication.getString("loanApplicationId");// (String)context.get("loanApplicationId");
		log.info("What we got is ############ " + loanApplicationId);

		Delegator delegator;
		// delegator = D
		// ctx.getDelegator();

		// delegator = DelegatorFactoryImpl.getDelegator("delegator");
		delegator = loanApplication.getDelegator();
		// GenericValue accountTransaction = null;
		try {
			loanApplication = delegator
					.findOne("LoanApplication", UtilMisc.toMap(
							"loanApplicationId", loanApplicationId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		//Get Salary processing day
		Timestamp repaymentStartDate = getProcessingDate(delegator);

		
		// loanApplication.set("monthlyRepayment", paymentAmount);
		loanApplication.set("repaymentStartDate", repaymentStartDate);
		log.info("##### End Date is ######## " + repaymentStartDate);
		log.info("##### ID is  ######## "
				+ loanApplicationId);
		loanApplication.set("repaymentStartDate", repaymentStartDate);
		
		 try {
		 delegator.createOrStore(loanApplication);
		 } catch (GenericEntityException e) {
		 e.printStackTrace();
		 }
		result.put("repaymentStartDate", repaymentStartDate);
		return repaymentStartDate;
	}
	
	private static Timestamp getProcessingDate(Delegator delegator){
		List<GenericValue> salaryProcessingDateELI = null; // =
		Long processingDay = 0l;
		try {
			salaryProcessingDateELI = delegator.findList("SalaryProcessingDate",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}


		for (GenericValue genericValue : salaryProcessingDateELI) {
			processingDay = genericValue.getLong("processingDay");
		}
		
		log.info("##### Salary Processing Day is ######## " + processingDay);
		
		Timestamp currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Timestamp repaymentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()); ;
		LocalDateTime localDateCurrent = new LocalDateTime(currentDate.getTime());
		LocalDateTime localDateRepaymentDate = new LocalDateTime(repaymentDate.getTime());
		
		
		if (localDateCurrent.getDayOfMonth() < processingDay.intValue()){
			//Repayment Date is Beginning of Next Month
			localDateRepaymentDate = localDateRepaymentDate.plusMonths(1);
			//localDateRepaymentDate = localDateRepaymentDate.getD
			//DateMidnight firstDay = new DateMidnight().withDayOfMonth(1);
			DateTime startOfTheMonth = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			DateTime startOfNextMonth = startOfTheMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			repaymentDate = new Timestamp(startOfNextMonth.toLocalDate().toDate().getTime());
		} else{
			//from 15th and beyond then start paying two months later
			DateTime startOfTheMonth = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			DateTime startOfNextMonth = startOfTheMonth.plusMonths(2).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			repaymentDate = new Timestamp(startOfNextMonth.toLocalDate().toDate().getTime());
		}
		
		return repaymentDate;

	}
	
	/**
	 * @author Japheth Odonya  @when Aug 20, 2014 7:07:07 PM
	 * Add Charges for the Product to the Product on application
	 * **/
	public static String addCharges(GenericValue loanApplication){
		return "";
	}

}
