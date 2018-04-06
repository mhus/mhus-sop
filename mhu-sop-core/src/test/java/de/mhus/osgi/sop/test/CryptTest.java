package de.mhus.osgi.sop.test;

import java.io.IOException;

import de.mhus.lib.core.MString;
import de.mhus.osgi.sop.impl.aaa.util.TmpMCrypt;
import junit.framework.TestCase;

public class CryptTest extends TestCase {

	public void testEnDeDirect() throws IOException {
		System.out.println(">>> testEnDeDirect");

		String passphrase = "Lorem ipsum";
		{
			byte[] org = MString.toBytes("Hello World!");
			byte[] enc = TmpMCrypt.encode(passphrase, org);
			byte[] dec = TmpMCrypt.decode(passphrase, enc);
			assertEquals(MString.toString(org), MString.toString(dec));
		}
		{
			byte[] org = new byte[256];
			for (int i=0; i < org.length; i++)
				org[i] = (byte)(i-128);
			byte[] enc = TmpMCrypt.encode(passphrase, org);
			byte[] dec = TmpMCrypt.decode(passphrase, enc);
			for (int i=0; i < org.length; i++)
				assertEquals(org[i], dec[i]);
		}
		
	}

	public void testLongToBytes() {
		System.out.println(">>> testLongToBytes");
		{
			long org = 0;
			byte[] b = TmpMCrypt.longToBytes(org);
			long copy = TmpMCrypt.bytesToLong(b);
			assertEquals(org, copy);
		}
		{
			long org = 1;
			byte[] b = TmpMCrypt.longToBytes(org);
			long copy = TmpMCrypt.bytesToLong(b);
			assertEquals(org, copy);
		}
		{
			long org = Long.MIN_VALUE;
			byte[] b = TmpMCrypt.longToBytes(org);
			long copy = TmpMCrypt.bytesToLong(b);
			assertEquals(org, copy);
		}
		{
			long org = Long.MAX_VALUE;
			byte[] b = TmpMCrypt.longToBytes(org);
			long copy = TmpMCrypt.bytesToLong(b);
			assertEquals(org, copy);
		}
	}
}
