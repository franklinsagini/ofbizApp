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
    <#if statement?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        STATEMENT OF FINANCIAL POSITION
    </fo:block>
	
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        FINANCIAL YEAR: 
    </fo:block>
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
       START DATE: ${fromDate}
    </fo:block>
	<fo:block font-size="10pt" text-align="left" font-weight="bold">
       END DATE: ${thruDate}
    </fo:block>
    <fo:block><fo:leader/></fo:block>
   


<#if assetsList?has_content>


    <#-- REPORT BODY -->
	
	
	
	 <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">ASSETS</fo:block>
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
           <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="300pt"/>
            <fo:table-column column-width="150pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Code</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Name</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Balance</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
			
			<#list assetsList as activity>
            <fo:table-body>
             
                     <fo:table-row>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.code?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.name?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.balance?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
           
            </fo:table-body>
			</#list>
        </fo:table>
    </fo:block>
	 
   
  <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To ShowFor: ${member.firstName} ${member.lastName}</fo:block>
    </fo:block>
  </#if>
  
  <#if liabilityList?has_content>


    <#-- REPORT BODY -->
	
	
	
	 <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">LIABILITIES</fo:block>
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
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="300pt"/>
            <fo:table-column column-width="150pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Code</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Name</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Balance</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
			
			<#list liabilityList as activity>
            <fo:table-body>
             
                     <fo:table-row>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.code?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.name?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.balance?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
           
            </fo:table-body>
			 </#list>
        </fo:table>
    </fo:block>
	 
  
  <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To ShowFor: ${member.firstName} ${member.lastName}</fo:block>
    </fo:block>
  </#if>
  
   <#if equityList?has_content>


    <#-- REPORT BODY -->
	
	
	
	 <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">EQUITY</fo:block>
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
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="300pt"/>
            <fo:table-column column-width="150pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Code</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Name</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Balance</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
			
			
			<#list equityList as activity>
            <fo:table-body>
             
                     <fo:table-row>
                       <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.code?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.name?if_exists}</fo:block>
                        </fo:table-cell>
						
						<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${activity.balance?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
           
            </fo:table-body>
			</#list>
        </fo:table>
    </fo:block>
	 
   
  <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">Nothing To ShowFor: ${member.firstName} ${member.lastName}</fo:block>
    </fo:block>
  </#if>
  
    <#else>
        <fo:block text-align="center">No Member Found With that ID</fo:block>
    </#if>
	
</#escape>

