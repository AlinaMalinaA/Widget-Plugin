package org.widgetplugin;

import hudson.util.RunList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.http.client.ClientProtocolException;
import org.kohsuke.stapler.Stapler;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;

//На уровне сервера  список джобов которые чаще всего ломаются 
//то есть получение всех сломанных билдов, сортировка по имени джобы, взять первые три джобы (или джобы с поломами больше двух)
//На уровне джоба таблица из пяти сборок две колонки имя и результат
//получение последних пяти сборок
//На уровне билда время выполнения и результат

public final class WidgetAction implements Action {

	static int NUMBER_OF_BUILDS_TO_CHECK_TO_DISPLAY_THE_NUMBER_OF_FAILS_ON_MAIN_PAGE = 10;
	static int NUMBER_OF_BUILDS_TO_DISPLAY_ON_JOB_PAGE = 5;
	
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
    
    
    //извлекает следующую информацию из ответа сервера: 
    //имя проекта, содержащего неудачные сборки
    //и количество неудачных сборок
    public static void makeServerInfo() {
    	System.out.println("========================");
    	//карта для хранения имени проекта и количества неудачных сборок
    	HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
    	
    	Hudson hudson = Hudson.getInstance();
    	
    	List<Item> allJobs = hudson.getAllItems();
    	System.out.println("0)  "+allJobs.size());
    	Job<?, ?> job = null;
    	int count = 0;
    	int M = 0;
    	for (int i = 0; i < allJobs.size(); i++) {
    		job = (Job<?, ?>) allJobs.get(i);
    		System.out.println(i+")  "+job.getName());
    		try {
    			System.out.println("number last fail "+job.getLastFailedBuild().getNumber());
        		if (job.getLastFailedBuild().getNumber()>NUMBER_OF_BUILDS_TO_CHECK_TO_DISPLAY_THE_NUMBER_OF_FAILS_ON_MAIN_PAGE) {M=job.getLastFailedBuild().getNumber()-NUMBER_OF_BUILDS_TO_CHECK_TO_DISPLAY_THE_NUMBER_OF_FAILS_ON_MAIN_PAGE;}
        		else {M=1;}
        		for (int j = M; j<job.getLastFailedBuild().getNumber(); j++){
        			System.out.println("result "+job.getBuildByNumber(j).getResult());
        			if (job.getBuildByNumber(j).getResult().toString().trim().equals("FAILURE")) {
        				count+=1;
        				System.out.println(j+".  "+count);
        			}
        			
        		}
        		hashMap.put(job.getName(), count);
        		count = 0;
    		
    		} catch (NullPointerException e) {
    			System.out.println("Exeption "+e+": "+e.getMessage());
	    	}
    	}
    	 try {
			createServerFile(hashMap);
		} catch (IOException e) {
			System.out.println("Exeption "+e+": "+e.getMessage());
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
	

	//логика:
		//получаем актуальный запрос
		//в нем смотрим название джобы и номер билда
		//из списка всех джоб находим запрошенную
		//(потому что я не знаю, как получить объект Job другим способом)
		//для нее запрашиваем информацию о конкретном билде
	
	//создает таблицу для страницы БИЛДА
	public static String getBuildInfo() throws ClientProtocolException, IOException{
		
		//возвращает /job/Application%20framework/19/
		String reqURL = Stapler.getCurrentRequest().getRequestURI();
		reqURL = reqURL.substring(0, reqURL.length()-1);//убираем последний слэш /job/Application%20framework/19
		String buildNumber = reqURL.substring(reqURL.lastIndexOf("/")+1);//19
		reqURL = reqURL.substring(0, reqURL.lastIndexOf("/"));//убираем после последнего слэша /job/Application%20framework
		String jobname = reqURL.substring(reqURL.lastIndexOf("/")+1);//убираем последний слэш Application%20framework

    	while (jobname.contains("%20")){
    		jobname = jobname.replace("%20", " ");//замена символов пробела на пробел
    	}
		
    	Hudson hudson = Hudson.getInstance();
    	
    	List<Item> allJobs = hudson.getAllItems();//список всех джоб
    	Job<?, ?> job = null;
    	for (int i = 0; i < allJobs.size(); i++) {
    		if (allJobs.get(i).getDisplayName().trim().equals(jobname.trim())){
    			job = (Job<?, ?>) allJobs.get(i);
    		}
    	}
    	if (job == null) {return "There is no such job" ; }
    	else {
	    	AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) job.getBuildByNumber(Integer.parseInt(buildNumber));
			
	    	makeServerInfo();
	    	return "<br><table border='1'> <tr> <td>Build`s name</td> <td>Build`s duration</td> <td>Result</td></tr><tr><td>"+build.getNumber()+"</td><td>"+ String.format("%.3f", (double) build.getDuration() * 0.001)  +" секунд</td><td>"+build.getResult()+"</td></tr></table><br>";
    	}
	
	}
	
	//логика:
	//получаем актуальный запрос
	//в нем смотрим название джобы
	//из списка всех джоб находим запрошенную
	//(потому что я не знаю, как получить объект Job другим способом)
	//для нее запрашиваем информацию о ее билдах
	
	//cоздает таблицу для страницы ДЖОБЫ
    public String getJobInfo() {
    	
    	//получаем имя запрошенной джобы
    	String reqURL = Stapler.getCurrentRequest().getRequestURI();//возвращает /job/Application%20framework/
    	String jobname = reqURL.substring(0, reqURL.length()-1);//убираем последний слэш
    	jobname = jobname.substring(jobname.lastIndexOf("/")+1);
    	while (jobname.contains("%20")){
    		jobname = jobname.replace("%20", " ");//замена символов пробела на пробел
    	}
    	Hudson hudson = Hudson.getInstance();
    	
    	List<Item> allJobs = hudson.getAllItems();
    	Job<?, ?> job = null;
    	for (int i = 0; i < allJobs.size(); i++) {
    		if (allJobs.get(i).getDisplayName().trim().equals(jobname.trim())){
    			job = (Job<?, ?>) allJobs.get(i);
    		}
    	}
    	if (job == null) {return "There is no such job"; }
    	else {
    	
	    	String towrite = "</div><div style='float:align'><br><table border='1'> <tr> <td>Build`s name</td><td>Result</td></tr>";
	        
	    	RunList<?> e = job.getBuilds();
	    	int N;//константа количества отображаемых сборок
	    	if (e.size()<NUMBER_OF_BUILDS_TO_DISPLAY_ON_JOB_PAGE) {N=e.size();}
	    	else {N=NUMBER_OF_BUILDS_TO_DISPLAY_ON_JOB_PAGE;}
	    	
	    	for (int i = 0; i < N; i++) {
	    		towrite += "<tr><td>"+e.get(i).getNumber()+"</td><td>"+e.get(i).getResult()+"</td></tr>";
	    	}
	    	towrite+="</table><br></div><div>";
	    	
	        makeServerInfo();
			return towrite;
    	}
    }	
}
