package io.qingxuan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet implementation class SearchDealApi
 */
@WebServlet("/SearchDeal")
public class SearchDealApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ServletConfig config = null;
	private String uiTemplate = "";
	private String adTemplate = "";
	
	private ProdSelector prodSelector = null;
	
    /**
     * Default constructor. 
     */
    public SearchDealApi() {
        // TODO Auto-generated constructor stub
    		super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		ServletContext application = config.getServletContext();
		
		String mysqlHost = application.getInitParameter("mysqlHost");
		String mysqlDb = application.getInitParameter("mysqlDB");
		String mysqlUser = application.getInitParameter("mysqlUser");
		String mysqlPass = application.getInitParameter("mysqlPass");		
		String uiTemplateFilePath = application.getInitParameter("uiTemplateFilePath");
	    String adTemplateFilePath = application.getInitParameter("adTemplateFilePath");
	    
		
		this.prodSelector = new ProdSelector(mysqlHost, mysqlDb, mysqlUser, mysqlPass);
		
		//load UI template
		try {
			byte[] uiData;
			byte[] adData;
			uiData = Files.readAllBytes(Paths.get(uiTemplateFilePath));
			uiTemplate = new String(uiData, StandardCharsets.UTF_8);
			adData = Files.readAllBytes(Paths.get(adTemplateFilePath));
			adTemplate = new String(adData, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("UI template initilized");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String query = request.getParameter("q");
		
		List<Prod> deals = this.prodSelector.getDeals(query, 5);
		String result = uiTemplate;
        String list = "";
		for(Prod prod: deals) {
			System.out.println("final selected prod = " + prod.prodTitle);
			System.out.println("final selected prod price = " + prod.curPrice);
			System.out.println("final selected prod deal = " + prod.deal);
			String prodContent = adTemplate;
			prodContent = prodContent.replace("$title$", prod.prodTitle);
			prodContent = prodContent.replace("$brand$", Double.toString(-prod.deal*100).substring(0, 4) + "%");
			prodContent = prodContent.replace("$link$", prod.prodURL);
			prodContent = prodContent.replace("$price$", Double.toString(prod.curPrice));
			list = list + prodContent;
			
		}
        result = result.replace("$list$", list);
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(result);;
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}
