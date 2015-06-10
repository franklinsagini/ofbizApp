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
    <#if listMembers?has_content>
        <#-- REPORT TITLE -->
        <!-- fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block -->
        <fo:block font-size="12pt" text-align="center"  font-weight="bold">
        </fo:block>
        
        <#--fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block> </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            < fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item >
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Station Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                    	       
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item >
            
        </fo:list-block -->
        <fo:block><fo:leader/></fo:block>

		<#function zebra index>
		  <#if (index % 2) == 0>
		    <#return "white" />
		  <#else>
		    <#return "#C0D9AF" />
		  </#if>
		</#function>

		 <#list listMembers as member>
		 <#-- assign totalAmount = statement.itemTotal -->
		 
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">First Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Last Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            
           <#-- fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">
                    <#if statement.loanNo??>
                    ${statement.loanNo}
                    </#if>
                    
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item -->
            <#-- fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.payrollNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item -->
            
        </fo:list-block>
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="30pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="55"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>

                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Count</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>First Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Middle Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Last Name </fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Percentage</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Relationship</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                
                    <#list member.listNominee as nominee>
                    
       	                
                        <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm"  text-align="left">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm"  text-align="left">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" >
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" >
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left">
                                <fo:block>
								     								
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block>
								
                               </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="30pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="55pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>                
                <fo:table-column column-width="50pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Total Total</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                
                <fo:table-row>
                	    <fo:table-cell padding="2pt" background-color="white" border="0pt solid" border-width="0" text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
        </#list>
    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>