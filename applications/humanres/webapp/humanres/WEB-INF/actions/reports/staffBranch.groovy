
branchId = parameters.branchId
departmentId=parameters.departmentId
countrr=0
countrrr2=0

if (branchId && departmentId) {
    staff = delegator.findByAnd("Person", [branchId : branchId, departmentId : parameters.departmentId , isSeparated : "N" ], null, false);
    
    branch = delegator.findOne('PartyGroup', ['partyId':branchId], false)
    context.branchName = branch.groupName
     
     department = delegator.findOne('department',['departmentId': parameters.departmentId], false)
     context.deptName=department.departmentName
     
    staff.eachWithIndex { staffItem, index ->
    countrr=countrr + 1
     }
	
   context.staff = staff;
   context.countrr = countrr;
   return
}





// if you want to have values from here on the attached FTL ensure you append them to the context