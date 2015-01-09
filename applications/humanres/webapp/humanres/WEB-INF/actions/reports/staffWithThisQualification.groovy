import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];
title = parameters.title

   employee = delegator.findByAnd("PartyQual", [title : title],null, false);
   context.employee = employee;
   context.title = title



