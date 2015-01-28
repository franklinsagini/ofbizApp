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
    <#if staff?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        STAFF LEAVE LOGS
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        Employee Name: ${staff.firstName} ${staff.lastName}
    </fo:block>
	<fo:block font-size="10pt" text-align="left" font-weight="bold">
        Employee Payroll Number: ${staff.employeeNumber}
    </fo:block>
<#if logs?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
			<fo:table-column column-width="100pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="90pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Leave Type</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Approved By</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Next Approver</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Rejected By</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Rejection Reason</fo:block>
                    </fo:table-cell>
					<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Approval Status</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Date</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>

                  <#list logs as activity>
                    <#if activity.leaveId?has_content>
                        <#assign leave = delegator.findOne("EmplLeave", {"leaveId" : activity.leaveId}, false)/>
                    </#if>
                    <#if leave.leaveTypeId?has_content>
                        <#assign leaveTyp = delegator.findOne("EmplLeaveType", {"leaveTypeId" : leave.leaveTypeId}, false)/>
                    </#if>
                     <#if activity.approvedBy?has_content>
                        <#assign apprvdBy = delegator.findOne("Person", {"partyId" : activity.approvedBy}, false)/>
                    </#if>
					<#if activity.nextApprover?has_content>
                        <#assign nextApprvd = delegator.findOne("Person", {"partyId" : activity.nextApprover}, false)/>
                    </#if>
					<#if activity.rejectedBy?has_content>
                        <#assign rejectBy = delegator.findOne("Person", {"partyId" : activity.rejectedBy}, false)/>
                    </#if>
                     <fo:table-row>						
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if leaveTyp?has_content>
                                <fo:block>${leaveTyp.description}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if apprvdBy?has_content>
                                <fo:block>${apprvdBy.firstName?if_exists} ${apprvdBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if nextApprvd?has_content>
                                <fo:block>${nextApprvd.firstName?if_exists} ${nextApprvd.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if rejectBy?has_content>
                                <fo:block>${rejectBy.firstName?if_exists} ${rejectBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.rejectReason?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.approvalStatus?if_exists}</fo:block>
                        </fo:table-cell>
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.createdStamp?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
                  </#list>

            </fo:table-body>
        </fo:table>
    </fo:block>
    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To Show For: ${staff.firstName} ${staff.lastName}</fo:block>
    </fo:block>
  </#if>
    <#else>
        <fo:block text-align="center">No Employees Found With that ID</fo:block>
    </#if>
</#escape>

