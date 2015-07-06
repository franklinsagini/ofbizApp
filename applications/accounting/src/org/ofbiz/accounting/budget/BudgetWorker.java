package org.ofbiz.accounting.budget;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Fidelity;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

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

	public static Boolean pushBudgetPlanToBudget(GenericValue budgetPlan) {
		boolean success = false;
		List<GenericValue> budgetPlanItems = null;

		if (budgetPlan == null) {
			throw new IllegalArgumentException("Null budgetPlan is not allowed in this method");
		}

		Delegator delegator = budgetPlan.getDelegator();

		String budgetId = pushBudgetPlanHeader(delegator, budgetPlan);
		GenericValue fiscalYear = getFisacalYearForBudgetPlan(delegator, budgetPlan);
		getBudgetPlanItemsForCustomPeriod(fiscalYear);
		// budgetPlanItems = getBudgetPlanItems(budgetPlan);
		//
		// if (budgetId != null) {
		// if (budgetPlanItems != null) {
		// for (GenericValue item : budgetPlanItems) {
		// GenericValue budgetItem = delegator.makeValue("BudgetItem");
		// budgetItem.put("budgetItemSeqId",
		// delegator.getNextSeqId("BudgetItem"));
		// budgetItem.put("budgetId", budgetId);
		// budgetItem.put("glAccountId", item.getString("glAccountId"));
		// budgetItem.put("amount", item.getBigDecimal("amount"));
		// try {
		// budgetItem.create();
		// success = true;
		// } catch (GenericEntityException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		//
		// }

		return success;
	}

	private static GenericValue getFisacalYearForBudgetPlan(Delegator delegator, GenericValue budgetPlan) {
		GenericValue fiscalYear = null;
		if (budgetPlan != null) {
			String fiscalYearId = budgetPlan.getString("customTimePeriodId");
			try {
				fiscalYear = delegator.findOne("CustomTimePeriod", UtilMisc.toMap("customTimePeriodId", fiscalYearId), false);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fiscalYear;
	}

	private static String pushBudgetPlanHeader(Delegator delegator, GenericValue budgetPlan) {
		String budgetId = delegator.getNextSeqId("Budget");
		GenericValue budget = delegator.makeValue("Budget");
		budget.put("budgetId", budgetId);
		budget.put("budgetTypeId", "OPERATING_BUDGET");
		budget.put("customTimePeriodId", budgetPlan.getString("customTimePeriodId"));
		budget.put("comments", budgetPlan.getString("comments"));

		try {
			budget.create();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return budgetId;

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

	private static void getBudgetPlanItemsForCustomPeriod(GenericValue customTimePeriod) {
		List<GenericValue> budgetPlanItems = null;

		if (customTimePeriod == null) {
			throw new IllegalArgumentException("Null customTimePeriod is not allowed in this method");
		}
		System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC " + customTimePeriod);
		Delegator delegator = customTimePeriod.getDelegator();

		try {
			budgetPlanItems = customTimePeriod.getRelated("BudgetPlanItem", null, null, false);
		} catch (GenericEntityException e) {
			throw new IllegalArgumentException("Budget Plan Passed does not match any existing Budget Plans. NOTHING FOUND");
		}

		String budgetId = delegator.getNextSeqId("Budget");

		if (budgetPlanItems != null) {
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB " + budgetPlanItems);
			GenericValue budget = delegator.makeValue("Budget");
			budget.put("budgetId", budgetId);
			budget.put("budgetTypeId", "OPERATING_BUDGET");
			budget.put("customTimePeriodId", customTimePeriod.getString("customTimePeriodId"));
			budget.put("comments", "HARMONIZED BUDGET FOR " + customTimePeriod.getString("periodName"));

			try {
				budget.create();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (GenericValue item : budgetPlanItems) {
				GenericValue budgetItem = delegator.makeValue("BudgetItem");
				budgetItem.put("budgetItemSeqId", delegator.getNextSeqId("BudgetItem"));
				budgetItem.put("budgetId", budgetId);
				budgetItem.put("budgetItemTypeId", "REQUIREMENT_BUDGET_A");
				budgetItem.put("glAccountId", item.getString("glAccountId"));
				budgetItem.put("amount", item.getBigDecimal("amount"));
				try {
					budgetItem.create();
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// return budgetPlanItems;
	}

	public static Map<String, Object> generateHarmonizedBudget(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String, Object> result = new HashMap<String, Object>();
		String customTimePeriodId = (String) context.get("customTimePeriodId");
		String budgetId = delegator.getNextSeqId("Budget");
		List<GenericValue> budgetItems = null;
		List<GenericValue> budgetPlans = null;
		if (customTimePeriodId == null) {
			throw new IllegalArgumentException("THE PASSED PARAMETER customTimePeriodId COULD NOT BE RETRIVED");
		}
		try {
			GenericValue period = delegator.findOne("CustomTimePeriod", UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
//			GenericValue budget = delegator.makeValue("Budget");
//			budget.put("budgetId", budgetId);
//			budget.put("budgetTypeId", "OPERATING_BUDGET");
//			budget.put("customTimePeriodId", period.getString("customTimePeriodId"));
//			budget.put("comments", "HARMONIZED BUDGET FOR " + period.getString("periodName"));
			Map<String, Object> budgetParams = new HashMap<String, Object>();
			budgetParams.put("budgetId", budgetId);
			budgetParams.put("budgetTypeId", "OPERATING_BUDGET");
			budgetParams.put("customTimePeriodId", period.getString("customTimePeriodId"));
			budgetParams.put("comments", "HARMONIZED BUDGET FOR " + period.getString("periodName"));
			budgetParams.put("userLogin", userLogin);
			try {
				dispatcher.runSync("createBudget", budgetParams);
			} catch (GenericServiceException e) {
				e.printStackTrace();
			}
//			budget.create();
			
			budgetPlans = period.getRelated("BudgetPlan", null, null, false);
			
			for (GenericValue plan : budgetPlans) {
				budgetItems = plan.getRelated("BudgetPlanItem", null, null, false);
				
				for (GenericValue item : budgetItems) {
					
					GenericValue budgetItem = delegator.makeValue("BudgetItem");
					budgetItem.put("budgetItemSeqId", delegator.getNextSeqId("BudgetItem"));
					budgetItem.put("budgetId", budgetId);
					budgetItem.put("budgetItemTypeId", "REQUIREMENT_BUDGET_A");
					String budgetJustificationId =  item.getString("budgetJustificationId");
					GenericValue justification = delegator.findOne("BudgetJustification", UtilMisc.toMap("budgetJustificationId", budgetJustificationId), false);
					budgetItem.put("justification", justification.getString("justificationName"));
					budgetItem.put("glAccountId", item.getString("glAccountId"));
					budgetItem.put("amount", item.getBigDecimal("amount"));
					
					budgetItem.create();
				}
			}
			
			
			
			if(budgetItems == null){
				throw new IllegalArgumentException("NO BUDGET ITEMS FOR THIS [" +period.getString("periodName") + "] PERIOD");
			}
		

			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
