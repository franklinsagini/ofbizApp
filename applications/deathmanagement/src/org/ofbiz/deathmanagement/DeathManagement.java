package org.ofbiz.deathmanagement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;


public class DeathManagement {
	
	public static BigDecimal getFuneralExpenseAmount(){
		BigDecimal bdAmount = BigDecimal.ZERO;
		
		
		List<GenericValue> FuneralExpenseAmountELI = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("funeralExpenseAmountId");
		try {
			FuneralExpenseAmountELI = delegator.findList("FuneralExpenseAmount", null, null,
					listOrder, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expenseAmount : FuneralExpenseAmountELI) {
			bdAmount = expenseAmount.getBigDecimal("amount");
			
		}
		
		
		return bdAmount;
	}

}
