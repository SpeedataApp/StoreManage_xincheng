package speedatagroup.brxu.com.myapplication.utils;

import android.CRC.CRC;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 串口部分常用数据转换方法
 *
 * @author Echo
 * @version 1.0
 * @date 2015-6-29
 */
public class DataConversionUtils {

    /**
     * hexString-byte[] "130632199104213021"->{0x13,0x06....,0x21}
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToByteArray(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * byte[]->String {0x23,0x32,0x12}-->"233212" 比如从卡里解析出身份证
     *
     * @param src
     * @return
     */
    public static String byteArrayToString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * byte[]->String {0x23,0x32,0x12}-->"23 32 12" 查看log
     *
     * @param res
     * @param len
     * @return
     */
    public static String byteArrayToStringLog(byte[] res, int len) {
        String result = "";
        for (int i = 0; i < len; i++) {
            result += String.format("%02x ", res[i]);
        }
        return result;
    }

    /**
     * byte[]->ascii String {0x71,0x72,0x73,0x41,0x42}->"qrsAB"
     *
     * @param cmds
     * @return
     */
    public static String byteArrayToAscii(byte[] cmds) {
        int tRecvCount = cmds.length;
        StringBuffer tStringBuf = new StringBuffer();
        String nRcvString;
        char[] tChars = new char[tRecvCount];
        for (int i = 0; i < tRecvCount; i++) {
            tChars[i] = (char) cmds[i];
        }
        tStringBuf.append(tChars);
        nRcvString = tStringBuf.toString(); // nRcvString从tBytes转成了String类型的"123"
        return nRcvString;
    }


    /**
     * byte[]转int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < bytes.length; i++) {
            int shift = (bytes.length - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }


    /**
     * @param format example:yyyyMMddHHmmss
     * @return
     */

    public static String getCurrentTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    /**
     * @return
     */

    public static String getDefautCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }


    public static byte[] intToByteArray1(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
//必须把我们要的值弄到最低位去，有人说不移位这样做也可以， result[0] = (byte)(i  & 0xFF000000);
//，这样虽然把第一个字节取出来了，但是若直接转换为byte类型，会超出byte的界限，出现error
// 。再提下数//之间转换的原则（不管两种类型的字节大小是否一样，原则是不改变值，内存内容可能会变，比如int转为//float肯定会变）所以此时的int转为byte会越界，只有int
// 的前三个字节都为0的时候转byte才不会越界。虽//然 result[0] = (byte)(i  & 0xFF000000); 这样不行，但是我们可以这样 result[0] =
// (byte)((i  & //0xFF000000) >>24);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static byte[] intTo2ByteArray1(int i) {
        byte[] result = new byte[2];
//		result[0] = (byte)((i >> 24) & 0xFF);
////必须把我们要的值弄到最低位去，有人说不移位这样做也可以， result[0] = (byte)(i  & 0xFF000000);
////，这样虽然把第一个字节取出来了，但是若直接转换为byte类型，会超出byte的界限，出现error
// 。再提下数//之间转换的原则（不管两种类型的字节大小是否一样，原则是不改变值，内存内容可能会变，比如int转为//float肯定会变）所以此时的int转为byte会越界，只有int
// 的前三个字节都为0的时候转byte才不会越界。虽//然 result[0] = (byte)(i  & 0xFF000000); 这样不行，但是我们可以这样 result[0] =
// (byte)((i  & //0xFF000000) >>24);
//		result[1] = (byte)((i >> 16) & 0xFF);
        result[0] = (byte) ((i >> 8) & 0xFF);
        result[1] = (byte) (i & 0xFF);
        return result;
    }
    public static byte[] intTo2ByteArray15byte(int i) {
        byte[] result = new byte[5];
		result[0] = 0x00;
		result[1] = (byte)((i >> 24) & 0xFF);
////必须把我们要的值弄到最低位去，有人说不移位这样做也可以， result[0] = (byte)(i  & 0xFF000000);
////，这样虽然把第一个字节取出来了，但是若直接转换为byte类型，会超出byte的界限，出现error
// 。再提下数//之间转换的原则（不管两种类型的字节大小是否一样，原则是不改变值，内存内容可能会变，比如int转为//float肯定会变）所以此时的int转为byte会越界，只有int
// 的前三个字节都为0的时候转byte才不会越界。虽//然 result[0] = (byte)(i  & 0xFF000000); 这样不行，但是我们可以这样 result[0] =
// (byte)((i  & //0xFF000000) >>24);
		result[2] = (byte)((i >> 16) & 0xFF);
        result[3] = (byte) ((i >> 8) & 0xFF);
        result[4] = (byte) (i & 0xFF);
        return result;
    }
    public static byte[] intTo2ByteArray1byte(int i) {
        byte[] result = new byte[1];
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    public static List<byte[]> getListData(byte start, int len,
                                           byte[] data, MyLogger logger) {
        List<byte[]> listbyte = new ArrayList<byte[]>();
        byte[] temp;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == start) {
                temp = new byte[len];// len
                for (int j = 0; j < len; j++) {
                    if ((j + i) < data.length) {
                        temp[j] = data[j + i];
                    }
                }
//                listbyte.add(temp);
                byte tt = CRC.crc(temp,0);
                if (temp[len-1] == tt) {
                    listbyte.add(temp);
                    logger.d("crc-ok-");
                } else {
                    logger.d("crc-error-" + "temp[15]=" + temp[len-1] + " crc=" + tt + "  " +
                            DataConversionUtils.byteArrayToString(temp));
                }
                i += len-1;
//                listbyte.add(temp);
            }
//            else{
//                logger.d("data[i]="+data[i]);
//            }
        }
        return listbyte;
    }
    /**
     * 获取当前应用程序的版本号
     */
    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packinfo = pm.getPackageInfo(context.getPackageName(), 0);
            String version = packinfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "版本号错误";
        }
    }
}
