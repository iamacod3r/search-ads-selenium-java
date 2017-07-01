package infrastructure;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class File {

	public File() {
		// TODO Auto-generated constructor stub
	}

	public String ReadFile(String filePath) throws IOException
	{
		FileInputStream in = null;
		String result="";
	    try {
	       in = new FileInputStream(filePath);
	       int c;
	       while ((c = in.read()) != -1) 
	       {
	          result = result + (char)c;
	       }
	    }finally {
	       if (in != null) {
	          in.close();
	       }
	    }
	    return result;
	}
	
	public void SaveFile(String data, String filePath) throws IOException
	{

		BufferedWriter output = null;
		try 
		{
			output = new BufferedWriter(new FileWriter(filePath));
			output.write(data);
		}
		finally 
		{
			if (output != null) 
			{
				output.close();
			}
		}
	}
}
