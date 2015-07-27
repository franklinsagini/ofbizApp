partyId = parameters.partyId
LpartyId = partyId.toLong();
if (partyId) {
   employee = delegator.findOne("Member", [partyId : LpartyId], false);
   if(employee){
    context.employee = employee;
   }

context.activities = delegator.findByAnd("RegistryFileMovement", [partyId : partyId], null, false);
context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);

}
