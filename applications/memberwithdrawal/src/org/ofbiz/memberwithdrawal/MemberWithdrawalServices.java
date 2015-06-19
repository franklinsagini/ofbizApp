package org.ofbiz.memberwithdrawal;

import java.util.List;
import java.util.Map;

import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

/****
 * @author Japheth Odonya  @when Jun 19, 2015 9:10:52 AM
 * Member Withdrawal Services
 * 
 * org.ofbiz.memberwithdrawal.MemberWithdrawalServices.validateApplication
 * 
 * */
public class MemberWithdrawalServices {
	public static String validateApplication(Long partyId, Map<String, String> userLogin){
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberWithdrawalELI = null;
		EntityConditionList<EntityExpr> memberWithdrawalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("partyId",
								EntityOperator.EQUALS, partyId),
								
								EntityCondition
								.makeCondition("withdrawalstatus",
										EntityOperator.NOT_EQUAL, "APPROVED"),
										
								EntityCondition
						.makeCondition("withdrawalstatus",
								EntityOperator.NOT_EQUAL, "REJECTED")
						),
						EntityOperator.AND);

		try {
			memberWithdrawalELI = delegator.findList("MemberWithdrawal",
					memberWithdrawalConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((memberWithdrawalELI != null) && (memberWithdrawalELI.size() > 0))
		{
			return " There is already a Withdrawal Application created for this member, you cannot create a new one";
		}
		
		
		//Check if a member is withdrawn already
		
		Long memberStatusId = LoanUtilities.getMemberStatusId("WITHDRAWN");
				
		//LoanUtilities.getMemberStatusGivenPayrollNo(payrollNo)
		List<GenericValue> memberELI = null;
		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("partyId",
								EntityOperator.EQUALS, partyId),
								
								EntityCondition
								.makeCondition("memberStatusId",
										EntityOperator.EQUALS, memberStatusId)
										
								
						),
						EntityOperator.AND);

		try {
			memberELI = delegator.findList("Member",
					memberConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((memberELI != null) && (memberELI.size() > 0))
		{
			return " The member you are trying to withdraw is already withdrawn";
		}
		
		
		return "success";
	}
	
}
