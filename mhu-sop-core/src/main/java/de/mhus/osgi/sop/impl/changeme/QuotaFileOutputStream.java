package de.mhus.osgi.sop.impl.changeme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// in mhu-lib 3.3.4 de.mhu.lib.core.io
public class QuotaFileOutputStream extends FileOutputStream {

	private File file;
	private long quota;

	public QuotaFileOutputStream(File file, Long quota) throws FileNotFoundException {
		super(file);
		this.file = file;
		this.quota = quota;
	}

    @Override
	public void write(int b) throws IOException {
		if (file.length()+1 > quota )
			throw new IOException("maximum file size reached " + quota);
    	super.write(b);
    }
    @Override
	public void write(byte b[]) throws IOException {
		if (file.length()+b.length > quota )
			throw new IOException("maximum file size reached " + quota);
    	super.write(b);
    }
    @Override
	public void write(byte b[], int off, int len) throws IOException {
		if (file.length()+len > quota )
			throw new IOException("maximum file size reached " + quota);
    	super.write(b, off, len);
    }
}
