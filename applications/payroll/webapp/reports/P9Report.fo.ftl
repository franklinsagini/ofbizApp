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
    <#if p9ItemsList?has_content>
        <#-- REPORT TITLE -->
         <fo:list-block provisional-distance-between-starts="2in">
            <fo:list-item>
                <fo:list-item-label>
                <fo:block  font-weight="bold" font-size="10pt">P.9</fo:block>
                    <fo:block font-weight="bold" font-size="10pt" margin-left="-10mm" margin-top="7mm" text-align="center">
                    	TAX DEDUCTION CARD  YEAR ${year.name?if_exists} 
                    </fo:block>  
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
         </fo:list-block>
        
        <fo:list-block provisional-distance-between-starts="5in" font-size="9pt">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">Employer's Name: ${empDet.employer?if_exists} </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">Employer's PIN: ${empDet.pinNumber?if_exists}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">Employee's Main Name: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">Employee's PIN: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">Employee's Other Names: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">PFNO: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
        
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
<#--        <fo:table border="0.5pt solid black"
          text-align="center"
          border-spacing="3pt">
		  <fo:table-column column-width="1in"/>
		  <fo:table-column column-width="0.5in" number-columns-repeated="2"/>
		  <fo:table-header>
		    <fo:table-row>
		      <fo:table-cell padding="6pt"
		                     border="1pt solid blue"
		                     background-color="silver"
		                     number-columns-spanned="3">
		        <fo:block text-align="center" font-weight="bold">
		          Header
		        </fo:block>
		      </fo:table-cell>
		    </fo:table-row>
		  </fo:table-header>
		  <fo:table-body>
		    <fo:table-row>
		      <fo:table-cell padding="6pt"
		                     border="1pt solid blue"
		                     background-color="silver"
		                     number-rows-spanned="2">
		        <fo:block text-align="end" font-weight="bold">
		          Items:
		        </fo:block>
		      </fo:table-cell>
		      <fo:table-cell padding="6pt" border="0.5pt solid black">
		        <fo:block> 1 : 1 </fo:block>
		      </fo:table-cell>
		      <fo:table-cell padding="6pt" border="0.5pt solid black">
		        <fo:block> 1 : 2 </fo:block>
		      </fo:table-cell>
		    </fo:table-row>
		    <fo:table-row>
		      <fo:table-cell padding="6pt" border="0.5pt solid black">
		        <fo:block> 2 : 1 </fo:block>
		      </fo:table-cell>
		      <fo:table-cell padding="6pt" border="0.5pt solid black">
		        <fo:block> 2 : 2 </fo:block>
		      </fo:table-cell>
		    </fo:table-row>
		  </fo:table-body>
		</fo:table> -->
        
            <fo:table table-layout="fixed" width="100%" border="0.5pt solid black" >
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="55pt" number-columns-repeated="3"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="70pt"/>
             <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
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
                    <fo:table-row font-weight="bold" font-size="9pt" text-align="center">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>MONTH</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Basic Pay</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Benefits Non-Cash</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>
                            	Value Of Quarters
                            </fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Total Gross Pay A+B+C</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" border="1pt solid" background-color="#FFFFFF" number-columns-spanned="3">
		        			<fo:block text-align="center" font-weight="bold">
					          Defined Contribution Retirement Scheme
					        </fo:block>
					      </fo:table-cell>
                       <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Savings Plan</fo:block>
                        </fo:table-cell>
                  		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Retirement Contribution And Savings Plan</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Chargable Pay</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Tax Charged</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Monthly Relief</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Insurance Relief</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>P.A.Y.E.</fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
                    
                    <fo:table-row font-weight="bold" font-size="9pt" text-align="center">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" background-color="#FFFFFF" number-columns-spanned="3">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
                    
                    <fo:table-row font-weight="bold" font-size="9pt" text-align="center">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>A</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>B</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>C</fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>D</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2" border="1pt solid" background-color="#FFFFFF" number-columns-spanned="3">
                            <fo:block>E</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>F</fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>G</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>H</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>J</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>K</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>-</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>L</fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
                    
                    <fo:table-row font-weight="bold" font-size="9pt" text-align="center">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
	                    	<fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>E1 30% of A</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>E2 Actual Contribution</fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>E3 Legal Limit</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Amount Deposited</fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>The Lowest of E added to F</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block text-align="center">Total</fo:block>
                            <fo:block margin-top="1mm" text-align="center">Kshs.</fo:block>
                        </fo:table-cell> 
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                		<#-- fo:table-row>
                            
                            <fo:table-cell padding="2pt" font-size="16pt" font-weight="bold">
                                <fo:block>Earnings</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt">
                                <fo:block>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row -->
                    <#list p9ItemsList as yearList>
                   <#-- <#if yearList.payeamount?if_exists != 0> -->
                         <fo:table-row font-size="9pt">                            
                            <fo:table-cell padding="2pt" border="1pt solid">
                                 <fo:block>
                                 	${yearList.periodName?if_exists}
                                 </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalBasicAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid">
                                <fo:block text-align="right">
									
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalGrossAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalE1Amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalE2Amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${maxPension.pension_maxContibution?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalRetConOwnAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalTaxablePayAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalTaxChargedAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalMprAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalInsuranceReliefAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" text-align="right">
                                	<fo:block>${yearList.totalPayeAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                       <#-- </#if> -->
                    </#list>
                    <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid">
                                <fo:block text-align="left" >
                                	TOTAL TAX KSH. 
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    
                </fo:table-body>
            </fo:table>
      
        </fo:block>
        
        <fo:list-block provisional-distance-between-starts="5in" font-size="9pt">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">NOTE:-</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="9pt">(1) Attach Photostat copies All the Pay-in Credit Slips (P11s) for the year.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="9pt">(2) Complete this certificate in triplicate and send the top two copies with the enclosures to your 
						Income Tax Office not later than 28th February.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="9pt">(3) Provide statistical information required by Central Bureau of Statistics. (See Overleaf)
						We/I certify the particulars entered above are correct.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
          </fo:list-block>
          
          
          <fo:list-block provisional-distance-between-starts="5in" font-size="9pt" margin-top="8mm">
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">NAME OF EMPLOYER_______________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">ADDRESS__________________________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">SIGNATURE_________________________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="9pt">DATE_______________________________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
        
     
        

    <#else>
        <fo:block margin-top="50mm" text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>