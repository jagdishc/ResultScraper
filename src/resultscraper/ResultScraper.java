package resultscraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ResultScraper 
{
    Map<String, HashMap<String, String>> grades;
    Map<String, String> names;
    public ResultScraper()
    {
        grades = new HashMap<String, HashMap<String, String>>();
        names = new HashMap<String, String>();
    }
    
    public void doScrape(int reg)
    {       
        String regno = Integer.toString(reg);
        try
        {
            HashMap<String, String> temp = new HashMap<String,String>();
            
            String data = "regnum="+reg+"&ressetid=3";            
            URL url = new URL("http://www.annatech.ac.in/results/");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
           
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line, page="";
            while ((line = rd.readLine()) != null) 
            {
                page += line;                
            }
            wr.close();
            rd.close();
            Document doc = Jsoup.parse(page);
            Elements table;
            table = doc.select("table");
            int i = 0;
            //System.out.println("Parsing");
            for(Element result : table)
            {               
               if(i==2)
               {
                   Element ele = result.child(0);    
                   String word,nam = "",txt = ele.text();
                   System.out.println(txt);
                   StringTokenizer tokens = new StringTokenizer(txt,": ");
                   while(tokens.hasMoreTokens())
                   {
                       word = tokens.nextToken();
                       if(word.equals("Name"))
                       {
                           while(!(word = tokens.nextToken()).equals("Degree"))
                           {
                               nam += word + " ";
                           }
                           break;
                       }
                   }
                   System.out.println(nam);
                   names.put(regno, nam);
               }
               else if(i==3)
               {
                   Element ele = result.child(0);
                   int len = ele.children().size();
                   for(int j=1;j<len;j++)
                   {
                       Element res = ele.child(j);
                       System.out.println(reg + "-" + res.child(1).text() + " - " + res.child(3).text());
                       temp.put(res.child(1).text(), res.child(3).text());
                       grades.put(regno, temp);
                   }
               }
               i+=1;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void writeToFile()
    {
        List <String>subjects = new ArrayList();
        File sub = new File("subjects.txt");        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sub));
            String line;
            while((line = br.readLine())!=null)
            {
                subjects.add(line.trim());
            }
            File file = new File("result-3rdsem.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("RegNo,Name,181301,141301,141302,141303,141304,185301,141351,141352,141353,186202,181202,182202,183202,147201,185204,185253,184252,147251,186101,181101,182101,183101,185101,185102,185151,185152,184252");
            bw.flush();
            bw.write("\n");
            bw.flush();            
            for(Iterator<String> it = grades.keySet().iterator();it.hasNext();)
            {
                
                String tempreg = it.next();
                HashMap<String, String> temp = grades.get(tempreg);
                bw.write(tempreg+","+names.get(tempreg)+",");
                bw.flush();
                for(Iterator<String> it2 = subjects.iterator();it2.hasNext();)
                {
                    String subject = it2.next();
                    String grade = temp.get(subject);
                    if(grade!=null)
                    {
                        bw.write(grade+",");
                        bw.flush();
                    }
                    else
                    {
                        bw.write("-,");
                        bw.flush();
                    }
                }
                bw.write("\n");
                bw.flush();
            }
            bw.close();           
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) 
    {
        ResultScraper rs = new ResultScraper();
        for(int i=1071726;i<=1071847;i++)
        {
            rs.doScrape(i);
        }
        for(int i=1104553;i<=1104574;i++)
        {
             rs.doScrape(i);
        }
        rs.doScrape(1014712);
        rs.writeToFile();
    }
}
