partyId = parameters.partyId
activityId = parameters.activityId
LpartyId = partyId.toLong();
if (partyId) {
   employee = delegator.findOne("Member", [partyId : LpartyId], false);
   if(employee){
    context.employee = employee;
   }

context.activities = delegator.findByAnd("RegistryFileLogs", [partyId : partyId, Reason : activityId], null, false);
context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);
}
