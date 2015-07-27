
 fileActionTypeId = parameters.fileActionTypeId;

context.activities = delegator.findByAnd("RegistryFileLogs", [fileActionTypeId : fileActionTypeId], null, false);

