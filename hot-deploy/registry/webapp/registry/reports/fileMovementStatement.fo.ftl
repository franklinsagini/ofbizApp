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
    <#if employee?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        FILE MOVEMENT REPORT
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        File Owner: ${employee.firstName} ${employee.lastName}
    </fo:block>
<#if file?has_content>
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        Current Status: ${file.status?if_exists}
    </fo:block>
</#if>


<#if activities?has_content>


    <#-- REPORT BODY -->
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
	
    </fo:block>
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="90pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Released By</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Released To</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Carried By</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Time Released</fo:block>
                    </fo:table-cell>
                   <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Activity</fo:block>
                    </fo:table-cell>
                    
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Received By</fo:block>
                    </fo:table-cell>
                    
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Time Received</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
            
            

                  <#list activities as activity>

                    <#if activity.releasedBy?has_content>
                        <#assign releasedBy = delegator.findOne("Person", {"partyId" : activity.releasedBy}, false)/>
                    </#if>
                    <#if activity.carriedBy?has_content>
                        <#assign carriedBy = delegator.findOne("Person", {"partyId" : activity.carriedBy}, false)/>
                    </#if>
                    <#if activity.releasedTo?has_content>
                        <#assign releasedTo = delegator.findOne("Person", {"partyId" : activity.releasedTo}, false)/>
                    </#if>
                     <#if activity.receivedBy?has_content>
                        <#assign receivedBy = delegator.findOne("Person", {"partyId" : activity.receivedBy}, false)/>
                    </#if>
                     <#if activity.activityCode?has_content>
                        <#assign reason = delegator.findOne("RegistryFileActivity", {"activityId" : activity.activityCode}, false)/>
                       </#if>

                     


                     <fo:table-row>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if releasedBy?has_content>
                                <fo:block>${releasedBy.firstName?if_exists} ${releasedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${activity.releasedBy}</fo:block>
                            </#if>
                        </fo:table-cell>
						
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if activity.releasedTo?has_content>
                                <fo:block>${releasedTo.firstName?if_exists} ${releasedTo.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${activity.releasedTo?if_exists}</fo:block>
                            </#if>
                        </fo:table-cell>
						
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if carriedBy?has_content>
                                <fo:block>${carriedBy.firstName?if_exists} ${carriedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${activity.carriedBy}</fo:block>
                            </#if>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.timeOut?if_exists}</fo:block>
                        </fo:table-cell>
                        
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if reason?has_content>
                                <fo:block>${reason.activity?if_exists}</fo:block>
                            <#else>
                                <fo:block>${activity.activityCode}</fo:block>
                            </#if>
                        </fo:table-cell>
                        
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if receivedBy?has_content>
                                <fo:block>${receivedBy.firstName?if_exists} ${receivedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${activity.receivedBy}</fo:block>
                            </#if>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.timeIn?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
           </#list>
                   
            </fo:table-body>
        </fo:table>
    </fo:block>
 
   
  <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To ShowFor: ${employee.firstName} ${employee.lastName}</fo:block>
    </fo:block>
  </#if>
  
    <#else>
        <fo:block text-align="center">No Employees Found With that ID</fo:block>
    </#if>
	
</#escape>

