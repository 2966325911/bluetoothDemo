package com.kangengine.bluebooth;


/**
 * @author : Vic
 * time    : 2018-12-26 15:24
 * desc    :
 */
public class UricAcidCovertUtil {


    /**
     * 获取最后的尿酸数据
     * 一般是4位
     * 1 .分离 第1位和最后3位
     * 2 .对第一位进行取反加1计算十进制数
     * 3 .对后三位转换为十进制
     * 4.后三位的十进制*10^的（-第一位取反加1后的十进制）
     * A276 = 0x276 * 10^(2’s complement of 0x0a) = 630 * 10^(-6) = 0.00063 kg/l = 63mg/dl
     * 0x0a的二进制为 1010取反为0101加 1 为0110 十进制为6  尿酸：UA : 1 mmol/L = 16.8mg/dL
     * @param uaData
     * @return
     */
    public static double getUAData(String uaData){
        double finalUaData = 0;
        String headerData = uaData.substring(0,1);
        //尿酸的数据
        String validData = uaData.substring(1,uaData.length());
        //将第一位取反转换为二进制
        String valBinaryData = decimalToBinary(notDecimal(hexToDecimal(headerData)));

        int valHeadData = binaryToDecimal(add(valBinaryData.substring(valBinaryData.length()-4,valBinaryData.length()),"1"));

        int valData = hexToDecimal(validData);
        //Math.pow(10,3)第2个参数3 看最后的结果单位 如果是kg/l = mg/dl 则为(10,5)
        //如果为 mol/l = mmol/l 则为(10,3) 尿酸：UA : 1 mmol/L = 16.8mg/dL
        finalUaData = valData / Math.pow(10,valHeadData) * Math.pow(10,3);

        return finalUaData;
    }

    /**
     * 16 进制转为十进制
     */
    public static int hexToDecimal(String hexStr) {
        return Integer.parseInt(hexStr,16);
    }

    /**
     * 2 进制转为十进制
     */
    public static int binaryToDecimal (String hexStr) {
        return Integer.parseInt(hexStr,2);
    }

    /**
     * 十进制取反
     * @param origin
     * @return
     */
    public static int notDecimal(int origin) {
        return ~origin;
    }

    /**
     * 十进制转换为2进制
     * @param origin
     * @return
     */
    public static String decimalToBinary(int origin) {
        return Integer.toBinaryString(origin);
    }

    /**
     * 二进制相加  尿酸最后的数据取反加1
     * @param a
     * @param b
     * @return
     */
    public static String add(String a, String b){
        StringBuilder sb=new StringBuilder();
        int x=0;
        int y=0;
        //进位
        int pre=0;
        //存储进位和另两个位的和
        int sum=0;
        //将两个二进制的数位数补齐,在短的前面添0
        while(a.length()!=b.length()){
            if(a.length()>b.length()){
                b="0"+b;
            }else{
                a="0"+a;
            }
        }
        for(int i=a.length()-1;i>=0;i--){
            x=a.charAt(i)-'0';
            y=b.charAt(i)-'0';
            //从低位做加法
            sum=x+y+pre;
            //进位
            if(sum>=2){
                pre=1;
                sb.append(sum-2);
            }else{
                pre=0;
                sb.append(sum);
            }
        }
        if(pre==1){
            sb.append("1");
        }
        //翻转返回
        return sb.reverse().toString();
    }

    /**
     * byte[]转换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
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
        return stringBuilder.toString().toUpperCase();
    }


}
