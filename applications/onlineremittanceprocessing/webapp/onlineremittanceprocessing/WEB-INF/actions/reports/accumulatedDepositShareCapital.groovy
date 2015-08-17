import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

headOfficeMonthYearId = parameters.headOfficeMonthYearId
headOfficeMonthYearIdLong = headOfficeMonthYearId.toLong()

accumulatedDepositShareCapitalList = delegator.findByAnd("AccumulatedDepositShareCapital",  [headOfficeMonthYearId : headOfficeMonthYearIdLong], null, false);

context.accumulatedDepositShareCapitalList = accumulatedDepositShareCapitalList
context.headOfficeMonthYearId = headOfficeMonthYearIdLong