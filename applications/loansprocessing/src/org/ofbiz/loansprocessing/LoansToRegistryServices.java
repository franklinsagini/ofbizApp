package org.ofbiz.loansprocessing;

import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

public class LoansToRegistryServices {

	public static Logger log = Logger.getLogger(LoansToRegistryServices.class);

	public static String checkIfUserHasFile(int memberPartyId, String userPartyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		log.info("-------------######### memberPartyId-----" + memberPartyId);
		log.info("-------------#########__userPartyId----" + userPartyId);
		String meString = "" + memberPartyId;
		log.info("-------------########----meString---" + meString);
		List<GenericValue> checkUserELE = null;
		String trueValue = "true";
		String falseValue = "false";
		try {
			checkUserELE = delegator.findList("RegistryFiles", EntityCondition.makeCondition("partyId", meString), null,
					null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String currentPossessor = null;
		for (GenericValue genericValue : checkUserELE) {
			currentPossessor = genericValue.getString("currentPossesser");
			if (currentPossessor.equalsIgnoreCase(userPartyId)) {
				log.info("------------THEY ARE EQAUL--------------");
				log.info("-------------#########__CURENT POSSESSER TRUE-----" + currentPossessor);
				return trueValue;
			} else {

			}
		}
		log.info("------------THEY ARE NOT EQAUL--------------");
		log.info("-------------#########__CURENT POSSESSER FALSE-----" + currentPossessor);

		return falseValue;
	}

	public static String getMemberNameGivenLoanAppId(Long loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue memberNameELI = null;

		try {
			memberNameELI = delegator.findOne("LoanApplication", UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		String firstName = null;
		String lastName = null;
		String fullName = null;

		if (memberNameELI.size() > 0) {
			firstName = memberNameELI.getString("firstName");
			lastName = memberNameELI.getString("lastName");
			fullName = firstName + "  " + lastName;
		}
		return fullName;
	}

	public static String getLoggedInUserPartyId(String userLoginId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		GenericValue checkUserELE = null;
		String userPartyId = null;

		try {
			checkUserELE = delegator.findOne("UserLogin", UtilMisc.toMap("userLoginId", userLoginId), false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		if (checkUserELE.size() > 0) {
			userPartyId = checkUserELE.getString("partyId");
		}
		log.info("-------------###### --PARTY ID -----" + userPartyId);
		return userPartyId;
	}
}
