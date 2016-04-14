//  ----- FOR AGE BRACKET

typeSelect = parameters.typeSelect;

String withBranch = "withBranch"

def employee_dob = []
def employee_listing = []
if(typeSelect.equalsIgnoreCase(withBranch)){

  employeeList = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
    employeeList.eachWithIndex { staffItem, index ->
          employeeNumber = staffItem.employeeNumber
          firstName      = staffItem.firstName
          lastName       = staffItem.lastName
          name = firstName +"   "+lastName
          birthDate      = staffItem.birthDate
          branchId       = staffItem.branchId
          departmentId   =  staffItem.departmentId
          dateOfApp      =  staffItem.appointmentdate  
          gender         = staffItem.gender
          yearOfBirth    =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
          yearsWorkedCY    = org.ofbiz.humanres.LeaveServices.getCurrentYearDate(dateOfApp); 
          yearsWorked    = org.ofbiz.humanres.LeaveServices.getPresentAge(yearsWorkedCY);
          
          // Current Years
          if(!(yearOfBirth==null)){
            presentYears = org.ofbiz.humanres.LeaveServices.getPresentAge(yearOfBirth);
          }else{
            presentYears = null
          }
          
          //Age In 5 years
           int five = 5;
           if(!(presentYears==null)){
            ageInFiveYears = org.ofbiz.humanres.LeaveServices.ageAfterYears(presentYears,five);
             println("----Age in 5 yrs---"+ageInFiveYears)
          }else{
            ageInFiveYears = null
          }
          
           //Age In 10 years
           int ten = 10;
           if(!(presentYears==null)){
            ageInTenYears = org.ofbiz.humanres.LeaveServices.ageAfterYears(presentYears,ten);
            println("----Age in 10 yrs---"+ageInTenYears)
          }else{
            ageInTenYears = null
          }
         
           empListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              branchId :branchId,
              departmentId :departmentId,
              gender : gender,
              dateOfApp : dateOfApp,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              yearsWorked :yearsWorked,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_dob.add(empListBuilder);
     }

  context.employee_dob = employee_dob;
       return
}



//  ---- FOR NO ITEM SELECTION

  employeeList = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
    employeeList.eachWithIndex { staffItem, index ->
          employeeNumber = staffItem.employeeNumber
          firstName      = staffItem.firstName
          lastName       = staffItem.lastName
          name = firstName +"   "+lastName
          birthDate      = staffItem.birthDate
          branchId       = staffItem.branchId
          departmentId   =  staffItem.departmentId
          dateOfApp      =  staffItem.appointmentdate  
          gender         = staffItem.gender
          yearOfBirth    =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
          yearsWorkedCY    = org.ofbiz.humanres.LeaveServices.getCurrentYearDate(dateOfApp); 
          yearsWorked    = org.ofbiz.humanres.LeaveServices.getPresentAge(yearsWorkedCY);
          
          // Current Years
          if(!(yearOfBirth==null)){
            presentYears = org.ofbiz.humanres.LeaveServices.getPresentAge(yearOfBirth);
          }else{
            presentYears = null
          }
          
          //Age In 5 years
           int five = 5;
           if(!(presentYears==null)){
            ageInFiveYears = org.ofbiz.humanres.LeaveServices.ageAfterYears(presentYears,five);
             println("----Age in 5 yrs---"+ageInFiveYears)
          }else{
            ageInFiveYears = null
          }
          
           //Age In 10 years
           int ten = 10;
           if(!(presentYears==null)){
            ageInTenYears = org.ofbiz.humanres.LeaveServices.ageAfterYears(presentYears,ten);
            println("----Age in 10 yrs---"+ageInTenYears)
          }else{
            ageInTenYears = null
          }
         
           empListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              branchId :branchId,
              departmentId :departmentId,
              gender : gender,
              dateOfApp : dateOfApp,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              yearsWorked :yearsWorked,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_listing.add(empListBuilder);
     }

  context.employee_listing = employee_listing;