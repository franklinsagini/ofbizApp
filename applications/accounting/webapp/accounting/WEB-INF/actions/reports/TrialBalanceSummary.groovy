import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.party.party.PartyHelper;
import org.ofbiz.base.util.UtilMisc;

fromDate = thruDate - 365
exprList = [];
exprList.add(EntityCondition.makeCondition('organizationPartyId', EntityOperator.IN, partyIds))

List organizationGlAccounts = delegator.findList('GlAccountOrganizationAndClass', EntityCondition.makeCondition(exprList, EntityOperator.AND), null, ['accountCode'], null, false)

accountBalances = []
postedDebitsTotal = 0
postedCreditsTotal = 0
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
        accountBalances.add(accountBalance)
        System.out.println("########################################################################### accountBalance " + accountBalance)
        GenericValue account = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", organizationGlAccount.glAccountId), true);
        isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(account);

        if (isDebit) {
          System.out.println("THIS IS A DEBIT ACCOUNT")
          dispatcher.runSync('storeGlAccountBlances', [runDate : thruDate,   glAccountId: accountBalance.glAccountId, debitCreditFlag:"D", balance:accountBalance.endingBalance, userLogin: userLogin]);
        }else {
          System.out.println("THIS IS A CREDIT ACCOUNT")
          dispatcher.runSync('storeGlAccountBlances', [runDate : thruDate,   glAccountId: accountBalance.glAccountId, debitCreditFlag:"C", balance:accountBalance.endingBalance, userLogin: userLogin]);
        }
    }

 }
    context.postedDebitsTotal = postedDebitsTotal
    context.postedCreditsTotal = postedCreditsTotal
    context.accountBalances = accountBalances
