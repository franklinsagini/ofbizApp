/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.ofbiz.entity.*
import org.ofbiz.entity.condition.*
import org.ofbiz.entity.transaction.*

action = request.getParameter("action");

inventoryItemTotals = [];
boolean beganTransaction = false;
if (action) {
    conditions = [EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INV_DELIVERED")];
    conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, null));
    conditionList = EntityCondition.makeCondition(conditions, EntityOperator.OR);
    try {
        beganTransaction = TransactionUtil.begin();
        invItemListItr = delegator.find("MSaccoApplication", null, null, null, null, null);
        while ((msacco = invItemListItr.next()) != null) {
            memberAccountId = msacco.memberAccountId;
            partyId = msacco.partyId;
            AccNo = delegator.findOne("MemberAccount", [memberAccountId : memberAccountId], false);
            Member = delegator.findOne("Member", [partyId : partyId], false);
            if (Member) {
                FName = Member.getString("firstName");
                LName = Member.getString("lastName");
                phoneNo = msacco.getString("mobilePhoneNumber");
                Id = Member.getString("idNumber");
                

                resultMap = [FName : "memberAccountId", LName : "memberAccountId", phoneNo : "memberAccountId",
                             Id : "memberAccountId", AccountNo : "memberAccountId"];
                inventoryItemTotals.add(resultMap);
            }
        }
        invItemListItr.close();
    } catch (GenericEntityException e) {
        errMsg = "Failure in operation, rolling back transaction";
        Debug.logError(e, errMsg, "findInventoryItemsByLabels");
        try {
            // only rollback the transaction if we started one...
            TransactionUtil.rollback(beganTransaction, errMsg, e);
        } catch (GenericEntityException e2) {
            Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), "findInventoryItemsByLabels");
        }
        // after rolling back, rethrow the exception
        throw e;
    } finally {
        // only commit the transaction if we started one... this will throw an exception if it fails
        TransactionUtil.commit(beganTransaction);
    }

}
context.inventoryItemTotals = inventoryItemTotals;
