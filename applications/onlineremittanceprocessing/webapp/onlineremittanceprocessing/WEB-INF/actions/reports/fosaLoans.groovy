import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

headOfficeMonthYearId = parameters.headOfficeMonthYearId
headOfficeMonthYearIdLong = headOfficeMonthYearId.toLong()






fosaLoansList = delegator.findByAnd("FosaLoans",  [headOfficeMonthYearId : headOfficeMonthYearIdLong], null, false);



context.fosaLoansList = fosaLoansList
context.headOfficeMonthYearId = headOfficeMonthYearIdLong