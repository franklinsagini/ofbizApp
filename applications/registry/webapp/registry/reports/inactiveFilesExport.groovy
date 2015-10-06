
action = request.getParameter("action");

inactiveFileslist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "INACTIVE"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->


fileBox = filesInDisposalItem.fileBox
fileBoxCode = filesInDisposalItem.fileBoxCode

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.inactiveStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 inactiveFileslist.add([fileOwner :fileOwner,fileBox :fileBox, fileBoxCode  : fileBoxCode, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.inactiveFileslist = inactiveFileslist;