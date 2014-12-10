memberPlusPersonId = parameters.memberPlusPersonId
if (memberPlusPersonId) {
   employee = delegator.findOne("memberPlusPerson", [memberPlusPersonId : memberPlusPersonId], false);
   if(employee){
    context.employee = employee;
   }
   else {
    
   }
context.activities = delegator.findByAnd("RegistryFileLogs", [memberPlusPersonId : memberPlusPersonId], null, false);
context.file = delegator.findOne("RegistryFiles", [memberPlusPersonId : memberPlusPersonId], false);
}
