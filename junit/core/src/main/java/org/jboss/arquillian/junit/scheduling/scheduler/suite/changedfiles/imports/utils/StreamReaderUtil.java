package org.jboss.arquillian.junit.scheduling.scheduler.suite.changedfiles.imports.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamReaderUtil {
	private static final int DEFAULT_BUFFERSIZE = 4096;
	
	// Writes the content to the given output stream
	public void exchange(InputStream in, OutputStream out) throws Exception {
		try {
			byte[] buffer = new byte[DEFAULT_BUFFERSIZE];
			int length = 0;
			while((length = in.read(buffer, 0, buffer.length)) != -1){
				out.write(buffer, 0, length);
			}
		} catch (Exception err) {
			throw new Exception("Error during stream exchange",err);
		}finally{
			try{
				if(in != null){
					in.close();
				}
				if(out != null){
					out.close();
				}
			}catch(Exception err){
				// Close steams quietly
			}
		}
	}
	
	public String getContentAsString(InputStream in) throws Exception{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		exchange(in, out);
			
		return out.toString();
	}
}
