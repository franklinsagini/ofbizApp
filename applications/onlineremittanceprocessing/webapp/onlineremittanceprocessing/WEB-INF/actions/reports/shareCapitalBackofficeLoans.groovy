import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

headOfficeMonthYearId = parameters.headOfficeMonthYearId
headOfficeMonthYearIdLong = headOfficeMonthYearId.toLong()

shareCapitalBackofficeLoansList = delegator.findByAnd("ShareCapitalBackofficeLoans",  [headOfficeMonthYearId : headOfficeMonthYearIdLong], null, false);

context.shareCapitalBackofficeLoansList = shareCapitalBackofficeLoansList
context.headOfficeMonthYearId = headOfficeMonthYearIdLong