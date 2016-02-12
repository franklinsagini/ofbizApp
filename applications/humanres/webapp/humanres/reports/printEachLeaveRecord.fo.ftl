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
<#if leaveDetail?has_content>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
        
        <fo:block font-size="10pt" text-align="center"  font-weight="bold">
            Leave Report
        </fo:block>
        
         <fo:block font-size="10pt" text-align="center" border-bottom="thin solid black"></fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         
         <fo:block font-size="9pt" text-align="center">
           Payroll Number :  ${payroll?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
          Employee Name(Applicant) :  ${firstName?if_exists}  ${lastName?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
               ID Number :  ${idNumber?if_exists}  
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                Appointment Date:  ${dateOfAppointment?if_exists} 
        </fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
              Leave Type:  ${leaveTypeDetail.description?if_exists} 
        </fo:block>
        
         
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                Reason For Leave:  ${leaveReason.reason?if_exists} 
        </fo:block>
        
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                Leave Duration:  ${leaveDuration?if_exists}  day(s)
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                 Leave Status :  ${leaveStatus?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                From Date:  ${fromDate?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                  Through Date :  ${thruDate?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                   Created Date:  ${createdDate?if_exists} 
        </fo:block>
        
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
            Handed Over To :   ${handOverPersonfirstName?if_exists}  ${handOverPersonlastName?if_exists} 
        </fo:block>

        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                   Reason For Rejection(if any):  ${rejectReason?if_exists} 
        </fo:block>
        
         <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
                 <fo:block font-size="6pt" text-align="left"><fo:leader/></fo:block>
         <fo:block font-size="9pt" text-align="center">
                  Approver:  ${approverPersonDetaifirstName?if_exists}  ${approverPersonDetailastName?if_exists} 
        </fo:block>
        
        
        
        
        
</#if>
</#escape>