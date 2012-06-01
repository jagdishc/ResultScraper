package resultscraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Regulation2008 {
    HashMap<String,HashMap<String,String>> results;
    HashMap<String,Boolean> subjects;
    HashMap<String,String> corpus;
    BigInteger startNumber;
    BigInteger endNumber;
    String url;
    public Regulation2008(){
        results = new HashMap<String,HashMap<String,String>>();
        subjects = new HashMap<String,Boolean>();
        corpus = new HashMap<String,String>();
        startNumber = BigInteger.valueOf(0);
        endNumber = BigInteger.valueOf(0);
        url = "result.annauniv.edu/cgi-bin/result/result12gr.pl?regno=";
    }
    public void crawl(String no){
        try{
            url += no;
            URL link = new URL(url);
            link.openConnection();
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
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void printCurrentSem(){
        try{
            HashMap<String,String> result;
            File file = new File("Result8thsem.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            String subjectscsv = "Regno/subjects,";
            for(Iterator<String>iter = subjects.keySet().iterator();iter.hasNext();){
                String sub = iter.next();
                subjectscsv += sub+",";
            }
            bw.write(subjectscsv);
            bw.write("\n");
            bw.flush();
            for(Iterator<String>it = results.keySet().iterator();it.hasNext();){
                String reg = it.next();
                result = results.get(reg);
                String txtToPrint = reg+",";
                for(Iterator<String>iter = result.keySet().iterator();iter.hasNext();){
                    String code = iter.next();
                    if(subjects.containsKey(code)){
                        String res = result.get(code);
                        txtToPrint += res+",";                       
                    }                    
                }
                bw.write(txtToPrint+"\n");
                bw.flush();
                bw.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void printOtherSems(){
        try{
            HashMap<String,String> result;
            File file = new File("Result8_other_sem.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("Regno,Subject,Grade\n");
            bw.flush();
            
            for(Iterator<String>it = results.keySet().iterator();it.hasNext();){
                String reg = it.next();
                result = results.get(reg);                
                for(Iterator<String>iter = result.keySet().iterator();iter.hasNext();){
                    String txtToPrint = reg+",";
                    String code = iter.next();
                    if(!subjects.containsKey(code)){
                        String res = result.get(code);
                        txtToPrint += code+","+res+"\n";
                        bw.write(txtToPrint);
                    }
                }
                bw.flush();
            }
            bw.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void getResults(){
        BigInteger incr = BigInteger.valueOf(1);       
        for( BigInteger counter = startNumber;;counter.add(incr)){
            if((endNumber.compareTo(counter) == -1)){
                break;
            }
            String no = counter.toString();
            crawl(no);            
        }
       printCurrentSem();
       printOtherSems();
    }
}
