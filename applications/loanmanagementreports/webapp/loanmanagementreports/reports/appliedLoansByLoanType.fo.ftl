<#escape x as x?xml>
    <#if startDate?has_content>

        <#-- REPORT TITLE -->
        <fo:block font-size="11pt" text-align="center"  font-weight="bold" >
            APPLIED LOANS BY LOAN TYPE
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- BANK Details -->
        <fo:block font-size="10pt" text-align="center">
           LOANS APPLIED BETWEEN ${startDate} - ${endDate}
        </fo:block>
        <fo:block font-size="10pt" text-align="center" font-weight="bold">
        </fo:block>
        <fo:block><fo:leader/></fo:block>

    <#-- REPORT BODY -->

    <#if loanTypes?has_content>
        <#list loanTypes as loanType>
            <fo:block font-size="10pt" font-weight="bold">
                ${loanType.code} - ${loanType.name} - ${loanType.loanProductId}
            </fo:block>

                    <fo:block space-after.optimum="10pt" font-size="9pt">
                    <fo:table table-layout="fixed" width="100%" font-size="9pt" >
                    <fo:table-column column-number="1" column-width="proportional-column-width(10)"/>
                    <fo:table-column column-number="2" column-width="proportional-column-width(22)"/>
                    <fo:table-column column-number="3" column-width="proportional-column-width(10)"/>
                    <fo:table-column column-number="4" column-width="proportional-column-width(21)"/>
                    <fo:table-column column-number="5" column-width="proportional-column-width(10)"/>
                    <fo:table-column column-number="6" column-width="proportional-column-width(21)"/>
                    <fo:table-header>
                        <fo:table-row font-weight="bold">
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Loan No</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Date Applied</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Member No</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Name</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Amt Applied</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Status</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                        <fo:table-row background-color="#D4D0C8" color="#FFFFFF">
                            <fo:table-cell>
                            </fo:table-cell>
                            <fo:table-cell>
                            </fo:table-cell>
                            <fo:table-cell>
                            </fo:table-cell>
                            <fo:table-cell>
                            </fo:table-cell>
                            <fo:table-cell>
                            </fo:table-cell>
                            <fo:table-cell>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </fo:block>
            <#assign count=0 />
            <#assign totalAmt=0.00 />

            <#if loanApps?has_content>
                <#list loanApps as loanApp>

                    <#if loanApp.loanProductId == loanType.loanProductId >
                        <fo:block space-after.optimum="10pt" font-size="9pt">
                        <fo:table table-layout="fixed" width="100%" font-size="9pt" >
                            <fo:table-column column-number="1" column-width="proportional-column-width(10)"/>
                            <fo:table-column column-number="2" column-width="proportional-column-width(22)"/>
                            <fo:table-column column-number="3" column-width="proportional-column-width(10)"/>
                            <fo:table-column column-number="4" column-width="proportional-column-width(21)"/>
                            <fo:table-column column-number="5" column-width="proportional-column-width(10)"/>
                            <fo:table-column column-number="6" column-width="proportional-column-width(21)"/>
                            <fo:table-header>
                              <fo:table-row font-weight="bold">
                                <fo:table-cell  background-color="#FFFFFF" color="#FFFFFF">
                                </fo:table-cell>
                                <fo:table-cell  background-color="#FFFFFF" color="#FFFFFF">
                                </fo:table-cell>
                                <fo:table-cell  background-color="#FFFFFF" color="#FFFFFF">
                                </fo:table-cell>
                                <fo:table-cell  background-color="#FFFFFF" color="#FFFFFF">
                                </fo:table-cell>
                                <fo:table-cell  background-color="#FFFFFF" color="#FFFFFF">
                                </fo:table-cell>
                                <fo:table-cell background-color="#FFFFFF" color="#FFFFFF" >
                                </fo:table-cell>
                              </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                        <fo:block>${loanApp.loanNo?if_exists}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                        <fo:block>${loanApp.createdStamp?if_exists}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                        <fo:block>${loanApp.memberNumber?if_exists}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                        <fo:block>${loanApp.firstName?if_exists} ${loanApp.lastName?if_exists}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                        <fo:block>${loanApp.appliedAmt?if_exists}</fo:block>
                                    </fo:table-cell>
                                    <#if loanApp.loanStatusId?has_content>
                                        <#assign loanStatus = delegator.findOne("LoanStatus", {"loanStatusId" : loanApp.loanStatusId}, false)/>
                                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                          <fo:block>${loanStatus.name?if_exists}</fo:block>
                                        </fo:table-cell>
                                    </#if>
                                 </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                        </fo:block>
                        <#assign count = count + 1 />
                        <#assign totalAmt = totalAmt + loanApp.appliedAmt />
                    </#if>
                </#list>
            </#if>

             <fo:block font-weight="bold">${loanType.name} Summary</fo:block>
             <fo:block>TOTAL No: ${count}</fo:block>
             <fo:block>TOTAL AMOUNT APPLIED: ${totalAmt}</fo:block>
             <fo:block><fo:leader/></fo:block>
             <fo:block><fo:leader/></fo:block>
             <fo:block><fo:leader/></fo:block>
             <fo:block><fo:leader/></fo:block>

        </#list>
    </#if>


    <#else>
        <fo:block text-align="center">NO LOANS FOUND BETWEEN THE SUPPLIED DATES</fo:block>

    </#if>

</#escape>
