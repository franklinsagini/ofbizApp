partyId = parameters.partyId
if (partyId) {
   employee = delegator.findOne("Member", [partyId : partyId], false);
   if(employee){
    context.employee = employee;
   }
   else {
    return
   }
context.activities = delegator.findByAnd("RegistryFileLogs", [partyId : partyId], null, false);
context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);
}
