package com.nihaocloud.sesamedisk.frag;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.nihaocloud.sesamedisk.frag.exception.ImpossibleFileFragmentationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class FileFragmenter {
//    /**
//     * Fragments a file in a specified number of pieces and saves the fragmented pieces to memory
//     *
//     * @param file   - The <code>File</code> to be fragmented
//     * @param pieces - The number of pieces to fragment
//     * @return A <code>Block</code> array containing the fragmented versions of the original file
//     * @throws ImpossibleFileFragmentationException The specified number of pieces is smaller than the actual file size
//     * @throws IOException                          The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
//     * @author EncodedBunny
//     */
//    public static Block[] fragmentFile(File file, int pieces) throws ImpossibleFileFragmentationException, IOException {
//        return fragmentFile(file, pieces, null, null);
//    }
//
//    /**
//     * Fragments a file in a specified number of pieces and saves the fragmented pieces to the HD with the default file name: <pre>"frag_" + Block.getID() + "-" + Block.getIndex()</pre>
//     *
//     * @param file       - The <code>File</code> to be fragmented
//     * @param pieces     - The number of pieces to fragment
//     * @param saveFolder - The folder that will contain all the fragmented pieces
//     * @return A <code>Block</code> array containing the fragmented versions of the original file
//     * @throws ImpossibleFileFragmentationException If the specified number of pieces is smaller than the actual file size
//     * @throws IOException                          If the file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
//     * @author EncodedBunny
//     */
//    public static Block[] fragmentFile(File file, int pieces, String saveFolder) throws ImpossibleFileFragmentationException, IOException {
//        return fragmentFile(file, pieces, saveFolder, null);
//    }
//
//    /**
//     * Fragments a file in a specified number of pieces and saves the fragmented pieces to the HD with a specified file base name
//     *
//     * @param file       - The <code>File</code> to be fragmented
//     * @param pieces     - The number of pieces to fragment
//     * @param saveFolder - The folder that will contain all the fragmented pieces
//     * @param fileName   - The base file name for the fragmented pieces (the individual block ID of each piece will be appended to this file name)
//     * @return A <code>Block</code> array containing the fragmented versions of the original file
//     * @throws ImpossibleFileFragmentationException If the specified number of pieces is smaller than the actual file size
//     * @throws IOException                          If the file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
//     * @author EncodedBunny
//     */
//    private static Block[] fragmentFile(File file, int pieces, String saveFolder, String fileName)
//            throws ImpossibleFileFragmentationException, IOException {
//        if (!file.exists() || !file.canRead() || !file.isFile())
//            throw new IOException();
//        long size = file.length();
//        String fileID = "";
//        try {
//            fileID = new BigInteger(1, MessageDigest.getInstance("MD5").digest(file.getAbsolutePath().getBytes())).toString(16).substring(20);
//        } catch (NoSuchAlgorithmException e) {
//        }
//        if (size < pieces)
//            throw new ImpossibleFileFragmentationException("Number of pieces larger than file size");
//        if (saveFolder != null) {
//            File f = new File(saveFolder);
//            if (!f.exists()) {
//                f.mkdirs();
//            }
//        }
//        return splitIntoBlocks(file, (int) Math.floor((double) size / pieces), pieces, (int) (size % pieces), false, saveFolder, fileName, fileID);
//    }
//
//    /**
//     * Fragments a file dynamically, creating how many fragments necessary with each having a defined maximum size, and saves the fragmented pieces to memory
//     *
//     * @param file         - The <code>File</code> to be fragmented
//     * @param maxBlockSize - The maximum size that each block/fragment of the file may have
//     * @return A <code>Block</code> array containing the fragmented versions of the original file
//     * @throws ImpossibleFileFragmentationException The specified maximum block size is negative, zero or NaN
//     * @throws IOException                          The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
//     * @author EncodedBunny
//     */
//    public static Block[] fragmentFileDynamically(File file, int maxBlockSize) throws ImpossibleFileFragmentationException, IOException {
//        return fragmentFileDynamically(file, maxBlockSize, null, null);
//    }
//
//    /**
//     * Fragments a file dynamically, creating how many fragments necessary with each having a defined maximum size, and saves the fragmented pieces to the HD
//     *
//     * @param file         - The <code>File</code> to be fragmented
//     * @param maxBlockSize - The maximum size that each block/fragment of the file may have
//     * @param saveFolder   - The folder that will contain all the fragmented pieces
//     * @return A <code>Block</code> array containing the fragmented versions of the original file
//     * @throws ImpossibleFileFragmentationException The specified maximum block size is negative, zero or NaN
//     * @throws IOException                          The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
//     * @author EncodedBunny
//     */
//    public static Block[] fragmentFileDynamically(File file, int maxBlockSize, String saveFolder) throws ImpossibleFileFragmentationException, IOException {
//        return fragmentFileDynamically(file, maxBlockSize, saveFolder, null);
//    }
//
//    public static Block[] fragmentFileDynamically(File file, int maxBlockSize, String saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException {
//        return fragmentFileDynamically(file, maxBlockSize, saveFolder, fileName);
//    }

    public static File defragmentFile(String fragmentsFolder, String saveFile) throws IOException {
        File folder = new File(fragmentsFolder);
        File[] frags = folder.listFiles();
        if (!folder.exists() || !folder.isDirectory() || !folder.canRead() || frags == null)
            throw new IOException();
        boolean complete;
        do {
            complete = true;
            for (int x = 0; x < frags.length - 1; x++) {
                if (getIndexOf(frags[x]) > getIndexOf(frags[x + 1])) {
                    File tmp = frags[x];
                    frags[x] = frags[x + 1];
                    frags[x + 1] = tmp;
                    complete = false;
                }
            }
        } while (!complete);

        File file = new File(saveFile);
        if (!file.exists())
            file.createNewFile();
        if (!file.canRead() || !file.isFile())
            throw new IOException();

        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        for (File frag : frags) {
            final BufferedReader reader = new BufferedReader(new FileReader(frag));
            for (; ; ) {
                String line = reader.readLine();
                if (line == null)
                    break;
                bw.append(line);
                bw.newLine();
            }
        }
        bw.close();
        return file;
    }

    public static File defragmentFile(Block[] blocks, String saveFile) throws IOException {
        File file = new File(saveFile);
        if (!file.exists())
            file.createNewFile();
        if (!file.canRead() || !file.isFile())
            throw new IOException();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        for (Block b : blocks)
            bw.append(new String(b.getContents()));
        bw.close();
        return file;
    }


    public static Block[] fragmentFileDynamically(Context context,Uri file, long maxBlockSize,
                                                   String saveFolder, String fileName)
            throws ImpossibleFileFragmentationException, IOException {
//        if (!file.exists() || !file.canRead() || !file.isFile())
//            throw new IOException();
        if (maxBlockSize <= 0)
            throw new ImpossibleFileFragmentationException("Invalid max block size '" + maxBlockSize + "'");
        if (saveFolder != null) {
            File f = new File(saveFolder);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        DocumentFile documentFile=DocumentFile.fromSingleUri(context,file);
        long size = documentFile.length();
        if (size <= maxBlockSize) {
            InputStream inputStream =
                    context.getContentResolver().openInputStream(file);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(inputStream)));
            int blockSize = (int) size;
            char[] block = new char[blockSize];
            br.read(block);
            br.close();
            Block block1 = (fileName == null) ? new Block(saveFolder, 0) : new Block(saveFolder, fileName, 0);
            block1.setContents(block);
            return new Block[]{block1};
        }
        return splitIntoBlocks(context,file, (int) maxBlockSize, (int) Math.floor((double) size / maxBlockSize), (int) (size % maxBlockSize), true, saveFolder, fileName);
    }

    private static Block[] splitIntoBlocks(Context context, Uri file, int blockSize, int pieces,
                                           int extra, boolean splitExtra, String saveFolder, String fileName) throws IOException {
        Block[] blocks = new Block[(splitExtra && extra != 0) ? pieces + 1 : pieces];

        InputStream inputStream =
                context.getContentResolver().openInputStream(file);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(inputStream)));
        for (int b = 0; b < blocks.length; b++) {
            if (b == blocks.length - 1) {
                if (splitExtra) {
                    char[] block = new char[extra];
                    br.read(block);
                    blocks[b] = (fileName == null) ? new Block(saveFolder, b) : new Block(saveFolder, fileName, b);
                    blocks[b].setContents(block);
                    br.close();
                    return blocks;
                } else
                    blockSize += extra;
            }
            char[] block = new char[blockSize];
            br.read(block);
            blocks[b] = (fileName == null) ? new Block(saveFolder, b) : new Block(saveFolder, fileName, b);
            blocks[b].setContents(block);
        }
        br.close();
        return blocks;
    }

    private static int getIndexOf(File frag) {
        String[] parts = frag.getName().split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}