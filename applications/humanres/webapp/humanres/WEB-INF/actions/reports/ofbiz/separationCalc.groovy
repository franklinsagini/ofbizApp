import groovy.time.*
import java.text.SimpleDateFormat


separationDetailId = parameters.separationDetailId

separationDetailELI = delegator.findOne("SeparationDetail", [separationDetailId : separationDetailId ], false);

partyId = separationDetailELI.partyId

personDetails = delegator.findOne("Person", [partyId: partyId ], false);


context.title = "Chai CO-OP. SAVINGS AND CREDIT SOCIETY LTD";

context.separationDetailId = separationDetailId

context.personDetails = personDetails

branch = delegator.findOne("PartyGroup", [partyId : personDetails.branchId], false);
context.branch =  branch

def currentDate = new Date()
sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss")

context.currentDate = sdf.format(currentDate)


