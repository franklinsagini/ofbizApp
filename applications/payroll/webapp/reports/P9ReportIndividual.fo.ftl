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
      
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="5pt" font-size="9pt" margin-top="1mm">
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
                <fo:table-column column-width="50pt"/>
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
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white" text-align="left">
                            <fo:block>P.9</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white" background-color="#FFFFFF" number-columns-spanned="3">
                            <fo:block font-weight="bold" font-size="9pt" text-align="right">
                            TAX DEDUCTION CARD </fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white" number-columns-spanned="3">
                            <fo:block font-weight="bold" font-size="9pt" text-align="left">YEAR ${year.name?if_exists} </fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
                	
                	<fo:table-row font-weight="bold" font-size="9pt" text-align="left">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" number-columns-spanned="3" border="1pt solid white">
                            <fo:block>Employer's Name: </fo:block>
                            <fo:block>Employee's Main Name: </fo:block>
                            <fo:block>Employee's Other Names: </fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" number-columns-spanned="3" border="1pt solid white">
                            <fo:block>${empDet.employer?if_exists}</fo:block>
                            <fo:block>${person.lastName?if_exists}</fo:block>
                            <fo:block>${person.firstName?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block font-size="1pt">. </fo:block>                            
                            <fo:block font-size="1pt">. </fo:block>                            
                            <fo:block>PFNO: </fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                 			<fo:block font-size="1pt">. </fo:block>                            
                            <fo:block font-size="1pt">. </fo:block>  
                            <fo:block>${person.employeeNumber?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white" background-color="#FFFFFF">
		        			<fo:block></fo:block>
					      </fo:table-cell>
                       <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white" number-columns-spanned="3">
                            <fo:block text-align="right" font-weight="bold">Employer's PIN: </fo:block>
		        			<fo:block text-align="right" font-weight="bold">Employee's PIN: </fo:block>
                        </fo:table-cell>
                  		<fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white" number-columns-spanned="3">
                  			<fo:block text-align="center" font-weight="bold">${empDet.pinNumber?if_exists}</fo:block>
		        			<fo:block text-align="center" font-weight="bold">${person.pinNumber?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid white">
                            <fo:block></fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
			                
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
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border="1pt solid" background-color="#FFFFFF" number-columns-spanned="3">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                       <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>Kshs.</fo:block>
                        </fo:table-cell> 
                    </fo:table-row>
                    
                    <fo:table-row font-weight="bold" font-size="9pt" text-align="center">
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>A</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>B</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>C</fo:block>
                        </fo:table-cell>
                 		<fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>D</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2" border="1pt solid" background-color="#FFFFFF" number-columns-spanned="3">
                            <fo:block>E</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>F</fo:block>
                        </fo:table-cell>
                   		<fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>G</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>H</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>J</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>K</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
                            <fo:block>-</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="1pt" background-color="#FFFFFF" border="1pt solid">
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
                         <fo:table-row font-size="8pt">                            
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
                    <fo:table-row column-height="30mm" font-weight="bold" font-size="9pt">
                    	<fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="left" >TOTALS</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>                        
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white">
                            <fo:block text-align="right"></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    
                    <fo:table-row column-height="30mm" font-size="9pt">
                    	<fo:table-cell padding="2pt" border="1pt solid white" number-columns-spanned="7">
                            <fo:block text-align="left" >To be completed by Employer at end of year</fo:block>
                            <fo:block margin-top="6pt" text-align="left" >TOTAL CHARGEABLE PAY (COL H.) Kshs. </fo:block>
                            <fo:block margin-top="2pt" text-align="left" text-decoration="underline">IMPORTANT</fo:block>
                            <fo:block margin-top="2pt" text-align="left" >1. Use P9A</fo:block>
                            <fo:block margin-top="2pt" margin-left="10pt" text-align="left" >
                            	(a) For all liable employees and where director/employee received Benefits in addition to cash emoluments.
                            </fo:block>
                            <fo:block margin-top="2pt" margin-left="10pt" text-align="left" >
                            	(b) Where an employee is eligibe to deduction of owner occupier interest.
                            </fo:block>
                            <fo:block margin-top="2pt" text-align="left" >2.</fo:block>
                            <fo:block margin-top="2pt" margin-left="10pt" text-align="left" >
                            	(a) Deductible interest in respect of any month must not exceed Kshs. 12,500/=
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white">
                            <fo:block text-align="left"></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white" number-columns-spanned="7">
                            <fo:block text-align="left">TOTAL TAX (COL L.) Kshs. </fo:block>
                            <fo:block margin-top="6pt" margin-left="10pt" text-align="left" >
                            	(b) Attach
                            </fo:block>
                            <fo:block margin-top="2pt" margin-left="15pt" text-align="left" >
                            	i. Photostat copy of interest certificate and statement of account from the Financial Institution.
                            </fo:block>
                            <fo:block margin-top="2pt" margin-left="15pt" text-align="left" >
                            	ii. The Declaration duly signed by the employee.
                            </fo:block>
                            <fo:block margin-top="10pt" text-align="left">NAMES OF FINANCIAL INSTITUTION ADVANCING MORGAGE LOAN</fo:block>
                            <fo:block margin-top="2pt" text-align="left">_____________________________________________________________</fo:block>
                            <fo:block margin-top="3pt" text-align="left">L.R. NO. OF OWNER OCCUPIED PROPERTY__________________________</fo:block>
                            <fo:block margin-top="3pt" text-align="left">DATE OF OCCUPATION OF HOUSE:_________________________________</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid white">
                            <fo:block text-align="left"></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
      
    <#else>
        <fo:block margin-top="50mm" text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>