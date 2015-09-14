package restcomponent;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.accountholdertransactions.Ministatement;
import org.ofbiz.accountholdertransactions.Transaction;
import org.ofbiz.accountholdertransactions.model.ATMTransaction;
import org.ofbiz.atmmanagement.ATMManagementServices;
import org.ofbiz.atmmanagement.ATMStatus;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.msaccomanagement.MSaccoManagementServices;

import com.google.gson.Gson;

@Path("/atm")
public class ATMServices {

	@GET
	@Produces("application/json")
	@Path("/balance/{cardNumber}")
	public Response getBalance(@PathParam("cardNumber") String cardNumber) {
		System.out.println(" GGGGGGGGGG Get Card Balance ###############");
		BigDecimal bdBalance = null;
		BigDecimal bdAvailableBalance = null;
		BigDecimal bdBookBalance = null;

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		if (atmStatus.getStatus().equals("SUCCESS")) {
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			bdBalance = AccHolderTransactionServices.getTotalSavings(String
					.valueOf(atmStatus.getCardApplication().getLong(
							"memberAccountId")), delegator);
//			bdBalance = AccHolderTransactionServices.getTotalBalance(String
//					.valueOf(atmStatus.getCardApplication().getLong(
//							"memberAccountId")), new Timestamp(Calendar
//					.getInstance().getTimeInMillis()));
		}

		if (bdBalance != null) {
			// transaction.setAmount(bdBalance);
			bdAvailableBalance = AccHolderTransactionServices
					.getTotalBalanceNow(String.valueOf(atmStatus
							.getCardApplication().getLong("memberAccountId")));
//			bdBookBalance = AccHolderTransactionServices.getBookBalanceVer2(
//					String.valueOf(atmStatus.getCardApplication().getLong(
//							"memberAccountId")),
//					DelegatorFactoryImpl.getDelegator(null));

			bdBookBalance = AccHolderTransactionServices.getBookBalance(
			String.valueOf(atmStatus.getCardApplication().getLong(
					"memberAccountId")),
			DelegatorFactoryImpl.getDelegator(null));

			//getBookBalance
			transaction.setAvailableBalance(bdAvailableBalance);
			transaction.setBookBalance(bdBookBalance);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setStatus(atmStatus.getStatus());
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			System.out.println("AAAAAAAAAAAAAAAAVVVVVVVVVVVV --- "
					+ bdAvailableBalance);
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBB ---- "
					+ bdBookBalance);
		}
		transaction.setStatus(atmStatus.getStatus());

		System.out.println("ATM TRANSACTION ---- " + transaction.toString());

		addServiceLog(cardNumber, "BALANCE", transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	private void addServiceLog(String cardNumber, String transactionType, String transactionStatus) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue atmTransactionsLog = null;
		String atmransactionsLogId = delegator.getNextSeqId("ATMTransactionsLog");
		atmTransactionsLog = delegator.makeValidValue("ATMTransactionsLog", UtilMisc
				.toMap("atmransactionsLogId", atmransactionsLogId, "cardNumber",
						cardNumber, "transactionType", transactionType, "transactionStatus", transactionStatus));
		try {
			delegator.createOrStore(atmTransactionsLog);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	private void addServiceLog(String cardNumber, String transactionType, BigDecimal transactionAmount, String transactionStatus) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue atmTransactionsLog = null;
		String atmransactionsLogId = delegator.getNextSeqId("ATMTransactionsLog");
		atmTransactionsLog = delegator.makeValidValue("ATMTransactionsLog", UtilMisc
				.toMap("atmransactionsLogId", atmransactionsLogId, "cardNumber",
						cardNumber, "transactionType", transactionType, "transactionAmount", transactionAmount, "transactionStatus", transactionStatus));
		try {
			delegator.createOrStore(atmTransactionsLog);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	

	@GET
	@Produces("application/json")
	@Path("/withdrawal/{cardNumber}/{amount}")
	public Response withdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount, @PathParam("SystemTrace") String SystemTrace) {

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());

		if (atmStatus.getStatus().equals("SUCCESS")) {
			String memberAccountId = atmStatus.getCardApplication()
					.getLong("memberAccountId").toString();
			memberAccountId = memberAccountId.replaceAll(",", "");
			System.out.println("AAAAAAAAAAAAA Account ID " + memberAccountId);

			// Check if Member Has Enough Money - Limit, charges
			transaction = AccHolderTransactionServices.cashWithdrawal(amount,
					String.valueOf(memberAccountId), "ATMWITHDRAWAL", SystemTrace);
			// transaction.setStatus("NOTENOUGHMONEY");
			// transaction.setStatus(atmStatus.getStatus());
			transaction.setCardNumber(cardNumber);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setMemberAccountId(atmStatus.getCardApplication()
					.getLong("memberAccountId"));
			// getMemberAccount(atmStatus.getCardApplication().getLong("memberAccountId"));
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));

		}

		addServiceLog(cardNumber, "WITHDRAWAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/withdrawal/{cardNumber}/{amount}/{SystemTrace}")
	public Response atmWithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace) {

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());

		if (atmStatus.getStatus().equals("SUCCESS")) {
			String memberAccountId = atmStatus.getCardApplication()
					.getLong("memberAccountId").toString();
			memberAccountId = memberAccountId.replaceAll(",", "");
			System.out.println("AAAAAAAAAAAAA Account ID " + memberAccountId);

			// Check if Member Has Enough Money - Limit, charges
			transaction = AccHolderTransactionServices.cashWithdrawal(amount,
					String.valueOf(memberAccountId), "ATMWITHDRAWAL", SystemTrace);
			// transaction.setStatus("NOTENOUGHMONEY");
			// transaction.setStatus(atmStatus.getStatus());
			transaction.setCardNumber(cardNumber);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setMemberAccountId(atmStatus.getCardApplication()
					.getLong("memberAccountId"));
			// getMemberAccount(atmStatus.getCardApplication().getLong("memberAccountId"));
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));

			// Save ATMTransaction in t_ATMTransactions with fields
			// (AccountID, SystemTrace, Amount, TransactionDate)
			createATMTransactionRecord(memberAccountId, SystemTrace,
					String.valueOf(transaction.getTransactionId()), amount,
					new Timestamp(Calendar.getInstance().getTimeInMillis()));
		}

		addServiceLog(cardNumber, "WITHDRAWAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	private void createATMTransactionRecord(String memberAccountId,
			String systemTrace, String transactionId, BigDecimal amount,
			Timestamp timestamp) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue atmTransactions = null;
		atmTransactions = delegator.makeValidValue("ATMTransactions", UtilMisc
				.toMap("AccountID", memberAccountId, "SystemTrace",
						systemTrace, "transactionId", transactionId, "Amount",
						amount, "TransactionDate", timestamp));
		try {
			delegator.createOrStore(atmTransactions);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces("application/json")
	@Path("/deposit/{cardNumber}/{amount}")
	public Response deposit(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount) {
		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());

		if (atmStatus.getStatus().equals("SUCCESS"))
			transaction.setStatus("CANNOTDEPOSIT");

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	// transactionId
	@GET
	@Produces("application/json")
	@Path("/reverseatmwithdrawal/{cardNumber}/{amount}/{SystemTrace}/{transactionId}")
	public Response reverseatmwithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace,
			@PathParam("transactionId") String transactionId) {
		//Get all the Account Transaction Items, given the transactionId and
		//Set the increaseDecrease to Reversed
		//AccountTransaction
		
		
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		GenericValue cardApplication = ATMManagementServices.getMemberATMApplication(cardNumber);
		String memberAccountId = cardApplication.getString("memberAccountId");
		accountTransaction = delegator.makeValue("AccountTransaction");
		String accountTransactionId = delegator.getNextSeqId("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);
		accountTransaction.set("accountTransactionId", accountTransactionId);
		accountTransaction.set("increaseDecrease", "I");
		accountTransaction.set("transactionType", "ATMWITHDRAWALREVERSAL");


		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");
		accountTransaction.setString("createdBy", userLogin.get("userLoginId"));
		
		GenericValue memberAccount = LoanUtilities.getEntityValue("MemberAccount", "memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.set("partyId", memberAccount.getLong("partyId"));
		
//		try {
//			delegator.createOrStore(accountTransaction);
//		} catch (GenericEntityException e) {
//			e.printStackTrace();
//		}
		//AccHolderTransactionServices.cashWithdrawalATMReversal(accountTransaction, userLogin, "ATMWITHDRAWALREVERSAL", SystemTrace);
		String reverseStatus = AccHolderTransactionServices.reverseATMTransaction(SystemTrace, "ATMWITHDRAWAL", "ATMWITHDRAWALREVERSAL");
		//Reverse GL
		
		//Now build the transaction and return it as json
		
		BigDecimal bdBalance = null;
		BigDecimal bdAvailableBalance = null;
		BigDecimal bdBookBalance = null;

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		if (atmStatus.getStatus().equals("SUCCESS")) {
			bdBalance = AccHolderTransactionServices.getTotalBalance(String
					.valueOf(atmStatus.getCardApplication().getLong(
							"memberAccountId")), new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
		}

		if (bdBalance != null) {
			// transaction.setAmount(bdBalance);
			bdAvailableBalance = AccHolderTransactionServices
					.getAvailableBalanceVer3(memberAccountId, new Timestamp(
							Calendar.getInstance().getTimeInMillis()));
			transaction.setAvailableBalance(bdAvailableBalance);
			bdBookBalance = AccHolderTransactionServices
					.getBookBalanceVer3(memberAccountId, delegator);
			transaction.setBookBalance(bdBookBalance);
					
			transaction.setAvailableBalance(bdAvailableBalance);
			transaction.setBookBalance(bdBookBalance);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setStatus(atmStatus.getStatus());
			memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			System.out.println("AAAAAAAAAAAAAAAAVVVVVVVVVVVV --- "
					+ bdAvailableBalance);
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBB ---- "
					+ bdBookBalance);
		}
		if (reverseStatus.equals("success"))
		{
			transaction.setStatus(atmStatus.getStatus());
		} else if (reverseStatus.equals("notransaction")){
			transaction.setStatus("NOTRANSACTION");
		} else if (reverseStatus.equals("reversed")){
			transaction.setStatus("REVRESEDALREADY");
		}
		

		System.out.println("ATM REVERSAL TRANSACTION ---- " + transaction.toString());
		addServiceLog(cardNumber, "ATMWITHDRAWALREVERSAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	// VISAWITHDRAW
	@GET
	@Produces("application/json")
	@Path("/visawithdrawal/{cardNumber}/{amount}/{SystemTrace}")
	public Response visawithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace) {

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());

		if (atmStatus.getStatus().equals("SUCCESS")) {
			String memberAccountId = atmStatus.getCardApplication()
					.getLong("memberAccountId").toString();
			memberAccountId = memberAccountId.replaceAll(",", "");
			System.out.println("AAAAAAAAAAAAA Account ID " + memberAccountId);

			// Check if Member Has Enough Money - Limit, charges
			
			//transaction = AccHolderTransactionServices.cashWithdrawal(amount,
			//		String.valueOf(memberAccountId), "VISAWITHDRAW");
			
			transaction = AccHolderTransactionServices.cashWithdrawal(amount,
					String.valueOf(memberAccountId), "VISAWITHDRAWAL", SystemTrace);
			// transaction.setStatus("NOTENOUGHMONEY");
			// transaction.setStatus(atmStatus.getStatus());
			transaction.setCardNumber(cardNumber);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setMemberAccountId(atmStatus.getCardApplication()
					.getLong("memberAccountId"));
			// getMemberAccount(atmStatus.getCardApplication().getLong("memberAccountId"));
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));

			// Save ATMTransaction in t_ATMTransactions with fields
			// (AccountID, SystemTrace, Amount, TransactionDate)
			createATMTransactionRecord(memberAccountId, SystemTrace,
					String.valueOf(transaction.getTransactionId()), amount,
					new Timestamp(Calendar.getInstance().getTimeInMillis()));
		}
		addServiceLog(cardNumber, "VISAWITHDRAWAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/poscashpurchase/{cardNumber}/{amount}/{SystemTrace}")
	public Response poscashpurchase(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace) {
		
		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());

		if (atmStatus.getStatus().equals("SUCCESS")) {
			String memberAccountId = atmStatus.getCardApplication()
					.getLong("memberAccountId").toString();
			memberAccountId = memberAccountId.replaceAll(",", "");
			System.out.println("AAAAAAAAAAAAA Account ID " + memberAccountId);

			// Check if Member Has Enough Money - Limit, charges
//			transaction = AccHolderTransactionServices.cashWithdrawal(amount,
//					String.valueOf(memberAccountId), "POSCASHPURCHASE");
			
			transaction = AccHolderTransactionServices.cashWithdrawal(amount,
					String.valueOf(memberAccountId), "POSWITHDRAWAL", SystemTrace);
			// transaction.setStatus("NOTENOUGHMONEY");
			// transaction.setStatus(atmStatus.getStatus());
			transaction.setCardNumber(cardNumber);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setMemberAccountId(atmStatus.getCardApplication()
					.getLong("memberAccountId"));
			// getMemberAccount(atmStatus.getCardApplication().getLong("memberAccountId"));
			GenericValue memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));

			// Save ATMTransaction in t_ATMTransactions with fields
			// (AccountID, SystemTrace, Amount, TransactionDate)
			createATMTransactionRecord(memberAccountId, SystemTrace,
					String.valueOf(transaction.getTransactionId()), amount,
					new Timestamp(Calendar.getInstance().getTimeInMillis()));
		}

		addServiceLog(cardNumber, "POSWITHDRAWAL", transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}
	
	/****
	 * ministatement
	 * */
	@GET
	@Produces("application/json")
	@Path("/ministatement/{cardNumber}/{transactionType}")
	public Response atmWithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("transactionType") String transactionType) {

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		// .getMSaccoAccount(phoneNumber);

		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		transaction.setStatus(atmStatus.getStatus());
		List<Ministatement> listMinistatement = null;
		if (atmStatus.getStatus().equals("SUCCESS")) {
			//partyId
			GenericValue card = ATMManagementServices.getMemberATMApplication(cardNumber);
			Long memberId = card.getLong("partyId");
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			EntityConditionList<EntityExpr> transactionConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS,
							Long.valueOf(memberId))), EntityOperator.AND);
			List<GenericValue> accountTransactionELI = null;
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("createdStamp DESC");
			try {
				accountTransactionELI = delegator.findList("AccountTransaction",
						transactionConditions, null, orderByList, null, false);

			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
			
			int MAXCOUNT = 10;
			
			if (accountTransactionELI.size() < MAXCOUNT){
				MAXCOUNT = accountTransactionELI.size();
			}
			
			List<GenericValue> statementList = accountTransactionELI.subList(0, MAXCOUNT);

			//accountTransactionELI = 
			//Transaction transaction;

			/***
			 * public String transactionType; public BigDecimal transactionAmount;
			 * public Timestamp createdStamp;
			 * 
			 * public String accountNo; public String accountName;
			 * 
			 * **/
			GenericValue memberAccount = null;

			listMinistatement = new ArrayList<Ministatement>();
			Ministatement ministatement = null;
			Timestamp dateCreated;

			for (GenericValue genericValue : statementList) {
				ministatement = new Ministatement();

				ministatement.setTransactionType(genericValue
						.getString("transactionType"));
				
				ministatement.setAmount(genericValue
						.getBigDecimal("transactionAmount"));

				// genericValue.getT
				dateCreated = new Timestamp(genericValue.getTimestamp(
						"createdStamp").getTime());
				DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
				ministatement.setDate(dateTimeCreated.toString("yyyy-MM-dd HH:mm:ss"));
				try {
					memberAccount = delegator
							.findOne("MemberAccount", UtilMisc.toMap(
									"memberAccountId",
									genericValue.getLong("memberAccountId")), false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				
				String increaseDecrease = genericValue.getString("increaseDecrease");
				String postingType = "";
				if (increaseDecrease.equals("I")){
					postingType = "Cr";
				} else{
					postingType = "Dr";
				}
				
				ministatement.setPostingType(postingType);
				listMinistatement.add(ministatement);
			}

		}

		addServiceLog(cardNumber, "MINISTATEMENT", null, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(listMinistatement);

		return Response.ok(json).type("application/json").build();
	}
	
	
	/****
	 * VISAWITHDRAWAL REVERSAL
	 * */
	@GET
	@Produces("application/json")
	@Path("/reversevisawithdrawal/{cardNumber}/{amount}/{SystemTrace}/{transactionId}")
	public Response reversevisawithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace,
			@PathParam("transactionId") String transactionId) {
		//Get all the Account Transaction Items, given the transactionId and
		//Set the increaseDecrease to Reversed
		//AccountTransaction
		
		
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		GenericValue cardApplication = ATMManagementServices.getMemberATMApplication(cardNumber);
		String memberAccountId = cardApplication.getString("memberAccountId");
		accountTransaction = delegator.makeValue("AccountTransaction");
		String accountTransactionId = delegator.getNextSeqId("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);
		accountTransaction.set("accountTransactionId", accountTransactionId);
		accountTransaction.set("increaseDecrease", "I");
		accountTransaction.set("transactionType", "VISAWITHDRAWALREVERSAL");


		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");
		accountTransaction.setString("createdBy", userLogin.get("userLoginId"));
		
		GenericValue memberAccount = LoanUtilities.getEntityValue("MemberAccount", "memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.set("partyId", memberAccount.getLong("partyId"));
		
//		try {
//			delegator.createOrStore(accountTransaction);
//		} catch (GenericEntityException e) {
//			e.printStackTrace();
//		}
		//AccHolderTransactionServices.cashWithdrawalATMReversal(accountTransaction, userLogin, "ATMWITHDRAWALREVERSAL", SystemTrace);
		String reverseStatus = AccHolderTransactionServices.reverseATMTransaction(SystemTrace, "VISAWITHDRAWAL", "VISAWITHDRAWALREVERSAL");
		//Reverse GL
		
		//Now build the transaction and return it as json
		
		BigDecimal bdBalance = null;
		BigDecimal bdAvailableBalance = null;
		BigDecimal bdBookBalance = null;

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		if (atmStatus.getStatus().equals("SUCCESS")) {
			bdBalance = AccHolderTransactionServices.getTotalBalance(String
					.valueOf(atmStatus.getCardApplication().getLong(
							"memberAccountId")), new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
		}

		if (bdBalance != null) {
			// transaction.setAmount(bdBalance);
			bdAvailableBalance = AccHolderTransactionServices
					.getAvailableBalanceVer3(memberAccountId, new Timestamp(
							Calendar.getInstance().getTimeInMillis()));
			transaction.setAvailableBalance(bdAvailableBalance);
			bdBookBalance = AccHolderTransactionServices
					.getBookBalanceVer3(memberAccountId, delegator);
			transaction.setBookBalance(bdBookBalance);
					
			transaction.setAvailableBalance(bdAvailableBalance);
			transaction.setBookBalance(bdBookBalance);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setStatus(atmStatus.getStatus());
			memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			System.out.println("AAAAAAAAAAAAAAAAVVVVVVVVVVVV --- "
					+ bdAvailableBalance);
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBB ---- "
					+ bdBookBalance);
		}
		if (reverseStatus.equals("success"))
		{
			transaction.setStatus(atmStatus.getStatus());
		} else if (reverseStatus.equals("notransaction")){
			transaction.setStatus("NOTRANSACTION");
		} else if (reverseStatus.equals("reversed")){
			transaction.setStatus("REVRESEDALREADY");
		}
		

		System.out.println("ATM REVERSAL TRANSACTION ---- " + transaction.toString());
		addServiceLog(cardNumber, "ATMWITHDRAWALREVERSAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	
	/****
	 * POSWITHDRAWALREVERSAL
	 * */
	@GET
	@Produces("application/json")
	@Path("/reverseposwithdrawal/{cardNumber}/{amount}/{SystemTrace}/{transactionId}")
	public Response reverseposwithdrawal(@PathParam("cardNumber") String cardNumber,
			@PathParam("amount") BigDecimal amount,
			@PathParam("SystemTrace") String SystemTrace,
			@PathParam("transactionId") String transactionId) {
		//Get all the Account Transaction Items, given the transactionId and
		//Set the increaseDecrease to Reversed
		//AccountTransaction
		
		
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		GenericValue cardApplication = ATMManagementServices.getMemberATMApplication(cardNumber);
		String memberAccountId = cardApplication.getString("memberAccountId");
		accountTransaction = delegator.makeValue("AccountTransaction");
		String accountTransactionId = delegator.getNextSeqId("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);
		accountTransaction.set("accountTransactionId", accountTransactionId);
		accountTransaction.set("increaseDecrease", "I");
		accountTransaction.set("transactionType", "POSWITHDRAWALREVERSAL");


		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");
		accountTransaction.setString("createdBy", userLogin.get("userLoginId"));
		
		GenericValue memberAccount = LoanUtilities.getEntityValue("MemberAccount", "memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.set("partyId", memberAccount.getLong("partyId"));
		
//		try {
//			delegator.createOrStore(accountTransaction);
//		} catch (GenericEntityException e) {
//			e.printStackTrace();
//		}
		//AccHolderTransactionServices.cashWithdrawalATMReversal(accountTransaction, userLogin, "ATMWITHDRAWALREVERSAL", SystemTrace);
		String reverseStatus = AccHolderTransactionServices.reverseATMTransaction(SystemTrace, "POSWITHDRAWAL", "POSWITHDRAWALREVERSAL");
		//Reverse GL
		
		//Now build the transaction and return it as json
		
		BigDecimal bdBalance = null;
		BigDecimal bdAvailableBalance = null;
		BigDecimal bdBookBalance = null;

		ATMStatus atmStatus = ATMManagementServices.getATMAccount(cardNumber);
		ATMTransaction transaction = new ATMTransaction();
		transaction.setCardNumber(cardNumber);
		if (atmStatus.getStatus().equals("SUCCESS")) {
			bdBalance = AccHolderTransactionServices.getTotalBalance(String
					.valueOf(atmStatus.getCardApplication().getLong(
							"memberAccountId")), new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
		}

		if (bdBalance != null) {
			// transaction.setAmount(bdBalance);
			bdAvailableBalance = AccHolderTransactionServices
					.getAvailableBalanceVer3(memberAccountId, new Timestamp(
							Calendar.getInstance().getTimeInMillis()));
			transaction.setAvailableBalance(bdAvailableBalance);
			bdBookBalance = AccHolderTransactionServices
					.getBookBalanceVer3(memberAccountId, delegator);
			transaction.setBookBalance(bdBookBalance);
					
			transaction.setAvailableBalance(bdAvailableBalance);
			transaction.setBookBalance(bdBookBalance);
			transaction.setCardStatusId(atmStatus.getCardStatusId());
			transaction.setCardStatus(atmStatus.getCardStatus());
			transaction.setStatus(atmStatus.getStatus());
			memberAccount = AccHolderTransactionServices
					.getMemberAccount(atmStatus.getCardApplication().getLong(
							"memberAccountId"));
			transaction.setMemberAccountId(memberAccount
					.getLong("memberAccountId"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			transaction.setAccountName(memberAccount.getString("accountName"));
			System.out.println("AAAAAAAAAAAAAAAAVVVVVVVVVVVV --- "
					+ bdAvailableBalance);
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBB ---- "
					+ bdBookBalance);
		}
		if (reverseStatus.equals("success"))
		{
			transaction.setStatus(atmStatus.getStatus());
		} else if (reverseStatus.equals("notransaction")){
			transaction.setStatus("NOTRANSACTION");
		} else if (reverseStatus.equals("reversed")){
			transaction.setStatus("REVRESEDALREADY");
		}
		

		System.out.println("POSWITHDRAWAL TRANSACTION ---- " + transaction.toString());
		addServiceLog(cardNumber, "POSWITHDRAWAL", amount, transaction.getStatus());
		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	

}
