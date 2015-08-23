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

import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.party.party.PartyHelper;

fromDate = thruDate - 365
println("THROUGH DATE##################################################################### "+ thruDate)
partyNameList = [];
parties.each { party ->
    partyName = PartyHelper.getPartyName(party);
    partyNameList.add(partyName);
}
context.partyNameList = partyNameList;

if (thruDate) {
    customTimePeriod = delegator.findOne('CustomTimePeriod', [customTimePeriodId:parameters.customTimePeriodId], true)
    exprList = [];
    exprList.add(EntityCondition.makeCondition('organizationPartyId', EntityOperator.IN, partyIds))
   // exprList.add(EntityCondition.makeCondition('fromDate', EntityOperator.LESS_THAN, customTimePeriod.getDate('thruDate').toTimestamp()))
   // exprList.add(EntityCondition.makeCondition(EntityCondition.makeCondition('thruDate', EntityOperator.GREATER_THAN_EQUAL_TO, customTimePeriod.getDate('fromDate').toTimestamp()), EntityOperator.OR, EntityCondition.makeCondition('thruDate', EntityOperator.EQUALS, null)))
    List organizationGlAccounts = delegator.findList('GlAccountOrganizationAndClass', EntityCondition.makeCondition(exprList, EntityOperator.AND), null, ['accountCode'], null, false)

    accountBalances = []
    postedDebitsTotal = 0
    postedCreditsTotal = 0

  oldglAccountId = ""
  oldAccountBalance = [:]
  oldAccountBalance = null
  lastAccountBalance = [:]

  def creditsAmt = 0
  def debitsAmt = 0

    organizationGlAccounts.each { organizationGlAccount ->
    accountBalance = [:]
        //accountBalance = dispatcher.runSync('computeGlAccountBalanceForTimePeriod', [organizationPartyId: organizationGlAccount.organizationPartyId, customTimePeriodId: customTimePeriod.customTimePeriodId, glAccountId: organizationGlAccount.glAccountId, userLogin: userLogin]);
        accountBalance = dispatcher.runSync('computeGlAccountBalanceForTrialBalance', [organizationPartyId: organizationGlAccount.organizationPartyId, thruDate : thruDate, fromDate: fromDate,  glAccountId: organizationGlAccount.glAccountId, userLogin: userLogin]);
        if (accountBalance.postedDebits != 0 || accountBalance.postedCredits != 0) {

            accountBalance.glAccountId = organizationGlAccount.glAccountId
            accountBalance.accountCode = organizationGlAccount.accountCode
            accountBalance.accountName = organizationGlAccount.accountName
            postedDebitsTotal = postedDebitsTotal + accountBalance.postedDebits
            postedCreditsTotal = postedCreditsTotal + accountBalance.postedCredits

      if (oldAccountBalance == null){
        oldAccountBalance = accountBalance

        //creditsAmt = accountBalance.endingBalanceCredit
        //debitsAmt = accountBalance.endingBalanceDebit
      }

      //if ()
      lastAccountBalance = accountBalance
      //creditsAmt = creditsAmt + accountBalance.postedCredits
      //debitsAmt = debitsAmt + accountBalance.postedDebits

      if ((!oldAccountBalance.glAccountId.equals(accountBalance.glAccountId)) ){

        //if ()
        //accountBalance.postedCredits = 0

        oldAccountBalance.put('endingBalanceCredit', creditsAmt)

        //oldAccountBalance.putAt(postedCredits, creditsAmt)

        oldAccountBalance.put('endingBalanceDebit', debitsAmt)

        //oldAccountBalance.putAt(postedDebits, debitsAmt)
        accountBalances.add(oldAccountBalance);

        creditsAmt = accountBalance.endingBalanceCredit
        debitsAmt = accountBalance.endingBalanceDebit
        oldAccountBalance = accountBalance
      } else {

        //if (!isEmpty){
        //creditsAmt = creditsAmt + oldAccountBalance.postedCredits
        //debitsAmt = debitsAmt + oldAccountBalance.postedDebits


        if (accountBalance.endingBalanceCredit != null){

          if (creditsAmt == null){
            creditsAmt = 0
          }
          creditsAmt = creditsAmt + accountBalance.endingBalanceCredit
        }
        //endingBalanceDebit
        //endingBalanceDebit
        if (accountBalance.endingBalanceDebit != null){

          if (debitsAmt == null){
            debitsAmt = 0
          }
          debitsAmt = debitsAmt + accountBalance.endingBalanceDebit
        }

        println ' The credit total '+creditsAmt
        println ' The Debit total '+debitsAmt

         //oldAccountBalance.postedCredits = creditsAmt;
         //oldAccountBalance.postedDebits = debitsAmt
        //}
      }


        }

    }

  lastAccountBalance.endingBalanceCredit = creditsAmt
  lastAccountBalance.endingBalanceDebit = debitsAmt

  accountBalances.add(lastAccountBalance)
    context.postedDebitsTotal = postedDebitsTotal
    context.postedCreditsTotal = postedCreditsTotal
    context.accountBalances = accountBalances

    println "ACCOUNT BALANCESSSSSSSSSSSSSSSSSSSSSS: "+accountBalances
}
