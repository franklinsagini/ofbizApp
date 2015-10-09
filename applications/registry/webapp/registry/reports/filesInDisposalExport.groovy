
action = request.getParameter("action");

filesInDisposallist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "DISPOSAL"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->

fileBox = filesInDisposalItem.fileBox
fileBoxCode = filesInDisposalItem.fileBoxCode

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.DisposalStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 filesInDisposallist.add([fileOwner :fileOwner,fileBox :fileBox, fileBoxCode  : fileBoxCode, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.filesInDisposallist = filesInDisposallist;