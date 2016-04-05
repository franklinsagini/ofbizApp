
typeSelect = parameters.typeSelect;

String dob = "dob"
String age = "age"
String gender = "gender"
String ageBracket = "ageBracket"
String comparison = "comparison"

def employee_dob = []
def employee_age = []
def employee_gender = []
def employee_comparison = []
def employee_listing = []

if(typeSelect.equalsIgnoreCase(dob)){

   dateOfBirth = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
   
    dateOfBirth.eachWithIndex { dateItem, index ->
          employeeNumber = dateItem.employeeNumber
          firstName      = dateItem.firstName
          lastName       = dateItem.lastName
          name = firstName +"   "+lastName
          birthDate      = dateItem.birthDate
          gender         = dateItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
         
           dateItemListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_dob.add(dateItemListBuilder);
          
     }
      context.employee_dob = employee_dob;
       return
}


// -------------------Age

if(typeSelect.equalsIgnoreCase(age)){

   dateOfBirth = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
   
    dateOfBirth.eachWithIndex { dateItem, index ->
          employeeNumber = dateItem.employeeNumber
          firstName      = dateItem.firstName
          lastName       = dateItem.lastName
          name = firstName +"   "+lastName
          birthDate      = dateItem.birthDate
          gender         = dateItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
            
          }else{
            ageInFiveYears = null
          }
          
           //Age In 10 years
           int ten = 10;
           if(!(presentYears==null)){
            ageInTenYears = org.ofbiz.humanres.LeaveServices.ageAfterYears(presentYears,ten);
           
          }else{
            ageInTenYears = null
          }
         
           dateItemListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_age.add(dateItemListBuilder);
          
     }
      context.employee_age = employee_age;
       return
}


///-------------GENDER

if(typeSelect.equalsIgnoreCase(gender)){

   dateOfBirth = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
   
    dateOfBirth.eachWithIndex { dateItem, index ->
          employeeNumber = dateItem.employeeNumber
          firstName      = dateItem.firstName
          lastName       = dateItem.lastName
          name = firstName +"   "+lastName
          birthDate      = dateItem.birthDate
          gender         = dateItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
         
           dateItemListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_gender.add(dateItemListBuilder);
          
     }
      context.employee_gender = employee_gender;
       return
}



//  ----FOR AGE COMPARISON

if(typeSelect.equalsIgnoreCase(comparison)){

   dateOfBirth = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
   
    dateOfBirth.eachWithIndex { dateItem, index ->
          employeeNumber = dateItem.employeeNumber
          firstName      = dateItem.firstName
          lastName       = dateItem.lastName
          name = firstName +"   "+lastName
          birthDate      = dateItem.birthDate
          gender         = dateItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
         
           dateItemListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_comparison.add(dateItemListBuilder);
          
     }
      context.employee_comparison = employee_comparison;
       return
}


//  ----- FOR AGE BRACKET

if(typeSelect.equalsIgnoreCase(ageBracket)){

   dateOfBirth = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);
   
    dateOfBirth.eachWithIndex { dateItem, index ->
          employeeNumber = dateItem.employeeNumber
          firstName      = dateItem.firstName
          lastName       = dateItem.lastName
          name = firstName +"   "+lastName
          birthDate      = dateItem.birthDate
          gender         = dateItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
         
           dateItemListBuilder = [
              employeeNumber :employeeNumber,
              name : name,
              birthDate :birthDate,
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_dob.add(dateItemListBuilder);
          
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
          gender         = staffItem.gender
          yearOfBirth =  org.ofbiz.humanres.LeaveServices.getCurrentYearDate(birthDate);
          
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
              gender : gender,
              yearOfBirth : yearOfBirth,
              presentYears : presentYears,
              ageInFiveYears : ageInFiveYears,
              ageInTenYears : ageInTenYears
              
              ]
          
           employee_listing.add(empListBuilder);
     }

  context.employee_listing = employee_listing;
