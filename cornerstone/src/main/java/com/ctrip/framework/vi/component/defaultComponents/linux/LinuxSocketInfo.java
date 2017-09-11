package com.ctrip.framework.vi.component.defaultComponents.linux;

import com.ctrip.framework.vi.annotation.ComponentStatus;
import com.ctrip.framework.vi.annotation.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static com.ctrip.framework.vi.util.LinuxInfoUtil.parseIp;

/**
 * Created by jiang.j on 2016/7/28.
 */
@ComponentStatus(id="vi.linux.socketinfo",name="netstats",description = "应用网络链接信息",list = true,auto = false)
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
    public final String local_address;
    @FieldInfo(name = "local port",description = "本地端口")
    public final String local_port;
    @FieldInfo(name = "remote ip",description = "远端地址")
    public final String rem_address;
    @FieldInfo(name = "remote port",description = "远端端口")
    public final String rem_port;
    @FieldInfo(name = "state",description = "状态")
    public final String state;
    @FieldInfo(name = "protocol",description = "协议")
    public final String protocol;
    @FieldInfo(name = "user id",description = "用户id")
    public final String uid;
    public LinuxSocketInfo(String local_address,String local_port,String rem_address,String rem_port,
                           String state,String protocol,String uid) {           //create a blank socket..just for the hell of it ie debugging

        this.local_address = local_address;
        this.local_port = local_port;
        this.rem_address = rem_address;
        this.rem_port = rem_port;
        this.state = state;
        this.protocol = protocol;
        this.uid = uid;
    }

    public static List<LinuxSocketInfo> getSockets(String type) throws FileNotFoundException {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        String pid = (jvmName.substring(0, index));
        ArrayList<LinuxSocketInfo> sockets = new ArrayList<>();
        String line;

        try(FileReader tcp = new FileReader("/proc/" + pid + "/net/"+type);LineNumberReader lnr = new LineNumberReader(tcp)) {
            lnr.readLine();
            while ((line=lnr.readLine())!=null) {
                LinuxSocketInfo socketInfo =parseSocket(line.trim(),type);
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

    public static LinuxSocketInfo parseSocket(String line,String type) {          //parse socket entries

        String[] parts = line.split("\\s+");
        Object[] localIp = parseIp(parts[1]);
        String local_address = (String) localIp[0];

        String local_port =  localIp[1].toString();

        Object[] remoteIp = parseIp(parts[2]);
        String rem_address = (String) remoteIp[0];

        String rem_port =  remoteIp[1].toString();
        String state = null;

        int st = Integer.valueOf(parts[3], 16);          //get the state number and convert to int from hex

        switch (st) {
            case 0:
                state = null;
                break;
            case 1:
                state = "ESTABLISHED";
                break;
            case 2:
                state = "SYN_SENT";
                break;
            case 3:
                state = "SYN_RECV";
                break;
            case 4:
                state = "FIN_WAIT1";
                break;
            case 5:
                state = "FIN_WAIT2";
                break;
            case 6:
                state = "TIME_WAIT";
                break;
            case 7:
                state = "CLOSE";
                break;
            case 8:
                state = "CLOSE_WAIT";
                break;
            case 9:
                state = "LAST_ACK";
                break;
            case 10:
                state = "LISTEN";
                break;
            case 11:
                state = "CLOSING";
                break;

        }
        String uid = (parts[7]);
        return new LinuxSocketInfo(local_address,local_port,rem_address,rem_port,state,type,uid);
    }
}
