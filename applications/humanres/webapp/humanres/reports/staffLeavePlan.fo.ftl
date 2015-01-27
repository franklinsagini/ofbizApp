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
    <#if employeeList?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        STAFF LEAVE PLAN
    </fo:block>
    <fo:block><fo:leader/></fo:block>
   
<#if employeeList?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="10pt"/>
			<fo:table-column column-width="60pt"/>
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="100pt"/>
			<fo:table-column column-width="50pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
				<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left"></fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Staff Payroll number</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Staff Names</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Gender</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Branch</fo:block>
                    </fo:table-cell>
                    
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Department</fo:block>
                    </fo:table-cell>
                    
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Leave Type</fo:block>
                    </fo:table-cell>
					<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Leave Duration</fo:block>
                    </fo:table-cell>
					<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Leave Start Date</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>

                  <#list employeeList as activity>
				  <#assign count=0>
				  
                    <#if activity.partyId?has_content>
                        <#assign staff = delegator.findOne("Person", {"partyId" : activity.partyId}, false)/>
                    </#if>
                     <#if activity.leaveTypeId?has_content>
                        <#assign leaveType = delegator.findOne("EmplLeaveType", {"leaveTypeId" : activity.leaveTypeId}, false)/>
                    </#if>
					 <#if activity.partyId?has_content>
                        <#assign branch = delegator.findOne("PartyGroup", {"partyId" : staff.branchId}, false)/>
                    </#if>
                     <#if activity.leaveTypeId?has_content>
                        <#assign department = delegator.findOne("department", {"departmentId" : staff.departmentId}, false)/>
                    </#if>
                     <fo:table-row>
					 <#assign count = count + 1>
					 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if staff?has_content>
                                <fo:block>${staff.employeeNumber?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if staff?has_content>
                                <fo:block>${staff.firstName?if_exists} ${staff.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if staff?has_content>
                                <fo:block>${staff.gender?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if branch?has_content>
                                <fo:block>${branch.groupName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if department?has_content>
                                <fo:block>${department.departmentName?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
						
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if leaveType?has_content>
                                <fo:block>${leaveType.description?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                        
                          <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.leaveDuration?if_exists}</fo:block>
                        </fo:table-cell>
                        
                          <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.fromDate?if_exists}</fo:block>
                        </fo:table-cell>
                       
                     </fo:table-row>
					
                  </#list>

            </fo:table-body>
        </fo:table>
    </fo:block>
    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To Show</fo:block>
    </fo:block>
  </#if>
    <#else>
        <fo:block text-align="center">No Employees Found With that ID</fo:block>
    </#if>
</#escape>

