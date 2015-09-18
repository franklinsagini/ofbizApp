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
import org.ofbiz.party.party.PartyHelper;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.accounting.ledger.GeneralLedgerServices;

fromDate = thruDate - 365
partyNameList = [];
parties.each { party ->
    partyName = PartyHelper.getPartyName(party);
    partyNameList.add(partyName);
}
context.partyNameList = partyNameList;
finalTransList = []
finalTransListBuilder = []
runningDebitBalance = 0
runningCreditBalance = 0
summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("ateTransactionDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));

if (organizationPartyId) {
  summaryCondition.add(EntityCondition.makeCondition("ateOrganizationPartyId", EntityOperator.EQUALS, organizationPartyId));
}

tBViewData = delegator.findList('TBViewData',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["gaoGlAccountId"],null,false)


tBViewData.each { obj ->
  GenericValue account = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", obj.gaoGlAccountId), true);
  isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(account);
  endingBalanceCredit = 0
  endingBalanceDebit = 0

  if (isDebit) {
    System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")

    if (obj.atDebitCreditFlag == "D") {
      System.out.println(endingBalanceDebit + " + " + obj.ateAmount)
      endingBalanceDebit = endingBalanceDebit + obj.ateAmount
    }else{
      System.out.println(endingBalanceDebit + " - " + obj.ateAmount)
       endingBalanceDebit = endingBalanceDebit - obj.ateAmount
    }

  }else{
    System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")
    if (obj.atDebitCreditFlag == "C") {
      System.out.println(endingBalanceCredit + " + " + obj.ateAmount)
      endingBalanceCredit = endingBalanceCredit + obj.ateAmount
    }else{
      System.out.println(endingBalanceCredit + " - " + obj.ateAmount)
       endingBalanceCredit = endingBalanceCredit - obj.ateAmount
    }
  }



  finalTransListBuilder = [
    accountCode:account.accountCode,
    accountName:account.accountName,
    endingBalanceCredit:endingBalanceCredit,
    endingBalanceDebit:endingBalanceDebit,
  ]
  finalTransList.add(finalTransListBuilder)

}

context.accountBalances = finalTransList

