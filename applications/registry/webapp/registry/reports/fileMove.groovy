import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

partyId = parameters.partyId

lpartyId = partyId.toLong();

whoHasFile = delegator.findOne("RegistryFiles", [partyId : partyId], false);
context.whoHasFile = whoHasFile;
println "---------------"+whoHasFile.fileLocation;



def movementActivityList = []
member = delegator.findOne("Member", [partyId : lpartyId], false);
System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMEMBER "+ member)
context.member = member;


class MovementActivity{
	def activityCode
	def grouper
	def partyId
	def activityDuration
	def total
	def listMovements = []
}

class FileMovement{
	def releasedBy
	def releasedTo
	def carriedBy
	def receivedBy
	def activityCode
	def grouper
	def timeIn
	def timeOut
	def timeStayedWithFile
}

List<String> orderByList = new ArrayList<String>();
		orderByList.add("timeOut");


//activityList = delegator.findList("fileMovementActivityView", null, null, null, null, false);
activityList = delegator.findByAnd("fileMovementActivityView", [partyId : partyId], null, false);
 
 activityList.eachWithIndex { activityItem, index ->
 activities = delegator.findByAnd("RegistryFileMovement", [partyId : partyId, grouper : activityItem.grouper], orderByList, false);

activities.eachWithIndex { movementItem, indexMovement ->
	
duration = delegator.findByAnd("RegistryFileActivity", [activityId : movementItem.activityCode], null, false);
 
 activity = new MovementActivity()
 activity.activityCode = duration.activity
 activity.activityDuration = duration.activityDuration
 activity.total = BigDecimal.ZERO;
 
 }
 
  
 
 def movement
activities.eachWithIndex { movementItem, indexMovement ->

	
	
	 movement = new FileMovement()
	 movement.receivedBy = movementItem.receivedBy
	 movement.releasedBy = movementItem.releasedBy
	 movement.releasedTo = movementItem.releasedTo
	 movement.carriedBy = movementItem.carriedBy
	 movement.activityCode = movementItem.activityCode
	 movement.grouper = movementItem.grouper
	 movement.timeIn = movementItem.timeIn 
	 movement.timeOut = movementItem.timeOut
	 movement.timeStayedWithFile = org.ofbiz.registry.FileServices.calculatehoursBetweenDates(movementItem.timeOut)
	 activity.listMovements.add(movement)
	 System.out.println("RELEASEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD TO " + movement.releasedTo)
	 
 }
 
 
 movementActivityList.add(activity)
  
 
 }
 context.movementActivityList = movementActivityList;