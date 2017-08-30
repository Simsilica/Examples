/*
 * Written by: Austin Barton 
 *
 * Currently handles :
 *	1 bit 	no compression
 *	4 bit	no compression
 *	8 bit   no compression
 *	8 bit	RLE compression
 *
 *
 * [File Header] 14 bytes
 * offset	length		info
 *-------------------------------------------
 *	0		2			Type of file "BM"
 *	2		4			Total file size
 *	6		2			Reserved
 *	8		2			Reserved
 *	10		4			Bitmap offset
 *
 * [Info Header] 40 bytes
 * offset	length		info
 *-------------------------------------------
 *	14		4			Bitmap info size
 *	18		4			Width
 *	22		4			Height	
 *	26		2			Bitplane size
 *	28		2			Bit count
 *	30		4			Compression type
 *	34		4			Image size
 *	38		4			Pixels per meter
 *	42		4			Pixels per meter
 *	46		4			Colors used
 *	50		4			Colors important
 *
 * [Color Table] for( i = 0; i < colors; i++ )
 *  4*i+0 	1			Red
 *	4*i+1	1			Green
 *	4*i+2	1			Blue
 *	4*i+3	1			Not used
 *	
 */	

package example.map;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class BitMap extends JPanel {
	
	public final static int BI_RGB = 0;  		//No compression
	public final static int BI_RLE8 = 1; 		//RLE 8-bit / pixel 
	public final static int BI_RLE4 = 2; 		//RLE 4-bit / pixel 
	public final static int BI_BITFIELDS = 3;	//Bitfields
	
	private BufferedInputStream m_stream;
	
	private byte[]	fileHeader;
	private byte[]  infoHeader;
	private ByteArray fileData;
	
	private String 	fh_type;
	private int	   	m_size;
	private int		m_offset;
	private int 	m_width;
	private	int		m_height;
	private int		m_bitCount;
	private int		m_compressionType;
	private	int		m_colorsUsed;
	
	private int[]  	m_colorTable;
	private int[]	m_image;
	
	private boolean m_validBMP = false;
	public boolean hasELVL = false; // since ELVL headers are so interlocked with the bitmap, this is 
									   // the appropriate place
	public int ELvlOffset = -1;
	
	public BitMap( BufferedInputStream stream ) {
		m_stream = stream;
	}
	
	public void readBitMap( boolean trans ) 
	{		
		fileData = new ByteArray();
		
		//Read 14 bytes for file header
		fileHeader = readIn( 14 );
		ByteArray array = new ByteArray( fileHeader );
		fh_type = array.readString( 0,2 );		
		
		if( array.readString( 0,2 ).equals( "BM" ) ) m_validBMP = true;
		else
		{ 
			if (array.readString( 0,4 ).equals( "elvl" ))
			{				
				hasELVL = true;
				ELvlOffset = 0;
			}
				
			return;
		}
		m_size = array.readLittleEndianInt( 2 );
		m_offset = array.readLittleEndianInt( 10 );
		
		if (m_size != 49718)
		{ // possible elvl header... check reserved bits to confirm 
			int offset = array.readLittleEndianShort( 6 );
			if (offset == 49720)
			{ // currently this is the only place for the eLvl Header
				ELvlOffset = 49720;
				hasELVL = true;
			}
		}

		//Read 40 bytes for info header
		infoHeader = readIn( 40 );
		array = new ByteArray( infoHeader );
		m_width = array.readLittleEndianInt( 4 );
		m_height = array.readLittleEndianInt( 8 );
		m_bitCount = array.readLittleEndianShort( 14 );
		m_compressionType = array.readLittleEndianInt( 16 );
		m_colorsUsed = array.readLittleEndianInt( 20 );
		
		//Create our image container
		m_image = new int[m_width*m_height];

		
		//If it is 8 bits or less it has a color table
		if( m_bitCount <= 8 ) {
			//Define our color tables/colors used
			m_colorTable = new int[(int)Math.pow( 2, m_bitCount )];	
			m_colorsUsed = (int)Math.pow( 2, m_bitCount );	
			//Read in the color table
			for( int i = 0; i < m_colorsUsed; i++ ) {
				byte c[] = readIn( 4 );
				array = new ByteArray( c );
				m_colorTable[i] = (array.readLittleEndianInt( 0 ) & 0xffffff) + 0xff000000;

				//Make black transparent. SS specific need, will adjust to be dynamic
				if( m_colorTable[i] == 0xff000000 && trans ) m_colorTable[i] = m_colorTable[i] & 0x00000000;
			}
		}
	
		if( m_compressionType == BI_RGB && m_bitCount <= 8 )
			readInRGB();
		else if( m_compressionType == BI_RLE8 && m_bitCount == 8 ) 
			readInRLE8();
	}
	
	public void readInRGB() {
		
		int shift[] = new int[8 / m_bitCount];
        for( int i = 0; i < 8 / m_bitCount; i++ )
            shift[i] = 8 - ( (i+1) * m_bitCount );

		//Create a mask for each pixel dependant on # of bitCount
		int mask = ( 1 << m_bitCount ) - 1;
		
		//How much padding after each line. Bitmaps pad to 32bits
		int pad = 4 - (int)Math.ceil( m_width * m_bitCount / 8.0 ) % 4;
		if( pad == 4 ) pad = 0;
		
		int y = m_height - 1;
		int x = 0;
		int bit = 0;
		
		int a = readByte();
		for( int i = 0; i < m_height*m_width; i++ ) {
			m_image[y*m_width+x] = m_colorTable[ a >> shift[bit] & mask];
				
			bit++;
			x++;
			if( x >= m_width ) {
				bit = 0;
				x = 0;
				y--;
				//Pad to 32 bits after each line
				for( int j = 0; j < pad; j++ )
						readByte();
				a = readByte();
			}
			if( bit >= 8 / m_bitCount ) {
                    bit = 0;
                    a = readByte();
            }
    	}

	}
	
	/** Reads RLE 8 bit bitmaps
	 */
	public void readInRLE8() {
		
		int y = m_height-1;
		int x = 0;
		
		int a = readByte();
		int b = readByte();
		while( !(a == 0 && b == 1) ) {
			
			if( a == 0 ) {
				if( b == 0 ) {
					y--;
					x = 0;
				} else if( b == 2 ) {
					x += readByte();
					y -= readByte();
				} else if( b >= 3 ) {
					for( int i = 0; i < b; i++ ) {
						m_image[y*m_width+x] = m_colorTable[readByte()];
						x++;
					}
					if( Math.round( b / 2.0 ) != b / 2.0 )
						readByte();
				}
			} else {
				for( int i = 0; i < a; i++ ) {
						m_image[y*m_width+x] = m_colorTable[b];
					x++;
				}
			}
			a = readByte();
			b = readByte();
		}
	}
	
	public byte[] readIn( int n ) {
		
		byte[] b = new byte[n];
		try {
			m_stream.read( b );
		//	fileData.addByteArray( b );
			return b;
		} catch (IOException e) { 
			return new byte[0];
		}
	}
	
	public int readByte() {
		
		byte[] b = new byte[1];
		try {
			m_stream.read( b );
			//fileData.addByteArray( b );
			return b[0] & 255;
		} catch (IOException e) {
			return 0;
		}
	}
	
	public void appendTo( BufferedOutputStream out ) {
		try {
			//Write bitmap File Data
			out.write( fileData.getByteArray(), 0, fileData.size() );
			out.close();
		} catch (IOException e) {
		}
	}
	
	public Image getImage() {
		return createImage( new MemoryImageSource(m_width, m_height, m_image, 0, m_width) );
	}
	
	/** Reads in a square tile of any size from the topleft
	 * Good for getting the first image in any of the /graphics/*.bm2
	 */
	public Image getImage( int size ) {
		
		int image[] = new int[size*size];
		for( int y = 0; y < size; y++ ) {
			for( int x = 0; x < size; x++ ) {
				image[y*size+x] = m_image[y*m_width+x];
			}
		}
		return createImage( new MemoryImageSource( size, size, image, 0, size ) );
	}
	
	public Image getImage( int width, int height ) {
		
		int image[] = new int[width*height];
		for( int y = 0; y < height; y++ ) {
			for( int x = 0; x < width; x++ ) {
				image[y*width+x] = m_image[y*m_width+x];
			}
		}
		return createImage( new MemoryImageSource( width, height, image, 0, width ) );
	}
	
	public boolean isBitMap() {
		return m_validBMP;
	}
	
	public int getFileSize() { return m_size; }
	public int getWidth() { return m_width; }
	public int getHeight() { return m_height; }
	public int[] getImageData() { return m_image; }
}