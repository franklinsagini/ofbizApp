
action = request.getParameter("action");

filesInArchivelist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "ARCHIVED"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.ArchiveStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 filesInArchivelist.add([fileOwner :fileOwner, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.filesInArchivelist = filesInArchivelist;