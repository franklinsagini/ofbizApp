
action = request.getParameter("action");

filesInDisposallist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "DISPOSAL"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.DisposalStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 filesInDisposallist.add([fileOwner :fileOwner, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.filesInDisposallist = filesInDisposallist;