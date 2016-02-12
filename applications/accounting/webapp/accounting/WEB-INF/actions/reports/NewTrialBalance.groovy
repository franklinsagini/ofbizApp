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
totalDebits = 0
summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("ateTransactionDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));


if (organizationPartyId=="Company") {
  System.out.println("SKIPPING ADDING OF ORGANIZATION CONDITION BECAUSE IT IS: " + organizationPartyId + " HENCE CONSOLIDATED")
}else{
  summaryCondition.add(EntityCondition.makeCondition("ateOrganizationPartyId", EntityOperator.EQUALS, organizationPartyId));
  System.out.println("USING OrganizationPartyId " + organizationPartyId + " AS PART OF THE CONDITIONS")
}

tBViewData = delegator.findList('TBViewData',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,["gaoGlAccountId"],null,false)
glAccounts = delegator.findList('GlAccount',  null, null,["accountCode"],null,false)

glAccounts.each { glAccount ->
  accountBalance = 0
  isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(glAccount);
  endingBalanceCredit = 0
  endingBalanceDebit = 0
  tBViewData.each { obj ->

    if (obj.gaoGlAccountId == glAccount.glAccountId) {
      if (isDebit) {
        System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")
        if (obj.atDebitCreditFlag == "D") {
          postedDebitsTotal = postedDebitsTotal + obj.ateAmount
          accountBalance = accountBalance + obj.ateAmount

        }else{
          postedCreditsTotal = postedCreditsTotal + obj.ateAmount
          accountBalance = accountBalance - obj.ateAmount
        }
        endingBalanceDebit = accountBalance
      }else{
        System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")
        if (obj.atDebitCreditFlag == "C") {
          postedCreditsTotal = postedCreditsTotal + obj.ateAmount
          accountBalance = accountBalance + obj.ateAmount
        }else{
          postedDebitsTotal = postedDebitsTotal + obj.ateAmount
          accountBalance = accountBalance - obj.ateAmount
        }
        endingBalanceCredit = accountBalance
      }
    }
  }
  if (endingBalanceCredit == 0) {
    totalDebits = totalDebits+endingBalanceDebit
  }
  if (endingBalanceDebit == 0) {
    totalCredits = totalCredits + endingBalanceCredit
  }
  if (endingBalanceCredit != 0 || endingBalanceDebit != 0) {
    finalTransListBuilder = [
        accountCode:glAccount.accountCode,
        accountName:glAccount.accountName,
        endingBalanceCredit:endingBalanceCredit,
        endingBalanceDebit:endingBalanceDebit,
      ]
      finalTransList.add(finalTransListBuilder)
  }

}

System.out.println("TOTAL CREDITS: "+totalCredits)
System.out.println("TOTAL DEBITS: "+totalDebits)

context.accountBalances = finalTransList
//context.postedDebitsTotal = postedDebitsTotal
context.postedDebitsTotal = totalDebits
//context.postedCreditsTotal = postedCreditsTotal
context.postedCreditsTotal = totalCredits

