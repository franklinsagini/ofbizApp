package org.ofbiz.humanres;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.json.*;
//import org.ofbiz.accounting.ledger.CrbReportServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

public class sendMessage {

	public static Logger log = Logger.getLogger(sendMessage.class);

	public static String senders(HttpServletRequest request, HttpServletResponse response) {

		log.info("-------SEND SMSs----------SENDERS() METHOD;");
		String suc = "Successesese";
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		try {
		  String saveManyGuys = sendMessage.send_Defaulter_SMS_Non_Payment_For_Three_Months();
		 // String sendManyDef = sendMessage.send_Defaulter_SMS_Non_Payment_For_Three_Months();
			System.out.println("____-------send_Defaulter_SMS_Non_Payment_For_Three_Month-----");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("______POULD NOT SEND TO MANY _-----");
		}

		log.info("&**************#senders Really Done##************####");

		return suc;
	}

	public static String sendMyMessage(String phoneNumber, String MemberNo) {
		String message = "You have been successfully registered as Chai Sacco Member.Your Member No is " + MemberNo
				+ ".Thank You";
		String recipients = phoneNumber;
		String saveMessage = sendMessage.sendAndStroreRegistrationMessage(phoneNumber, message);
		String sendMany = sendMessage.sendMessagesForUnRepaidLoans();
		System.out.println("" + saveMessage);
		System.out.println("" + sendMany);
		// getSavedSMSMessagesAndSend();

		return recipients;
	}

	public static String sendAndStroreRegistrationMessage(String phoneNo, String Body) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue smsMessageELI = null;
		String successMessage = "Successful";

		java.util.Date date = new Date(0);
		Object param = new java.sql.Timestamp(date.getTime());

		java.sql.Date dateCreated = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		LocalDate todayToLocalTime = new LocalDate(dateCreated);
		Timestamp timestamp = new Timestamp(todayToLocalTime.toDateTimeAtStartOfDay().getMillis());

		try {
			smsMessageELI = delegator.makeValue("SMSMessages");
			String smsId = delegator.getNextSeqId("SMSMessages");
			smsMessageELI.put("SMSMessagesId", smsId);
			smsMessageELI.put("phoneNumber", phoneNo);
			smsMessageELI.put("smsBody", Body);
			smsMessageELI.put("timeDateSend", timestamp);
			smsMessageELI.put("sendStatus", "NOT SEND");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			smsMessageELI.create();
			System.out.println("Message Saved: " + smsMessageELI);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
			System.out.println("Message NOT SAVED: " + smsMessageELI);
		}
		return successMessage;
	}

	public static void getSavedSMSMessagesAndSend() {

		String username = "vergeint";
		String apiKey = "82d9cb8c1132c6e5e07d354f6fc08ddc1f1d4d813153d997f465c28a4092515c";
		AfricasTalkingGateway gateway = new AfricasTalkingGateway(username, apiKey);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> notSendSMS = null;
		String phoneNumbers = null;
		String bodyMessage = null;
		try {
			notSendSMS = delegator.findList("SMSMessages",
					EntityCondition.makeCondition("sendStatus", EntityOperator.EQUALS, "NOT SEND"), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : notSendSMS) {
			phoneNumbers = genericValue.getString("phoneNumber");
			bodyMessage = genericValue.getString("smsBody");
			try {
				JSONArray results = gateway.sendMessage(phoneNumbers, bodyMessage);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			genericValue.set("sendStatus", "SEND");
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void newLoanApplicationMessages() {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> sendNewLoanApplicationMessagesELI = null;
		GenericValue sendNewLoanDetails = null;
		GenericValue getLoanProduct = null;
		Long loanApplicationId = null;
		String loanNo = null;
		Long loanProductId = null;
		BigDecimal appliedAmount = BigDecimal.ZERO;
		String loanProductName = null;
		String messageToSent = null;
		String mobileNumber = null;

		long one = 1;
		EntityConditionList<EntityExpr> loanStatusCondition = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("smsStatus", EntityOperator.EQUALS, ""),
						EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, one),
						EntityCondition.makeCondition("smsStatus", EntityOperator.NOT_EQUAL, "SEND")),
				EntityOperator.AND);

		try {
			sendNewLoanApplicationMessagesELI = delegator.findList("LoanStatusLog", loanStatusCondition, null, null,
					null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		for (GenericValue genericValue : sendNewLoanApplicationMessagesELI) {
			loanApplicationId = genericValue.getLong("loanApplicationId");

			try {
				sendNewLoanDetails = delegator.findOne("LoanApplication",
						UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			if (sendNewLoanDetails != null) {
				loanProductId = sendNewLoanDetails.getLong("loanProductId");
				loanNo = sendNewLoanDetails.getString("loanNo");
				appliedAmount = sendNewLoanDetails.getBigDecimal("appliedAmt");
				mobileNumber = sendNewLoanDetails.getString("mobileNumber");
			}

			try {
				getLoanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap("loanProductId", loanProductId),
						false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			if (getLoanProduct != null) {
				loanProductName = getLoanProduct.getString("name");
			}

			messageToSent = "Your " + loanProductName + " application of Ksh " + appliedAmount + " has been received";

			// Save The Message
			String saveMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNumber, messageToSent);
			System.out.println("" + saveMessage);
			// set The status SEND
			genericValue.set("smsStatus", "SEND");
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void approvedLoanApplicationMessages() {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> sendNewLoanApplicationMessagesELI = null;
		GenericValue sendNewLoanDetails = null;
		GenericValue getLoanProduct = null;
		Long loanApplicationId = null;
		String loanNo = null;
		Long loanProductId = null;
		BigDecimal appliedAmount = BigDecimal.ZERO;
		String loanProductName = null;
		String messageToSent = null;
		String mobileNumber = null;
		String firstName = null;
		String lastName = null;
		String messageToSentToGuarantor = null;
		List<GenericValue> guarantorList = null;

		long three = 3;
		EntityConditionList<EntityExpr> loanStatusCondition = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("smsStatus", EntityOperator.EQUALS, null),
						EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, three),
						EntityCondition.makeCondition("smsStatus", EntityOperator.NOT_EQUAL, "SEND")),
				EntityOperator.AND);

		try {
			sendNewLoanApplicationMessagesELI = delegator.findList("LoanStatusLog", loanStatusCondition, null, null,
					null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		for (GenericValue genericValue : sendNewLoanApplicationMessagesELI) {
			loanApplicationId = genericValue.getLong("loanApplicationId");

			try {
				sendNewLoanDetails = delegator.findOne("LoanApplication",
						UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			if (sendNewLoanDetails != null) {
				loanProductId = sendNewLoanDetails.getLong("loanProductId");
				loanNo = sendNewLoanDetails.getString("loanNo");
				appliedAmount = sendNewLoanDetails.getBigDecimal("appliedAmt");
				mobileNumber = sendNewLoanDetails.getString("mobileNumber");
				firstName = sendNewLoanDetails.getString("firstName");
				lastName = sendNewLoanDetails.getString("lastName");
			}

			try {
				getLoanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap("loanProductId", loanProductId),
						false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			if (getLoanProduct != null) {
				loanProductName = getLoanProduct.getString("name");
			}
			messageToSent = "Your " + loanProductName + " application of Ksh " + appliedAmount
					+ " has been Approved awaiting disbursement";
			String saveMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNumber, messageToSent);
			System.out.println("" + saveMessage);

			// SEND MESSAGES TO GUARANTORS

			EntityConditionList<EntityExpr> loanGuarantorConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, loanApplicationId)),
					EntityOperator.AND);
			try {
				guarantorList = delegator.findList("LoanGuarantor", loanGuarantorConditions, null, null, null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String fname = null;
			String mobileNo = null;
			Long guarantorsPartyId = null;

			for (GenericValue genericGuarantor : guarantorList) {
				guarantorsPartyId = genericGuarantor.getLong("guarantorId");
				mobileNo = getGuarantorPhoneNumber(guarantorsPartyId);
				fname = getGuarantorName(guarantorsPartyId);
				messageToSentToGuarantor = "Dear " + fname + ", Your have guaranteed loan No : " + loanNo + " for  "
						+ firstName + " " + lastName + " of Ksh " + appliedAmount + ". Thank you";
				String saveGuarantorMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNo,
						messageToSentToGuarantor);
				System.out.println("" + saveGuarantorMessage);
			}

			genericValue.set("smsStatus", "SEND");
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void disbursedLoanApplicationMessages() {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> sendNewLoanApplicationMessagesELI = null;
		GenericValue sendNewLoanDetails = null;
		GenericValue getLoanProduct = null;
		List<GenericValue> guarantorList = null;
		Long loanApplicationId = null;
		String loanNo = null;
		Long loanProductId = null;
		BigDecimal appliedAmount = BigDecimal.ZERO;
		String loanProductName = null;
		String messageToSent = null;
		String mobileNumber = null;
		String firstName = null;
		String lastName = null;
		String disbursedMsg = null;

		long six = 6;
		EntityConditionList<EntityExpr> loanStatusCondition = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("smsStatus", EntityOperator.EQUALS, null),
						EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, six),
						EntityCondition.makeCondition("smsStatus", EntityOperator.NOT_EQUAL, "SEND")),
				EntityOperator.AND);

		try {
			sendNewLoanApplicationMessagesELI = delegator.findList("LoanStatusLog", loanStatusCondition, null, null,
					null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		for (GenericValue genericValue : sendNewLoanApplicationMessagesELI) {
			loanApplicationId = genericValue.getLong("loanApplicationId");

			try {
				sendNewLoanDetails = delegator.findOne("LoanApplication",
						UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
			} catch (GenericEntityException ex) {
				ex.printStackTrace();
			}
			if (sendNewLoanDetails != null) {
				loanProductId = sendNewLoanDetails.getLong("loanProductId");
				loanNo = sendNewLoanDetails.getString("loanNo");
				appliedAmount = sendNewLoanDetails.getBigDecimal("appliedAmt");
				mobileNumber = sendNewLoanDetails.getString("mobileNumber");
				firstName = sendNewLoanDetails.getString("firstName");
				lastName = sendNewLoanDetails.getString("lastName");

			}

			disbursedMsg = "Your loan of kshs " + appliedAmount
					+ " has been disbursed to your account. Thank for Banking with us";
			String saveApprovedMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNumber, disbursedMsg);
			System.out.println("" + saveApprovedMessage);

			genericValue.set("smsStatus", "SEND");
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static String getGuarantorPhoneNumber(long partyIdAsGuarantor) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue guarantorsELE = null;
		String mobileNumber = null;
		try {
			guarantorsELE = delegator.findOne("Member", UtilMisc.toMap("partyId", partyIdAsGuarantor), false);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (guarantorsELE != null) {
			mobileNumber = guarantorsELE.getString("mobileNumber");
		}

		return mobileNumber;
	}

	public static String getGuarantorName(long partyIdAsGuarantor) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue guarantorsELE = null;
		String firstName = null;
		String lastName = null;
		try {
			guarantorsELE = delegator.findOne("Member", UtilMisc.toMap("partyId", partyIdAsGuarantor), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (guarantorsELE != null) {
			firstName = guarantorsELE.getString("firstName");
		}

		return firstName;
	}

	public static String sendMemberWithdrawal() {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberWithdraw = null;
		String memberPhoneNumber = null;
		String msgToBeSent = null;

		EntityConditionList<EntityExpr> withdrawalConditions = EntityConditionList.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("withdrawalstatus", EntityOperator.EQUALS, "APPLIED"),
						EntityCondition.makeCondition("smsSendStatus", EntityOperator.EQUALS, "")),
				EntityOperator.AND);

		try {
			memberWithdraw = delegator.findList("MemberWithdrawal", withdrawalConditions, null, null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		for (GenericValue genericValue : memberWithdraw) {
			memberPhoneNumber = genericValue.getString("mobilePhoneNumber");
			msgToBeSent = "We have received your withdrawal request from the Society for Processing.";
			String saveMessage = sendMessage.sendAndStroreRegistrationMessage(memberPhoneNumber, msgToBeSent);
			System.out.println("" + saveMessage);
			genericValue.set("smsSendStatus", "SEND");
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return memberPhoneNumber;
	}

	public static String loanNotInService(int noOfUuservicedDays, String msgSendStatus, String msgStatusControl,
			String SaveString) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		int numberOfDays = noOfUuservicedDays;
		int numberControl = numberOfDays + 1;
		String sendStatus = msgSendStatus;
		String statusControl = msgStatusControl;

		java.sql.Date today = new java.sql.Date(0);
		java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		LocalDate todayToLocalTime = new LocalDate(date);
		LocalDate todayToLocalTimeMinusRealDays = todayToLocalTime.minusDays(numberOfDays);
		Timestamp timestamp = new Timestamp(todayToLocalTimeMinusRealDays.toDateTimeAtStartOfDay().getMillis());

		LocalDate todayAdditionRealDays = todayToLocalTime.minusDays(numberControl);
		Timestamp timestampAddition = new Timestamp(todayAdditionRealDays.toDateTimeAtStartOfDay().getMillis());

		List<GenericValue> ListNotInServe = null;
		BigDecimal zeroOutStandingBalance = BigDecimal.ZERO;
		BigDecimal oneM = new BigDecimal(100000);

		log.info("################## timestamp*******##########+" + timestamp);
		log.info("################## timestampAddition*******##########+" + timestampAddition);

		EntityConditionList<EntityExpr> listConditions = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("msgSendStatus", EntityOperator.EQUALS, sendStatus),
						EntityCondition.makeCondition("msgStatusControl", EntityOperator.EQUALS, statusControl),
						EntityCondition.makeCondition("outstandingBalance", EntityOperator.LESS_THAN,
								zeroOutStandingBalance),
				EntityCondition.makeCondition("mobileNumber", EntityOperator.NOT_EQUAL, ""),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.LESS_THAN, timestamp),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.GREATER_THAN, timestampAddition)),
				EntityOperator.AND);
		try {
			ListNotInServe = delegator.findList("LoanApplication", listConditions, null, null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String phoneNumber = null;
		BigDecimal appliedAmt = BigDecimal.ZERO;
		BigDecimal loanAmt = BigDecimal.ZERO;
		int loanAmtStr = 0;
		String saveToTable = null;

		for (GenericValue genericValue : ListNotInServe) {
			long loanApplicationId = genericValue.getLong("loanApplicationId");
			// int noOfDaysSinceLastRepayment =
			// CrbReportServices.lastRepaymentDurationToDateInDays(loanApplicationId);

			phoneNumber = genericValue.getString("mobileNumber");
			appliedAmt = genericValue.getBigDecimal("appliedAmt");
			loanAmt = genericValue.getBigDecimal("loanAmt");
			loanAmtStr = loanAmt.intValue();
			String msgBody = "Your Loan of Kshs " + loanAmtStr + " has not been in service for over "
					+ noOfUuservicedDays + " days.Kindly Repay Your Loan Promptly.";

			log.info("&**************#Applied Amount##****####" + loanAmt);

			saveToTable = sendMessage.sendAndStroreRegistrationMessage(phoneNumber, msgBody);

			genericValue.set("msgSendStatus", "SEND");
			genericValue.set("msgStatusControl", SaveString);
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("##################   PHONENO_SMS_********##########+" + phoneNumber);
		}

		return saveToTable;
	}

	public static String sendMessagesForUnRepaidLoans() {
		String success = "Success";
		String sendMsgForThoseWithSixtyDays = null;
		String sendMsgForThoseWithSeventyDays = null;
		String sendMsgForThoseWithEigthyDays = null;

		log.info("&**************#REached sendMessagesForUnRepaidLoans METHOD##************####");

		try {
			sendMsgForThoseWithSixtyDays = sendMessage.loanNotInService(60, null, null, "SiDs");

			log.info("&***********Tried for the sixty Days RUN##************####");

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			sendMsgForThoseWithSeventyDays = sendMessage.loanNotInService(70, "SEND", "SiDs", "SeDs");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			sendMsgForThoseWithEigthyDays = sendMessage.loanNotInService(80, "SEND", "SeDs", "EiDs");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("------For 60 ^ --------" + sendMsgForThoseWithSixtyDays);
		System.out.println("------For 70 ^ --------" + sendMsgForThoseWithSeventyDays);
		System.out.println("------For 80 ^ --------" + sendMsgForThoseWithEigthyDays);

		log.info("&**************#REached sendMessagesForUnRepaidLoans METHOD AFTER SUCCESS##************####");

		return success;
	}

	
	
	//---------------TO Loanee And Guaranteers--------------------
	
	public static String send_Defaulter_SMS_Non_Payment_For_Three_Months() {

		//log.info("Your Loan of Kshs **sendDefaulterSMS** has not been in service for 3 months.It's due for attachment");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		int numberOfDays = 90;
		int numberControl = numberOfDays + 5;
		String sendStatus = "DEFAULTEDLOANSMS2MONTHSEND";
		String statusControl = "DEFAULTEDLOANSMS2MONTHSEND";
		String SaveString = "DEFAULTEDLOANSMSSEND";

		java.sql.Date today = new java.sql.Date(0);
		java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		LocalDate todayToLocalTime = new LocalDate(date);
		LocalDate todayToLocalTimeMinusRealDays = todayToLocalTime.minusDays(numberOfDays);
		Timestamp timestamp = new Timestamp(todayToLocalTimeMinusRealDays.toDateTimeAtStartOfDay().getMillis());

		LocalDate todayAdditionRealDays = todayToLocalTime.minusDays(numberControl);
		Timestamp timestampAddition = new Timestamp(todayAdditionRealDays.toDateTimeAtStartOfDay().getMillis());

		List<GenericValue> ListNotInServeForThreeMonths = null;
		BigDecimal zeroOutStandingBalance = BigDecimal.ZERO;
		String loanNo = null;
		// BigDecimal oneM = new BigDecimal(100000);

		log.info("--------------TIMESTAMP FOR DEFAULTER SMS---------------" + timestamp);
		log.info("--------------TIMESTAMP FOR DEFAULTER SMS ADDED DAYS---------------" + timestampAddition);

		EntityConditionList<EntityExpr> listConditionsForThree_Month_Non_Payment = EntityCondition.makeCondition(
				UtilMisc.toList(//EntityCondition.makeCondition("msgSendStatus", EntityOperator.EQUALS, ""),
						//EntityCondition.makeCondition("msgStatusControl", EntityOperator.EQUALS, statusControl),
						EntityCondition.makeCondition("outstandingBalance", EntityOperator.GREATER_THAN,
								zeroOutStandingBalance),
				EntityCondition.makeCondition("mobileNumber", EntityOperator.NOT_EQUAL, ""),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.LESS_THAN, timestamp),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.GREATER_THAN, timestampAddition)),
				EntityOperator.AND);
		try {
			ListNotInServeForThreeMonths = delegator.findList("LoanApplication", listConditionsForThree_Month_Non_Payment, null, null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String phoneNumber = null;
		BigDecimal appliedAmt = BigDecimal.ZERO;
		BigDecimal loanAmt = BigDecimal.ZERO;
		int loanAmtStr = 0;
		String saveToTable = null;
		
		int sizeOfReturnedValues = ListNotInServeForThreeMonths.size();
		
		log.info("----------sizeOfReturnedValues-ListNotInServeForThreeMonths------"+sizeOfReturnedValues);
		
		for (GenericValue genericValue : ListNotInServeForThreeMonths) {
			long loanApplicationId = genericValue.getLong("loanApplicationId");
			phoneNumber = genericValue.getString("mobileNumber");
			appliedAmt = genericValue.getBigDecimal("appliedAmt");
			loanAmt = genericValue.getBigDecimal("loanAmt");
			loanNo = genericValue.getString("loanNo");
			loanAmtStr = loanAmt.intValue();
			String msgBody = "Your Loan of Kshs " + appliedAmt
					+ " has not been in service for 3 months.It's due for attachment";
			
			log.info("---------------APPLIED___DEFAULTERSMS__LOAN_AMT" + loanAmt);
			log.info("---------------MOBILE NUMBER" + phoneNumber);

			saveToTable = sendMessage.sendAndStroreRegistrationMessage(phoneNumber, msgBody);

			List<GenericValue> guarantorList = null;

			EntityConditionList<EntityExpr> loanGuarantorConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, loanApplicationId)),
					EntityOperator.AND);
			try {
				guarantorList = delegator.findList("LoanGuarantor", loanGuarantorConditions, null, null, null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String fname = null;
			String mobileNo = null;
			Long guarantorsPartyId = null;

			String messageToSentToGuarantor = null;

			for (GenericValue genericGuarantor : guarantorList) {
				guarantorsPartyId = genericGuarantor.getLong("guarantorId");
				mobileNo = getGuarantorPhoneNumber(guarantorsPartyId);
				fname = getGuarantorName(guarantorsPartyId);
				messageToSentToGuarantor = "Dear " + fname + ", Loan of Loan No: "+loanNo+" has not been paid for 3 months and has been attached to you.";
				String saveGuarantorMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNo,
						messageToSentToGuarantor);
				System.out.println("" + saveGuarantorMessage);
			}

			genericValue.set("msgSendStatus", SaveString);
			//genericValue.set("msgStatusControl", SaveString);
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("-------------------------PHONENO--DEFAULTER_SMS_********---------------" + phoneNumber);
		}

		return "Execute Defaulter SmS Saving";

	}

	//---------------------------------
	//---------------TO Loanee And Guaranteers--------------------
	
	public static String send_Defaulter_SMS_Non_Payment_For_Two_Months() {

		//log.info("Your Loan of Kshs **sendDefaulterSMS** has not been in service for 3 months.It's due for attachment");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		int numberOfDays = 60;
		int numberControl = numberOfDays + 2;
		String sendStatus = "SendForNonPayment";
		String statusControl = "SendForNonPayment";
		String SaveString = "DEFAULTEDLOANSMS2MONTHSEND";

		java.sql.Date today = new java.sql.Date(0);
		java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		LocalDate todayToLocalTime = new LocalDate(date);
		LocalDate todayToLocalTimeMinusRealDays = todayToLocalTime.minusDays(numberOfDays);
		Timestamp timestamp = new Timestamp(todayToLocalTimeMinusRealDays.toDateTimeAtStartOfDay().getMillis());

		LocalDate todayAdditionRealDays = todayToLocalTime.minusDays(numberControl);
		Timestamp timestampAddition = new Timestamp(todayAdditionRealDays.toDateTimeAtStartOfDay().getMillis());

		List<GenericValue> ListNotInServeForTwoMonths = null;
		BigDecimal zeroOutStandingBalance = BigDecimal.ZERO;
		String loanNo = null;
		// BigDecimal oneM = new BigDecimal(100000);

		log.info("--------------TIMESTAMP FOR DEFAULTER SMS TWO MONTHS---------------" + timestamp);
		log.info("--------------TIMESTAMP FOR DEFAULTER SMS ADDED DAYS TWO MONTHS---------------" + timestampAddition);

		EntityConditionList<EntityExpr> listConditionsForTwo_Month_Non_Payment = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("msgSendStatus", EntityOperator.EQUALS, sendStatus),
						//EntityCondition.makeCondition("msgStatusControl", EntityOperator.EQUALS, statusControl),
						EntityCondition.makeCondition("outstandingBalance", EntityOperator.GREATER_THAN,
								zeroOutStandingBalance),
				EntityCondition.makeCondition("mobileNumber", EntityOperator.NOT_EQUAL, ""),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.LESS_THAN, timestamp),
				EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.GREATER_THAN, timestampAddition)),
				EntityOperator.AND);
		try {
			ListNotInServeForTwoMonths = delegator.findList("LoanApplication", listConditionsForTwo_Month_Non_Payment, null, null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String phoneNumber = null;
		BigDecimal appliedAmt = BigDecimal.ZERO;
		BigDecimal loanAmt = BigDecimal.ZERO;
		int loanAmtStr = 0;
		String saveToTable = null;
		for (GenericValue genericValue : ListNotInServeForTwoMonths) {
			long loanApplicationId = genericValue.getLong("loanApplicationId");
			phoneNumber = genericValue.getString("mobileNumber");
			appliedAmt = genericValue.getBigDecimal("appliedAmt");
			loanAmt = genericValue.getBigDecimal("loanAmt");
			loanNo = genericValue.getString("loanNo");
			loanAmtStr = loanAmt.intValue();
			String msgBody = "Your Loan of Kshs " + loanAmtStr
					+ " has not been in service for 2 months.Kindly repay Your Loan Promptly";
			
			log.info("---------------APPLIED___DEFAULTERSMS__LOAN_AMT--TWO MONTHS" + loanAmt);

			saveToTable = sendMessage.sendAndStroreRegistrationMessage(phoneNumber, msgBody);

			List<GenericValue> guarantorList = null;

			EntityConditionList<EntityExpr> loanGuarantorConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, loanApplicationId)),
					EntityOperator.AND);
			try {
				guarantorList = delegator.findList("LoanGuarantor", loanGuarantorConditions, null, null, null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String fname = null;
			String mobileNo = null;
			Long guarantorsPartyId = null;

			String messageToSentToGuarantor = null;

			for (GenericValue genericGuarantor : guarantorList) {
				guarantorsPartyId = genericGuarantor.getLong("guarantorId");
				mobileNo = getGuarantorPhoneNumber(guarantorsPartyId);
				fname = getGuarantorName(guarantorsPartyId);
				messageToSentToGuarantor = "Dear " + fname + ", Loan of Loan No: "+loanNo+" has not been Paid for 2 Months. Chai Sacco";
				String saveGuarantorMessage = sendMessage.sendAndStroreRegistrationMessage(mobileNo,
						messageToSentToGuarantor);
				System.out.println("" + saveGuarantorMessage);
			}

			genericValue.set("msgSendStatus", "DEFAULTEDLOANSMS2MONTHSEND");
			//genericValue.set("msgStatusControl", SaveString);
			try {
				delegator.createOrStore(genericValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("##################   PHONENO--DEFAULTER_SMS_********##########+" + phoneNumber);
		}

		return "Execute Defaulter SmS Saving";

	}

	
	
	// ---------------SMS Notification on LOan Non Payment For More than One
	// Month------------------------

	public static String SMS_OnLoanNonPayment_ToLoanee_One_Month() {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		int numberOfDays = 30;
		int numberControl = numberOfDays + 2;
		String msgReturn = "---Send For Non Payment For Thirty Days Method Succes --------";

		java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		LocalDate todayToLocalTime = new LocalDate(date);
		LocalDate todayToLocalTimeMinusThirtyDays = todayToLocalTime.minusDays(numberOfDays);
		Timestamp timestamp = new Timestamp(todayToLocalTimeMinusThirtyDays.toDateTimeAtStartOfDay().getMillis());

		LocalDate todayAdditionMinusThirtyDays = todayToLocalTime.minusDays(numberControl);
		Timestamp timestampAdditionMinusThirtyDays = new Timestamp(
				todayAdditionMinusThirtyDays.toDateTimeAtStartOfDay().getMillis());

		List<GenericValue> ListNotInServeForOneMonth = null;
		BigDecimal zeroOutStandingBalance = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> listConditionsFotLoanNonPaymentThirtyDays = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("msgSendStatus", EntityOperator.EQUALS, null),
						EntityCondition.makeCondition("msgStatusControl", EntityOperator.EQUALS, null),
						EntityCondition.makeCondition("outstandingBalance", EntityOperator.GREATER_THAN,
								zeroOutStandingBalance),
						EntityCondition.makeCondition("mobileNumber", EntityOperator.NOT_EQUAL, ""),
						EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.LESS_THAN, timestamp),
						EntityCondition.makeCondition("lastRepaymentDate", EntityOperator.GREATER_THAN,
								timestampAdditionMinusThirtyDays)),
				EntityOperator.AND);

		String phoneNumber = null;
		BigDecimal appliedAmt = BigDecimal.ZERO;
		BigDecimal loanAmt = BigDecimal.ZERO;
		int loanAmtinStringFormat = 0;
		String saveTheMessageOfNonPayment = null;
		try {

			ListNotInServeForOneMonth = delegator.findList("LoanApplication", listConditionsFotLoanNonPaymentThirtyDays,
					null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueNonPayment : ListNotInServeForOneMonth) {
			phoneNumber = genericValueNonPayment.getString("mobileNumber");
			appliedAmt = genericValueNonPayment.getBigDecimal("appliedAmt");
			loanAmt = genericValueNonPayment.getBigDecimal("loanAmt");
			loanAmtinStringFormat = loanAmt.intValue();
			String msgBody = "Your Loan of Kshs " + loanAmtinStringFormat
					+ " has not been in service for over 30 days days.Kindly Repay Your Loan Promptly.";

			log.info("-----------APPLIED(LOAN AMOUNT)----------------" + loanAmt);

			saveTheMessageOfNonPayment = sendMessage.sendAndStroreRegistrationMessage(phoneNumber, msgBody);

			genericValueNonPayment.set("msgSendStatus", "SendForNonPayment");
			//genericValueNonPayment.set("msgStatusControl", "SendForNonPayment");
			try {
				delegator.createOrStore(genericValueNonPayment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return msgReturn;

	}

}
