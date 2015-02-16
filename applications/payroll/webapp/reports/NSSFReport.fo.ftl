<#escape x as x?xml>
    <#if nssfList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            NSSF REPORT SUMMARY 
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            ${period.name?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->
        
            <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-header>

                    <fo:table-row font-weight="bold" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>Employee No</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>Name</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>ID Number</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>NSSF Number</fo:block></fo:table-cell>                            
                            <fo:table-cell border-bottom="thin solid grey" text-align="right"><fo:block>Statutory Contr</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey" text-align="right"><fo:block>Voluntary Contr</fo:block></fo:table-cell>                            
                            <fo:table-cell border-bottom="thin solid grey" text-align="right"><fo:block>Employer Contr</fo:block></fo:table-cell>                           
                            <fo:table-cell border-bottom="thin solid grey" text-align="center"><fo:block>Total NSSF Contr</fo:block></fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <#assign rowColor = "white">
                    <#list nssfList as nssflist>
                        <fo:table-row>
                        
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${nssflist.employeeNumber?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${nssflist.firstName?if_exists} ${nssflist.lastName?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${nssflist.nationalIDNumber?if_exists}</fo:block>
                                </fo:table-cell>
                                 <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${nssflist.socialSecurityNumber?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}" text-align="right">
                                    <fo:block>${nssflist.statutoryAmount?if_exists}</fo:block>
                                </fo:table-cell>
                                 <fo:table-cell padding="2pt" background-color="${rowColor}" text-align="right">
                                    <fo:block>${nssflist.nssfVolAmount?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}" text-align="right">
                                    <fo:block>${nssflist.statutoryAmount?if_exists}</fo:block>
                                </fo:table-cell>                                
                                <fo:table-cell padding="2pt" background-color="${rowColor}" text-align="right">
                                    <fo:block>${nssflist.lineTotalAmount?if_exists}</fo:block>
                                </fo:table-cell>
                         </fo:table-row>
                        <#-- toggle the row color -->
                        <#if rowColor == "white">
                            <#assign rowColor = "#D4D0C8">
                        <#else>
                            <#assign rowColor = "white">
                        </#if>
                    </#list>
                     <#list totalsList as totalsList>
                    	<fo:table-row rowColor="white">
                    		<fo:table-cell padding="2pt" background-color="white">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="white">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="white">
                                <fo:block></fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="white" font-weight="bold" text-align="right">
                                <fo:block>TOTALS</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="white" font-weight="bold" text-align="right">
                                <fo:block>${totalsList.TotalStatutoryAmount?if_exists}</fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" background-color="white" font-weight="bold" text-align="right">
                                <fo:block>${totalsList.TotalVoluntaryAmount?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="white" font-weight="bold" text-align="right">
                                <fo:block>${totalsList.TotalEmployerAmount?if_exists}</fo:block>
                            </fo:table-cell>                                
                            <fo:table-cell padding="2pt" background-color="white" font-weight="bold" text-align="right">
                                <fo:block>${totalsList.TotalLineAmount?if_exists}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                         </#list>
                </fo:table-body>
            </fo:table>
            </fo:block>
        <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>