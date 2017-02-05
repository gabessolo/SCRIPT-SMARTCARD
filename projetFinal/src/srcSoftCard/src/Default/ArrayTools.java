package Default;

/* Author : Romain Pignard */

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class ArrayTools {	
	
	
	
	public static void write_file(byte[] input, String path) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(path);
	    
	    fos.write(input);
	    fos.close();     
		
		
	}
	/**
	* @author Romain Pignard
	* @param input the byte array to be printed
	*/
	public static void printHex(byte[] input)
	{
		
		for(int i = 0; i< input.length-1;i=i+1 )
		{
			System.out.print(String.format("%02X", input[i] & 0xFF));
			//System.out.print(String.format("%X", input[i+1] & 0xFF));
			
		}	
		System.out.print(String.format("%02X", input[input.length-1] & 0xFF));
		
	}
	
	
	
	
	/**
	*  print an array of bytes
	*  @param array array to be printed
	*/
	public static void printByteArray(byte[] array)
	{
		
		for(int i = 0; i< array.length;i++ )
		{
			System.out.print(array[i]+" ");
		}
		System.out.println();
		
	}
	/**
	*  print an array of bytes with max values on each line
	*  @param array array to be printed
	*  @param max max number of values on a line
	*/
	public static void printByteArray(byte[] array, short max)
	{
		
		byte[][] splitted = split(array,max);
		
		for(int i = 0; i< splitted.length;i++ )
		{
			for (int j = 0; j < splitted[i].length; j++) 
			{
				System.out.print((splitted[i][j]  & 0xFF)+" ");
			}System.out.println();
			
		}
		
		
	}
	public static void fuzz_Array(byte[] input)
	{
		Random rng = new Random();
		input[rng.nextInt(input.length)]++; 	
	}
	
	
	public static boolean equals(byte [] array1, byte[] array2, short off2, short length)
	{
		// check if 2 arrays with different offsets are equals.
		// the first one starts at 0, the other starts at off2
		boolean verif = true;
		for(int i=0; i< length;i++)
		{
			verif = verif & (array1[i] == array2[off2 + i]);
		}		
		return verif;		
	}
	
	
	public static boolean verif_padd(byte[] mess, short blockSize)
	{
		// check the padding
		short padding = mess[mess.length- 1];
		for(int i = mess.length - padding; i< mess.length;i++ )
		{
			if(mess[i] != padding)
			{return false;}	
		}
		return true;		
	}

	
	public static byte[] pad(byte[] mess, short blockSize)
	{
		// return the padded version of mess according to pkcs7
		
		byte[] padded = new byte[mess.length + blockSize -  (mess.length % blockSize)];
		//copy of the original message into padded 
		for(int i =0; i < mess.length; i++)
		{
			padded[i] = mess[i];
		}
		//padding of the message according to pkcs7
		if(mess.length % blockSize == 0)
		{	
			//if the last block is full, we create another full block 
			for(int i = mess.length; i < mess.length +  blockSize ; i++)
			{
				padded[i] = (byte)  blockSize;
			}
		}
		else
		{
			//we fill the last block with the required number of bytes
			for(int i =mess.length; i < mess.length + blockSize -  (mess.length % blockSize); i++)
			{
				
				padded[i] = (byte) (blockSize - mess.length  % blockSize);
			}			
		}
		return padded;
		
	}	
	
	public static byte[] extractMAC(byte[] msg)
	{
		// extract the MAC from the message 
		byte[] MAC = new byte[CryptoTools.MAC_LENGTH];
		System.out.println("lg = " + msg.length);
		System.arraycopy(msg, msg.length - CryptoTools.MAC_LENGTH, MAC, 0, CryptoTools.MAC_LENGTH);
		return MAC;		
		
	}
	
	
	
	public static byte[] unpad(byte[] pad, short blockSize)
	{
				
		// remove the padding
		byte[] mess = new byte[pad.length - pad[pad.length-1]];
		
		for(int i =0; i < mess.length; i++)
		{
			mess[i] = pad[i];
		}		
		return mess;
	}
	

	
	public static byte[] ExtractLastBytes(byte[] buff, short lg)
	{
		// return the  last <lg> bytes of the input 
		byte[] output = new byte[lg];		
		System.arraycopy(buff, buff.length - lg, output, 0, lg);
		return output;			
	}
	
	/**
	 * 
	 * @param buff
	 * @param lg
	 * @return
	 */
	public static byte[] ExtractFirstBytes(byte[] buff, short lg)
	{
		// return the  first <lg> bytes of the input 
		byte[] output = new byte[lg];		
		System.arraycopy(buff, 0, output, 0, lg);
		return output;			
	}
	
	/**
	 * Generates a random array of specified length 
	 * @param lg size of the array
	 * @return a random byte[] array of length lg
	 */
	public static  byte[] RandomArray(short lg)
	{
		// return an array with <lg> random bytes
		
		Random rng = new Random();
	    byte[] iv1 = new byte[lg];
	    rng.nextBytes(iv1);	    
		return iv1;		
	}
	
	/**
	 * concatenates two arrays of the same type
	 * @param A first array
	 * @param B second array
	 * @return an array C such as C = A | B
	 */
	
	public static byte[] concat(byte[] A, byte[] B) 
	{
		// concatenate two arrays   
		int aLen = A.length;
		int bLen = B.length;
		byte[] C= new byte[aLen+bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}
	/**
	 * Splits an array into an array of arrays
	 * @param source the array to be split
	 * @param max_length the maximum length of the array
	 * @return an array of arrays 
	 */
	public static byte[][] split(byte[] source, short max_length)
	{
		// split an array into an array of arrays with 
		// at most <max_length> elements by array.
		int nb_arrays =  1 + (source.length / max_length); 
		int length_last_array = (source.length % max_length);
		
		byte[][] output = new byte[nb_arrays][];
		int i;
		for(i = 0;i<nb_arrays-1;i++)
		{
			output[i] = new byte[max_length];
			System.arraycopy(source,i*max_length, output[i],0,max_length);
		}
		output[i] = new byte[length_last_array];
		System.arraycopy(source,i*max_length, output[i],0,length_last_array);
		
		return output;	
	}
	public static byte[] add_size(byte[] data, short size_length)
	{
		byte[] output = new byte[data.length + size_length];
		output[0] =  (byte) (data.length % 256);
		output[1] =  (byte) (data.length / 256);
		
		
		System.arraycopy(data, 0, output, size_length, data.length);		
		return output;
				
	}
		
}
