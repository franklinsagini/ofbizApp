partyId = parameters.partyId
leaveType = parameters.leaveType
context.leaveType = leaveType
context.title = "Chai Sacco"

if (partyId) {
    employee = delegator.findOne("LeavesBalanceView", [partyId : partyId], false);
   context.employee = employee;
   return
}

leaveBalances = delegator.findList("LeavesBalanceView", null, null, null, null, false);
context.leaveBalances = leaveBalances;
