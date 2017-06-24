package com.fxdms.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
/**
 * 
 * @author Thor Hsu
 * @version 1.0
 * 2008/01/22
 */

public class LockFile {
	public static boolean checkFileIsReady(String path){
		return checkFileIsReady(new File(path));
	}
	public static boolean checkFileIsReady(File file){		
		FileChannel channel = null;
		FileLock lock = null;
	    try {
			channel = new RandomAccessFile(file, "rw").getChannel();		    
			lock = channel.tryLock();
			return true;
		} catch (FileNotFoundException e) {
		    return false;	
		} catch (IOException e) {
			return false;	
		} finally{
			try {
				if (lock != null) 
					lock.release();
				if (channel != null)
					channel.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}finally{
				lock = null;
				channel = null;
			}
			
		}
	}

}
