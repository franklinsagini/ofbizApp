

action = request.getParameter("action");

staffSkillslist = [];
partyId = parameters.partyId
 staffskill = delegator.findByAnd("PartySkill", [partyId : partyId], null, false);
 
 staffskill.eachWithIndex { staffskillItem, index ->
 
 partyskill = delegator.findOne("SkillType", [skillTypeId : staffskillItem.skillTypeId], false);
 
 skill = partyskill.getString("description");
 experience = staffskillItem.getString("yearsExperience");
 level = staffskillItem.getString("skillLevel");
 
 
 
 staffSkillslist.add([skill :skill, experience :experience, level : level]);
 }
 
 
context.staffSkillslist = staffSkillslist;
