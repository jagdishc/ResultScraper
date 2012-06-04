package resultscraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Regulation2008 implements Serializable{
    HashMap<String,HashMap<String,String>> results;
    HashMap<String,Integer> subjects;
    HashMap<String, Integer> otherSemSubjects;
    HashMap<String,String> corpus;
    BigInteger startNumber;
    BigInteger endNumber;
    String url;
    public Regulation2008(){
        results = new HashMap<String,HashMap<String,String>>();
        subjects = new HashMap<String,Integer>();
        otherSemSubjects = new HashMap<String, Integer>();
        corpus = new HashMap<String,String>();
        startNumber = BigInteger.valueOf(0);
        endNumber = BigInteger.valueOf(0);
        //url = "http://result.annauniv.edu/cgi-bin/result/firstsemgr.pl";
        url = "http://result.annauniv.edu/cgi-bin/result/result11gr.pl?regno=";
    }
    public Regulation2008(HashMap<String,HashMap<String,String>>res,HashMap<String,Integer>sub,HashMap<String,String>cor,BigInteger s,BigInteger e,String u){
        results = res;
        subjects = sub;
        corpus = cor;
        startNumber = s;
        endNumber = e;
        url = u;
    }
    public String toString(){
        return "results: "+results+"\nsubjects: "+subjects+"\ncorpus: "+corpus+"\nstart: "+startNumber+"\nend: "+endNumber+"\nurl: "+url;
    }
    public void crawl(String no){
        try{
            //String data = "regno="+no;
            String data = no;
            URL link = new URL(url+data);
            //URLConnection conn = link.openConnection();
            //conn.setDoOutput(true);
            //conn.setDoInput(true);
            //OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            //wr.write(data);
            //wr.flush();
            //BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(link.openStream()));
            String line, html = "";
            while((line = br.readLine())!=null){
                html += line;
            }
            if(!corpus.containsKey(no)){
                corpus.put(no, html);
            }
            br.close();
            scrape(html, no);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void scrape(String html, String no){
        try{
            //html = Jsoup.clean(html, null);
            Document doc = Jsoup.parse(html);
            Elements table = doc.select("table");
            int i = 0;           
            HashMap<String, String> temp = new HashMap<String, String>();
            for(Element resultTable : table){
                if(i == 0){
                    Element ele = resultTable.child(0);                    
                    Elements grades = ele.children();
                    int j = 0;
                    for(Element element : grades){
                        if(j == 0){
                            String name = element.child(3).text();
                            if(!temp.containsKey("Name")){
                                temp.put("Name", name);
                            }
                        }else if(j == 1){
                            j += 1;
                            continue;
                        }else {
                            String code = element.child(0).text().trim();
                            if((!subjects.containsKey(code)) && (!otherSemSubjects.containsKey(code))){
                                int credit = Integer.parseInt(element.child(1).text().trim());
                                otherSemSubjects.put(code, credit);
                            }
                            String grade = element.child(2).text().trim();
                            if(!temp.containsKey(code)){
                                temp.put(code, grade);
                            }
                        }
                        j += 1;
                    }
                    if(!results.containsKey(no)){
                        results.put(no, temp);
                    }
                }
                break;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void printCurrentSem(){
        try{            
            HashMap<String,String> result;
            File file = new File("Result7thsem.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            String subjectscsv = "Regno/subjects,Name,";
            for(Iterator<String>iter = subjects.keySet().iterator();iter.hasNext();){
                String sub = iter.next();
                subjectscsv += sub+",";
            }
            bw.write(subjectscsv);
            bw.write("\n");
            bw.flush();
            BigInteger incr = BigInteger.valueOf(1);  
            for( BigInteger counter = startNumber;;counter = counter.add(incr)){
                if((endNumber.compareTo(counter) == -1)){
                    break;
                }
            String no = counter.toString();
            result = results.get(no);
            String txtToPrint = no+",";
            String name = result.get("Name");
            txtToPrint += name+",";
            for(Iterator<String>iter = subjects.keySet().iterator();iter.hasNext();){
                String code = iter.next();
                String res = result.get(code);
                txtToPrint += res+",";                                    
            }
            bw.write(txtToPrint+"\n");
            bw.flush();             
            }
            bw.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void printOtherSems(){
        try{
            HashMap<String,String> result;
            ArrayList<String> order = new ArrayList<String>();
            File file = new File("Result7_other_sem.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            String subjectscsv = "Regno/subjects,Name,";
            for(Iterator<String>iter = otherSemSubjects.keySet().iterator();iter.hasNext();){
                String sub = iter.next();
                order.add(sub);
                subjectscsv += sub+",";
            }
            bw.write(subjectscsv);
            bw.write("\n");
            bw.flush();            
            BigInteger incr = BigInteger.valueOf(1);  
            for( BigInteger counter = startNumber;;counter = counter.add(incr)){
                if((endNumber.compareTo(counter) == -1)){
                    break;
                }
            String no = counter.toString();
            result = results.get(no);
            String txtToPrint = no+",";
            String name = result.get("Name");
            txtToPrint += name+",";
            for(Iterator<String>iter = otherSemSubjects.keySet().iterator();iter.hasNext();){
                String code = iter.next();                
                String res = result.get(code);
                txtToPrint += res+",";                                  
            }
            bw.write(txtToPrint+"\n");
            bw.flush();             
            }
            bw.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void getResults(){
        BigInteger incr = BigInteger.valueOf(1);       
        for( BigInteger counter = startNumber;;counter = counter.add(incr)){
            if((endNumber.compareTo(counter) == -1)){
                break;
            }
            System.out.println(counter);
            String no = counter.toString();
            crawl(no);            
        }
       printCurrentSem();       
       printOtherSems(); 
        try{
            FileOutputStream fos = new FileOutputStream(new File("data.dat"));
            ObjectOutputStream obj = new ObjectOutputStream(fos);
            obj.writeObject(this);
            obj.flush();
            obj.close();            
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
