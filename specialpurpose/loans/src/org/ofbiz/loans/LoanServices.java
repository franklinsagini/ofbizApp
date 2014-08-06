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

import org.joda.time.JodaTimePermission;
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

}
