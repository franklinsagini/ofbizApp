//partyId = parameters.partyId
//LpartyId = partyId.toLong();
//if (partyId) {
//   employee = delegator.findOne("Member", [partyId : LpartyId], false);
//   if(employee){
//    context.employee = employee;
//   }

//context.activities = delegator.findByAnd("RegistryFileMovement", [partyId : partyId], null, false);
//context.file = delegator.findOne("RegistryFiles", [partyId : partyId], false);

//}


finAccountId = parameters.finAccountId
glReconciliationId = parameters.glReconciliationId
if (finAccountId) {
  bankAccount = delegator.findOne("FinAccount", [finAccountId : finAccountId], false)
}
if (glReconciliationId) {
   reconciliation = delegator.findOne("GlReconciliation", [glReconciliationId : glReconciliationId], false)
}
context.bankAccount = bankAccount
context.reconciliation = reconciliation


//context.activities = delegator.findByAnd("RegistryFileMovement", [partyId : partyId], null, false);
