<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<script language="JavaScript" type="text/javascript">
function togglefinAccountTransId(master) {
    var form = document.selectAllForm;
    var finAccountTransList = form.elements.length;
    for (var i = 0; i < finAccountTransList; i++) {
        var element = form.elements[i];
        if (element.type == "checkbox") {
            element.checked = master.checked;
        }
    }
    getFinAccountTransRunningTotalAndBalances();
}

function getFinAccountTransRunningTotalAndBalances() {
    var form = document.selectAllForm;
    var finAccountTransList = form.elements.length;
    var isSingle = true;
    var isAllSelected = true;
    for (var i = 0; i < finAccountTransList; i++) {
        var element = form.elements[i];
        if (jQuery(element[name^="_rowSubmit_o_"])) {
            if (element.checked) {
                isSingle = false;
            } else {
                isAllSelected = false;
            }
        }
    }
    if (isAllSelected) {
        jQuery('#checkAllTransactions').attr('checked', true);
    } else {
        jQuery('#checkAllTransactions').attr('checked', false);
    }
    if (!isSingle) {
        jQuery('#submitButton').removeAttr('disabled');
        if (jQuery('#showFinAccountTransRunningTotal').length) {
            jQuery.ajax({
                url: 'getFinAccountTransRunningTotalAndBalances',
                async: false,
                type: 'POST',
                data: jQuery('#listFinAccTra').serialize(),
                success: function(data) {
                    jQuery('#showFinAccountTransRunningTotal').html(data.finAccountTransRunningTotal);
                    jQuery('#finAccountTransRunningTotal').html(data.finAccountTransRunningTotal);
                    jQuery('#numberOfFinAccountTransaction').html(data.numberOfTransactions);
                    jQuery('#endingBalance').html(data.endingBalance);
                    jQuery('#uncreditedBankings').html(data.uncreditedBankingsTotal);

                }
            });
        }
    } else {
        if (jQuery('#showFinAccountTransRunningTotal').length) {
            jQuery('#showFinAccountTransRunningTotal').html("");
            jQuery('#finAccountTransRunningTotal').html("");
            jQuery('#numberOfFinAccountTransaction').html("");
            jQuery('#endingBalance').html(jQuery('#endingBalanceInput').val());

        }
        jQuery('#submitButton').attr('disabled', true);
    }
}
</script>

<div class="screenlet screenlet-body">
  <#if finAccountTransList?has_content && parameters.noConditionFind?exists && parameters.noConditionFind == 'Y'>
   <#if !grandTotal?exists>
    <h3>
    <table class="basic-table">
      <tr>
        <td>Cash Book Balance</td>
        <td id="showFinAccountTransRunningTotal"></td>
        <td>Bank Statement Balance</td>
        <td id="showBankStatementBalance"></td>
      </tr>
      <tr>
        <td>Unidentified Debits</td>
        <td></td>
        <td>Uncredited Bankings</td>
        <#if uncreditedBankingsTotal?has_content>
         <td id="uncreditedBankings"><@ofbizCurrency amount=uncreditedBankingsTotal?if_exists/></td>
         <#else>
         <td>0</td>
        </#if>
      </tr>
      <tr>
        <td>Unreceipted Bankings</td>
        <td></td>
        <td>Unpresented Cheques</td>
        <#if unpresentedChequesTotal?has_content>
         <td id="unpresentedCheques"><@ofbizCurrency amount=unpresentedChequesTotal?if_exists/></td>
         <#else>
         <td>0</td>
        </#if>
      </tr>
      <tr>
        <td>Adjusted Cash Book Balance</td>
        <td></td>
        <td>Adjusted Bank Statement Balance</td>
        <td></td>
      </tr>
    </table>
  </h3>
   </#if>
    <#if !grandTotal?exists>
    <span class="label">${uiLabelMap.AccountingRunningTotal} :</span>
    <span class="label" id="showFinAccountTransRunningTotal"></span>

    </#if>
    <form id="listFinAccTra" name="selectAllForm" method="post" action="<@ofbizUrl><#if !grandTotal?exists>reconcileFinAccountTrans?clearAll=Y<#else>assignGlRecToFinAccTrans?clearAll=Y</#if></@ofbizUrl>">
      <input name="_useRowSubmit" type="hidden" value="Y"/>
      <input name="finAccountId" type="hidden" value="${parameters.finAccountId}"/>
      <input name="uncreditedBankingsTotal" id="uncreditedBankingsTotal" type="hidden" value="60"/>
      <input name="statusId" type="hidden" value="${parameters.statusId?if_exists}"/>
      <#if !grandTotal?exists>
        <input name="reconciledBalance" type="hidden" value="${(glReconciliation.reconciledBalance)?if_exists}"/>
        <input name="reconciledBalanceWithUom" type="hidden" id="reconciledBalanceWithUom" value="<@ofbizCurrency amount=(glReconciliation.reconciledBalance)?default('0')/>"/>
      </#if>
      <#assign glReconciliations = delegator.findByAnd("GlReconciliation", {"glAccountId" : finAccount.postToGlAccountId?if_exists, "statusId" : "GLREC_CREATED"}, Static["org.ofbiz.base.util.UtilMisc"].toList("reconciledDate DESC"), false)>
      <#if (glReconciliationId?has_content && (glReconciliationId == "_NA_" && finAccountTransList?has_content)) || !grandTotal?exists>
        <div align="right">
          <#if grandTotal?exists>
            <#if glReconciliations?has_content>
              <select name="glReconciliationId">
                <option value="">--${uiLabelMap.CommonSelect}--</option>
                <#list glReconciliations as glReconciliation>
                  <option value="${glReconciliation.glReconciliationId}">${glReconciliation.glReconciliationName?if_exists}[[${glReconciliation.glReconciliationId}] [${glReconciliation.reconciledDate?if_exists}] [${glReconciliation.reconciledBalance?if_exists}]]</option>
                </#list>
              </select>
              <input id="submitButton" type="submit" onclick="javascript:document.selectAllForm.submit();" value="${uiLabelMap.AccountingAssignToReconciliation}" disabled="disabled" />
            <#else>
              <span class="tooltip">${uiLabelMap.AccountingNoGlReconciliationExists} <a href="<@ofbizUrl>EditFinAccountReconciliations?finAccountId=${parameters.finAccountId?if_exists}</@ofbizUrl>">${uiLabelMap.CommonClickHere}</a></span>
            </#if>
          <#else>
            <input id="submitButton" type="submit" onclick="javascript:document.selectAllForm.submit();" value="${uiLabelMap.AccountingReconcile}" disabled="disabled" />
          </#if>
        </div>
      </#if>
      <table class="basic-table hover-bar" cellspacing="0">
        <#-- Header Begins -->
        <tr class="header-row-2">
          <th>${uiLabelMap.FormFieldTitle_finAccountTransId}</th>
          <th>${uiLabelMap.FormFieldTitle_finAccountTransTypeId}</th>
          <th>${uiLabelMap.PartyParty}</th>
          <th>${uiLabelMap.FormFieldTitle_glReconciliationName}</th>
          <th>${uiLabelMap.FormFieldTitle_transactionDate}</th>
          <th>${uiLabelMap.FormFieldTitle_entryDate}</th>
          <th>${uiLabelMap.CommonAmount}</th>
          <th>${uiLabelMap.FormFieldTitle_paymentId}</th>
          <th>${uiLabelMap.OrderPaymentType}</th>
          <th>${uiLabelMap.FormFieldTitle_paymentMethodTypeId}</th>
          <th>${uiLabelMap.CommonStatus}</th>
          <th>${uiLabelMap.CommonComments}</th>
   <#--        <#if grandTotal?exists>
            <th>Transactions Actions</th>
          </#if> -->
          <#if !grandTotal?exists>
            <#if (parameters.glReconciliationId?has_content && parameters.glReconciliationId != "_NA_")>
              <th>${uiLabelMap.AccountingRemoveFromGlReconciliation}</th>
            </#if>
          </#if>
          <#if ((glReconciliationId?has_content && glReconciliationId == "_NA_") && (glReconciliations?has_content && finAccountTransList?has_content)) || !grandTotal?exists>
            <th>${uiLabelMap.CommonSelectAll} <input name="selectAll" type="checkbox" value="N" id="checkAllTransactions" onclick="javascript:togglefinAccountTransId(this);"/></th>
          </#if>
        </tr>
        <#-- Header Ends-->
        <#assign alt_row = false>
        <#list finAccountTransList as finAccountTrans>
          <#assign payment = "">
          <#assign payments = "">
          <#assign status = "">
          <#assign paymentType = "">
          <#assign paymentMethodType = "">
          <#assign glReconciliation = "">
          <#assign partyName = "">
          <#if finAccountTrans.paymentId?has_content>
            <#assign payment = delegator.findOne("Payment", {"paymentId" : finAccountTrans.paymentId}, true)>
          <#else>
            <#assign payments = delegator.findByAnd("Payment", {"finAccountTransId" : finAccountTrans.finAccountTransId}, null, false)>
          </#if>
          <#assign finAccountTransType = delegator.findOne("FinAccountTransType", {"finAccountTransTypeId" : finAccountTrans.finAccountTransTypeId}, true)>
          <#if finAccountTrans.statusId?has_content>
            <#assign status = delegator.findOne("StatusItem", {"statusId" : finAccountTrans.statusId}, true)>
          </#if>
          <#if payment?has_content && payment.paymentTypeId?has_content>
            <#assign paymentType = delegator.findOne("PaymentType", {"paymentTypeId" : payment.paymentTypeId}, true)>
          </#if>
          <#if payment?has_content && payment.paymentMethodTypeId?has_content>
            <#assign paymentMethodType = delegator.findOne("PaymentMethodType", {"paymentMethodTypeId" : payment.paymentMethodTypeId}, true)>
          </#if>
          <#if finAccountTrans.glReconciliationId?has_content>
            <#assign glReconciliation = delegator.findOne("GlReconciliation", {"glReconciliationId" : finAccountTrans.glReconciliationId}, true)>
            <input name="openingBalance_o_${finAccountTrans_index}" type="hidden" value="${glReconciliation.openingBalance?if_exists}"/>
          </#if>
          <#if finAccountTrans.partyId?has_content>
            <#assign partyName = (delegator.findOne("PartyNameView", {"partyId" : finAccountTrans.partyId}, true))!>
          </#if>
          <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
            <td>
              <#if payments?has_content>
                <a id="togglePayment_${finAccountTrans.finAccountTransId}" href="javascript:void(0)"><img src="<@ofbizContentUrl>/images/expand.gif</@ofbizContentUrl>" alt=""/></a> ${finAccountTrans.finAccountTransId}
                <div id="displayPayments_${finAccountTrans.finAccountTransId}" style="display: none;width: 650px;">
                  <table class="basic-table hover-bar" cellspacing="0" style"width :">
                    <tr class="header-row-2">
                      <th>${uiLabelMap.AccountingDepositSlipId}</th>
                      <th>${uiLabelMap.FormFieldTitle_paymentId}</th>
                      <th>${uiLabelMap.OrderPaymentType}</th>
                      <th>${uiLabelMap.FormFieldTitle_paymentMethodTypeId}</th>
                      <th>${uiLabelMap.CommonAmount}</th>
                      <th>${uiLabelMap.PartyPartyFrom}</th>
                      <th>${uiLabelMap.PartyPartyTo}</th>
                    </tr>
                    <#list payments as payment>
                      <#if payment?exists && payment.paymentTypeId?has_content>
                        <#assign paymentType = delegator.findOne("PaymentType", {"paymentTypeId" : payment.paymentTypeId}, true)>
                      </#if>
                      <#if payment?has_content && payment.paymentMethodTypeId?has_content>
                        <#assign paymentMethodType = delegator.findOne("PaymentMethodType", {"paymentMethodTypeId" : payment.paymentMethodTypeId}, true)>
                      </#if>
                      <#if payment?has_content>
                        <#assign paymentGroupMembers = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(payment.getRelated("PaymentGroupMember", null, null, false)?if_exists) />
                        <#assign fromParty = payment.getRelatedOne("FromParty", false)?if_exists />
                        <#assign fromPartyName = delegator.findOne("PartyNameView", {"partyId" : fromParty.partyId}, true) />
                        <#assign toParty = payment.getRelatedOne("ToParty", false)?if_exists />
                        <#assign toPartyName = delegator.findOne("PartyNameView", {"partyId" : toParty.partyId}, true) />
                        <#if paymentGroupMembers?has_content>
                          <#assign paymentGroupMember = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(paymentGroupMembers) />
                        </#if>
                      </#if>
                      <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                        <td><#if paymentGroupMember?has_content><a href="<@ofbizUrl>EditDepositSlipAndMembers?paymentGroupId=${paymentGroupMember.paymentGroupId?if_exists}&amp;finAccountId=${parameters.finAccountId?if_exists}</@ofbizUrl>">${paymentGroupMember.paymentGroupId?if_exists}</a></#if></td>
                        <td><#if payment?has_content><a href="<@ofbizUrl>paymentOverview?paymentId=${payment.paymentId?if_exists}</@ofbizUrl>">${payment.paymentId?if_exists}</a></#if></td>
                        <td><#if paymentType?has_content>${paymentType.description?if_exists}</#if></td>
                        <td><#if paymentMethodType?has_content>${paymentMethodType.description?if_exists}</#if></td>
                        <td><@ofbizCurrency amount=payment.amount?if_exists/></td>
                        <td><#if fromPartyName?has_content>${fromPartyName.groupName?if_exists}${fromPartyName.firstName?if_exists} ${fromPartyName.lastName?if_exists}<a href="/partymgr/control/viewprofile?partyId=${fromPartyName.partyId?if_exists}">[${fromPartyName.partyId?if_exists}]</a></#if></td>
                        <td><#if toPartyName?has_content>${toPartyName.groupName?if_exists}${toPartyName.firstName?if_exists} ${toPartyName.lastName?if_exists}<a href="/partymgr/control/viewprofile?partyId=${toPartyName.partyId?if_exists}">[${toPartyName.partyId?if_exists}]</a></#if></td>
                      </tr>
                    </#list>
                  </table>
                </div>
                <script type="text/javascript">
                   jQuery(document).ready( function() {
                        jQuery("#displayPayments_${finAccountTrans.finAccountTransId}").dialog({autoOpen: false, modal: true,
                                buttons: {
                                '${uiLabelMap.CommonClose}': function() {
                                    jQuery(this).dialog('close');
                                    }
                                }
                           });
                   jQuery("#togglePayment_${finAccountTrans.finAccountTransId}").click(function(){jQuery("#displayPayments_${finAccountTrans.finAccountTransId}").dialog("open")});
                   });
                </script>
                <a href="<@ofbizUrl>DepositSlip.pdf?finAccountTransId=${finAccountTrans.finAccountTransId}</@ofbizUrl>" target="_BLANK" class="buttontext">${uiLabelMap.AccountingDepositSlip}</a>
              <#else>
                ${finAccountTrans.finAccountTransId}
              </#if>
            </td>
            <td>${finAccountTransType.description?if_exists}</td>
            <td><#if partyName?has_content>${(partyName.firstName)!} ${(partyName.lastName)!} ${(partyName.groupName)!}<a href="/partymgr/control/viewprofile?partyId=${partyName.partyId}">[${(partyName.partyId)!}]</a></#if></td>
            <td><#if glReconciliation?has_content>${glReconciliation.glReconciliationName?if_exists}<a href="ViewGlReconciliationWithTransaction?glReconciliationId=${glReconciliation.glReconciliationId?if_exists}&amp;finAccountId=${parameters.finAccountId?if_exists}">[${glReconciliation.glReconciliationId?if_exists}]</a></#if></td>
            <td>${finAccountTrans.transactionDate?if_exists}</td>
            <td>${finAccountTrans.entryDate?if_exists}</td>
            <td>${finAccountTrans.amount?if_exists}</td>
            <td>
              <#if finAccountTrans.paymentId?has_content>
                <a href="<@ofbizUrl>paymentOverview?paymentId=${finAccountTrans.paymentId}</@ofbizUrl>">${finAccountTrans.paymentId}</a>
              </#if>
            </td>
            <td><#if paymentType?has_content>${paymentType.description?if_exists}</#if></td>
            <td><#if paymentMethodType?has_content>${paymentMethodType.description?if_exists}</#if></td>
            <td>
<#--               <#if status?has_content>
                ${status.description?if_exists}
              </#if> -->
              <#if finAccountTrans.statusId == "FINACT_TRNS_CREATED">
                NOT RECONCILED
              <#else>
                RECONCILED
              </#if>
            </td>
            <td>${finAccountTrans.comments?if_exists}</td>
<#--             <#if grandTotal?exists>
              <td>
                <#if finAccountTrans.statusId?has_content && finAccountTrans.statusId == 'FINACT_TRNS_CREATED'>
                  <a href="javascript:document.cancelFinAccountTrans_${finAccountTrans.finAccountTransId}.submit();" class="buttontext">${uiLabelMap.CommonCancel}</a>
                  <a href="javascript:document.approveFinAccountTrans_${finAccountTrans.finAccountTransId}.submit();" class="buttontext">Approve</a>
                </#if>
              </td>
            </#if> -->
            <input name="finAccountTransId_o_${finAccountTrans_index}" type="hidden" value="${finAccountTrans.finAccountTransId}"/>
            <input name="organizationPartyId_o_${finAccountTrans_index}" type="hidden" value="${defaultOrganizationPartyId}"/>
            <#if glReconciliationId?has_content && glReconciliationId != "_NA_">
              <input name="glReconciliationId_o_${finAccountTrans_index}" type="hidden" value="${glReconciliationId}"/>
            </#if>
            <#if !(grandTotal?exists)>
              <#if (parameters.glReconciliationId?has_content && parameters.glReconciliationId != "_NA_")>
                <#if finAccountTrans.statusId == "FINACT_TRNS_CREATED">
                  <td><a href="javascript:document.removeFinAccountTransFromReconciliation_${finAccountTrans.finAccountTransId}.submit();" class="buttontext">${uiLabelMap.CommonRemove}</a></td>
                </#if>
              </#if>
            </#if>
            <#if ((glReconciliationId?has_content && glReconciliationId == "_NA_") && (glReconciliations?has_content && finAccountTransList?has_content)) || !grandTotal?exists>
              <#if finAccountTrans.statusId == "FINACT_TRNS_CREATED">
                <td><input id="finAccountTransId_${finAccountTrans_index}" name="_rowSubmit_o_${finAccountTrans_index}" type="checkbox" value="Y" onclick="javascript:getFinAccountTransRunningTotalAndBalances();"/></td>
              </#if>
            </#if>
          </tr>
          <#-- toggle the row color -->
          <#assign alt_row = !alt_row>
        </#list>
      </table>
    </form>
    <#list finAccountTransList as finAccountTrans>
      <form name="removeFinAccountTransFromReconciliation_${finAccountTrans.finAccountTransId}" method="post" action="<@ofbizUrl>removeFinAccountTransFromReconciliation</@ofbizUrl>">
        <input name="finAccountTransId" type="hidden" value="${finAccountTrans.finAccountTransId}"/>
        <input name="finAccountId" type="hidden" value="${finAccountTrans.finAccountId}"/>
      </form>
    </#list>
    <#if grandTotal?exists>
      <#list finAccountTransList as finAccountTrans>
        <#if finAccountTrans.statusId?has_content && finAccountTrans.statusId == 'FINACT_TRNS_CREATED'>
          <form name="cancelFinAccountTrans_${finAccountTrans.finAccountTransId}" method="post" action="<@ofbizUrl>setFinAccountTransStatus</@ofbizUrl>">
            <input name="noConditionFind" type="hidden" value="Y"/>
            <input name="finAccountTransId" type="hidden" value="${finAccountTrans.finAccountTransId}"/>
            <input name="finAccountId" type="hidden" value="${finAccountTrans.finAccountId}"/>
            <input name="statusId" type="hidden" value="FINACT_TRNS_CANCELED"/>
          </form>
          <form name="approveFinAccountTrans_${finAccountTrans.finAccountTransId}" method="post" action="<@ofbizUrl>setFinAccountTransStatus</@ofbizUrl>">
            <input name="noConditionFind" type="hidden" value="Y"/>
            <input name="finAccountTransId" type="hidden" value="${finAccountTrans.finAccountTransId}"/>
            <input name="finAccountId" type="hidden" value="${finAccountTrans.finAccountId}"/>
            <input name="statusId" type="hidden" value="FINACT_TRNS_APPROVED"/>
          </form>
        </#if>
      </#list>
      <table class="basic-table">
        <tr>
          <th>${uiLabelMap.FormFieldTitle_grandTotal} / ${uiLabelMap.AccountingNumberOfTransaction}</th>
          <th>${uiLabelMap.AccountingCreatedGrandTotal} / ${uiLabelMap.AccountingNumberOfTransaction}</th>
          <th>${uiLabelMap.AccountingApprovedGrandTotal} / ${uiLabelMap.AccountingNumberOfTransaction}</th>
          <th>${uiLabelMap.AccountingCreatedApprovedGrandTotal} / ${uiLabelMap.AccountingNumberOfTransaction}</th>
        </tr>
        <tr>
          <td>${grandTotal} / ${searchedNumberOfRecords}</td>
          <td>${createdGrandTotal} / ${totalCreatedTransactions}</td>
          <td>${approvedGrandTotal} / ${totalApprovedTransactions}</td>
          <td>${createdApprovedGrandTotal} / ${totalCreatedApprovedTransactions}</td>
        </tr>
      </table>
    <#else>
      <table class="basic-table">
        <tr>
          <th>${uiLabelMap.AccountingRunningTotal} / ${uiLabelMap.AccountingNumberOfTransaction}</th>
          <th>${uiLabelMap.AccountingOpeningBalance}</th>
          <th>${uiLabelMap.FormFieldTitle_reconciledBalance}</th>
          <th>${uiLabelMap.FormFieldTitle_closingBalance}</th>
        </tr>
        <tr>
          <td>
            <span id="finAccountTransRunningTotal">0</span> /
            <span id="numberOfFinAccountTransaction">0</span>
          </td>
          <td><@ofbizCurrency amount=glReconciliation.openingBalance?default('0')/></td>
          <td><@ofbizCurrency amount=glReconciliation.reconciledBalance?default('0')/></td>
          <td id="endingBalance"><@ofbizCurrency amount=glReconciliationApprovedGrandTotal?if_exists/></td>
          <input type="hidden" id="endingBalanceInput" value="<@ofbizCurrency amount=glReconciliationApprovedGrandTotal?if_exists/>"/>
        </tr>
      </table>
    </#if>
  <#else>
    <h2>${uiLabelMap.CommonNoRecordFound}</h2>
  </#if>
</div>
