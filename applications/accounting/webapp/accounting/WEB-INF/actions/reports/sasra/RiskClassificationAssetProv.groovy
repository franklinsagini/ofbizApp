import org.ofbiz.base.util.UtilMisc;

provisionList = [];
grandTotalList = [];
totalAccounts = BigDecimal.ZERO
totalRequiredAmount = BigDecimal.ZERO
totalOutstandingAmount = BigDecimal.ZERO

assestProvisioningList = delegator.findList("AssetProvisioning", null, UtilMisc.toSet("riskClassificationId", "noOfAccounts", "outstandingAmount", "provisioningPercentage", "provisioningAmount"), UtilMisc.toList("riskClassificationId"), null, false);

if (assestProvisioningList) {
  assestProvisioningList.each{ provision ->
    classifications = delegator.findOne("RiskClassification", UtilMisc.toMap("riskClassificationId", provision.riskClassificationId), true);
    provisionPercentage = (classifications.provisioningPercentage).toFloat()
    outstandingAmount = provision.outstandingAmount
    requiredProvisionAmount = (outstandingAmount * provisionPercentage)/100
    provisionList.add(["classification":classifications.name, noOfAccounts:provision.noOfAccounts, "outstandingAmount":provision.outstandingAmount, provisioningPercentage:classifications.provisioningPercentage, provisioningAmount:requiredProvisionAmount]);

    if (classifications.name == "Doubtful Loan" || classifications.name == "Loan Loss") {
      totalRequiredAmount = totalRequiredAmount - requiredProvisionAmount
    }else {
      totalRequiredAmount = totalRequiredAmount + requiredProvisionAmount
    }

    totalAccounts = totalAccounts + (provision.noOfAccounts).toInteger()

    totalOutstandingAmount = totalOutstandingAmount + outstandingAmount
  }
}


context.provisionList = provisionList;
grandTotalList.add("totalName":"Total Accounts", "balance":totalAccounts);
grandTotalList.add("totalName":"Total Outstanding Amount in KES", "balance":totalOutstandingAmount);
grandTotalList.add("totalName":"Total Required Provisioning Amount in KES", "balance":totalRequiredAmount);
context.grandTotalList = grandTotalList;
