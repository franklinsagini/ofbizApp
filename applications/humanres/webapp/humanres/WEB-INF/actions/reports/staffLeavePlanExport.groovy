



action = request.getParameter("action");

leaveCalendarlist = [];
 leaveCalendar = delegator.findList("EmplLeaveCalender", null, null, null,null, false);
 
 leaveCalendar.eachWithIndex { leaveCalendarItem, index ->
 
 staff = delegator.findOne("Person", [partyId : leaveCalendarItem.partyId], false);
 leaveType = delegator.findOne("EmplLeaveType", [leaveTypeId : leaveCalendarItem.leaveTypeId], false);
 branch = delegator.findOne("PartyGroup", [partyId : staff.branchId], false);
 department = delegator.findOne("department", [departmentId : staff.departmentId], false);
 
 
 payrollNo = staff.getString("employeeNumber");
 name = "${staff.firstName} ${staff.lastName}";
 gender = staff.getString("gender");
 branch = branch.getString("groupName");
 department = department.getString("departmentName");
 leaveType = leaveType.getString("description");
 duration = leaveCalendarItem.leaveDuration;
 startDate = leaveCalendarItem.fromDate;
 
 leaveCalendarlist.add([payrollNo :payrollNo, name :name, gender : gender, branch : branch, department : department,
 leaveType : leaveType, duration : duration, startDate : startDate]);
 }
 
 
context.leaveCalendarlist = leaveCalendarlist;
