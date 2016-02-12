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
<#if separationDetail?has_content>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
        
        <fo:block font-size="10pt" text-align="center"  font-weight="bold">
            EMPLOYEE TERMINATION REPORT
        </fo:block>
        
         <fo:block font-size="10pt" text-align="center" border-bottom="thin solid black"></fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="center"  font-weight="bold">
                        EMPLOYEE DETAILS
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         
         <fo:block font-size="9pt" text-align="left">
           Payroll Number :  ${payroll?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
          Employee Name :  ${firstName?if_exists}  ${lastName?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
               ID Number :  ${idNumber?if_exists}  
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                Appointment Date:  ${dateOfAppointment?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                Branch:  ${branchName?if_exists} 
        </fo:block>
        
          <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Given Notice Period:  ${noticePeriod?if_exists} 
        </fo:block>
         
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="center"  font-weight="bold">
                          EARNING
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                Basic Salary:  ${salary?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                 Leave Allowance :  ${leaveAllowance?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                  Golden Handshake:  ${goldenHandShake?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                  Transport Allowance :  ${transportAllowance?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Service Pay:  ${servicePay?if_exists} 
        </fo:block>
        
        <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Lieu Of Notice Against Administration :  ${lienOfNoticeAdminToEmpl?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="left"  font-weight="bold">
              Total Earning:  ${total?if_exists}
        </fo:block>
        
        
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="center"  font-weight="bold">
                         DEDUCTIONS
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   PAYEE:  ${PAYE?if_exists} 
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Lost/Uncleared Dues:  ${lostItemAmount?if_exists} 
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Given Notice Period:  ${noticePeriod?if_exists} 
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="left">
                   Lieu Of Notice Against Employee:  ${lienOfNotice?if_exists} 
        </fo:block>
        
          <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="left"  font-weight="bold">
              Total Deductions:  ${totalDeductions?if_exists}
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="11pt" text-align="left"  font-weight="bold">
                  Total OutStanding Loans:  ${staffLoans?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="12pt" text-align="left"  font-weight="bold">
              Balance(Payable To Chai):  ${amountPayableToChai?if_exists}
        </fo:block>
        
        
        
        
</#if>
</#escape>