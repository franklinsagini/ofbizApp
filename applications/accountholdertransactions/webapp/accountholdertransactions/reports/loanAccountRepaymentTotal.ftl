<div class="screenlet screenlet-body">
  
    
    <div class="screenlet">
    <div class="screenlet-title-bar">
      <ul>
        <li></li>
        <li class="h3">Total Amount</li>
      </ul>
      <br class="clear" />
    </div>
    <div class="screenlet-body">
    <h3>
    <form id="reconValues" method="post" name="reconValues" action="">
    <table width="100%" border='0' cellspacing='0' cellpadding='0'>
    <input name="glReconciliationId" type="hidden" value="${parameters.glReconciliationId}"/>
      <tr>
        <td>
  
        </td>
        <td>
          <table>
          	<tr >
              <td>Payment Type</td>
              <td><select>
					  <option value="Cash">Cash</option>
					  <option value="Cheque">Cheque</option>
			</select>
			</td>
            </tr>
            <tr>
              <td>Cheque No</td>
              <td><input type='text' size='15' maxlength='100' name='bankBalanceTotal' id='bankBalanceTotal' value="0"/></td>
            </tr>
            <tr>
              <td>Total Amount</td>
              <td><input type='text' size='15' maxlength='100' name='bankBalanceTotal' id='bankBalanceTotal' value="0"/></td>
            </tr>
            
            
            
          </table>
        </td>
      </tr>
    </table>
  </form>
    </h3>
  </div>
  </div>
    
</div>