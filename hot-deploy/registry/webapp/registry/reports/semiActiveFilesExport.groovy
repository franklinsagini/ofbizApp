
action = request.getParameter("action");

semiActiveFileslist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "SEMIACTIVE"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.SemiActiveStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 semiActiveFileslist.add([fileOwner :fileOwner, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.semiActiveFileslist = semiActiveFileslist;