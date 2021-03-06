import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.spi.RepositorySelector;

public class TrackingPixel {
    static int numOfPixels = 6;
    static String reportBody="";
    
    String openedCounter; 
    String clickedCounter; 
    

    public static List<String> readFile(int number) throws FileNotFoundException, IOException {
        List<String> list = new ArrayList<>();
        String TEXT_FILE = "Pixels/TrackingPixel" + number + ".txt";

        File textFile = new File(TEXT_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(textFile));) {
            list = br.lines().collect(Collectors.toList());
        }
        return list;
    }

    public static void Init() throws FileNotFoundException, IOException {
        HashMap<String, String> viewed = new HashMap<String, String>();
        for (int j=1;j<numOfPixels+1;j++){
        List<String> pixel = new ArrayList<>();
        pixel = readFile(j);
        for (int i = 0; i < pixel.size(); i++) {
            String key = pixel.get(i);

            if (viewed.containsKey(key)) {
                viewed.put(key, viewed.get(key) + j+ ",");
            } else
                viewed.put(key, j+ ",");
            i = i + 2;
        }
    }
        printMap(viewed);
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String employee = (String) pair.getValue();
            // System.out.println(employee);
            returnEmployee(employee);
            it.remove(); // avoids a ConcurrentModificationException
        }
        
    }

    public static void returnEmployee(String employee) {
        String[] numbersarray = employee.split(",");
        List<String> numberslist = new ArrayList<>();
        numberslist = Arrays.asList(numbersarray);
        String binary = "";

        for (int i = numOfPixels; i > 0; i--) {
            String ii = i + "";
            if (numberslist.contains(ii)) {
                binary = binary + "1";
            } else
                binary = binary + "0";
        }
        int decimal = Integer.parseInt(binary, 2);
        reportBody += "Employee Number " + decimal + " has viewed the email. <br>"; 
        //System.out.println("Employee Number " + decimal + " has viewed the email.");
    }

    public static String readFiles() throws FileNotFoundException, IOException {
        Init();
        return reportBody;

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println(readFiles());
    }
}
