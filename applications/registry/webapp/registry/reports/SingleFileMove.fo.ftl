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
    <#if singleActivityfileMovement?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        
         <#if singleActivityfileMovement?has_content>
            <#assign reason = delegator.findOne("RegistryFileActivity", {"activityId" : activityCode}, false)/>
FILE MOVEMENT FOR  [${reason.activity}]  REPORT
        </#if>
        
       
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        Member Name: ${member.firstName} ${member.lastName}
    </fo:block>
<#if singleActivityfileMovementlist?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
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
                        <fo:block text-align="left">Received By</fo:block>
                    </fo:table-cell>
                    
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Time Received</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>

                  <#list singleActivityfileMovementlist as activity>
                   
                     <fo:table-row>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.releasedBy?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.releasedTo?if_exists}</fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.carriedBy?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.timeReleased?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.receivedBy?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.timeReceived?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
                  </#list>

            </fo:table-body>
        </fo:table>
    </fo:block>
    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To Show For: ${member.firstName} ${member.lastName}</fo:block>
    </fo:block>
  </#if>
    <#else>
        <fo:block text-align="center">Nothing Found</fo:block>
    </#if>
</#escape>

