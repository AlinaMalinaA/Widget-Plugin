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

//На уровне сервера  список джобов которые чаще всего ломаются 
//то есть получение всех сломанных билдов, сортировка по имени джобы, взять первые три джобы (или джобы с поломами больше двух)
//На уровне джоба таблица из пяти сборок две колонки имя и результат
//получение последних пяти сборок
//На уровне билда время выполнения и результат

public final class WidgetAction implements Action {

	private static final String USERNAME = "Alena"; //логин
	private static final String PASSWORD = "b96bebd02ee08eb7c7f5b51e2d154baf";//сюда вставлять свой токен
	private static String HOST = "";//
	
	//возвращает имя плагина
    public String getDisplayName() {
        return "Widget plugin";
    }

    //должна возвращать иконку плагина, но ее нет
    public String getIconFileName() {
        return null;
    }

    //возвращает юрл плагина
    public String getUrlName() {
        return "widgetplugin";
    }
    
    
    
    
    //делает запрос на сервер
    //принимает часть юрл-строки после локального адреса дженкинс для обращения по ней
    //возвращает тело ответа
    public static String doGet(String URL) throws ClientProtocolException, IOException {
    	CloseableHttpClient httpclient = HttpClientBuilder.create().build();
	    String encoding = Base64.getEncoder().encodeToString((USERNAME +":" + PASSWORD).getBytes());
    	
    	HttpGet httpget = new HttpGet(HOST+URL);
    	httpget.setHeader("Authorization", "Basic " + encoding);

    	String response = EntityUtils.toString(httpclient.execute(httpget).getEntity());

    	httpclient.close();
    	
    	return response;
    }
    

    //извлекает следующую информацию из ответа сервера: 
    //имя проекта, содержащего неудачные сборки
    //и количество неудачных сборок
    public static void makeServerInfo() {
    	//карта для хранения имени проекта и количества неудачных сборок
    	HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
	    String responseBody;
	    String Name;
		try {
			responseBody = doGet("rssFailed");
			//String temp = responseBody;
		    int ind = responseBody.indexOf("y><title"); 
		    int count2 = StringUtils.countMatches(responseBody, "y><title>");//количество вхождений 
		    for (int i = 0; i<count2; i++) {
		    	responseBody = responseBody.substring(ind+9); 
			    ind = responseBody.indexOf("#"); 
			    Name = responseBody.substring(0, ind).trim();
			    if (hashMap.containsKey(Name)){
			    	hashMap.put(Name, hashMap.get(Name)+1);
			    }
			    else {
			    	hashMap.put(Name, 1);
			    }
			    
			    responseBody = responseBody.substring(ind+1); 
			    ind = responseBody.indexOf(" "); 
			    ind = responseBody.indexOf("y><title"); 
		    }
		    //передает карту методу, который сформирует из нее таблицу для отображения на главной странице
		    createServerFile(hashMap);
		    
	    	
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
    	
    }

    //создает таблицу для ГЛАВНОЙ страницы
	public static void createServerFile(HashMap<String, Integer> hashMap) throws IOException {
		String HOME = System.getProperty("user.dir");
		String jenkinsUserContent =  File.separator + "userContent";
		String nameForGeneralPage = "banner.html";//имя файла 
		File file = new File(HOME + jenkinsUserContent + File.separator + nameForGeneralPage);
		file.createNewFile();
    	PrintWriter writr = new PrintWriter(file, "UTF-8");
    	Date date = new Date(); //возвращает актуальную дату
    	//формирует ХТМЛ-таблицу
    	writr.append(" Актуально на "+date.toString());
    	writr.append("<table border='1'> <tr> <td>Job`s name</td> <td>Number of fails</td></tr>");
    		for (Entry<String, Integer> entry : hashMap.entrySet()) {
    		    writr.append("<tr><td>" + entry.getKey() + "</td>");
    		    writr.append("<td>" + entry.getValue()+"</td></tr>");
    		}
    		writr.append("</table>");
    	    writr.close();
	}
	

	//создает таблицу для страницы БИЛДА
	public static String getBuildInfo() throws ClientProtocolException, IOException{
		String apiJson = "api/json?pretty=true";
        String temp = "";
        String buildInf = doGet(Stapler.getCurrentRequest().getRequestURI() + apiJson);
		//Stapler.getCurrentRequest().getRequestURI();
		//возвращает строку типа этой 
		// /job/ProjectName/5/
		temp = Stapler.getCurrentRequest().getRequestURI();
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
	    makeServerInfo();
	    
		return "<br><table border='1'> <tr> <td>Build`s name</td> <td>Build`s duration</td> <td>Result</td></tr><tr><td>"+buildName+"</td><td>"+buildDuration/1000+" секунд</td><td>"+buildResult+"</td></tr></table><br>";
	}
	
	
	//cоздает таблицу для страницы ПРОЕКТА
    public String getJobInfo() {
    	//его надо именно тут инициализировать
    	HOST = Stapler.getCurrentRequest().getRootPath()+"/";
    	    	
    	String apiJson = "api/json?pretty=true";
        String towrite = "</div><div style='float:align'><br><table border='1'> <tr> <td>Build`s name</td><td>Result</td></tr>";
        try {
			
			//возвращает строку типа этой 
			// /job/ProjectName/
		    String zapros  = Stapler.getCurrentRequest().getRequestURI();
			String temp = zapros;
			if(temp.endsWith("/")) {temp=temp.substring(0, temp.length()-1);}
		    int ind = temp.lastIndexOf("/"); 
		   //ProjectName
		    String projectName = temp.substring(ind+1);
		    
		    zapros = zapros + "lastBuild/";
		    		
		    String responseBody = doGet(zapros+apiJson);	
		    ind = responseBody .indexOf("number");
		    temp = responseBody.substring(ind+10);
		    ind = temp.indexOf(",");
		    int  buildNumber = Integer.parseInt(temp.substring(0,ind));
		    String buildName = "";
		    String buildResult = "";
		    
		    int  buildCounter = 0;
		    if (buildNumber>5) buildCounter=5;
		    else buildCounter = buildNumber;
		    //System.out.println("номер buildCounter "+buildCounter);
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
			    responseBody = doGet("job/" + projectName + "/" +buildNumber+"/"+apiJson);
			}
	    	towrite+="</table><br></div><div>";
        }
	    	catch(IOException e0) {
 			   System.out.println(e0.toString());
 			  return e0.getCause().toString();
 		 }
        makeServerInfo();
		    return towrite;
    }	
    
}
