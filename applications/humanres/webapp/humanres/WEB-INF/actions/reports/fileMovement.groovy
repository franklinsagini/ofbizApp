partyId = parameters.partyId
if (partyId) {
   employee = delegator.findOne("Person", [partyId : partyId], false);
   if(employee){
    context.employee = employee;
   }
   else {
    LpartyId = partyId.toLong();
    employee = delegator.findOne("Member", [partyId : LpartyId], false);
    context.employee = employee;
   }
context.activities = delegator.findByAnd("RegistryFileLogs", [partyId : partyId], null, false);
context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);
}
