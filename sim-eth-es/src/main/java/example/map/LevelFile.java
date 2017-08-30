/*
 * [File Header]
 * offset	length		info
 *-------------------------------------------
 *	0		2			Type of file "BM"
 *	2		4			Total file size
 *	6		2			Reserved
 *	8		2			Reserved
 *	10		4			Bitmap offset
 *
 * [Info Header]
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
 * [Color Table] for( i = 0; i < 256; i++ )
 *  54*i+0 	1			Red
 *	54*i+1	1			Green
 *	54*i+2	1			Blue
 *	54*i+3	1			Not used
 *	
 *  Notes:
 *	Type 		== BM
 *	Compression == 0
 *	Width 		== 304
 *  Height 		== 160
 *  Bits 		== 8
 *	
 */
package example.map;

import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LevelFile extends JPanel {

    public String m_file;
    private BitMap m_bitmap;
    private BufferedInputStream m_stream;
    private boolean m_containsBM;
    private boolean hasELVLData;

    // eLVL ATTR tags... vector of vector of Strings
    public Vector eLvlAttrs = new Vector();
    public static final int DEFAULT_TAG_COUNT = 6;

    // Vector of loaded reginons  
    public Vector regions;

    // unkownn ELVL chunks read in on load
    public Vector unknownELVLData = new Vector();

    // the actual data we're going to save, as a Vector of Bytes... saved by makeELvlDataForSaving
    public Vector eLVLData;

    private String m_type;
    private int m_size;
    private int m_offset;
    private int m_width;
    private int m_height;
    private int m_bitCount;
    private int m_compressionType;
    private int m_colorsUsed;

    private short[][] m_level = new short[1024][1024];

    /**
     * Reads in a *.lvl file.
     *
     * @param stream	The file to load/save to
     * @param b	The tileset bitmap (note read in default bitmap if lvl file does
     * not contain bitmap portion)
     * @param had	If the lvl file contained the tileset bitmap
     */
    public LevelFile(BufferedInputStream bufferedStream, BitMap b, boolean hasBMP, boolean hasELVL, String file) {

        m_bitmap = b;
        m_containsBM = hasBMP;
        hasELVLData = hasELVL;
        m_stream = bufferedStream;
        m_file = file;
    }

    /**
     * Creates a new default lvl file
     *
     * @param b	The tileset bitmap (note read in default bitmap if lvl file does
     * not contain bitmap portion)
     */
    public LevelFile(BitMap b) {
        m_bitmap = b;
    }

    /**
     * Add the default eLvl tags to this level file.
     */
    public void addDefaultELvLTags() {
        String userName = System.getProperty("user.name");
        if (userName == null) {
            userName = "User";
        }

        Vector row = new Vector();
        row.add("NAME");
        row.add("Unnamed");
        eLvlAttrs.add(row);

        row = new Vector();
        row.add("VERSION");
        row.add("1.0");
        eLvlAttrs.add(row);

        row = new Vector();
        row.add("ZONE");
        row.add(userName + "'s Zone");
        eLvlAttrs.add(row);

        row = new Vector();
        row.add("MAPCREATOR");
        row.add(userName);
        eLvlAttrs.add(row);

        row = new Vector();
        row.add("TILESETCREATOR");
        row.add(userName);
        eLvlAttrs.add(row);

        row = new Vector();
        row.add("PROGRAM");
        row.add("Continuum Level Ini Tool");
        eLvlAttrs.add(row);
    }

    /**
     * Read in the eLVL data starting at the current position
     *
     * @return null if no error or the error string
     */
    private String readELvlData() {
        String error = null;
        regions = new Vector();

        if (!available(12)) {
            error = "File ended before we could read the eLVL header.";
        } else {
            // read header
            byte[] header = readIn(12);
            ByteArray headerArray = new ByteArray(header);
            ByteArray curData;

            if (!headerArray.readString(0, 4).equals("elvl")) {
                error = "The elvl header tag was not detected at the start of "
                        + " the eLVL data section.";
            } else {
                int size = headerArray.readLittleEndianInt(4); // total size of the metadata section
                int current = 12; // current number of bytes read

                while (current < size && error == null) {
                    if (available(8)) {
                        curData = new ByteArray(readIn(8));
                        current += 8;
                        String type = curData.readString(0, 4);
                        int chunkLength = curData.readLittleEndianInt(4);

                        if (!available(chunkLength)) {
                            error = "EOF while reading in a eLVL chunk of type " + type;

                            break;
                        }

                        if (type.equals("ATTR")) { // attribute chunk
                            current += chunkLength;
                            curData = new ByteArray(readIn(chunkLength));
                            String attr = curData.readString(0, chunkLength);
                            String[] keyTag = attr.split("=");
                            if (keyTag.length != 2) {
                                error = "ATTR tag does not contain exactly "
                                        + "one '=' sign: " + attr;
                                break;
                            }

                            Vector row = new Vector();
                            row.add(keyTag[0]);
                            row.add(keyTag[1]);
                            eLvlAttrs.add(row);
                        } else if (type.equals("REGN")) { // region chunk
                            curData = new ByteArray(readIn(chunkLength));
                            current += chunkLength;

                            Region r = new Region();
                            String rv = r.decodeRegion(curData);

                            if (rv != null) {
                                error = rv;
                                break;
                            }

                            regions.add(r);
                        } else // unknown chunk
                        {
                            //System.out.println("unknown chunk: " + type);
                            curData = new ByteArray(readIn(chunkLength));

                            // encode header
                            unknownELVLData.add(new Byte((byte) type.charAt(0)));
                            unknownELVLData.add(new Byte((byte) type.charAt(1)));
                            unknownELVLData.add(new Byte((byte) type.charAt(2)));
                            unknownELVLData.add(new Byte((byte) type.charAt(3)));
                            byte[] dword = BitmapSaving.toDWORD(chunkLength);
                            for (int c = 0; c < 4; ++c) {
                                unknownELVLData.add(new Byte(dword[c]));
                            }

                            // encode data
                            for (int c = 0; c < chunkLength; ++c) {
                                byte b = curData.readByte(c);
                                unknownELVLData.add(new Byte(b));
                            }

                            // encode padding
                            int padding = 4 - chunkLength % 4;
                            if (padding != 4) {
                                unknownELVLData.add(new Byte((byte) 0));
                            }
                        }

                        // read in padding up to 4 byte boundry
                        int padding = 4 - (chunkLength % 4);
                        if (padding != 4) {
                            if (available(padding)) {
                                readIn(padding);
                                current += padding;
                            } else {
                                JOptionPane.showMessageDialog(null, "EOF while reading eLVL chunk padding.");
                            }
                        }
                    } else {
                        error = "File ended while expecting a generic chunk header.";
                    }
                }
            }
        }

        return error;
    }

    /**
     * Actually do the read on the .lvl file, ignoring the already loaded bitmap
     * part
     *
     * @return null or the error message
     */
    public String readLevel() {
        if (hasELVLData) {
            readIn(m_bitmap.ELvlOffset);
        } else if (m_containsBM) {
            readIn(m_bitmap.getFileSize());
        }

        String error = null;

        // right now we're at our tile data, or our eLVL Data
        if (hasELVLData) {
            error = readELvlData();
        }

        if (error == null) {
            while (available(4)) {
                byte[] b = readIn(4);
                ByteArray array = new ByteArray(b);
                int i = array.readLittleEndianInt(0);
                int tile = i >> 24 & 0x00ff;
                int y = (i >> 12) & 0x03FF;
                int x = i & 0x03FF;
                m_level[x][y] = (short) tile;
            }
        }

        //Close the stream so it doesn't remain opened.
        try {
            m_stream.close();
        } catch (IOException e) {
        }

        return error;
    }

    /**
     * Save the level file with a different file name
     *
     * @param where the new file to save it as
     * @param tileset the tileset to save
     * @param map the map array[][] to save
     * @param regions the vector of regions to save
     */
    public void saveLevelAs(String where, Image tileset, short[][] map, Vector regions) {
        m_file = where;
        saveLevel(tileset, map, regions);
    }

    /**
     * make the eLVL data and store it as a Vector of Bytes in eLVLData, not
     * including the header
     *
     * @param regions the vector of Regions
     */
    private void makeELvlDataForSaving(Vector regions) {
        eLVLData = new Vector();

        // first save the ATTR tags
        int size = eLvlAttrs.size();
        for (int x = 0; x < size; ++x) {
            Vector row = (Vector) eLvlAttrs.get(x);
            String one = ((String) row.get(0)).replace('=', '-');
            String two = ((String) row.get(1)).replace('=', '-');
            String save = one + "=" + two;

            // save chunk header
            eLVLData.add(new Byte((byte) 'A'));
            eLVLData.add(new Byte((byte) 'T'));
            eLVLData.add(new Byte((byte) 'T'));
            eLVLData.add(new Byte((byte) 'R'));

            int chunkLength = save.length();
            byte[] chunkSizeBytes = BitmapSaving.toDWORD(chunkLength);
            for (int c = 0; c < 4; ++c) {
                eLVLData.add(new Byte(chunkSizeBytes[c]));
            }

            int len = save.length();
            for (int c = 0; c < len; ++c) {
                byte letter = (byte) save.charAt(c);
                eLVLData.add(new Byte(letter));
            }

            //padding
            int padding = 4 - (chunkLength % 4);
            if (padding != 4) {
                for (int c = 0; c < padding; ++c) {
                    eLVLData.add(new Byte((byte) 0)); // padding byte
                }
            }
        }

        // now the REGN tags
        for (int x = 0; x < regions.size(); ++x) {
            Region r = (Region) regions.get(x);

            eLVLData.add(new Byte((byte) 'R'));
            eLVLData.add(new Byte((byte) 'E'));
            eLVLData.add(new Byte((byte) 'G'));
            eLVLData.add(new Byte((byte) 'N'));

            Vector curRegionEncoded = r.getEncodedRegion();
            byte[] dword = BitmapSaving.toDWORD(curRegionEncoded.size());
            for (int c = 0; c < 4; ++c) {
                eLVLData.add(new Byte(dword[c]));
            }

            eLVLData.addAll(curRegionEncoded);
        }

        // now any unknown tags we enocuntered while loading
        eLVLData.addAll(unknownELVLData);
    }

    /**
     * Save the ELVL data to the current position in the stream. It's stored in
     * a vector of Bytes in eLVLData
     *
     * @param out the output stream to save to
     */
    private void saveELvlData(BufferedOutputStream out) throws IOException {
        int size = eLVLData.size();
        byte[] array = new byte[size];
        byte[] dword = new byte[4];
        byte[] word = new byte[2];
        word[0] = word[1] = 0;

        // save two bytes padding
        out.write(word);

        // save header
        dword[0] = (byte) 'e';
        dword[1] = (byte) 'l';
        dword[2] = (byte) 'v';
        dword[3] = (byte) 'l';
        out.write(dword);

        dword = BitmapSaving.toDWORD(size + 12); // size
        out.write(dword);

        dword = BitmapSaving.toDWORD(0); // reserved
        out.write(dword);

        // save data
        for (int x = 0; x < size; ++x) {
            array[x] = ((Byte) eLVLData.get(x)).byteValue();
        }

        out.write(array);

        eLVLData.clear();
    }

    /**
     * Actually save the .lvl file
     *
     * @param tileset the tileset to save it with
     * @param map the map aray[][] to save
     */
    public void saveLevel(Image tileset, short[][] map, Vector regions) {
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(m_file));

            boolean containsELVLData = eLvlAttrs.size() > 0;
            makeELvlDataForSaving(regions);

            // save bitmap
            BitmapSaving.saveAs256ColorBitmap(out, tileset, eLVLData.size());

            // save eLVL data
            if (eLVLData.size() > 0) {
                saveELvlData(out);
            }

            // save tiles
            for (int y = 0; y < 1024; ++y) {
                for (int x = 0; x < 1024; ++x) {
                    int tile = map[x][y];

                    if (tile == 0 || tile == -1) {
                        continue;
                    }

                    int intstruct = (tile << 24) | (y << 12) | x;
                    byte[] ar = BitmapSaving.toDWORD(intstruct);
                    out.write(ar);
                }
            }

            out.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }

    }

    public byte[] readIn(int n) {
        byte[] b = new byte[n];
        try {
            m_stream.read(b);
            return b;
        } catch (IOException e) {
            System.out.println(e);
            return new byte[0];
        }
    }

    public boolean available(int n) {
        try {
            if (m_stream.available() >= n) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    public Image getTileSet() {
        return m_bitmap.getImage();
    }

    public Image[] getTiles() {

        int[] m_image = m_bitmap.getImageData();

        Image[] tiles = new Image[190];
        for (int i = 0; i < 190; i++) {

            int yOffset = (int) Math.floor(i / 19) * 4864;
            int xOffset = (int) (i - Math.floor(i / 19) * 19) * 16;

            int thisTile[] = new int[256];
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    thisTile[y * 16 + x] = m_image[(yOffset + xOffset) + y * 304 + x];
                }
            }
            tiles[i] = createImage(new MemoryImageSource(16, 16, thisTile, 0, 16));
        }
        return tiles;
    }

    public short[][] getMap() {
        return m_level;
    }
}
