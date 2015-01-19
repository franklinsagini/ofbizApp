/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.widget.screen;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.PdfBook;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.webapp.view.AbstractViewHandler;
import org.ofbiz.webapp.view.ApacheFopWorker;
import org.ofbiz.webapp.view.ViewHandlerException;
import org.ofbiz.widget.form.FormStringRenderer;
import org.ofbiz.widget.form.MacroFormRenderer;
import org.ofbiz.widget.html.HtmlScreenRenderer;

import com.ibm.icu.util.Calendar;

/**
 * Uses XSL-FO formatted templates to generate PDF, PCL, POSTSCRIPT etc. views
 * This handler will use JPublish to generate the XSL-FO
 */
public class ScreenFopViewHandler extends AbstractViewHandler {
	public static final String module = ScreenFopViewHandler.class.getName();
	protected static final String DEFAULT_ERROR_TEMPLATE = "component://common/widget/CommonScreens.xml#FoError";

	protected ServletContext servletContext = null;

	/**
	 * @see org.ofbiz.webapp.view.ViewHandler#init(javax.servlet.ServletContext)
	 */
	public void init(ServletContext context) throws ViewHandlerException {
		this.servletContext = context;
	}

	/**
	 * @see org.ofbiz.webapp.view.ViewHandler#render(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void render(String name, String page, String info,
			String contentType, String encoding, HttpServletRequest request,
			HttpServletResponse response) throws ViewHandlerException {

		// render and obtain the XSL-FO
		Writer writer = new StringWriter();
		try {
			ScreenStringRenderer screenStringRenderer = new MacroScreenRenderer(
					UtilProperties.getPropertyValue("widget", getName()
							+ ".name"), UtilProperties.getPropertyValue(
							"widget", getName() + ".screenrenderer"));
			FormStringRenderer formStringRenderer = new MacroFormRenderer(
					UtilProperties.getPropertyValue("widget", getName()
							+ ".formrenderer"), request, response);
			// TODO: uncomment these lines when the renderers are implemented
			// TreeStringRenderer treeStringRenderer = new
			// MacroTreeRenderer(UtilProperties.getPropertyValue("widget",
			// getName() + ".treerenderer"), writer);
			// MenuStringRenderer menuStringRenderer = new
			// MacroMenuRenderer(UtilProperties.getPropertyValue("widget",
			// getName() + ".menurenderer"), writer);
			ScreenRenderer screens = new ScreenRenderer(writer, null,
					screenStringRenderer);
			screens.populateContextForRequest(request, response, servletContext);

			// this is the object used to render forms from their definitions
			screens.getContext().put("formStringRenderer", formStringRenderer);
			screens.getContext().put(
					"simpleEncoder",
					StringUtil.getEncoder(UtilProperties.getPropertyValue(
							"widget", getName() + ".encoder")));
			screens.render(page);
		} catch (Exception e) {
			renderError("Problems with the response writer/output stream", e,
					"[Not Yet Rendered]", request, response);
			return;
		}

		// set the input source (XSL-FO) and generate the output stream of
		// contentType
		String screenOutString = writer.toString();
		if (!screenOutString.startsWith("<?xml")) {
			screenOutString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ screenOutString;
		}
		if (Debug.verboseOn())
			Debug.logVerbose("XSL:FO Screen Output: " + screenOutString, module);

		if (UtilValidate.isEmpty(contentType)) {
			contentType = UtilProperties.getPropertyValue("widget", getName()
					+ ".default.contenttype");
		}
		Reader reader = new StringReader(screenOutString);

		StreamSource src = new StreamSource(reader);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Fop fop = ApacheFopWorker.createFopInstance(out, contentType);
			ApacheFopWorker.transform(src, null, fop);

			// System.out.println("TTTTTTTTTTTTT "+src);
			// System.out.println("TTTTTTTTTTTTT "+src.getInputStream());
			
			//System.out.println("OOOOOOOOOOOOO " + out.size());
			//System.out.println("BBBBBBBBBBBBBB" + " String :: " + out);
			//System.out.println(" Bytes :: " + out.toByteArray());
			
			// System.out.println(" FOOOOOOOOOOP "+fop.getResults().toString());
		} catch (Exception e) {
			renderError("Unable to transform FO file", e, screenOutString,
					request, response);
			return;
		}

		// set the content type and length
		response.setContentType(contentType);
		response.setContentLength(out.size());

		System.out.println("NNNNNNNNNNNNNNNNNNNNNNN - " + name);
		System.out.println("PPPPPPPPPPPPPPPPPPPPPPP - " + page);
		System.out.println("IIIIIIIIIIIIIIIIIIIIIII - " + info);

		if (name.equals("transactionPrintOut")) {

			String printoutname = "printout"
					+ Calendar.getInstance().getTimeInMillis() + ".pdf";

			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(printoutname);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				out.writeTo(outputStream);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				outputStream.close();
				outputStream.flush();

				//out.close();
				//out.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out
					.println("SSSSSSSSSSSSSSSSSSSSSSSSSSS Sending this to the Printer !!!!!!!!");

			PdfDecoder decodePdf = new PdfDecoder(true);
			try {
				decodePdf.openPdfFile(printoutname);
				FontMappings.setFontReplacements();
			} catch (PdfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
			JobName jobName = new JobName("Transaction Printout", null);
			attributeSet.add(jobName);

			decodePdf.setPrintAutoRotateAndCenter(true);
			decodePdf
					.setPrintPageScalingMode(PrinterOptions.PAGE_SCALING_FIT_TO_PRINTER_MARGINS);
			decodePdf.setPrintPageScalingMode(PrinterOptions.PAGE_SCALING_NONE);
			decodePdf
					.setPrintPageScalingMode(PrinterOptions.PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS);
			try {
				decodePdf.setPagePrintRange(1, decodePdf.getPageCount());
			} catch (PdfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// PDFDecoder decodePdf = new PdfDecoder();
			// DocFlavor.SERVICE_FORMATTED.PAGEABLE
			PrintService[] services = PrintServiceLookup.lookupPrintServices(
					null, attributeSet);
			for (PrintService s : services) {
				System.out.println(s.getName());
			}
			PrintService printingDevice = null;
			for (PrintService s : services) {
				// Microsoft XPS Document Writer
				//EPSON LX-300+ /II
				if (s.getName().equals("EPSON LX-300+ /II")) {
					printingDevice = s;
				}
			}

			PdfBook pdfBook = new PdfBook(decodePdf, printingDevice,
					attributeSet);
			SimpleDoc doc = new SimpleDoc(pdfBook,
					DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);

			DocPrintJob printJob = printingDevice.createPrintJob();

			try {
				printJob.print(doc, attributeSet);
			} catch (PrintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// PdfBook pdfBook = new PdfBook(response.getOutputStream(),
			// printingDevice, attributeSet);
			// SimpleDoc doc = new SimpleDoc(pdfBook,
			// DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
			// Doc pdfDoc = null;
			// DocFlavor.SERVICE_FORMATTED.PAGEABLE
			// pdfDoc = new SimpleDoc(src, DocFlavor.SERVICE_FORMATTED, null);
			// System.out.println("RRRRRRRRRRRR REARDERRRRRRRRR "+screenOutString);
			// System.out.println("RRRRRRRRRRRR SSSSSSSSSSSSSSSSSS "+src.getInputStream());
			// FileInputStream fis = new FileInputStream(new File)

			// OutputStream outputStream = null;
			// try {
			// outputStream = new FileOutputStream ("printout.pdf");
			// } catch (FileNotFoundException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// try {
			// out.writeTo(outputStream);
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			//
			// try {
			// outputStream.close();
			// outputStream.flush();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			//
			// FileInputStream fis = null;
			// try {
			// fis = new FileInputStream("printout.pdf");
			// } catch (FileNotFoundException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// System.out.println("Current Directory "+System.getProperty("user.dir"));
			//
			// DocFlavor docflavor = new DocFlavor.INPUT_STREAM
			// ("application/pdf");
			//
			// //DocFlavor.INPUT_STREAM.AUTOSENSE
			// pdfDoc = new SimpleDoc(fis, docflavor, null);
			//
			//
			//
			// DocPrintJob printJob = printingDevice.createPrintJob();
			//
			// attributeSet.add(new Copies(1));
			// attributeSet.add(Sides.ONE_SIDED);
			// try {
			// printJob.print(pdfDoc, attributeSet);
			// } catch (PrintException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			//
			// try {
			// fis.close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// Doc pdfDoc = null;
			// DocPrintJob printJob = PrintService
			// try {
			// pdfDoc = new SimpleDoc(response.getOutputStream(), null, null);
			//
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// PDFDecoder decodePdf = new PdfDec
			
			/***
			 * Print this
			 * **/
			System.out.println("############### name "+name);
			System.out.println("############### page "+page);
			System.out.println("############### info "+info);
			System.out.println("############### contentType "+contentType);
			System.out.println("############### encoding "+encoding);
			
			// page =
			// "component://accountholdertransactions/widget/accountholdertransactions/AccHolderTransactionsScreens.xml#NewCashWithdrawal";
			// name = "cashWithdrawal";
			// contentType = "";
			//
			// render(name, page, info, contentType, encoding, request,
			// response);
			
			
			
			//ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		
			try {
				out.writeTo(response.getOutputStream());
				out.close();
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {

			// write to the browser
			System.out
					.println("WWWWWWWWWWWWWWWWWWWWWWWWWWW Writing to the browser !!!!!!!!");
			try {
				out.writeTo(response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				renderError("Unable to write to OutputStream", e,
						screenOutString, request, response);
			}

		}

	}

	protected void renderError(String msg, Exception e, String screenOutString,
			HttpServletRequest request, HttpServletResponse response)
			throws ViewHandlerException {
		Debug.logError(msg + ": " + e + "; Screen XSL:FO text was:\n"
				+ screenOutString, module);
		try {
			Writer writer = new StringWriter();
			ScreenRenderer screens = new ScreenRenderer(writer, null,
					new HtmlScreenRenderer());
			screens.populateContextForRequest(request, response, servletContext);
			screens.getContext().put("errorMessage", msg + ": " + e);
			screens.render(DEFAULT_ERROR_TEMPLATE);
			response.setContentType("text/html");
			response.getWriter().write(writer.toString());
			writer.close();
		} catch (Exception x) {
			Debug.logError("Multiple errors rendering FOP", module);
			throw new ViewHandlerException("Multiple errors rendering FOP", x);
		}
	}
}
