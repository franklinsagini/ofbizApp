<div class="screenlet">
    <div class="screenlet-body">
    <form id="syncPayrollTotals" name="syncPayrollTotals" method="post" action="<@ofbizUrl>syncPayrollTotals</@ofbizUrl>">
        <#if totals?has_content>
         <#if paymentList?has_content>
            <table>
                <tr>
                    <td>Period ID</td>
                    <td>Payroll Element</td>
                    <td>Total</td>
                    <td>Posted</td>
                    <td>Select For Posting</td>
                </tr>
                 <#assign alt_row = false>
                    <#list paymentList as payment>
            </table>
        </#if>
    </form>
    </div>
</div>