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
    <#if member?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        FILE MOVEMENT REPORT FOR [${member.firstName} ${member.lastName}'s] FILE
    </fo:block>
    <fo:block><fo:leader/></fo:block>
   


<#if movementActivityList?has_content>


    <#-- REPORT BODY -->
	
	<#list movementActivityList as activity>
	
	 <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Activity : ${activity.activityCode}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Max Activity Duration (In Days) : ${activity.activityDuration}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
        </fo:list-block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
	
    </fo:block>
	
	
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="150pt"/>
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
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Time With File</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
			
            <fo:table-body>
             
            
					<#list activity.listMovements as movement>
                  

                    <#if movement.releasedBy?has_content>
                        <#assign releasedBy = delegator.findOne("Person", {"partyId" : movement.releasedBy}, false)/>
                        
                    </#if>
					
                    <#if movement.carriedBy?has_content>
                        <#assign carriedBy = delegator.findOne("Person", {"partyId" : movement.carriedBy}, false)/>
                    </#if>
                    <#if movement.releasedTo?has_content>
                        <#-- <#assign releasedTo = delegator.findOne("Person", {"partyId" : movement.releasedTo}, false)/>		 -->
                        <#assign releasedTo = delegator.findOne("Person", {"partyId" : movement.releasedTo}, false)/>
                   </#if>
                   <#if movement.releasedTo?if_exists == "REGISTRY">
                        <#assign registry = "REGISTRY"/>
                   </#if>
                     <#if movement.receivedBy?has_content>
                        <#assign receivedBy = delegator.findOne("Person", {"partyId" : movement.receivedBy}, false)/>
                        
                        
                        
                       
                    </#if>
                     <#if movement.activityCode?has_content>
                        <#assign reason = delegator.findOne("RegistryFileActivity", {"activityId" : movement.activityCode}, false)/>
                        
                      
                     	
                      </#if>

                     


                     <fo:table-row>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if releasedBy?has_content>
                                <fo:block>${releasedBy.firstName?if_exists} ${releasedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${movement.releasedBy?if_exists}</fo:block>
                            </#if>
                        </fo:table-cell>
						
                     <#--     <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if releasedTo?has_content>
                                <fo:block>${releasedTo.firstName?if_exists} ${releasedTo.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>REGISTRY</fo:block>
                            </#if>
                        </fo:table-cell> -->
						 <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if releasedTo?has_content>
                             
                               <fo:block>${releasedTo.firstName} ${releasedTo.lastName}</fo:block>
                            <#else> 
                            <fo:block>${registry}</fo:block>
                            </#if>
                        </fo:table-cell> 
						
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if carriedBy?has_content>
                                <fo:block>${carriedBy.firstName?if_exists} ${carriedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${movement.carriedBy?if_exists}</fo:block>
                            </#if>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${movement.timeOut?if_exists}</fo:block>
                        </fo:table-cell>
                        
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if receivedBy?has_content>
                                <fo:block>${receivedBy.firstName?if_exists} ${receivedBy.lastName?if_exists}</fo:block>
                            <#else>
                                <fo:block>${movement.receivedBy?if_exists}</fo:block>
                            </#if>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${movement.timeIn?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                             <fo:block>${movement.timeStayedWithFile?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
           
                   </#list>
            </fo:table-body>
        </fo:table>
    </fo:block>
	 
   </#list>
  <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To ShowFor: ${member.firstName} ${member.lastName}</fo:block>
    </fo:block>
  </#if>
  
    <#else>
        <fo:block text-align="center">No Member Found With that ID</fo:block>
    </#if>
	
</#escape>

