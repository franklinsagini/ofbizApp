import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

headOfficeMonthYearId = parameters.headOfficeMonthYearId
headOfficeMonthYearIdLong = headOfficeMonthYearId.toLong()

fosaJuniorHolidayList = delegator.findByAnd("FosaJuniorHoliday",  [headOfficeMonthYearId : headOfficeMonthYearIdLong], null, false);

context.fosaJuniorHolidayList = fosaJuniorHolidayList
context.headOfficeMonthYearId = headOfficeMonthYearIdLong