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
    <#if yearList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="10pt" margin-top="14mm"  font-weight="bold" text-align="left">
         	P.10
        </fo:block>
        <fo:block font-size="10pt" margin-top="14mm" margin-left="61mm" font-weight="bold" text-align="left">
         P.A.Y.E - EMPLOYER'S CERTIFICATE
          YEAR ${year.name?if_exists}
        </fo:block>
        <fo:block font-size="10pt" margin-top="14mm" margin-left="100mm" font-weight="bold" text-align="right">
         EMPLOYER'S PIN 
         ${empDet.pinNumber?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>
       
		 
        <fo:list-block provisional-distance-between-starts="5in" font-size="10pt">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">To Senior Assistant Commissioner _______________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">We/I forward herewith ............................... Tax Deduction Cards (P9/P9B) showing the total tax deducted</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">(as listed on P10A) amounting to KSH </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">This total tax has been paid as follows:- </fo:block>
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
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold" font-size="12pt">
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>MONTH</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>
                            PAYE
                            (KSH.)
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>AUDIT TAX INTEREST/PENALTY (KSH. )</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>
                            	FRINGE TAX BENEFITS 
                            	(KSH.)
                            </fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#FFFFFF" border="1pt solid" border-width=".1mm">
                            <fo:block>DATE PAID (PER RECEIVING BANK STAMP)</fo:block>
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
                    <#list yearList as yearList>
                    
                         <fo:table-row font-size="10pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                 <fo:block>
                                 	${yearList.periodName?if_exists}
                                 </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                	${yearList.amount?string(",##0.00")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
									
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
									${yearList.amount?string(",##0.00")}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
									
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <fo:table-row column-height="30mm" font-weight="bold" font-size="12pt">
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" >
                                	TOTAL TAX KSH. 
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
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    
                </fo:table-body>
            </fo:table>
      
        </fo:block>
        
        <fo:list-block provisional-distance-between-starts="5in" font-size="10pt">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">NOTE:-</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="10pt">(1) Attach Photostat copies All the Pay-in Credit Slips (P11s) for the year.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="10pt">(2) Complete this certificate in triplicate and send the top two copies with the enclosures to your 
						Income Tax Office not later than 28th February.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-size="10pt">(3) Provide statistical information required by Central Bureau of Statistics. (See Overleaf)
						We/I certify the particulars entered above are correct.</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
          </fo:list-block>
          
          
          <fo:list-block provisional-distance-between-starts="5in" font-size="10pt" >
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">NAME OF EMPLOYER_______________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">ADDRESS__________________________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">SIGNATURE_________________________________________________________</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold" font-size="10pt">DATE_______________________________________________________________</fo:block>
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