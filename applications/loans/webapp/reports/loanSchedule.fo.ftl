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
<#escape x as x?xml>
 <#if loanApplication?has_content>
    <#if loanApplication?has_content>
    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
        RE: LOAN REPAYMENT AND VOLUNTARY CONTRIBUTION TO SOCIETY
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline">
      
       Loan Type : ${loanProduct.name?if_exists}
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
       I ${salutation.name} ${member.lastName} ${member.middleName} ${member.firstName} Payroll No ${member.payrollNumber} Member No ${member.memberNumber}
       hereby authorise you to deduct from my salary Kshs.  ${loanApplication.loanAmt?string(",##0.00")}
       <b/>
       to be credited to Chai Co-Operative Savings and credit society.
       P.R.D being my Monthly subscription as follows: -  
    </fo:block>


    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" margin-left="35%" margin-bottom="0.2in">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Minimum Share Contribution</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>Kshs ${minimumShareContribution?string(",##0.00")}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan Repayment</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>Kshs ${loanRepayment?string(",##0.00")}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold"> - </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> - </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Total Deduction</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>Kshs ${totalDeduction?string(",##0.00")}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
    </fo:list-block>
    
     <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      For a period of ${loanApplication.repaymentPeriod} Months with effect from ${startDate?date} until ${lastDate?date} 
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      When you will stop deductions toward loan repayment until further notice 
      I hereby undertaking that these instructions will only terminate with the Knowledge 
      and writtern approval of the Treasurer.
    </fo:block>
    
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      Kindly note that deposit contribution will change from ${currentContributionAmt?string(",##0.00")} to ${minimumShareContribution?string(",##0.00")} in accordance to your current loans totals.Your can vary your 
      deposit contribution downwards only after completing payment of any or all of running loans
    </fo:block>
    
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      SIGNATURE ------------------------------------------------------------------------------------
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      DESIGNATION ------------------------------------------------------------------------------------
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      PLACE OF WORK ------------------------------------------------------------------------------------
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      C.C -----------------------------------------------DATE ------------------------------------------
    </fo:block>
    </#if>
	 </#if>
</#escape>