package org.ofbiz.accounting.budget;

import java.math.BigDecimal;
import java.util.List;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

public class BudgetWorker {
	public static final String module = BudgetWorker.class.getName();

	public static BigDecimal getBudgetPlanTotal(Delegator delegator, String budgetPlanId) {
		BigDecimal budgetPlanTotal = BigDecimal.ZERO;
		List<GenericValue> budgetPlanItems = null;

		if (delegator == null) {
			throw new IllegalArgumentException("Null delegator is not allowed in this method");
		}

		GenericValue budgetPlan = null;

		try {
			budgetPlan = delegator.findOne("BudgetPlan", UtilMisc.toMap("budgetPlanId", budgetPlanId), false);
		} catch (GenericEntityException e) {
			Debug.logError(e, "Problem getting Budget Plan Header", module);
		}

		if (budgetPlan == null) {
			throw new IllegalArgumentException("The passed budgetPlanId [" + budgetPlanId + "] does not match an existing Budget Plan Header");
		}

		budgetPlanItems = getBudgetPlanItems(budgetPlan);
		
		budgetPlanTotal = getBudgetPlanTotal(budgetPlanItems);

		return budgetPlanTotal;
	}

	private static BigDecimal getBudgetPlanTotal(List<GenericValue> budgetPlanItems) {
		BigDecimal budgetPlanTotal = BigDecimal.ZERO;
		
		if (budgetPlanItems != null) {
			for (GenericValue item : budgetPlanItems) {
				budgetPlanTotal = budgetPlanTotal.add(item.getBigDecimal("amount"));
			}
		}
		
		return budgetPlanTotal;
	}

	private static List<GenericValue> getBudgetPlanItems(GenericValue budgetPlan) {
		List<GenericValue> budgetPlanItems = null;

		if (budgetPlan == null) {
			throw new IllegalArgumentException("Null Budget Plan Passed. NOTHING FOUND");
		}

		try {
			budgetPlanItems = budgetPlan.getRelated("BudgetPlanItem", null, null, false);
		} catch (GenericEntityException e) {
			throw new IllegalArgumentException("Budget Plan Passed does not match any existing Budget Plans. NOTHING FOUND");
		}

		return budgetPlanItems;
	}
}
