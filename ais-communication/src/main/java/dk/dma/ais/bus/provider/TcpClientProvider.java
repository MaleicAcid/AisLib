/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.bus.provider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.bus.AisBusProvider;
import dk.dma.ais.bus.tcp.IClientStoppedListener;
import dk.dma.ais.bus.tcp.TcpClient;
import dk.dma.ais.bus.tcp.TcpClientConf;
import dk.dma.ais.bus.tcp.TcpReadClient;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.util.function.Consumer;

/**
 * Round robin TCP client provider
 */
@ThreadSafe
public final class TcpClientProvider extends AisBusProvider implements Runnable, Consumer<AisPacket>, IClientStoppedListener {

    private static final Logger LOG = LoggerFactory.getLogger(TcpClientProvider.class);

    @GuardedBy("this")
    private TcpReadClient readClient;

    private TcpClientConf clientConf = new TcpClientConf();
    private List<String> hostsPorts = new ArrayList<>();
    private int reconnectInterval = 10;
    private int timeout = 10;

    private List<String> hostnames = new ArrayList<>();
    private List<Integer> ports = new ArrayList<>();
    private int currentHost = -1;

    public TcpClientProvider() {
        super();
    }

    @Override
    public void run() {
        while (true) {
            Socket socket = new Socket();

            // Get next host and port
            selectHost();
            String host = hostnames.get(currentHost);
            int port = ports.get(currentHost);

            // Connect
            try {
                InetSocketAddress address = new InetSocketAddress(host, port);
                LOG.info("Connecting to " + host + ":" + port + " ...");
                socket.connect(address);
                socket.setKeepAlive(true);
                if (timeout > 0) {
                    socket.setSoTimeout(timeout * 1000);
                }

                // Start client
                synchronized (this) {
                    readClient = new TcpReadClient(this, this, socket, clientConf);
                }
                readClient.start();

                // Wait for client to loose connection
                readClient.join();

                synchronized (this) {
                    readClient = null;
                }

            } catch (IOException e) {
                LOG.info("Connection error: " + e.getMessage());
            } catch (InterruptedException e) {
                // TODO handle
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }

            try {
                LOG.info("Waiting to reconnect");
                Thread.sleep(reconnectInterval * 1000);
            } catch (InterruptedException e) {
                // TODO handle interuption
                e.printStackTrace();
            }

        }
    }

    @Override
    public void accept(AisPacket packet) {
        push(packet);
    }

    @Override
    public synchronized void start() {
        // Start self as thread
        Thread t = new Thread(this);
        setThread(t);
        t.start();
        super.start();
    }

    @Override
    public synchronized void init() {
        for (String hostPort : hostsPorts) {
            String[] parts = StringUtils.split(hostPort, ':');
            hostnames.add(parts[0]);
            ports.add(Integer.parseInt(parts[1]));
        }
        super.init();
    }

    public List<String> getHostsPorts() {
        return hostsPorts;
    }

    public void setHostsPorts(List<String> hostsPorts) {
        this.hostsPorts = hostsPorts;
    }

    public void setClientConf(TcpClientConf clientConf) {
        this.clientConf = clientConf;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    @Override
    public void clientStopped(TcpClient client) {

    }

    private void selectHost() {
        currentHost = (currentHost + 1) % hostnames.size();
    }

}
