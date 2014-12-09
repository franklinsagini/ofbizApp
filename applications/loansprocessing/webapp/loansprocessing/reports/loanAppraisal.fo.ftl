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
        CHAI SACCO SAVINGS AND CREDIT SOCIETY LTD.
    </fo:block>
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
        LOAN APPRAISAL REPORT.
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="12pt" font-weight="bold" space-after="0.04in" text-decoration="underline" text-align="center">
      
       MEMBER NO : ${member.memberNumber} MEMBER NAME : ${salutation.name} ${member.lastName} ${member.middleName} ${member.firstName}
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      
    </fo:block>

   <fo:block font-size="12pt" font-weight="bold" space-after="0.04in" margin-left="10%" text-decoration="underline" text-align="left">
      
       APPRAISAL
    </fo:block>

    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" margin-left="15%" margin-bottom="0.2in">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan No</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${loanApplication.loanNo} </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan Type</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${loanProduct.name}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold"> Terms of Service </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${employmentType.name} </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan Period</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${loanApplication.repaymentPeriod} </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
         <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Share(Savings) Value</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Last Share Contribution Amount</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Last Share Payment Date</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Applied Amount</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${loanApplication.loanAmt}  </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Deduction Method</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${loanProduct.deductionType?if_exists} </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Outstanding Loan Balance</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan Entitlement</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${loanApplication.maxLoanAmt}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
    </fo:list-block>
    
     <fo:block font-size="12pt" font-weight="bold" space-after="0.04in" margin-left="10%" text-decoration="underline" text-align="left">
      
       2/3 RULE
    </fo:block>
    	    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" margin-left="15%" margin-bottom="0.2in">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Gross Salary</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${deductionEvaluation.grossSalaryAmt} </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Payslip Deduction</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.payslipDeductionAmt}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Recovery Method Used</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${loanProduct.deductionType?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan Repayment</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.monthlyRepaymentAmt?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Insurance Amount</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.firstMonthInsuranceAmt?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Total Repayment</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.totalDeductionAmt?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Total Deduction</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.totalDeductionAfterLoanAddedAmt?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Violation Verdict</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${deductionEvaluation.violationDecision?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
 
        </fo:list-block>
    
     <fo:block font-size="12pt" font-weight="bold" space-after="0.04in" margin-left="10%" text-decoration="underline" text-align="left">
      
       APPRAISAL 2
    </fo:block>
    
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
    
    
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
     I Certify that the application is within the rules of the society.
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
    COMMENT -------------------------------------------------------------------------------------------
 
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
       SIGNATURE ---------------------------DESIGNATION-----------------------DATE----------------
    </fo:block>
    </fo:block>

	 <fo:block font-size="12pt" font-weight="bold" space-after="0.04in" margin-left="10%" text-decoration="underline" text-align="left">
      
       CREDIT MANAGER COMMENT
    </fo:block> 
    
    
     <fo:block font-size="12pt" text-align="left" margin-bottom="0.2in">
    
    
    <fo:block font-size="12pt"  margin-bottom="0.2in">
     This Loan application should be Accepted/Rejected for the amount of .
    </fo:block>
    <fo:block font-size="12pt" margin-bottom="0.2in">
    KShs --------------------Payable in -------------------------Monthly installments of ----------------KShs-----------------------------
    </fo:block>
    
    <fo:block font-size="12pt"  margin-bottom="0.2in">
    The loan application is rejected or amount requested reduced, repayment changed due to the following reasons.
    </fo:block>
    <fo:block font-size="12pt"  margin-bottom="0.2in">
       ---------------------------------------------------------------------------------------------
    </fo:block>
        <fo:block font-size="12pt"  margin-bottom="0.2in">
       ---------------------------------------------------------------------------------------------
    </fo:block>
    
    <fo:block font-size="12pt" margin-bottom="0.2in">
       SIGNATURE ----------------------------------------------DATE----------------
    </fo:block>
    </fo:block>
    
    
    
     <fo:block font-size="12pt" font-weight="bold" space-after="0.04in"  margin-left="10%" text-decoration="underline" text-align="left">
      
       CREDIT COMMITTEE COMMENT
    </fo:block>   
     
     <fo:block font-size="12pt" text-align="left" margin-bottom="0.2in">
    
    
    <fo:block font-size="12pt"  margin-bottom="0.2in">
     We have today examined the above application in conjunction with the above remarks and have decided as follows:-
    </fo:block>
    <fo:block font-size="12pt" margin-bottom="0.2in">
    Loan Approved KShs --------------------Recoverable in -------------------------Months ----------------KShs-----------------------------
    </fo:block>

    <fo:block font-size="12pt" margin-bottom="0.2in">
       ---------------------------------------------------------------------------------------------
    </fo:block>
        <fo:block font-size="12pt"  margin-bottom="0.2in">
       ---------------------------------------------------------------------------------------------
    </fo:block>
    
    <fo:block font-size="12pt"  margin-bottom="0.2in">
       SIGNED BY -------------------------------------------------------------------
    </fo:block>
    
    <fo:block font-size="12pt"  margin-bottom="0.2in">
       (CHAIRMAN) ---------------------------(SECRETARY)-----------------------------(MEMBER)---------------
    </fo:block>
    </fo:block>
    
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
     </fo:block>
    

    </#if>
	 </#if>
</#escape>