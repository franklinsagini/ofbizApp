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
    <#if msacco?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
         MSACCO APPLICATION REPORT
    </fo:block>
    <fo:block><fo:leader/></fo:block>

<#if msacco?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
        	<fo:table-column column-width="60pt"/>
            <fo:table-column column-width="120pt"/>
            <fo:table-column column-width="60pt"/>
            <fo:table-column column-width="120pt"/>
            <fo:table-column column-width="120pt"/>
            <fo:table-column column-width="60pt"/>
            <fo:table-header>
               <fo:table-row font-weight="bold">
               		<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Member</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Id Number</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Phone Number</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Account Number</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Status</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
					<#assign count = 0>
                   <#list msacco as dep>
                   	<#assign count = count + 1>
                    <#if dep.memberAccountId?has_content>
                    <#assign memberPartyId = dep.memberAccountId?number /> 
                        <#assign accNo = delegator.findOne("MemberAccount", {"memberAccountId" : memberPartyId?long}, false)/>
                    </#if>
                    <#if dep.partyId?has_content>
                    <#assign memberPartyId = dep.partyId?number /> 
                        <#assign member = delegator.findOne("Member", {"partyId" : memberPartyId?long}, false)/>
                    </#if>
                    
                    <#if dep.cardStatusId?has_content>
                        <#assign cardStatus = delegator.findOne("CardStatus", {"cardStatusId" : dep.cardStatusId}, false)/>
                    </#if>
                     <fo:table-row>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${member.firstName?if_exists} ${member.lastName?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${member.idNumber?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${dep.mobilePhoneNumber?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${accNo.accountNo?if_exists}</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block  text-align="left">${cardStatus.name?if_exists}</fo:block>
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
        <fo:block text-align="center">Nothing</fo:block>
    </#if>
</#escape>

