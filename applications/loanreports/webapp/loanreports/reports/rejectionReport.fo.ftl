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
 <#if rejectedLoan?has_content>
     
    <#if loanStatusRejection?has_content>
      <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
      <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
      <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
      
       <#--fo:block font-size="12pt" font-weight="bold" text-align="left">
                     CHAI SAVINGS AND CREDIT CO-OP SOCIETY LTD.
        </fo:block>
         <fo:block font-size="12pt" font-weight="bold" text-align="left">
              <fo:leader/>    
        </fo:block>
        <fo:block font-size="10pt" font-weight="bold" text-align="left">
                      P.O BOX: 278-00200 NAIROBI  Tel: 020-214406/10
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left">
              <fo:leader/>        
        </fo:block -->
        
         <fo:block font-size="7pt" text-align="left">
               DATE  :   ${today}
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
               <fo:leader/>       
        </fo:block>
         <fo:block font-size="7pt" text-align="left">
               MEMBER NO   :   ${rejectedLoan.memberNumber}  
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>        
        </fo:block>
         <fo:block font-size="7pt" text-align="left">
              NAME   :   ${rejectedLoan.firstName}  ${rejectedLoan.middleName} ${rejectedLoan.lastName} 
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="7pt"  text-align="left">
              STATION  : ${getStationOfMember.name}     
        </fo:block>
        
        <fo:block font-size="9pt" text-align="left">
               <fo:leader/>       
        </fo:block>
        
        <fo:block font-size="9pt" text-align="left">
               <fo:leader/>        
        </fo:block>
        
         <fo:block font-size="7pt" font-weight="bold" text-align="left">
             Dear  ${rejectedLoan.firstName}  ${rejectedLoan.lastName}  ,  
        </fo:block>
        
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
        
        
          <fo:block font-size="10pt" font-weight="bold"  text-align="left">
              RE : LOAN REJECTION
        </fo:block>
            <fo:block border="thin solid black"> </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
        


         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
        
         <fo:block font-size="9pt" text-align="left">
            We acknowledge receipt of your ${loanType} Application for Kshs ${rejectedLoan.appliedAmt} on ${todayy}
            Payable in ${rejectedLoan.repaymentPeriod} month(s) Installment.
            <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                <fo:block font-size="9pt" text-align="left">
                    We hereby inform you that the loan was rejected ${loanStatusRejection}.
                </fo:block>
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
        
        <fo:block font-size="9pt" text-align="left">
                     We thank you for your support and co-operation.
         </fo:block>
        
        <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         
        <fo:block font-size="9pt" text-align="left">
                    Yours Faithfully,
         </fo:block>
         
          <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         
          <fo:block font-size="10pt" text-align="left">
                    Chai Sacco Ltd
         </fo:block>
         
        <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>
         
        <fo:block font-size="9pt" text-align="left">
                   BRANCH MANAGER
         </fo:block>
        
   
        </#if>
	 </#if>
</#escape>