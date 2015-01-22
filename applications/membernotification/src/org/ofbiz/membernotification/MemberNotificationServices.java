package org.ofbiz.membernotification;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.webapp.event.EventHandlerException;

public class MemberNotificationServices {

	public static Logger log = Logger
			.getLogger(MemberNotificationServices.class);

	public static String createLoanDefaulterMessage() {

		// Create One Month Defaulter Message
		saveLoanInArrearsMessages();

		// Create Two Month Defaulter Message

		// Create Three Month Defaulter Message

		return "";
	}

	private static void saveLoanInArrearsMessages() {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		int monthsAgo = 0;
		for (GenericValue genericValue : loanApplicationELI) {
			monthsAgo = getLastRepaymentDurationToDate(genericValue
					.getTimestamp("lastRepaymentDate"));
			log.info("MMMMMMMMM --- " + monthsAgo + " months ago for "
					+ genericValue.getString("loanNo"));

			/****
			 * We are interested in 1, 2 and 3 months
			 * */
			Long messageStatusId = getMessageStatusId("NEW");
			Long notificationTypeId;
			String message;
			BigDecimal bdLoanAmt = genericValue.getBigDecimal("loanAmt");
			String loanNo = genericValue.getString("loanNo");

			if (monthsAgo == 1) {
				// Add the Ist Month Notification
				log.info("FFFFFFFFFF First Month Notification for "
						+ genericValue.getString("loanNo"));
				notificationTypeId = getNotificationTypeId("Defaulter Message 1");
				message = " The loan Loan No. "+loanNo+" of Amount : "+bdLoanAmt+" is in arrears for one month. ";
				addNewMessage(messageStatusId, notificationTypeId, message, genericValue, 1, false, genericValue.getLong("partyId"));
				addGuarantorMessage(messageStatusId, notificationTypeId, message, genericValue, 1, true);
			} else if (monthsAgo == 2) {
				// Add the 2nd Month Notification
				log.info("SSSSSSSSSSSSSSSS Second Month Notification for "
						+ genericValue.getString("loanNo"));
				notificationTypeId = getNotificationTypeId("Defaulter Message 2");
				message = " The loan Loan No. "+loanNo+" of Amount : "+bdLoanAmt+" is in arrears for two months. ";
				addNewMessage(messageStatusId, notificationTypeId, message, genericValue, 2, false, genericValue.getLong("partyId"));
				addGuarantorMessage(messageStatusId, notificationTypeId, message, genericValue, 1, true);
			} else if (monthsAgo == 3) {
				// Add the 3rd Month Notification
				log.info("TTTTTTTTTTTTTTT Third Month Notification for "
						+ genericValue.getString("loanNo"));
				notificationTypeId = getNotificationTypeId("Defaulter Message 3");
				message = " The loan Loan No. "+loanNo+" of Amount : "+bdLoanAmt+" is in arrears for three months. ";
				addNewMessage(messageStatusId, notificationTypeId, message, genericValue, 3, false, genericValue.getLong("partyId"));
				addGuarantorMessage(messageStatusId, notificationTypeId, message, genericValue, 1, true);
			}

		}

	}

	private static void addGuarantorMessage(Long messageStatusId,
			Long notificationTypeId, String message, GenericValue genericValue,
			int noOfMonths, boolean isGuarantorMessage) {
		
		//Get Guarantors for Member
		List<GenericValue> loanGuarantorELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							genericValue.getLong("loanApplicationId")), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue loanGuarantor : loanGuarantorELI) {
			addNewMessage(messageStatusId, notificationTypeId, message, genericValue, noOfMonths, true, loanGuarantor.getLong("guarantorId"));
		}
		
	}

	private static void addNewMessage(Long messageStatusId,
			Long notificationTypeId, String message, GenericValue loanApplication, long months, Boolean guarantorMessage, Long partyId) {
			//Add Principal Loanee Message
		// Create a MemberMessage
		if (guarantorMessage){
			if (months==1){
				message = " The loan Loan No. "+loanApplication.getString("loanNo")+" of Amount : "+loanApplication.getBigDecimal("loanAmt")+" that you guaranteed for "+getMemberNames(loanApplication.getLong("partyId"))+" is in arrears for one month. ";
			}else if (months == 2){
				message = " The loan Loan No. "+loanApplication.getString("loanNo")+" of Amount : "+loanApplication.getBigDecimal("loanAmt")+" that you guaranteed for "+getMemberNames(loanApplication.getLong("partyId"))+" is in arrears for two months. ";
			} else if (months == 3){
				message = " The loan Loan No. "+loanApplication.getString("loanNo")+" of Amount : "+loanApplication.getBigDecimal("loanAmt")+" that you guaranteed for "+getMemberNames(loanApplication.getLong("partyId"))+" is in arrears for three months. ";
			}
			
		}
		GenericValue memberMessage;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long memberMessageId = delegator.getNextSeqIdLong("MemberMessage", 1);
		
		memberMessage = delegator.makeValue("MemberMessage", UtilMisc.toMap(
				"memberMessageId", memberMessageId, "loanApplicationId",
				loanApplication.getLong("loanApplicationId"),
				
				"notificationTypeId", notificationTypeId,
				"messageStatusId", messageStatusId,
				"partyId", partyId,
				"isActive", "Y",
				"createdBy", "admin",
				"updatedBy", "admin",
				"name", message));
		try {
			delegator.createOrStore(memberMessage);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
	}

	private static String getMemberNames(Long partyId) {
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return member.getString("firstName")+" "+member.getString("middleName")+" "+member.getString("lastName");
	}

	private static Long getNotificationTypeId(String name) {
		List<GenericValue> notificationTypeELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			notificationTypeELI = delegator.findList("NotificationType",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long notificationTypeId = 0L;
		for (GenericValue genericValue : notificationTypeELI) {
			notificationTypeId = genericValue.getLong("notificationTypeId");
		}

		return notificationTypeId;
	}

	public static String sendLoanDefaulterMessage() {

		// Create One Month Defaulter Message

		// Create Two Month Defaulter Message

		// Create Three Month Defaulter Message

		return "";
	}

	public static int getLastRepaymentDurationToDate(Timestamp lastRepaymentDate) {
		int months = 0;
		if (lastRepaymentDate == null)
			return months;

		DateTime startDate = new DateTime(lastRepaymentDate.getTime());
		// DateTime endDate = new DateTime(Calendar.getInstance()
		// .getTimeInMillis());
		DateTime endDate = getEndDate();
		log.info(" NNNNNNNNNNNNN Member Notification Date  " + endDate);
		if (endDate.isAfterNow()) {
			log.info(" ############# Its not yet time to run the notifications ########");
			return months;
		} else {
			log.info(" ############# Will run the notifications ########");
		}
		// Days noOfDays = Days.daysBetween(startDate, endDate);
		Months noOfMonths = Months.monthsBetween(startDate, endDate);
		// if (noOfDays.get(DurationFieldType.days()) <= 60) {
		// days = noOfDays.get(DurationFieldType.days());
		// } else {
		months = noOfMonths.getMonths();
		// }
		log.info("MMMMMMMMMMMMMMMMMMM The MONTHS are " + months);
		return months;
	}

	private static DateTime getEndDate() {

		// Get the day by which payment must have been made
		int paymentByDay = getPaymentByDay();

		LocalDate localDate = new LocalDate(Calendar.getInstance()
				.getTimeInMillis());
		localDate = localDate.withDayOfMonth(paymentByDay);

		return new DateTime(localDate.toDate().getTime());
	}

	private static int getPaymentByDay() {

		List<GenericValue> repaymentDeadlineELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			repaymentDeadlineELI = delegator.findList("RepaymentDeadline",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		int payByDay = 0;
		for (GenericValue genericValue : repaymentDeadlineELI) {
			payByDay = genericValue.getLong("deadLine").intValue();
		}
		return payByDay;
	}

	public static String addDefaulterMessages(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator g = DelegatorFactoryImpl.getDelegator(null);
		// LocalDispatcher dispatcher = (new GenericDispatcherFactory())
		// .createLocalDispatcher("interestcalculations", delegator);
		//
		// Map<String, String> context = UtilMisc.toMap("message",
		// "Interest Testing !!");
		// Map<String, Object> result = new HashMap<String, Object>();
		// try {
		// long startTime = (new Date()).getTime();
		// // result = dispatcher.runSync("calculateInterestEarned", context);
		// // dispatcher.schedule("calculateInterestEarned", startTime,
		// // context);
		// int frequency = RecurrenceRule.SECONDLY;
		// int interval = 5;
		// int count = -1;
		// dispatcher.schedule("calculateInterestEarned", context, startTime,
		// frequency, interval, count);
		// } catch (GenericServiceException e) {
		// e.printStackTrace();
		// }

		// Add Messages
		createLoanDefaulterMessage();

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

	public static Long getMessageStatusId(String name) {
		List<GenericValue> messageStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			messageStatusELI = delegator.findList("MessageStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long messageStatusId = 0L;
		for (GenericValue genericValue : messageStatusELI) {
			messageStatusId = genericValue.getLong("messageStatusId");
		}

		return messageStatusId;
	}
	
	public static String sendTheMessage(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		//Delegator delegator = (Delegator) request.getAttribute("delegator");
		String msaccoMessageId = null;
		
		msaccoMessageId = (String) request
				.getParameter("msaccoMessageId");
		Long lmsaccoMessageId;
		if (msaccoMessageId == null)
			lmsaccoMessageId = Long.valueOf(msaccoMessageId);
		
		log.info("RRRRRRR The real msaccoMessageId is "+msaccoMessageId);
		GenericValue msaccoMessage = null;
	//	loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			msaccoMessage = delegator.findOne("MsaccoMessage",
					UtilMisc.toMap("msaccoMessageId", msaccoMessageId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		System.out.println(" ###############MMMMMM "+msaccoMessage.getString("message"));

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

}
