package org.ofbiz.sharemanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class ShareManagementServices {
	public static String getBranches(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String bankDetailsId = (String) request.getParameter("bankDetailsId");
		//GenericValue saccoProduct = null;
		//EntityListIterator branchesELI;// = delegator.findListIteratorByCondition("BankBranch", new EntityExpr("bankDetailsId", EntityOperator.EQUALS,  bankDetailsId), null, UtilMisc.toList("bankBranchId", "branchName"), "branchName", null);
		//branchesELI = delegator.findListIteratorByCondition(dynamicViewEntity, whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy, findOptions)
		//branchesELI = delegator.findListIteratorByCondition("BankBranch", new EntityExpr("productId", EntityOperator.NOT_EQUAL, null), UtilMisc.toList("productId"), null);
		List<GenericValue> branchesELI = null;
		
		//branchesELI = delegator.findList("BankBranch", new EntityExpr(), UtilMisc.toList("bankBranchId", "branchName"), null, null, null);
		try {
			//branchesELI = delegator.findList("BankBranch", EntityCondition.makeConditionWhere("(bankDetailsId = "+bankDetailsId+")"), null, null, null, false);
			branchesELI = delegator.findList("BankBranch", EntityCondition.makeCondition("bankDetailsId", bankDetailsId), null, null, null, false);
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//SaccoProduct
	
		//Add Branches to a list
		
		if (branchesELI == null){
			result.put("", "No Braches");
		}
		
		for (GenericValue genericValue : branchesELI) {
			result.put(genericValue.get("bankBranchId").toString(), genericValue.get("branchName"));
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
	 * Get the default Share Account (Owner Equity)
	 * **/
	public static String getShareAccount() {

		//From ShareSetup get the glAccountId
		// Get the Product by first accessing the MemberAccount
		//String accountProductId = getAccountProduct(accountTransaction);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> shareSetupELI = null;
		try {
			shareSetupELI = delegator.findList("ShareSetup",
					EntityCondition.makeCondition("isActive", "Y"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (shareSetupELI == null) {
			return "";
		}
		String glAccountId = "";
		for (GenericValue genericValue : shareSetupELI) {
			//productChargeId
			glAccountId = genericValue.get("glAccountId").toString();
		}


		return glAccountId;
	}
	
	/***
	 * Get the share price
	 * */
	public static BigDecimal getSharePrice() {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> shareSetupELI = null;
		try {
			shareSetupELI = delegator.findList("ShareSetup",
					EntityCondition.makeCondition("isActive", "Y"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (shareSetupELI == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal bdSharePrice = BigDecimal.ZERO;
		for (GenericValue genericValue : shareSetupELI) {
			//productChargeId
			bdSharePrice = genericValue.getBigDecimal("sharePrice");
		}

		return bdSharePrice;
	}
}
