
action = request.getParameter("action");

deceasedMemberFileslist = [];
 filesInDisposal = delegator.findByAnd("RegistryFiles", [stageStatus : "DECEASED"],null, false);
 
 filesInDisposal.eachWithIndex { filesInDisposalItem, index ->

fileBox = filesInDisposalItem.fileBox
fileBoxCode = filesInDisposalItem.fileBoxCode

 fileOwner = "${filesInDisposalItem.firstName} ${filesInDisposalItem.lastName}";
 memberNumber = filesInDisposalItem.memberNumber;
 payroll = filesInDisposalItem.payrollNumber;
 timein = filesInDisposalItem.deceasedStartDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 since = dateStringin;
  
 
 
 
 deceasedMemberFileslist.add([fileOwner :fileOwner,fileBox :fileBox, fileBoxCode  : fileBoxCode, memberNumber :memberNumber, payroll : payroll, since : since]);
 }
 
 
context.deceasedMemberFileslist = deceasedMemberFileslist;