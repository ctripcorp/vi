package com.ctrip.framework.vi.util;


import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiang.j on 2016/5/3.
 */
public final class IPUtil {
    private final static int INADDR4SZ = 4;

    public static boolean internalIp(String ip) {
        if(ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return true;
        }

        byte[] addr = textToNumericFormatV4(ip);
        return internalIp(addr);
    }
    private static byte[] textToNumericFormatV4(String src)
    {
        if (src.length() == 0) {
            return null;
        }
        byte[] res = new byte[INADDR4SZ];
        String[] s = src.split("\\.", -1);
        long val;
        try {
            switch(s.length) {
                case 1:
                /*
                 * When only one part is given, the value is stored directly in
                 * the network address without any byte rearrangement.
                 */
                    val = Long.parseLong(s[0]);
                    if (val <  0 || val  > 0xffffffffL)
                        return null;
                    res[0] = (byte) ((val  >> 24) & 0xff);
                    res[1] = (byte) (((val & 0xffffff)  >> 16) & 0xff);
                    res[2] = (byte) (((val & 0xffff)  >> 8) & 0xff);
                    res[3] = (byte) (val & 0xff);
                    break;
                case 2:
                /*
                 * When a two part address is supplied, the last part is
                 * interpreted as a 24-bit quantity and placed in the right
                 * most three bytes of the network address. This makes the
                 * two part address format convenient for specifying Class A
                 * network addresses as net.host.
                 */
                    val = Integer.parseInt(s[0]);
                    if (val <  0 || val  > 0xff)
                        return null;
                    res[0] = (byte) (val & 0xff);
                    val = Integer.parseInt(s[1]);
                    if (val <  0 || val  > 0xffffff)
                        return null;
                    res[1] = (byte) ((val  >> 16) & 0xff);
                    res[2] = (byte) (((val & 0xffff)  >> 8) &0xff);
                    res[3] = (byte) (val & 0xff);
                    break;
                case 3:
                /*
                 * When a three part address is specified, the last part is
                 * interpreted as a 16-bit quantity and placed in the right
                 * most two bytes of the network address. This makes the
                 * three part address format convenient for specifying
                 * Class B net- work addresses as 128.net.host.
                 */
                    for (int i = 0; i <  2; i++) {
                        val = Integer.parseInt(s[i]);
                        if (val <  0 || val  > 0xff)
                            return null;
                        res[i] = (byte) (val & 0xff);
                    }
                    val = Integer.parseInt(s[2]);
                    if (val <  0 || val  > 0xffff)
                        return null;
                    res[2] = (byte) ((val  >> 8) & 0xff);
                    res[3] = (byte) (val & 0xff);
                    break;
                case 4:
                /*
                 * When four parts are specified, each is interpreted as a
                 * byte of data and assigned, from left to right, to the
                 * four bytes of an IPv4 address.
                 */
                    for (int i = 0; i <  4; i++) {
                        val = Integer.parseInt(s[i]);
                        if (val <  0 || val  > 0xff)
                            return null;
                        res[i] = (byte) (val & 0xff);
                    }
                    break;
                default:
                    return null;
            }
        } catch(NumberFormatException e) {
            return null;
        }
        return res;
    }
    private static boolean internalIp(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;
        }
    }

    public static int parseIP(String address) {

        String[] parts = address.split("\\.");
        return (Integer.parseInt(parts[0]) << 24) | (Integer.parseInt(parts[1]) << 16) | (Integer.parseInt(parts[2]) << 8) | (Integer.parseInt(parts[3]));
    }

    public static int getIPV4(HttpServletRequest req){
       return 0;
    }
}
