package main.encoder;

import main.utils.MatConstants;
import org.opencv.core.Mat;

/**
 * Created by Magda on 10/07/2017.
 */
public class ByteCode {
    private final static int BYTE_SIZE = 8;
    //those two just to estimate how to toDisplayableMat the code
    private final int cols;     //TODO get rid of cols and rows -> I can get it from filterConstants
    private final int rows;
    private byte[] code;
    private Mat display;
    private int padding;

    public ByteCode(Mat mat) {
        cols = mat.cols();
        rows = mat.rows();
        generateCode(mat);
    }

    public byte[] getCode() {
        return code.clone();
    }

    public Mat toDisplayableMat() {
        //lazy init so display wouldn't be computed unnecessarily
        if (display == null) {
            display = new Mat(rows, cols, MatConstants.TYPE);
            byte b;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    b = getBit(getPos(i, j, cols /*step*/));
                    assert b == 0 || b == 1;
                    if (b == 0)
                        display.put(i, j, 0);
                    if (b == 1)
                        display.put(i, j, 255);
                }
            }
        }
        return display;
    }

    public byte[] getMask() {
        //byte array of ones for now - except for padding
        byte[] mask = new byte[code.length];
        for (int i = 0; i < code.length - 1; i++)
//            mask[i] = (byte) ~mask[i];
            mask[i] = (byte) ~0x00;
        //TODO add padding to mask
//        mask[code.length-1] = -0b00000000;
        return mask;
    }

    private void generateCode(Mat mat) {
        code = new byte[getCodeSize(mat)];
        int step = mat.cols();
        double[] pixel;
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                pixel = mat.get(i, j);
                assert pixel.length == 1; //greyscale
                //has to be <= otherwise it won't be the equivalent of Imgproc.threshold
                if (pixel[0] <= 0)
                    setBit((byte) 0, getPos(i, j, step));
                else
                    setBit((byte) 1, getPos(i, j, step));
            }
        }
    }

    private int getPos(int row, int col, int step) {
        return row * step + col;
    }

    private int getCodeSize(Mat mat) {
        int matSize = mat.cols() * mat.rows();
        int codeSize = matSize / BYTE_SIZE;
        padding = matSize % BYTE_SIZE;
        if (padding != 0) {
            codeSize += 1;
            padding = BYTE_SIZE - padding; //last bits not encoding iris
        }
        return codeSize;
    }


    //TODO move these to inner class
    //https://stackoverflow.com/questions/4674006/set-specific-bit-in-byte
    private byte setBit1(byte b, int pos) {
        return (byte) (b | (byte) (1 << pos));
    }

    private byte setBit0(byte b, int pos) {
        return (byte) (b & ~(byte) (1 << pos));
    }

    private byte getBit(byte b, int pos) {
        return (byte) ((b >> pos) & (byte) 1);
    }

    private void setBit(int value, int pos) {
        assert value == 0 || value == 1;
        int posInArr = pos / BYTE_SIZE;
        int posInByte = pos % BYTE_SIZE;
        if (value == 0)
            code[posInArr] = setBit0(code[posInArr], posInByte);
        else
            code[posInArr] = setBit1(code[posInArr], posInByte);
    }

    private byte getBit(int pos) {
        return getBit(code[pos / BYTE_SIZE], pos % BYTE_SIZE);
    }

    public int getPadding() {
        return padding;
    }

}
