partyId = parameters.partyId

if (partyId) {

  context.employees = delegator.findByAnd("EmployeeLeavesView", [partyId : parameters.partyId], null, false);
  return
}
employees = delegator.findList("EmployeeLeavesView", null, null, null, null, false);
context.employees = employees
