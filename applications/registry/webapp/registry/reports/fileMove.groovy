import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId

lpartyId = partyId.toLong();


def movementActivityList = []
member = delegator.findOne("Member", [partyId : lpartyId], false);
System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMEMBER "+ member)
context.member = member;


class MovementActivity{
	def activityCode
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
	def timeIn
	def timeOut
}

//activityList = delegator.findList("fileMovementActivityView", null, null, null, null, false);
activityList = delegator.findByAnd("fileMovementActivityView", [partyId : partyId], null, false);
 
 activityList.eachWithIndex { activityItem, index ->
 activities = delegator.findByAnd("RegistryFileMovement", [partyId : partyId, activityCode : activityItem.activityCode], null, false);

 duration = delegator.findByAnd("RegistryFileActivity", [activityId : activityItem.activityCode], null, false);
 
 activity = new MovementActivity()
 activity.activityCode = duration.activity
 activity.activityDuration = duration.activityDuration
 activity.total = BigDecimal.ZERO;

 def movement
activities.eachWithIndex { movementItem, indexMovement ->
	
	movement = new FileMovement()
	 movement.receivedBy = movementItem.receivedBy
	 movement.releasedBy = movementItem.releasedBy
	 movement.releasedTo = movementItem.releasedTo
	 movement.carriedBy = movementItem.carriedBy
	 movement.activityCode = movementItem.activityCode
	 movement.timeIn = movementItem.timeIn
	 movement.timeOut = movementItem.timeOut
	 activity.listMovements.add(movement)
	 System.out.println("RELEASEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD TO " + movement.releasedTo)
	 
 }
 
 
 movementActivityList.add(activity)
  
 
 }
 context.movementActivityList = movementActivityList;