partyId = parameters.partyId
LpartyId = partyId.toLong();
if (partyId) {
   context.employee = delegator.findOne("Person", [partyId : partyId], false);
  

context.activities = delegator.findByAnd("RegistryFileLogs", [actionBy : partyId, fileActionTypeId : "Release File"], null, false);

}
