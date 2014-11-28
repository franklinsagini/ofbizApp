partyId = parameters.partyId
if (partyId) {
   employee = delegator.findOne("Person", [partyId : partyId], false);
   if(employee){
    context.employee = employee;
   }
   else {
    employee = delegator.findOne("Member", [partyId : partyId], false);
    context.employee = employee;
   }
context.activities = delegator.findByAnd("RegistryFileLogs", [partyId : partyId], null, false);
context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);
}
