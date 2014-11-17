package restcomponent;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.msaccomanagement.MSaccoManagementServices;
import org.ofbiz.msaccomanagement.MSaccoStatus;

import restcomponent.model.MSaccoTransaction;

import com.google.gson.Gson;

/***
 * @author Japheth Odonya @when Nov 13, 2014 9:00:17 PM MSacco Services
 * 
 *         MSacco Services - Balance - Withdrawal - Deposit - Loan Repayment
 * */
@Path("/msacco")
public class MSaccoServices {

	@GET
	@Produces("application/json")
	@Path("/balance/{phoneNumber}")
	public Response getBalance(@PathParam("phoneNumber") String phoneNumber) {

		BigDecimal bdBalance = null;
		// Long memberAccountId =
		// MSaccoManagementServices.getMemberAccountId(phoneNumber);

		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		if (msaccoStatus.getStatus().equals("SUCCESS")) {
			bdBalance = AccHolderTransactionServices.getTotalBalance(String
					.valueOf(msaccoStatus.getMsaccoApplication().getLong(
							"memberAccountId")), new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
		}
		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);

		if (bdBalance != null)
			transaction.setAmount(bdBalance);
		transaction.setStatus(msaccoStatus.getStatus());

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/withdrawal/{phoneNumber}/{amount}")
	public Response withdrawal(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("amount") BigDecimal amount) {

		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);
		transaction.setStatus(msaccoStatus.getStatus());
		
		

		if (msaccoStatus.getStatus().equals("SUCCESS")){
			//Check if Member Has Enough Money - Limit, charges
			transaction.setStatus("NOTENOUGHMONEY");
		}

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/deposit/{phoneNumber}/{amount}")
	public Response deposit(@PathParam("phoneNumber") String phoneNumber,
			@PathParam("amount") BigDecimal amount) {
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);
		transaction.setStatus(msaccoStatus.getStatus());

		if (msaccoStatus.getStatus().equals("SUCCESS"))
			transaction.setStatus("CANNOTDEPOSIT");

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

	@GET
	@Produces("application/json")
	@Path("/loanrepayment/{phoneNumber}")
	public Response loanrepayment(@PathParam("phoneNumber") String phoneNumber) {
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);
		transaction.setStatus(msaccoStatus.getStatus());

		if (msaccoStatus.getStatus().equals("SUCCESS"))
			transaction.setStatus("NOLOANSTOREPAY");

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}
	
	
	@GET
	@Produces("application/json")
	@Path("/loanrepayment/{phoneNumber}/{loanNumber}/{amount}")
	public Response loanrepayment(@PathParam("phoneNumber") String phoneNumber, @PathParam("loanNumber") String loanNumber, @PathParam("amount") String amount) {
		MSaccoStatus msaccoStatus = MSaccoManagementServices
				.getMSaccoAccount(phoneNumber);

		MSaccoTransaction transaction = new MSaccoTransaction();
		transaction.setPhoneNumber(phoneNumber);
		transaction.setStatus(msaccoStatus.getStatus());

		if (msaccoStatus.getStatus().equals("SUCCESS"))
			transaction.setStatus("LOANNOTFOUND");

		Gson gson = new Gson();
		String json = gson.toJson(transaction);

		return Response.ok(json).type("application/json").build();
	}

}
