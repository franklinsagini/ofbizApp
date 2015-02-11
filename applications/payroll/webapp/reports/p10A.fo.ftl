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
    <#if p10AItemsList?has_content>
        <#-- REPORT TITLE -->
        <fo:list-block provisional-distance-between-starts="2in">
        	<fo:list-item>
                <fo:list-item-label>
                <fo:block  font-weight="bold" font-size="10pt">P.10A</fo:block>
                    <fo:block font-size="10pt" margin-top="8mm" margin-left="-15mm" font-weight="bold" text-align="center">
			         P.A.Y.E. SUPPORTING LIST FOR END OF YEAR CERTIFICATE:
			         <fo:block  font-weight="bold" font-size="10pt" text-align="center">YEAR ${year.name?if_exists}</fo:block>
			        </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt" margin-left="-100mm" text-align="center">EMPLOYER NAME </fo:block>                    
                    <fo:block font-weight="bold" font-size="10pt" margin-left="130mm">EMPLOYER PIN </fo:block>         
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt" margin-left="-100mm" text-align="center">${empDet.employer?if_exists}</fo:block>
                    <fo:block font-weight="bold" font-size="10pt" margin-left="130mm">${empDet.pinNumber?if_exists}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
         </fo:list-block>
        
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="120pt"/>
                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold" font-size="12pt">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>EMPLOYEE'S PIN</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block> EMPLOYEE'S NAME</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>TOTAL EMOLUMENTS (KSH.)</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>PAYE DEDUCTED (KSH.)</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                		<#-- fo:table-row>
                            
                            <fo:table-cell padding="2pt" border-width=".1mm" font-size="16pt" font-weight="bold">
                                <fo:block>Earnings</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border-width=".1mm">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border-width=".1mm">
                                <fo:block>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row -->
                    <#list p10AItemsList as yearList>
                    
                         <fo:table-row font-size="10pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                 <fo:block>
                                 	${yearList.pinNumber?if_exists}
                                 </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	${yearList.firstName?if_exists} ${yearList.lastName?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
									${yearList.totalGrossAmount?string(",##0.00")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
									${yearList.totalPayeAmount?string(",##0.00")}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left"> TOTAL EMOLUMENTS
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                     <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">TOTAL PAYE TAX
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                     <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
                                	TOTAL WCPS
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                         <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
                                	* TAX ON LUMPSUM/AUDIT/INTEREST/PENALTY
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                         <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
						      <fo:block text-align="left">* TOTAL TAX DEDUCTED/ TOTAL C/F TO NEXT LIST 
						      </fo:block>
						    </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    
                </fo:table-body>
            </fo:table>
      
        </fo:block>
        
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="10pt">* DELETE AS APPROPRIATE</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="10pt" font-weight="bold">NOTE TO EMPLOYER:-</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>ATTACH TWO COPIES OF THIS LIST TO END YEAR CERTIFICATE</fo:block>
                </fo:list-item-body>
            </fo:list-item>
          </fo:list-block>
    <#else>
        <fo:block margin-top="50mm" text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>