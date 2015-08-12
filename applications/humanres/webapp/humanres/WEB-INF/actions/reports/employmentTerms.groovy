
employmentTerm = parameters.employmentTerms
countrr=0
countrrr2=0

if (employmentTerm) {
    staffTerm = delegator.findByAnd("Person", [employmentTerms : employmentTerm ,  isSeparated : "N"  ], null, false);
     
    staffTerm.eachWithIndex { staffItem, index ->
    countrr=countrr + 1
     }
	
   context.staffTerm = staffTerm;
   context.countrr = countrr;
   return
}


