package restcomponent;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import javolution.util.FastMap;

import org.joda.time.DateTime;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import restcomponent.model.LoanAccount;
import restcomponent.model.LoanAccountTransaction;
import restcomponent.model.StatementAccount;
import restcomponent.model.StatementAccountTransaction;

import com.google.gson.Gson;

@Path("/statement")
public class MemberStatement {

	@GET
	// @Produces("text/plain")
	@Produces("application/json")
	@Path("{user}")
	public Response getStatement(@PathParam("user") String user) {

		System.out.println(" Testing for this user ########### " + user);

		String username = null;
		String password = null;

		GenericDelegator delegator = (GenericDelegator) DelegatorFactory
				.getDelegator("default");

		GenericDispatcherFactory genericDispatcherFactory = new GenericDispatcherFactory();
		LocalDispatcher dispatcher = genericDispatcherFactory
				.createLocalDispatcher("default", delegator);
		// Map<String, String> paramMap = UtilMisc.toMap("message", message);

		Map<String, String> paramMap = UtilMisc.toMap("user", user,
				"login.username", username, "login.password", password);

		Map<String, Object> result = FastMap.newInstance();

		try {
			result = dispatcher.runSync("statement", paramMap);
		} catch (GenericServiceException e1) {
			Debug.logError(e1, PingResource.class.getName());
			return Response.serverError().entity(e1.toString()).build();
		}

		if (ServiceUtil.isSuccess(result)) {
			// text/json
			// return Response.ok("RESPONSE: *** " + result.get("transactions")
			// + " ***").type("text/plain").build();
			return Response.ok(result.get("transactions"))
					.type("application/json").build();
		}

		if (ServiceUtil.isError(result) || ServiceUtil.isFailure(result)) {
			return Response.serverError()
					.entity(ServiceUtil.getErrorMessage(result)).build();
		}
		// shouldn't ever get here ... should we?
		throw new RuntimeException("Invalid ");
	}

	@GET
	@Produces("application/json")
	@Path("/accounts/{user}")
	public Response getAccountBalances(@PathParam("user") String user) {
		BigDecimal bdBalance = null;

		List<StatementAccount> listStatmentAccount = new ArrayList<StatementAccount>();
		StatementAccount statementAccount;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Give User Login get the :-
		GenericValue userLogin = null;
		try {
			userLogin = delegator.findOne("UserLogin",
					UtilMisc.toMap("userLoginId", user), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String partyIdStr = userLogin.getString("partyId");

		if ((partyIdStr == null) || (partyIdStr.equals(""))) {
			return null;
		}
		partyIdStr = partyIdStr.replaceAll(",", "");
		Long partyId = Long.valueOf(partyIdStr);

		// accounts subscribed to
		List<GenericValue> memberAccountBalanceELI = null;
		try {
			memberAccountBalanceELI = delegator.findList(
					"MemberAccountBalance",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// private Long memberAccountId;
		// private String accountCode;
		// private String accountName;
		// private BigDecimal bdAvailableBalance;
		// private BigDecimal bdBookBalance;

		for (GenericValue genericValue : memberAccountBalanceELI) {
			// Build accounts list
			statementAccount = new StatementAccount();
			Long memberAccountId = genericValue.getLong("memberAccountId");
			statementAccount.setMemberAccountId(memberAccountId);
			statementAccount.setProductName(genericValue.getString("name"));
			statementAccount
					.setAccountCode(genericValue.getString("accountNo"));
			statementAccount.setAccountName(genericValue
					.getString("accountName"));
			statementAccount.setBdAvailableBalance(AccHolderTransactionServices
					.getTotalBalanceNow(memberAccountId.toString()));
			statementAccount.setBdBookBalance(AccHolderTransactionServices
					.getBookBalanceNow(memberAccountId.toString()));
			statementAccount.setIsLoan("N");

			listStatmentAccount.add(statementAccount);
		}
		

		Gson gson = new Gson();
		String json = gson.toJson(listStatmentAccount);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/accounts/{memberAccountId}/{isLoan}")
	public Response getTransactions(
			@PathParam("memberAccountId") Long memberAccountId,
			@PathParam("isLoan") String isLoan) {
		BigDecimal bdBalance = null;

		List<StatementAccountTransaction> listStatmentAccountTransaction = new ArrayList<StatementAccountTransaction>();
		StatementAccountTransaction statementAccountTransaction;
		// Give memberAccountId if
		// private Long memberAccountId;
		// private Timestamp transactionDate;
		// private String description;
		// private BigDecimal amount;
		// private String accountNo;
		// private String accountName;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		if (isLoan.equals("N"))
		// Account - get transactions to this account
		{
			// Add Account Transactions
			EntityConditionList<EntityExpr> transactionConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("memberAccountId",
									EntityOperator.EQUALS, memberAccountId)),
							EntityOperator.AND);
			List<GenericValue> accountTransactionELI = null;
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("createdStamp DESC");
			try {
				accountTransactionELI = delegator.findList(
						"AccountTransaction", transactionConditions, null,
						orderByList, null, false);

			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}

			/***
			 * public String transactionType; public BigDecimal
			 * transactionAmount; public Timestamp createdStamp;
			 * 
			 * public String accountNo; public String accountName;
			 * 
			 * **/
			GenericValue memberAccount = null;

			try {
				memberAccount = delegator.findOne("MemberAccount",
						UtilMisc.toMap("memberAccountId", memberAccountId),
						false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			// Add Opening Balance
			EntityConditionList<EntityExpr> openingBalanceConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("memberAccountId",
									EntityOperator.EQUALS, memberAccountId)),
							EntityOperator.AND);
			List<GenericValue> memberAccountDetailsELI = null;
			List<String> openingOrderByList = new ArrayList<String>();
			openingOrderByList.add("createdStamp DESC");
			try {
				memberAccountDetailsELI = delegator.findList(
						"MemberAccountDetails", openingBalanceConditions, null,
						orderByList, null, false);

			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
			BigDecimal bdCurrentBalanceAmount = BigDecimal.ZERO;
			for (GenericValue genericValueOpening : memberAccountDetailsELI) {
				statementAccountTransaction = new StatementAccountTransaction();
				statementAccountTransaction.setMemberAccountId(memberAccountId);
				// statementAccountTransaction.setTransactionDate(genericValueOpening.getTimestamp("openingBalanceDate"));
				statementAccountTransaction.setDescription("Opening Balance");
				// transaction.setTransactionType();
				statementAccountTransaction.setAmount(genericValueOpening
						.getBigDecimal("savingsOpeningBalance"));
				statementAccountTransaction.setDebitCredit("C");
				// transaction.setTransactionAmount();

				// genericValue.getT
				Timestamp dateCreated = new Timestamp(genericValueOpening
						.getTimestamp("openingBalanceDate").getTime());
				DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
				// transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));

				statementAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				statementAccountTransaction.setAccountName(memberAccount
						.getString("accountName"));
				statementAccountTransaction.setAccountNo(memberAccount
						.getString("accountNo"));

				bdCurrentBalanceAmount = bdCurrentBalanceAmount
						.add(genericValueOpening
								.getBigDecimal("savingsOpeningBalance"));
				statementAccountTransaction
						.setBdBalanceAmount(bdCurrentBalanceAmount);
				listStatmentAccountTransaction.add(statementAccountTransaction);
			}

			for (GenericValue genericValue : accountTransactionELI) {
				statementAccountTransaction = new StatementAccountTransaction();
				statementAccountTransaction.setMemberAccountId(memberAccountId);
				// statementAccountTransaction.setTransactionDate(genericValue
				// .getTimestamp("createdStamp"));
				statementAccountTransaction.setDescription(genericValue
						.getString("transactionType"));
				// transaction.setTransactionType();
				statementAccountTransaction.setAmount(genericValue
						.getBigDecimal("transactionAmount"));
				// transaction.setTransactionAmount();

				// genericValue.getT
				Timestamp dateCreated = new Timestamp(genericValue
						.getTimestamp("createdStamp").getTime());
				DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
				statementAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				statementAccountTransaction.setAccountName(memberAccount
						.getString("accountName"));
				statementAccountTransaction.setAccountNo(memberAccount
						.getString("accountNo"));

				if (genericValue.getString("increaseDecrease").equals("I")) {
					bdCurrentBalanceAmount = bdCurrentBalanceAmount
							.add(genericValue
									.getBigDecimal("transactionAmount"));
					statementAccountTransaction.setDebitCredit("C");
				} else {
					bdCurrentBalanceAmount = bdCurrentBalanceAmount
							.subtract(genericValue
									.getBigDecimal("transactionAmount"));
					statementAccountTransaction.setDebitCredit("D");
				}
				statementAccountTransaction
						.setBdBalanceAmount(bdCurrentBalanceAmount);

				listStatmentAccountTransaction.add(statementAccountTransaction);
			}
		}

		// Loan - get transactions to this loan
		else {
			// Add Loan Transactions

		}

		Gson gson = new Gson();
		String json = gson.toJson(listStatmentAccountTransaction);

		return Response.ok(json).type("application/json").build();
	}

	// Add Loans
	@GET
	@Produces("application/json")
	@Path("/loansbalances/{user}")
	public Response getLoanBalances(@PathParam("user") String user) {
		// BigDecimal bdBalance = null;

		List<LoanAccount> listLoanAccount = new ArrayList<LoanAccount>();
		LoanAccount loanAccount;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Give User Login get the :-
		GenericValue userLogin = null;
		try {
			userLogin = delegator.findOne("UserLogin",
					UtilMisc.toMap("userLoginId", user), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (userLogin == null)
			return null;
		
		String partyIdStr = userLogin.getString("partyId");

		if ((partyIdStr == null) || (partyIdStr.equals(""))) {
			return null;
		}
		partyIdStr = partyIdStr.replaceAll(",", "");
		Long partyId = Long.valueOf(partyIdStr);

		// Loan Products
		EntityConditionList<EntityExpr> loansConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId)),
						EntityOperator.AND);
		List<GenericValue> loanApplicationsELI = null;
		// List<String> orderByList = new ArrayList<String>();
		// orderByList.add("createdStamp DESC");
		try {
			loanApplicationsELI = delegator.findList("LoanApplication",
					loansConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationsELI) {

			loanAccount = new LoanAccount();
			Long loanApplicationId = genericValue.getLong("loanApplicationId");
			loanAccount.setLoanApplicationId(loanApplicationId);
			loanAccount.setLoanNo(genericValue.getString("loanNo"));

			GenericValue loanProduct = null;

			try {
				loanProduct = delegator.findOne(
						"LoanProduct",
						UtilMisc.toMap("loanProductId",
								genericValue.getLong("loanProductId")), false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			loanAccount.setLoanType(loanProduct.getString("name"));

			if (genericValue.getTimestamp("disbursementDate") != null){
			Timestamp dateCreated = new Timestamp(genericValue.getTimestamp(
					"disbursementDate").getTime());
			DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
			// transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));

			loanAccount
					.setDateDisbursed(dateTimeCreated.toString("dd/MM/yyyy"));
			} 
			BigDecimal bdLoanAmt = genericValue.getBigDecimal("loanAmt");
			loanAccount.setLoanAmt(bdLoanAmt);

			BigDecimal bdBalance = bdLoanAmt.subtract(LoanServices
					.getLoansRepaidByLoanApplicationId(loanApplicationId));
			loanAccount.setLoanBalance(bdBalance);

			listLoanAccount.add(loanAccount);

		}

		Gson gson = new Gson();
		String json = gson.toJson(listLoanAccount);

		return Response.ok(json).type("application/json").build();
	}

	// Add Loan Transactions

	@GET
	@Produces("application/json")
	@Path("/loanstransactions/{loanApplicationId}")
	public Response getLoanBalances(
			@PathParam("loanApplicationId") Long loanApplicationId) {
		// BigDecimal bdBalance = null;

		List<LoanAccountTransaction> listLoanAccountTransaction = new ArrayList<LoanAccountTransaction>();
		LoanAccountTransaction loanAccountTransaction;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Give User Login get the :-
		GenericValue loanApplication = null;
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		BigDecimal bdCurrentLoanBalance = loanApplication
				.getBigDecimal("loanAmt");
		//Add Disbursement
		loanAccountTransaction = new LoanAccountTransaction();

		Timestamp dateCreated = new Timestamp(loanApplication.getTimestamp(
				"disbursementDate").getTime());
		DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
		// transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));

		loanAccountTransaction.setTransactionDate(dateTimeCreated
				.toString("dd/MM/yyyy"));
		loanAccountTransaction.setDescription("Loan Disbursed");
		loanAccountTransaction.setDebitCredit("C");

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		//BigDecimal bdOutstandingBalance = loanApplication
		//		.getBigDecimal("outstandingBalance");

		BigDecimal dbAmount = bdLoanAmt;
		loanAccountTransaction.setDbAmount(dbAmount);

		loanAccountTransaction.setBdBalanceAmount(bdLoanAmt);
		bdCurrentLoanBalance = dbAmount;
		listLoanAccountTransaction.add(loanAccountTransaction);

		// Add opening repayment
		
		
		
		
		if (loanApplication.getBigDecimal("outstandingBalance") != null) {
			// Add opening repayment balance
			loanAccountTransaction = new LoanAccountTransaction();

			dateCreated = new Timestamp(loanApplication.getTimestamp(
					"createdStamp").getTime());
			dateTimeCreated = new DateTime(dateCreated.getTime());
			// transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));

			loanAccountTransaction.setTransactionDate(dateTimeCreated
					.toString("dd/MM/yyyy"));
			loanAccountTransaction.setDescription("Total Repaid at Opening");
			loanAccountTransaction.setDebitCredit("D");

			bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
			BigDecimal bdOutstandingBalance = loanApplication
					.getBigDecimal("outstandingBalance");

			dbAmount = bdLoanAmt.subtract(bdOutstandingBalance);
			loanAccountTransaction.setDbAmount(dbAmount);

			loanAccountTransaction.setBdBalanceAmount(bdOutstandingBalance);
			bdCurrentLoanBalance = bdOutstandingBalance;
			listLoanAccountTransaction.add(loanAccountTransaction);

		}
		// Add Interest and Insurance Charges
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)), EntityOperator.AND);
		List<GenericValue> loanExpectationsELI = null;
		List<String> orderByList = new ArrayList<String>();
		orderByList.add("createdStamp DESC");
		try {
			loanExpectationsELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationsELI) {

			String repaymentName = loanExpectation.getString("repaymentName");
			String description = "";
			if ((repaymentName.equals("INTEREST"))
					|| (repaymentName.equals("INSURANCE"))) {
				// Add Interest or Insurance

				if (repaymentName.equals("INTEREST")) {
					description = "Interest Charged";
				} else if (repaymentName.equals("INSURANCE")) {
					description = "Insurance Charged";
				}

				loanAccountTransaction = new LoanAccountTransaction();

				dateCreated = new Timestamp(loanExpectation
						.getTimestamp("dateAccrued").getTime());
				dateTimeCreated = new DateTime(dateCreated.getTime());
				loanAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				loanAccountTransaction.setDescription(description);
				loanAccountTransaction.setDebitCredit("C");
				loanAccountTransaction.setDbAmount(loanExpectation
						.getBigDecimal("amountAccrued"));

				bdCurrentLoanBalance = bdCurrentLoanBalance.add(loanExpectation
						.getBigDecimal("amountAccrued"));
				loanAccountTransaction.setBdBalanceAmount(bdCurrentLoanBalance);
				listLoanAccountTransaction.add(loanAccountTransaction);
			}

		}

		// Add Loan Repayments
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)), EntityOperator.AND);

		List<GenericValue> loanRepaymentsELI = null;
		List<String> orderByRepaymentList = new ArrayList<String>();
		orderByList.add("createdStamp DESC");
		try {
			loanRepaymentsELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, orderByRepaymentList, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentsELI) {

			// Add Insurance if Insurance Amount is greater than ZERO

			BigDecimal bdInsuranceAmount = loanRepayment
					.getBigDecimal("insuranceAmount");
			if ((bdInsuranceAmount != null)
					&& (bdInsuranceAmount.compareTo(BigDecimal.ZERO) == 1)) {
				// Add Insurance
				loanAccountTransaction = new LoanAccountTransaction();
				dateCreated = new Timestamp(loanRepayment
						.getTimestamp("createdStamp").getTime());
				dateTimeCreated = new DateTime(dateCreated.getTime());
				loanAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				loanAccountTransaction.setDebitCredit("D");
				loanAccountTransaction.setDbAmount(bdInsuranceAmount);

				loanAccountTransaction.setDescription("Insurance Paid");

				bdCurrentLoanBalance = bdCurrentLoanBalance
						.subtract(bdInsuranceAmount);
				loanAccountTransaction.setBdBalanceAmount(bdCurrentLoanBalance);

				listLoanAccountTransaction.add(loanAccountTransaction);
			}

			// Add Interest is Interest Amount is greater than ZERO
			BigDecimal interestAmount = loanRepayment
					.getBigDecimal("interestAmount");
			if ((interestAmount != null)
					&& (interestAmount.compareTo(BigDecimal.ZERO) == 1)) {
				// Add Insurance
				loanAccountTransaction = new LoanAccountTransaction();
				dateCreated = new Timestamp(loanRepayment
						.getTimestamp("createdStamp").getTime());
				dateTimeCreated = new DateTime(dateCreated.getTime());
				loanAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				loanAccountTransaction.setDebitCredit("D");
				loanAccountTransaction.setDbAmount(interestAmount);

				loanAccountTransaction.setDescription("Interest Paid");

				bdCurrentLoanBalance = bdCurrentLoanBalance
						.subtract(interestAmount);
				loanAccountTransaction.setBdBalanceAmount(bdCurrentLoanBalance);
				listLoanAccountTransaction.add(loanAccountTransaction);
			}
			// Add Principal if Principal Amount Greater than ZERO

			BigDecimal principalAmount = loanRepayment
					.getBigDecimal("principalAmount");
			if ((principalAmount != null)
					&& (principalAmount.compareTo(BigDecimal.ZERO) == 1)) {
				// Add Insurance
				loanAccountTransaction = new LoanAccountTransaction();
				dateCreated = new Timestamp(loanRepayment
						.getTimestamp("createdStamp").getTime());
				dateTimeCreated = new DateTime(dateCreated.getTime());
				loanAccountTransaction.setTransactionDate(dateTimeCreated
						.toString("dd/MM/yyyy"));

				loanAccountTransaction.setDebitCredit("D");
				loanAccountTransaction.setDbAmount(principalAmount);

				loanAccountTransaction.setDescription("Principal Paid");
				bdCurrentLoanBalance = bdCurrentLoanBalance
						.subtract(principalAmount);

				loanAccountTransaction.setBdBalanceAmount(bdCurrentLoanBalance);

				listLoanAccountTransaction.add(loanAccountTransaction);
			}

		}

		Gson gson = new Gson();
		String json = gson.toJson(listLoanAccountTransaction);

		return Response.ok(json).type("application/json").build();
	}

}
