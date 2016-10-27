package th.co.iconext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import net.sf.jasperreports.engine.JasperCompileManager;
//import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRProperties;

public class Sample {

	public static void main(String[] args) {
		System.out.println("Start ....");
		
		if(args.length ==0)
			System.err.println("Require Parameter [MODEL_CODE]");
		
		Sample sample = new Sample();
		for(String modelCode : args){
			sample.execute(modelCode);
		}
		System.out.println("Finish ....");
	}
	
	public Sample(){
		try{
			getConnection();
		} catch(Exception ex){
			ex.printStackTrace(System.err);
		}
	}
	
	private String REPORT_JASPER	= "sample1.jasper" ;
	private String PDF_OUTPUT		= "D:\\output\\models";
	
	private String OUTPUT_PATH		= "D:\\output\\" ;
	private String PDF_FILE_TYPE	= "pdf";
	
	private String RPT_FORM011		= "form011";
	private String RPT_FORM012		= "form012";
	private String RPT_FORM013		= "form013";
	
	private Connection conn = null;
	
	private String defaultPDFFont = "Microsoft Sans Serif";
	
	private List<String> outputFileL = null ;
	
	private void execute(String modelCode){
		try {
			outputFileL = null;
			gernerateReport(modelCode);

			System.out.println("Done exporting reports to pdf "+modelCode);

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	private void gernerateReport(String modelCode) throws Exception{
		System.out.println("Generate Report [Model Code :"+modelCode+"]");
		
		outputFileL = new ArrayList<String>();
		
		JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		JRProperties.setProperty("net.sf.jasperreports.default.font.name", defaultPDFFont);
		
		// Prepare Parameter
		HashMap params = getParameters(modelCode);
		
		JasperReport frm011Jasper = getJasperReport(RPT_FORM011);
		JasperPrint frm011JasPrint = fillReport( frm011Jasper, params, conn);
		exportToPdf(modelCode,RPT_FORM011,frm011JasPrint);
		
		JasperReport frm012Jasper = getJasperReport(RPT_FORM012);
		JasperPrint frm012JasPrint = fillReport( frm012Jasper, params, conn);
		exportToPdf(modelCode,RPT_FORM012,frm012JasPrint);
		
		JasperReport frm013Jasper = getJasperReport(RPT_FORM013);
		JasperPrint frm013JasPrint = fillReport( frm013Jasper, params, conn);
		exportToPdf(modelCode,RPT_FORM013,frm013JasPrint);
		
		mergePdfFile(modelCode);
	}
	
	private void mergePdfFile(String modelCode) throws Exception{
		System.out.println("Merge PDF File ........."+modelCode);
		PDFMergerUtility ut = new PDFMergerUtility();
		for(String srcFile  : outputFileL){
			ut.addSource(new File(srcFile));
		}
		
		String finalOutputFile = this.OUTPUT_PATH+modelCode+"."+PDF_FILE_TYPE;
		
		ut.setDestinationFileName(finalOutputFile);
		
		ut.mergeDocuments();
	}
	
	private JasperPrint fillReport(JasperReport jasperRpt , HashMap params , Connection conn)throws Exception{
		System.out.println("Fill Report ......"+jasperRpt.getName());
		return JasperFillManager.fillReport( jasperRpt, params, conn);
	}
	
	private void exportToPdf(String modelCode, String rptName, JasperPrint jasperPrint) throws Exception{
		System.out.println("Export to PDF ....."+modelCode+"_"+rptName+".pdf");
		
		String output = this.OUTPUT_PATH+modelCode+"_"+rptName+"."+PDF_FILE_TYPE;
		
		outputFileL.add(output);
		
		JasperExportManager.exportReportToPdfFile(jasperPrint, output);
	}
	
	private HashMap getParameters(String modelCode){
		HashMap params = new HashMap();
		params.put("P_MODEL_CODE", modelCode);
		return params;
	}
	
	private JasperReport getJasperReport(String rptFrm) throws Exception{
		System.out.println("Load Jasper Report ...."+rptFrm);
		
		if(rptFrm == null)
			return null;
		
		InputStream jasperIO = getClass().getClassLoader().getResourceAsStream(rptFrm+".jasper");
		
		JasperReport jasperRpt = (JasperReport)JRLoader.loadObject(jasperIO);
		return jasperRpt;
	}
	
	private Connection getConnection() throws Exception{
		System.out.println("Get Connection ....");
		
		if(conn ==null){
			String dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
			String dbDriver = "oracle.jdbc.OracleDriver";
			String dbUname = "arms";
			String dbPwd = "arms";
			
			Class.forName(dbDriver);
			// Get the connection
			conn = DriverManager.getConnection(dbUrl, dbUname, dbPwd);
		}
		
		return conn;
	}
}
