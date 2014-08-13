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
<#-- This file has been modified by Open Source Strategies, Inc. -->

<#assign extInfo = parameters.extInfo?default("N")>
<#assign inventoryItemId = parameters.inventoryItemId?default("")>
<#assign serialNumber = parameters.serialNumber?default("")>
<#assign softIdentifier = parameters.softIdentifier?default("")>
<#-- Only allow the search fields to be hidden when we have some results -->
<#if applicationsList?has_content>
  <#assign hideFields = parameters.hideFields?default("N")>
<#else>
  <#assign hideFields = "N">
</#if>
<h1>Loan Applications List</h1>
<#if (parameters.firstName?has_content || parameters.lastName?has_content)>
    <#assign createUrl = "editperson?create_new=Y&amp;lastName=${parameters.lastName?if_exists}&amp;firstName=${parameters.firstName?if_exists}"/>
<#elseif (parameters.groupName?has_content)>
    <#assign createUrl = "editpartygroup?create_new=Y&amp;groupName=${parameters.groupName?if_exists}"/>
<#else>
    <#assign createUrl = "loanapplication"/>
</#if>
<div class="button-bar"><a href="<@ofbizUrl>${createUrl}</@ofbizUrl>" class="buttontext create">Create New Application</a></div>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <#if applicationsList?has_content>
      <#-- ul>
        <#if hideFields == "Y">
          <li class="collapsed"><a href="<@ofbizUrl>loanapplicationslist?hideFields=N${paramList}</@ofbizUrl>" title="${uiLabelMap.CommonShowLookupFields}">&nbsp;</a></li>
        <#else>
          <li class="expanded"><a href="<@ofbizUrl>loanapplicationslist?hideFields=Y${paramList}</@ofbizUrl>" title="${uiLabelMap.CommonHideFields}">&nbsp;</a></li>
        </#if>
        <#if (partyListSize > 0)>
          <#if (partyListSize > highIndex)>
            <li><a class="nav-next" href="<@ofbizUrl>loanapplicationslist?VIEW_SIZE=${viewSize}&amp;VIEW_INDEX=${viewIndex+1}&amp;hideFields=${hideFields}${paramList}</@ofbizUrl>">${uiLabelMap.CommonNext}</a></li>
          <#else>
            <li class="disabled">${uiLabelMap.CommonNext}</li>
          </#if>
          <li>${lowIndex} - ${highIndex} ${uiLabelMap.CommonOf} ${partyListSize}</li>
          <#if (viewIndex > 0)>
            <li><a class="nav-previous" href="<@ofbizUrl>loanapplicationslist?VIEW_SIZE=${viewSize}&amp;VIEW_INDEX=${viewIndex-1}&amp;hideFields=${hideFields}${paramList}</@ofbizUrl>">${uiLabelMap.CommonPrevious}</a></li>
          <#else>
            <li class="disabled">${uiLabelMap.CommonPrevious}</li>
          </#if>
        </#if>
      </ul -->
      <br class="clear"/>
    </#if>
  </div>
  <div class="screenlet-body">
    <div id="findPartyParameters" <#if hideFields != "N"> style="display:none" </#if> >
      <!-- h2>${uiLabelMap.CommonSearchOptions}</h2 -->
      <#-- NOTE: this form is setup to allow a search by partial partyId or userLoginId; to change it to go directly to
          the viewprofile page when these are entered add the follow attribute to the form element:

           onsubmit="javascript:lookupParty('<@ofbizUrl>viewprofile</@ofbizUrl>');"
       -->
     
    </div>
    <script language="JavaScript" type="text/javascript">
      document.lookupparty.partyId.focus();
    </script>

  <#if applicationsList?exists>
    <#if hideFields != "Y">
      <hr />
    </#if>
    <div id="findPartyResults">
      <h2>${uiLabelMap.CommonSearchResults}</h2>
    </div>
    <#if applicationsList?has_content>
      <table class="basic-table hover-bar" cellspacing="0">
        <tr class="header-row-2">
          <td>Loan No</td>
          <td>Loan Type</td>
          <td>Member No</td>
          <td>First Name</td>
          <td>Last Name</td>
          <td>Mobile No</td>
          <td>Loan Amount</td>
          <td>Loan Status</td>
          <td>Repayment Period</td>
          <td>Interest Rate (P.M)</td>
          <td>&nbsp;</td>
        </tr>
        <#assign alt_row = false>
        <#assign rowCount = 0>
        <#list applicationsList as applicationRow>
          <#assign loanType = applicationRow.getRelatedOne("LoanProduct")?if_exists>
          <#assign loanStatus = applicationRow.getRelatedOne("LoanStatus")?if_exists>
          <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
          	<td><a href="<@ofbizUrl>viewapplicationprofile?loanApplicationId=${applicationRow.loanApplicationId}</@ofbizUrl>">${applicationRow.loanNo?if_exists}</a></td>
            <td><a href="<@ofbizUrl>viewapplicationprofile?loanApplicationId=${applicationRow.loanApplicationId}</@ofbizUrl>">${loanType.name} ${loanType.code}</a></td>
           
            <td>${applicationRow.memberNumber?if_exists}</td>
            <td>${applicationRow.firstName?if_exists}</td>
            <td>${applicationRow.lastName?if_exists}</td>
          	<td>${applicationRow.mobileNumber?if_exists}</td>
          	<td>${applicationRow.loanAmt?if_exists}</td>
          	<td>${applicationRow.applicationStatus?if_exists}</td>
          	<td>${applicationRow.repaymentPeriod?if_exists}</td>
          	<td>${applicationRow.interestRatePM?if_exists}</td>
          	
            <td class="button-col align-float">
            	<a href="<@ofbizUrl>loanapplication?loanApplicationId=${applicationRow.loanApplicationId}</@ofbizUrl>">Edit </a> 
            	<a href="<@ofbizUrl>loanapplication?loanApplicationId=${applicationRow.loanApplicationId}</@ofbizUrl>">Delete </a> 
              <a href="<@ofbizUrl>viewapplicationprofile?loanApplicationId=${applicationRow.loanApplicationId}</@ofbizUrl>">${uiLabelMap.CommonDetails}</a>
              <#-- #if security.hasRolePermission("ORDERMGR", "_VIEW", "", "", session)>
                  <form name= "searchorders_o_${rowCount}" method= "post" action= "/ordermgr/control/searchorders">
                    <input type= "hidden" name= "lookupFlag" value= "Y" />
                    <input type= "hidden" name= "hideFields" value= "Y" />
                    <input type= "hidden" name= "partyId" value= "${partyRow.partyId}" />
                    <input type= "hidden" name= "viewIndex" value= "1" />
                    <input type= "hidden" name= "viewSize" value= "20" />
                    <a href="javascript:document.searchorders_o_${rowCount}.submit()">${uiLabelMap.OrderOrders}</a>
                </form>
                <a href="/ordermgr/control/FindQuote?partyId=${partyRow.partyId + externalKeyParam}">${uiLabelMap.OrderOrderQuotes}</a>
              </#if>
              <#if security.hasEntityPermission("ORDERMGR", "_CREATE", session)>
                <a href="/ordermgr/control/checkinits?partyId=${partyRow.partyId + externalKeyParam}">${uiLabelMap.OrderNewOrder}</a>
                <a href="/ordermgr/control/EditQuote?partyId=${partyRow.partyId + externalKeyParam}">${uiLabelMap.OrderNewQuote}</a>
              </#if -->
            </td>
          </tr>
          <#assign rowCount = rowCount + 1>
          <#-- toggle the row color -->
          <#assign alt_row = !alt_row>
        </#list>
      </table>
    <#else>
      <div id="findPartyResults_2">
        <h3>No Loan Applications Found </h3>
      </div>
    </#if>
    <#if lookupErrorMessage?exists>
      <h3>${lookupErrorMessage}</h3>
    </#if>
  </div>
</#if>
</div>