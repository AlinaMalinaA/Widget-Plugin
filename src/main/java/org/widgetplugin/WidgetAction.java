package org.widgetplugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.Stapler;

import hudson.model.Action;

public final class WidgetAction implements Action {

    public String getDisplayName() {
        return "Widget plugin";
    }

    /**
     * This action doesn't provide any icon file.
     * @return null
     */
    public String getIconFileName() {
        return null;
    }

    /**
     * Gets the URL name for this action.
     * @return the URL name
     */
    public String getUrlName() {
        return "widgetplugin";
    }
    
    	public static String doGet(CloseableHttpClient httpclient, String URL, String encoding) throws ClientProtocolException, IOException {
    		HttpGet httpget = new HttpGet(URL);
    	    httpget.setHeader("Authorization", "Basic " + encoding);

    	    return EntityUtils.toString(httpclient.execute(httpget).getEntity());
    	}
    

    public static void makeServerInfo() {
    	String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String usernName = "Alena";
        String password = "q12345";
        String jenkinsUrl = protocol + "://" + host + ":" + port + "/";
        
    	CloseableHttpClient httpclient = HttpClientBuilder.create().build();
	    String encoding = Base64.getEncoder().encodeToString((usernName +":" + password).getBytes());
	   
    	HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
	    String responseBody;
		try {
			responseBody = doGet(httpclient, jenkinsUrl + "rssFailed",  encoding);
			String temp = responseBody;
		    int ind = temp.indexOf("y><title"); 
		    int count2 = StringUtils.countMatches(temp, "y><title>");//количество вхождений 
		    for (int i = 0; i<count2; i++) {
			    temp = temp.substring(ind+9); 
			    ind = temp.indexOf("#"); 
			    String Name = temp.substring(0, ind).trim();
			    System.out.println("имя поломки  "+ Name);
			    if (hashMap.containsKey(Name)){
			    	hashMap.put(Name, hashMap.get(Name)+1);
			    }
			    else {
			    	hashMap.put(Name, 1);
			    }
			    
			    temp = temp.substring(ind+1); 
			    ind = temp.indexOf(" "); 
			    ind = temp.indexOf("y><title"); 
		    }
		    
		    createServerFile(hashMap);
		    httpclient.close();
	    	
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
    	
        }
    	
    	
    public String getBuildInfo() {
    	String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String usernName = "Alena";
        String password = "q12345";
        String apiJson = "api/json?pretty=true";
        String last = "rssLatest"; //данные о последних сборках
        String temp = "";
        String jenkinsUrl = protocol + "://" + host + ":" + port + "/";
        String[][] arr = null;
        String xx = null;
    		System.out.println( "Hello World from qqq! V6" );
    	    System.out.println( );
            String globalTemp = "";
            //создание соединения
            //На уровне сервера  список джобов которые чаще всего ломаются 
            //то есть получение всех сломанных билдов, сортировка по имени джобы, взять первые три джобы (или джобы с поломами больше двух)
            //На уровне джоба таблица из пяти сборок две колонки имя и результат
            //получение последних пяти сборок
            //На уровне билда время выполнения и результат
    			try {
    				CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    			    String encoding = Base64.getEncoder().encodeToString((usernName +":" + password).getBytes());
    			   
    			    String responseBody = doGet(httpclient, jenkinsUrl + last, encoding);
    			    globalTemp = responseBody.substring(responseBody.indexOf("<title>")+7);//обрезание заголовка
    			    int count = StringUtils.countMatches(globalTemp, "<title>");//количество вхождений 
    			    //имя проекта, номер сборки проекта
    			    int ind = 0;
    			    xx = doGet(httpclient, jenkinsUrl + Stapler.getCurrentRequest().getRequestURI() + apiJson,  encoding);
    			    String JobName = "";
    			    String JobBuild = "";
    			    //имя проекта, номер последней джобы, длительность, давность
    			    arr = new String[count][4];
    			    for (int i = 0; i<count; i++) {
    			    	ind = globalTemp.indexOf("/job/");//поиск заголовка еще раз
    			    	//обрезание заголовка
    			    	globalTemp = globalTemp.substring(ind+5); 
    			    	temp = globalTemp;
    			    	ind = temp.indexOf("/");
    				    JobName  = temp.substring(0, ind);
    				    System.out.println("имя джобы   "+ JobName);
    				    temp = temp.substring(ind+1); 
    				    ind = temp.indexOf("/");
    				    JobBuild  = temp.substring(0, ind); 
    				    arr[i][0] = JobName.trim();
    			    	arr[i][1] = JobBuild.trim();
    			    	System.out.println("номер  джобы   "+ JobBuild);
    			    	if ( arr[i][0].contains(" ")) {
    			    		responseBody = doGet(httpclient, jenkinsUrl + "job/"+arr[i][0].replaceAll(" ", "%20")+"/"+arr[i][1]+"/" + apiJson,  encoding);}
    			    	else {responseBody = doGet(httpclient, jenkinsUrl + "job/"+arr[i][0]+"/"+arr[i][1]+"/" + apiJson,  encoding);}
    			    	ind = responseBody.indexOf("estimatedDuration");//поиск длительности 
    				    temp = responseBody.substring(ind+17+4); //4 символа на кавычки, пробел, двоеточие и пробел
    				    temp = temp.substring(0, temp.indexOf(",")); 
    				    arr[i][2] = temp;
    				    System.out.println("длительность  "+ arr[i][2]);
    				    
    				    temp = responseBody.substring(responseBody.indexOf("timestamp")+9+4); //4 символа на кавычки, пробел, двоеточие и пробел
    				    temp = temp.substring(0, temp.indexOf(",")); 
    				    arr[i][3] = temp;
    				    System.out.println("давность  "+ arr[i][3]);
    				    ind = globalTemp.indexOf("title");//поиск заголовка еще раз
    			    	//обрезание заголовка
    				    if (ind >0)
    			    	globalTemp = globalTemp.substring(ind); 
    			    }
    			    
    			    httpclient.close();
    			
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (Exception e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			} 
    			makeServerInfo();
        return createBuildTable(xx);
    }

    
    //создает таблицу для ГЛАВНОЙ страницы
	public static void createServerFile(HashMap<String, Integer> hashMap) throws IOException {
		String home = "C:" + File.separator + "Program Files (x86)" + File.separator + "Jenkins" + File.separator + "userContent";
		String nameForGeneralPage = "banner";
		File file = new File(home + File.separator + nameForGeneralPage+"2.html");
    	file.createNewFile();
    			
    	    PrintWriter writr = new PrintWriter(file, "UTF-8");
    	    Date date = new Date();
    	  
    	    	writr.append("  Актуально на "+date.toString());
    		    writr.append("<table border='1'> <tr> <td>Job`s name</td> <td>Number of fails</td></tr>");
    		    for (Entry<String, Integer> entry : hashMap.entrySet()) {
    		    	writr.append("<tr><td>" + entry.getKey() + "</td>");
    		    	writr.append("<td>" + entry.getValue()+"</td></tr>");
    		    }
    		    writr.append("</table>");
    	    
    		
    			writr.close();
    		
	}

	//создает таблицу для страницы БИЛДА
	public static String createBuildTable(String buildInf){
		Stapler.getCurrentRequest().getRequestURI();
		//возвращает строку типа этой 
		// /job/ProjectName/5/
		String temp = Stapler.getCurrentRequest().getRequestURI();
		if(temp.endsWith("/")) {temp=temp.substring(0, temp.length()-1);}
	    int ind = temp.lastIndexOf("/"); 
	   
		ind = buildInf.indexOf("duration");
	    temp = buildInf.substring(ind+12);
	    ind = temp.indexOf(",");
	    float buildDuration = Float.parseFloat(temp.substring(0,ind));
		
		ind = buildInf.indexOf("displayName");
	    temp = buildInf.substring(ind+16);
	    ind = temp.indexOf(",");
	    String buildName = temp.substring(0, ind-1);
	    
	    ind = temp.indexOf("result");
	    temp = temp.substring(ind+11);
	    ind = temp.indexOf(",");
	    String buildResult = temp.substring(0, ind-1);
	    
	    
		return "<br><table border='1'> <tr> <td>Build`s name</td> <td>Build`s duration</td> <td>Result</td></tr><tr><td>"+buildName+"</td><td>"+buildDuration/1000+" секунд</td><td>"+buildResult+"</td></tr></table><br>";
	
	}
	
	//cоздает таблицу для страницы ПРОЕКТА
    public String getTestString() {
    	String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String usernName = "Alena";
        String password = "q12345";
        String apiJson = "api/json?pretty=true";
        String jenkinsUrl = protocol + "://" + host + ":" + port + "/";
        String towrite = "</div><div style='float:align'><hr><br><table border='1'> <tr> <td>Build`s name</td><td>Result</td></tr>";
        try {
			CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		    String encoding = Base64.getEncoder().encodeToString((usernName +":" + password).getBytes());
	
			//возвращает строку типа этой 
			// /job/ProjectName/
		    String zapros  = Stapler.getCurrentRequest().getRequestURI();
			String temp = zapros;
			if(temp.endsWith("/")) {temp=temp.substring(0, temp.length()-1);}
		    int ind = temp.lastIndexOf("/"); 
		   //ProjectName
		    String projectName = temp.substring(ind+1);
		    
		    zapros = zapros + "lastBuild/";
		    		
		    String responseBody = doGet(httpclient, jenkinsUrl + zapros+apiJson, encoding);	
		    ind = responseBody .indexOf("number");
		    temp = responseBody.substring(ind+10);
		    ind = temp.indexOf(",");
		    int  buildNumber = Integer.parseInt(temp.substring(0,ind));
		    String buildName = "";
		    String buildResult = "";
		    
		    int  buildCounter = 0;
		    if (buildNumber>5) buildCounter=5;
		    else buildCounter = buildNumber;
		    System.out.println("номер buildCounter "+buildCounter);
	    	for (int j = 0; j<buildCounter; j++) {
				ind = responseBody.indexOf("displayName");
			    temp = responseBody.substring(ind+16);
			    ind = temp.indexOf(",");
			    buildName = temp.substring(0, ind-1);
			    ind = responseBody .indexOf("number");
			    temp = responseBody.substring(ind+10);
			    ind = temp.indexOf(",");
			    buildNumber = Integer.parseInt(temp.substring(0,ind));
			    ind = temp.indexOf("result");
			    temp = temp.substring(ind+11);
			    ind = temp.indexOf(",");
			    buildResult = temp.substring(0, ind-1);
			    towrite += "<tr><td>"+buildName+"</td><td>"+buildResult+"</td></tr>";
			    buildNumber = buildNumber-1;
			    //http://localhost:8080/job/ProjectName/6/api/json?pretty=true
			    responseBody = doGet(httpclient, jenkinsUrl +"job/" + projectName + "/" +buildNumber+"/"+apiJson, encoding);
			}
	    	towrite+="</table><br></div><div>";
        }
	    	catch(IOException e0) {
 			   System.out.println(e0.toString());
 		 }
        makeServerInfo();
		    return towrite;
    }	
}
