
employeeList = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);

def employeelisting = []

employeeList.eachWithIndex { staffItem, index ->
          employeeNumber = staffItem.employeeNumber
          firstName      = staffItem.firstName
          lastName       = staffItem.lastName
          name = firstName +"   "+lastName
          birthDate      = staffItem.birthDate
          gender         = staffItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYear(birthDate);
          presentYears = org.ofbiz.humanres.LeaveServices.getPresentAge(yearOfBirth);
          
           empListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears
              ]
          
           employeelisting.add(empListBuilder);
          
     }

 context.employeeList = employeelisting;
