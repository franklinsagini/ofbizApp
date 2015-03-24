package org.ofbiz.loanclearing;

import java.math.BigDecimal;
import java.util.List;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;


public class LoanClearingServices {
	
		public static BigDecimal getTotalAmountToClear(Long loanClearId){
			BigDecimal totalToClear = BigDecimal.ZERO;
			
			List<GenericValue> loanClearItemELI = null; // =
			EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"loanClearId", EntityOperator.EQUALS, loanClearId)

					), EntityOperator.AND);

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			try {
				loanClearItemELI = delegator.findList("LoanClearItem",
						loanClearItemConditions, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for (GenericValue genericValue : loanClearItemELI) {
				totalToClear = totalToClear.add(genericValue.getBigDecimal("loanAmt"));
			}
			
			return totalToClear;
		}

}
