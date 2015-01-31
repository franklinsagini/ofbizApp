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
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

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

		// Loan Products
		EntityConditionList<EntityExpr> loansConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("partyId",
								EntityOperator.EQUALS, partyId)),
						EntityOperator.AND);
		List<GenericValue> loanApplicationsELI = null;
		//List<String> orderByList = new ArrayList<String>();
		//orderByList.add("createdStamp DESC");
		try {
			loanApplicationsELI = delegator.findList(
					"LoanApplication", loansConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : loanApplicationsELI) {
			
			statementAccount = new StatementAccount();
			Long memberAccountId = genericValue.getLong("loanApplicationId");
			statementAccount.setMemberAccountId(memberAccountId);
			
			GenericValue loanProduct = null;

			try {
				loanProduct = delegator.findOne("LoanProduct",
						UtilMisc.toMap("loanProductId", genericValue.getLong("loanProductId")),
						false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			statementAccount.setProductName(loanProduct.getString("name"));
			statementAccount
					.setAccountCode("");
			statementAccount.setAccountName("");
			statementAccount.setBdAvailableBalance(genericValue.getBigDecimal("loanAmt"));
			statementAccount.setBdBookBalance(null);
			statementAccount.setIsLoan("Y");
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

			for (GenericValue genericValueOpening : memberAccountDetailsELI) {
				statementAccountTransaction = new StatementAccountTransaction();
				statementAccountTransaction.setMemberAccountId(memberAccountId);
				//statementAccountTransaction.setTransactionDate(genericValueOpening.getTimestamp("openingBalanceDate"));
				statementAccountTransaction.setDescription("Opening Balance");
				// transaction.setTransactionType();
				statementAccountTransaction.setAmount(genericValueOpening.getBigDecimal("savingsOpeningBalance"));
				// transaction.setTransactionAmount();

				// genericValue.getT
				Timestamp dateCreated = new Timestamp(genericValueOpening.getTimestamp("openingBalanceDate").getTime());
				 DateTime dateTimeCreated = new	 DateTime(dateCreated.getTime());
				// transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));
				
				statementAccountTransaction.setTransactionDate(dateTimeCreated.toString("dd/MM/yyyy"));

				statementAccountTransaction.setAccountName(memberAccount
						.getString("accountName"));
				statementAccountTransaction.setAccountNo(memberAccount
						.getString("accountNo"));
				listStatmentAccountTransaction.add(statementAccountTransaction);
			}

			for (GenericValue genericValue : accountTransactionELI) {
				statementAccountTransaction = new StatementAccountTransaction();
				statementAccountTransaction.setMemberAccountId(memberAccountId);
				//statementAccountTransaction.setTransactionDate(genericValue
				//		.getTimestamp("createdStamp"));
				statementAccountTransaction.setDescription(genericValue
						.getString("transactionType"));
				// transaction.setTransactionType();
				statementAccountTransaction.setAmount(genericValue
						.getBigDecimal("transactionAmount"));
				// transaction.setTransactionAmount();

				// genericValue.getT
				 Timestamp dateCreated = new Timestamp(genericValue.getTimestamp("createdStamp").getTime());
				 DateTime dateTimeCreated = new
				 DateTime(dateCreated.getTime());
				 statementAccountTransaction.setTransactionDate(dateTimeCreated.toString("dd/MM/yyyy"));

				statementAccountTransaction.setAccountName(memberAccount
						.getString("accountName"));
				statementAccountTransaction.setAccountNo(memberAccount
						.getString("accountNo"));
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

}
