
//typeSelect = parameters.typeSelect;

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
