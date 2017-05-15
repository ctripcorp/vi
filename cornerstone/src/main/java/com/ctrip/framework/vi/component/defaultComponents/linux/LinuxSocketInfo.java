package com.ctrip.framework.cornerstone.component.defaultComponents.linux;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.annotation.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static com.ctrip.framework.cornerstone.util.LinuxInfoUtil.parseIp;

/**
 * Created by jiang.j on 2016/7/28.
 */
@ComponentStatus(id="vi.linux.socketinfo",name="netstats",description = "应用网络链接信息",list = true)
public class LinuxSocketInfo {

    static transient Logger _logger = LoggerFactory.getLogger(LinuxSocketInfo.class);
    /**
     * TCP_ESTABLISHED = 1,
     * TCP_SYN_SENT = 2,
     * TCP_SYN_RECV = 3,
     * TCP_FIN_WAIT1 = 4,
     * TCP_FIN_WAIT2 = 5,
     * TCP_TIME_WAIT = 6,
     * TCP_CLOSE = 7,
     * TCP_CLOSE_WAIT = 8,
     * TCP_LAST_ACK = 9,
     * TCP_LISTEN = 10,
     * TCP_CLOSING = 11,
     * <p/>
     * unix_LISTENING = 01,
     * unix_CONNECTED = 03,
     */
    @FieldInfo(name = "local ip",description = "本地地址")
    String local_address;
    @FieldInfo(name = "local port",description = "本地端口")
    String local_port;
    @FieldInfo(name = "remote ip",description = "远端地址")
    String rem_address;
    @FieldInfo(name = "remote port",description = "远端端口")
    String rem_port;
    @FieldInfo(name = "state",description = "状态")
    String state = null;
    @FieldInfo(name = "protocol",description = "协议")
    String protocol;
    @FieldInfo(name = "user id",description = "用户id")
    String uid;
    public LinuxSocketInfo() {           //create a blank socket..just for the hell of it ie debugging

    }

    public static List<LinuxSocketInfo> getSockets(String type) throws FileNotFoundException {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        String pid = (jvmName.substring(0, index));
        ArrayList<LinuxSocketInfo> sockets = new ArrayList<>();
        FileReader tcp = null;
        LineNumberReader lnr = null;
        String line = null;

        tcp = new FileReader("/proc/" + pid + "/net/"+type);
        lnr = new LineNumberReader(tcp);
        try {
            lnr.readLine();
            while ((line=lnr.readLine())!=null) {
                LinuxSocketInfo socketInfo =parseSocket(line.trim());
                socketInfo.protocol = type;
                sockets.add(socketInfo);
            }
        } catch (Throwable e) {
            _logger.warn("read linux tcp file failed",e);
        }
        return sockets;
    }

    public static List<LinuxSocketInfo> list() throws FileNotFoundException {
        List<LinuxSocketInfo> rtn = new ArrayList<>();
        rtn.addAll(getSockets("tcp"));
        rtn.addAll(getSockets("tcp6"));
        rtn.addAll(getSockets("udp"));
        rtn.addAll(getSockets("udp6"));
        return rtn;
    }

    public static LinuxSocketInfo parseSocket(String line) {          //parse socket entries

        LinuxSocketInfo socketInfo = new LinuxSocketInfo();
        String[] parts = line.split("\\s+");
        Object[] localIp = parseIp(parts[1]);
        socketInfo.local_address = (String) localIp[0];

        socketInfo.local_port =  localIp[1].toString();

        Object[] remoteIp = parseIp(parts[2]);
        socketInfo.rem_address = (String) remoteIp[0];

        socketInfo.rem_port =  remoteIp[1].toString();

        int st = Integer.valueOf(parts[3],16).intValue();          //get the state number and convert to int from hex

        switch (st) {
            case 0:
                socketInfo.state = null;
                break;
            case 1:
                socketInfo.state = "ESTABLISHED";
                break;
            case 2:
                socketInfo.state = "SYN_SENT";
                break;
            case 3:
                socketInfo.state = "SYN_RECV";
                break;
            case 4:
                socketInfo.state = "FIN_WAIT1";
                break;
            case 5:
                socketInfo.state = "FIN_WAIT2";
                break;
            case 6:
                socketInfo.state = "TIME_WAIT";
                break;
            case 7:
                socketInfo.state = "CLOSE";
                break;
            case 8:
                socketInfo.state = "CLOSE_WAIT";
                break;
            case 9:
                socketInfo.state = "LAST_ACK";
                break;
            case 10:
                socketInfo.state = "LISTEN";
                break;
            case 11:
                socketInfo.state = "CLOSING";
                break;

        }
        socketInfo.uid = (parts[7]);
        return socketInfo;
    }
}
