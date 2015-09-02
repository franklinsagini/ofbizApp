import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 




quarterSearch = parameters.quarter


countrr=0
countrrr2=0

if (quarterSearch) {
    staffReview = delegator.findByAnd("PerfPartyReview", [quarter : quarterSearch , stage : 'FORWARDED' ],null,false);
     
    staffReview.eachWithIndex { staffRev, index ->
    
      countrr = countrr + 1
     
      party =  staffReview.partyId
      
      context.party = party ;
      
      appraisser =  staffReview.hod
      
      appraisserDetail = delegator.findByAnd("Person", [partyId : staffReview.hod ],null,false); 
      context.appraisserDetail = appraisserDetail ;
     
      staffDetail = delegator.findByAnd("Person", [partyId : staffReview.partyId ],null,false); 
      context.staffDetail = staffDetail ;
      
      staffBranch =  delegator.findByAnd("PartyGroup", [partyId : staffDetail.branchId ],null,false); 
      context.staffBranch = staffBranch
      
      staffDept =  delegator.findByAnd("department", [departmentId : staffDetail.departmentId ],null,false); 
      context.staffDept = staffDept
      
      postStaff = delegator.findByAnd("EmplPositionType", {emplPositionTypeId : staffDetail.emplPositionTypeId },null, false)
       context.postStaff = postStaff    
          
          
    
     }

   
    context.staffReview = staffReview ;
    context.countrr = countrr ;
   
    return
  }


