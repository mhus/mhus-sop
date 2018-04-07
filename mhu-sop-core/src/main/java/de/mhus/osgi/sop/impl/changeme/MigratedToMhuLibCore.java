package de.mhus.osgi.sop.impl.changeme;

import org.apache.activemq.util.ByteArrayOutputStream;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MMath;
import de.mhus.lib.core.crypt.MRandom;

public class MigratedToMhuLibCore {

	private static final int MAX_SPACE = 10;

	// in mhu-lib 3.3.4 MCrypt
	public static byte[] encode(String passphrase, byte[] in) {
		@SuppressWarnings("resource")
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] pp = passphrase.getBytes();
		int ppPos = 0;
		MRandom random = MApi.lookup(MRandom.class);
		byte salt = random.getByte();
		
		// save salt
		byte o = MMath.addRotate(salt, pp[ppPos]);
		ppPos = (ppPos+1) % pp.length;
		out.write(o);
		
		for (int pos = 0; pos < in.length; pos++) {
			byte space = (byte) (random.getInt() % MAX_SPACE);
			// save space
			o = MMath.addRotate(space, pp[ppPos]);
			o = MMath.addRotate(o, salt);
			ppPos = (ppPos+1) % pp.length;
			out.write(o);
			// fill space
			for (int j = 0; j < space; j++)
				out.write(random.getByte());
			// write one byte
			o = MMath.addRotate(in[pos], pp[ppPos]);
			o = MMath.addRotate(o, salt);
			ppPos = (ppPos+1) % pp.length;
			out.write(o);
		}
		// one more trailing space
		byte space = (byte) (random.getInt() % MAX_SPACE);
		// save space
		o = MMath.addRotate(space, pp[ppPos]);
		o = MMath.addRotate(o, salt);
		ppPos = (ppPos+1) % pp.length;
		out.write(o);
		// fill space
		for (int j = 0; j < space; j++)
			out.write(random.getByte());

		return out.toByteArray();
	}

	// in mhu-lib 3.3.4 MCrypt
	public static byte[] decode(String passphrase, byte[] in) {
		@SuppressWarnings("resource")
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] pp = passphrase.getBytes();
		int ppPos = 0;
		
		// read salt
		byte salt = MMath.subRotate(in[0], pp[ppPos]);
		ppPos = (ppPos+1) % pp.length;
		
		int mode = 0;
		byte space = 0;
		for (int pos = 1; pos < in.length; pos++) {
			if (mode == 0) {
				// read space length
				byte o = MMath.subRotate(in[pos], salt);
				space = MMath.subRotate(o, pp[ppPos]);
				ppPos = (ppPos+1) % pp.length;
				if (space == 0)
					mode = 2;
				else
					mode = 1;
			} else
			if (mode == 1) {
				space--;
				if (space <= 0)
					mode = 2;
			} else
			if (mode == 2) {
				byte o = MMath.subRotate(in[pos], salt);
				o = MMath.subRotate(o, pp[ppPos]);
				ppPos = (ppPos+1) % pp.length;
				out.write(o);
				mode = 0;
			}
		}
		
		return out.toByteArray();
	}

	// in mhu-lib 3.3.4 MCast
	public static byte[] longToBytes(long l) {
	    byte[] result = new byte[8];
	    for (int i = 7; i >= 0; i--) {
	        result[i] = (byte)(l & 0xFF);
	        l >>= 8;
	    }
	    return result;
	}

	// in mhu-lib 3.3.4 MCast
	public static long bytesToLong(byte[] b) {
	    long result = 0;
	    for (int i = 0; i < 8; i++) {
	        result <<= 8;
	        result |= (b[i] & 0xFF);
	    }
	    return result;
	}
	
}
