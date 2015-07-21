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

<fo:block font-size="17pt" text-align="center"> GL REPORT</fo:block>
<fo:block> </fo:block>  
<fo:block> </fo:block>



<fo:table table-layout="fixed" width="100%" table-align="center" >
            <fo:table-column column-width="20pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="90pt"/>
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="100pt"/>
    <fo:table-header>
    <fo:table-row font-weight="bold">
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block></fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>DATE</fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>PARTICULARS</fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>TRANS#</fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>DEBIT</fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>CREDIT</fo:block>
               </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                <fo:block>BALANCE</fo:block>
               </fo:table-cell>
    </fo:table-row>
    </fo:table-header>
    
    <fo:table-body>

         <#assign count = 0>
        
                     <fo:table-row>
                      <#assign count = count+1>
                     
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>

                     </fo:table-row>
    </fo:table-body>
</fo:table>





</#escape>