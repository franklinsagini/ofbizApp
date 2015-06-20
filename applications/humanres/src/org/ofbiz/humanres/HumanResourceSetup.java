package org.ofbiz.humanres;

import java.util.List;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * @author RONNY
 * 
 *         HCM Setup validations
 * */
public class HumanResourceSetup {

	private static Logger log = LoggerFactory.getLogger(HumanResourceSetup.class);

	/****
	 * Checking that a paygrade already exists
	 * 
	 * org.ofbiz.humanres.HumanResourceSetup.payGradeExists()
	 * */
	public static Boolean payGradeExists(String name) {

		log.info("GGGGGGGGGGGGGGGGG The Grade Type is " + name);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> listPayGradesELI = null;

		try {
			listPayGradesELI = delegator.findList("PayGrade",
					EntityCondition.makeCondition("payGradeName", name), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((listPayGradesELI != null) && (listPayGradesELI.size() > 0)) {
			return true;
		}

		return false;
	}

}
