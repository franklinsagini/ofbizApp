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
postedDebitsTotal = 0
postedCreditsTotal = 0
totalCredits = 0
totalDebit = 0
summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("ateTransactionDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));


if (organizationPartyId=="Company") {
  System.out.println("SKIPPING ADDING OF ORGANIZATION CONDITION BECAUSE IT IS: " + organizationPartyId + " HENCE CONSOLIDATED")
}else{
  summaryCondition.add(EntityCondition.makeCondition("ateOrganizationPartyId", EntityOperator.EQUALS, organizationPartyId));
  System.out.println("USING OrganizationPartyId " + organizationPartyId + " AS PART OF THE CONDITIONS")
}

tBViewData = delegator.findList('TBViewData',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,null,null,false)
glAccounts = delegator.findList('GlAccount',  null, null,["glAccountId"],null,false)


//glAccounts.eachWithIndex { item, index ->
//    println item
//    println index
//}
grandDebitBalance = 0
grandCreditBalance = 0
glAccounts.each { glAccount ->
  accountBalance = 0
  accountCode = glAccount.accountCode
  accountName = glAccount.accountName
  totalAccountDebits = 0
  totalAccountCredits = 0

  tBViewData.eachWithIndex { glBalance, index ->
    if (glBalance.gaoGlAccountId == glAccount.glAccountId) {
      //Total Debits
      if (glBalance.atDebitCreditFlag == "D") {
         totalAccountDebits = totalAccountDebits + glBalance.ateAmount
      }
      //Total Credits
      if (glBalance.atDebitCreditFlag == "C") {
         totalAccountCredits = totalAccountCredits + glBalance.ateAmount
      }

    }
  }

  //Accouunt Balance
  isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(glAccount);
  if (isDebit) {
    accountBalance = totalAccountDebits - totalAccountCredits
    grandDebitBalance = grandDebitBalance + accountBalance
  }
  else{
    accountBalance = totalAccountCredits - totalAccountDebits
    grandCreditBalance = grandCreditBalance + accountBalance
  }
  println "Account " + accountName + " Balance " + accountBalance
}


println("##################### TOTAL DEBITS " +grandDebitBalance)
println("##################### TOTAL CREDITS " +grandCreditBalance)

context.accountBalances = finalTransList
context.postedDebitsTotal = grandDebitBalance
context.postedCreditsTotal = grandCreditBalance

