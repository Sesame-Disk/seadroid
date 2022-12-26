package com.nihaocloud.sesamedisk.frag;

import androidx.room.util.UUIDUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Block {
    private String blockID;
    private String hdFolder;
    private String filePath;
    private String fullPath;
    private int index;

    public Block(String saveFolder, int index) {
        this(saveFolder, "frag_", index);
    }

    public Block(String hdFolder, String fileName, int index) {
        this.blockID = UUID.randomUUID().toString().replaceAll("-", "");
        this.index = index;
        this.hdFolder = hdFolder;
       // this.filePath = fileName + blockID + "-" + index;
        this.filePath = blockID;
    }

    public Block setContents(char[] block) throws IOException {
        File file = new File(this.hdFolder, filePath);
        fullPath=file.getAbsolutePath();
        if (!file.exists()) file.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(block);
        bw.close();
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    /**
     * Gets the contents of this <code>Block</code>, either from memory or from the HD
     *
     * @return The contents of this <code>Block</code>, or <code>null</code> in case an error occurs
     */
    public char[] getContents() {
        try {
            File file = new File(filePath);
            char[] contents = new char[(int) file.length()];
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.read(contents);
            br.close();
            return contents;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the ID used by this <code>Block</code>
     * <p>
     * <b>Note:</b> This ID is added to the middle of the save file name in case this <code>Block</code> is saving it's information in the HD
     *
     * @return The last 12 characters of a MD5 hash of the <code>File.getAbsolutePath().getBytes()</code> invoked at this <code>Block</code> instantiation
     */
    public String getID() {
        return blockID;
    }

    /**
     * Gets the index of this <code>Block</code> in respect to it's original <code>File</code>
     * <p>
     * <b>Note:</b> The value of this index ranges from 0 to the number of fragments - 1
     *
     * @return The index representing this <code>Block</code>
     */
    public int getIndex() {
        return index;
    }

    public String getFilePath() {
        return filePath;
    }
}