/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package X3dToX3dom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author MarcoSaku
 */
public class ReadReverse {

    private static final int BUFFER_SIZE = 8192;
    private FileChannel channel;
    private long filePos;
    private int bufPos;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private MappedByteBuffer buf;
    private byte lastLineBreak = '\n';

    //read a file in reverse mode
    public String read(RandomAccessFile file, long pointer) throws IOException {
        channel = file.getChannel();
        filePos = pointer;
        while (true) {
            if (bufPos < 0) {
                if (filePos == 0) {
                    if (baos == null) {
                        return null;
                    }
                    String line = bufToString();
                    baos = null;
                    return line;
                }

                long start = Math.max(filePos - BUFFER_SIZE, 0);
                long end = filePos;
                long len = end - start;

                buf = channel.map(FileChannel.MapMode.READ_ONLY, start,
                        len);
                bufPos = (int) len;
                filePos = start;
            }

            while (bufPos-- > 0) {
                byte c = buf.get(bufPos);
                if (c == '\r' || c == '\n') {
                    if (c != lastLineBreak) {
                        lastLineBreak = c;
                        continue;
                    }
                    lastLineBreak = c;
                    return bufToString();
                }
                baos.write(c);
            }
        }
    }

    public String bufToString() throws UnsupportedEncodingException {
        if (baos.size() == 0) {
            return "";
        }

        byte[] bytes = baos.toByteArray();
        for (int i = 0; i < bytes.length / 2; i++) {
            byte t = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = t;
        }

        baos.reset();

        return new String(bytes, "UTF-8");
    }

    public int getBufPos() {
        return bufPos;
    }

}
