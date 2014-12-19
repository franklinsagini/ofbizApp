
actionDate = parameters.actionDate;

if (actionDate) {


	
  

context.activities = delegator.findByAnd("RegistryFileLogs", [actionDate : actionDate, fileActionTypeId : "Release File"], null, false);

}
