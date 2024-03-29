package com.susiha.apkanalysis.dexanalysis;

import com.susiha.apkanalysis.dexanalysis.header.HeaderItem;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static boolean showLog = true;

    public static final File DEX = new File("/Users/yrd/Desktop/010EditorTemple/MyApplication1/classes.dex");

    /**
     * 读取Dex文件
     *
     * @return
     * @throws IOException
     */
    public static BufferedSource getDexBufferedSource() throws IOException {
        return Okio.buffer(Okio.source(Utils.DEX));
    }

    /**
     * 插入排序算法
     */
    public static void insertion(ArrayList<HeaderItem> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            for (int j = i; j > 0 && list.get(j).getIndex() < list.get(j - 1).getIndex(); j--) {
                Collections.swap(list, j, j - 1);
            }
        }
    }


    public static void Logger(String tag, String msg) {
        if (showLog) {
            System.out.println(tag + " = " + msg);
        }
    }


    public static void Logger(String tag) {
        if (showLog) {
            System.out.println(tag );
        }
    }

    public static void Logger(String tag, int msg) {
        if (showLog) {
            System.out.println(tag + " = " + msg);
        }
    }

    /**
     * 反转ByteString
     *
     * @param byteString
     * @return
     */
    public static ByteString reverseByteString(byte[] byteString) {
        if(byteString==null||byteString.length==0){
            return null;
        }
        byte[] bytes = new byte[byteString.length];
        //反转ByteString
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteString[byteString.length - 1 - i];
        }

        return new ByteString(bytes);
    }

    /**
     * Dex 是小端存储，如果涉及到值的问题，需要进行反转 再求值
     *
     * @param byteString
     * @return
     */
    public static int reverseInt(byte[] byteString) {
        ByteString reverseByteString = reverseByteString(byteString);
        if(byteString==null||byteString.length==0){
            return -1;
        }
        byte[] bytes = reverseByteString.toByteArray();
        /**
         * Byte数组转Int值
         */
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            int shift = (bytes.length - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    /**
     * 读取ULeb128字节信息
     * 读取规则 字节的最高位表示符号位，是否继续读取下一个字节，1 表示继续读取，0 表示不读取 字节的低7位表示数据
     * 一个最大的int值需要用四个字节表示，所以如果使用leb128表示的话 最多只能是5个  读取5个字节后如果还需要读取，
     * 则抛出异常
     *
     * @param bufferedSource 读取byte
     * @return 返回的是byte List也是小端存储的，低数据存放在前面，高数据存放在后面
     */
    public static ArrayList<Byte> readLeb128(BufferedSource bufferedSource) throws IOException {
        if (bufferedSource == null) {
            return null;
        }
        int count = 1;
        byte b = 0;
        ArrayList<Byte> bytes = new ArrayList<>();
        //首先先读取一个字节
        b = bufferedSource.readByte();
        bytes.add(b);
        while ((b & 0x80) != 0 && count <= 5) {
            b = bufferedSource.readByte();
            bytes.add(b);
            count++;
        }
        return bytes;
    }




    /**
     * 解析无符号
     * 根据读取的字节进行数据转换求出size 转换规则
     * 1 先通过byte&0x7f 计算出真实的值(去掉高位的符号位)
     * 2 把结果右移,右移位数是7的倍数(真实数据就7位)
     * 3 把所有右移后的结果通过或操作 得出最终的结果
     *
     * @param bytes
     * @return
     */
    public static int decodeULeb128(List<Byte> bytes) {
        if (bytes == null || bytes.size() == 0) {
            throw new IllegalStateException("read uleb128 error!");
        }
        int result = 0;
        for (int i = 0; i < bytes.size(); i++) {
            result = result | ((bytes.get(i) & 0x7f) << (i * 7));
        }
        return result;
    }


    /**
     * 解析ULeb128p1,这是Uleb128的一种变异
     * @param bytes
     * @return
     */
    public static int decodeULeb128P1(List<Byte> bytes){
        return decodeULeb128(bytes)-1;
    }

    /**
     * 解析有符号的Leb128，最后一个字节要做符号扩展
     * @param bytes
     * @return
     */
    public static int decodeSLeb128(List<Byte> bytes){
        if (bytes == null || bytes.size() == 0) {
            throw new IllegalStateException("read sleb128 error!");
        }
        int result = 0;
        for (int i = 0; i < bytes.size(); i++) {
            result = result | ((bytes.get(i) & 0x7f) << (i * 7));
        }

        //进行符号位扩展
        int syExt = 25-(bytes.size()-1)*7;
        if(syExt>0){
            result = (result<<syExt)>>syExt;
        }
        return result;
    }









    /**
     * byte 十六进制表示
     *
     * @param b
     * @return
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     * Byte 数组用Hex的String表示
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToHex(ArrayList<Byte> bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        if (bytes == null || bytes.size() == 0) {
            return "byteArray empty";
        }
        for (byte b : bytes) {
            sb.append(byteToHex(b));
        }
        return sb.toString();

    }


    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        if (bytes == null || bytes.length == 0) {
            return "byteArray empty";
        }
        for (byte b : bytes) {
            sb.append(byteToHex(b));
        }
        return sb.toString();

    }


    /**
     * 在特定场合中需要byteArray 因此提供一个转化的方法
     * 大部分情况下不需要 因此在存储的时候使用ArrayList
     *
     * @param bytes
     * @return
     */
    public static byte[] byteListToByteArray(ArrayList<Byte> bytes) {

        if (bytes == null || bytes.size() == 0) {
            return null;
        }
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }
        return byteArray;
    }


    /**
     * ByteArray 转List
     *
     * @param bytes
     * @return
     */
    public static ArrayList<Byte> byteArrayToByteList(byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ArrayList<Byte> byteArrayList = new ArrayList<>();

        for (int i = 0; i < bytes.length; i++) {
            byteArrayList.add(bytes[i]);
        }
        return byteArrayList;
    }

    /**
     * byteArray 转String utf-8 编码
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        /**
         * 把结尾的0x00 给去掉
         */
        byte[] newBytes = new byte[bytes.length - 1];
        for (int i = 0; i < newBytes.length; i++) {
            newBytes[i] = bytes[i];
        }
        String data = "initData";

        try {
            data = new String(newBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 解析字节码中类型表示
     *
     * @param source
     * @return
     */
    public static String basicTypeConversion(String source) {
        int count = 0; //表示几维数组
        String returnData;
        if (source == null || source.equals("")) {
            return "";
        }
        if (source.length() > 1) { //包括数组和Object 以及Object数组
            while(source.startsWith("[")){
                count++;
                source = source.substring(1);//减去一个前缀[
            }

            if(source.length()>1){ //表示Obeject对象类型
                //去掉前缀L和后缀;
                if(source.startsWith("L")&&source.endsWith(";")){
                    source = source.substring(1,source.length()-1);
                    StringBuilder sb = new StringBuilder();
                    sb.append(source);
                    if(count>0){
                        for(int i =0;i<count;i++){
                            sb.append("[]");
                        }
                    }
                    return sb.toString();
                }


            }
        }
        switch (source) {
            case "B":
                returnData = "byte";
                break;
            case "Z":
                returnData = "boolean";
                break;
            case "S":
                returnData = "short";
                break;
            case "C":
                returnData = "char";
                break;
            case "I":
                returnData = "int";
                break;
            case "J":
                returnData = "long";
                break;
            case "F":
                returnData = "float";
                break;
            case "D":
                returnData = "double";
                break;
            case "V":
                returnData = "void";
                break;
            default:
                returnData = source;
        }
        StringBuilder returnsb = new StringBuilder();
        returnsb.append(returnData);
        if(count>0){
            for(int i =0;i<count;i++){
                returnsb.append("[]");
            }
        }
        return returnsb.toString();
    }


}
