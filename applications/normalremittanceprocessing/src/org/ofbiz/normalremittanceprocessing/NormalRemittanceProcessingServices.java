package org.ofbiz.normalremittanceprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;


/*****
 * @author Japheth Odonya  @when Jun 21, 2015 8:08:43 PM
 * */
public class NormalRemittanceProcessingServices {
	
	public static Logger log = Logger.getLogger(NormalRemittanceProcessingServices.class);
	
	public static void processCSV(String csvPath, String normalRemittanceMonthYearId) {

		log.info(" GGGGGGGGGGGGGGGGGGG ");
		log.info(" CSV Path (absolute is ) :::  " + csvPath);
		log.info(" normalRemittanceMonthYearId is :::  ) " + normalRemittanceMonthYearId);

		/***
		 * Create MemberRemittance
		 * 
		 * 
		 * <field name="memberSalaryId" type="id-vlong-int"></field> <field
		 * name="salaryMonthYearId" type="id-vlong-int"></field> <field
		 * name="isActive" type="indicator"></field> <field name="createdBy"
		 * type="id"></field> <field name="month" type="id"></field> <field
		 * name="year" type="id"></field> <field name="employerCode"
		 * type="id"></field> <field name="payrollNumber" type="id"></field>
		 * <field name="netSalary" type="fixed-point"></field> <field
		 * name="processed" type="indicator"></field>
		 * 
		 * */

		// String month = "";
		// String year = "";
		// String employerCode = "";
		Long normalRemittanceMonthYearIdLong = Long.valueOf(normalRemittanceMonthYearId);
		// Need month, year and employerCode

		// Find the SalaryMonthYear
		GenericValue normalRemittanceMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			normalRemittanceMonthYear = delegator.findOne("NormalRemittanceMonthYear",
					UtilMisc.toMap("normalRemittanceMonthYearId", normalRemittanceMonthYearIdLong),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long month = normalRemittanceMonthYear.getLong("month");
		Long year = normalRemittanceMonthYear.getLong("year");

		GenericValue station = LoanUtilities.getStation(normalRemittanceMonthYear
				.getString("stationId"));
		String employerCode = station.getString("employerCode");

		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";

		Long memberRemittanceId;
		GenericValue memberRemittance;

		List<GenericValue> listMemberRemittance = new ArrayList<GenericValue>();

		// Add the records to Member Salaries
		int count = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;
		try {
			br = new BufferedReader(new FileReader(csvPath));

			while ((line = br.readLine()) != null) {
				String[] remittance = line.split(csvSplitBy);
				count++;

				System.out.println(" Count " + count + " Payroll No "
						+ remittance[0] + " Total Amount " + remittance[1]);
				totalAmount = new BigDecimal(remittance[1]);
				memberRemittanceId = delegator.getNextSeqIdLong("MemberRemittance");
				memberRemittance = delegator.makeValue("MemberRemittance", UtilMisc
						.toMap("memberRemittanceId", memberRemittanceId,
								"normalRemittanceMonthYearId", normalRemittanceMonthYearIdLong,
								"isActive", "Y", "createdBy", "admin",
								// "transactionType", "LOANREPAYMENT",
								"month", month.toString(), "year",
								year.toString(),

								"employerCode", employerCode,

								"payrollNumber", remittance[0].trim(),

								"totalAmount", totalAmount,

								"processed", "N"));

				listMemberRemittance.add(memberRemittance);
			}

			try {
				delegator.storeAll(listMemberRemittance);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
