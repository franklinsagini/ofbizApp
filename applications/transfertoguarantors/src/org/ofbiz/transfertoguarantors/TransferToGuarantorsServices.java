package org.ofbiz.transfertoguarantors;

import org.ofbiz.accountholdertransactions.LoanUtilities;


public class TransferToGuarantorsServices {
	
	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:51:03 PM
	 * */
	public static String getMemberNumber(Long partyId) {
		String memberNumber = "";

		memberNumber = LoanUtilities
				.getMemberNumberGivenPartyId(partyId);

		return memberNumber;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:45 PM
	 * **/
	public static String getPayrollNumber(Long partyId) {
		String payrollNumber = "";

		payrollNumber = LoanUtilities
				.getPayrollNumberGivenPartyId(partyId);

		return payrollNumber;
	}

	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:27 PM
	 * */
	public static String getMobileNumber(Long partyId) {
		String mobileNumber = "";

		mobileNumber = LoanUtilities
				.getMobileNumberGivenPartyId(partyId);

		return mobileNumber;
	}

	// getMemberStationName
	public static String getMemberStationName(Long partyId) {
		String stationName = "";

		stationName = LoanUtilities.getMemberStationNameGivenPartyId(partyId);

		return stationName;
	}

}
