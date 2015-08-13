package be.uantwerpen.ProgramCombiner;
import java.io.*;


public class main {
	
	 public static void main(String[] args)
	 {
		 InputStream in;
		 String name = "fft1";
		try {
			in = new FileInputStream(new File("./fft1.c"));
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.contains("int main"))
            {
            	line = "int main_"+name+"(void)";
            }
            out.append(line+"\n");
        }
        System.out.println(out.toString());   //Prints the string content read from input stream
        reader.close();
        
        File file = new File("./fft1.c");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(out.toString());
        }		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 
	 }

}
