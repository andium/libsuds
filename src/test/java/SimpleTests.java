import com.andium.unix.socket.SimpleUnixDomainSocket;
import com.andium.unix.socket.SimpleUnixDomainSocketClient;
import com.andium.unix.socket.SimpleUnixDomainSocketServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.andium.unix.socket.SimpleUnixDomainSocket.SOCK_STREAM;

/**
 * Created by @Author lukewhittington
 * on 2017/03/21
 * Copyright of Andium, Inc.
 */
public class SimpleTests {

    private SimpleUnixDomainSocketServer server;
    private int conns = 5;
    private final String message = "message";
    private final String testSock = "test.sock";

    @Before
    @Test
    public void createServerInstance(){
        try {
            server = new SimpleUnixDomainSocketServer(testSock, SOCK_STREAM, conns);
        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void cleanUpServerInstance(){
        try {
            server.close();
            File f = new File(testSock);
            if (f.exists()){
                f.delete();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testUnidirectionalComms(){
        try {
            ExecutorService pool = Executors.newFixedThreadPool(2);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try (SimpleUnixDomainSocket s = server.accept();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))){
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                    } catch (Exception e){
                        e.printStackTrace();
                        Assert.fail();
                    }
                }
            });

            Future<String> client = pool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String result = "we didn't get a message";
                    try (SimpleUnixDomainSocketClient sockClient = createClient();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(sockClient.getInputStream()))){
                        result = reader.readLine();
                    } catch (Exception e){
                        e.printStackTrace();
                        Assert.fail();
                    }

                    return result;
                }
            });

            String result = client.get(10, TimeUnit.SECONDS);
            Assert.assertEquals(result, message);

        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testBidirectionalComms(){
        try {
            ExecutorService pool = Executors.newFixedThreadPool(2);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try (SimpleUnixDomainSocket s = server.accept();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                         BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()))){
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                        String response = reader.readLine();
                        Assert.assertEquals(response, message);

                    } catch (Exception e){
                        e.printStackTrace();
                        Assert.fail();
                    }
                }
            });

            Future<String> client = pool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String result = "we didn't get a message";
                    try (SimpleUnixDomainSocketClient sockClient = createClient();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(sockClient.getInputStream()));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sockClient.getOutputStream()))){
                        result = reader.readLine();
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                    } catch (Exception e){
                        e.printStackTrace();
                        Assert.fail();
                    }

                    return result;
                }
            });

            String result = client.get(10, TimeUnit.SECONDS);
            Assert.assertEquals(result, message);

        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testSimultaneousClients(){
        try {
            ExecutorService pool = Executors.newFixedThreadPool(2);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            SimpleUnixDomainSocket s = server.accept();
                            Executors.newSingleThreadExecutor().submit(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Client connected");
                                }
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                            Assert.fail();
                        }
                    }
                }
            });

            List<Future<Boolean>> futureList = new ArrayList<>();
            ExecutorService futureService = Executors.newFixedThreadPool(5);
            for (byte i = 0; i < 5; i++){
                futureList.add(getClientConn(futureService));
            }

            for (Future<Boolean> f : futureList){
                Assert.assertTrue(f.get());
            }

            pool.shutdownNow();

        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    private Future<Boolean> getClientConn(ExecutorService service){
        return service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try (SimpleUnixDomainSocketClient c = createClient()){
                    Thread.sleep(1000);
                } catch (Exception e){
                    e.printStackTrace();
                    Assert.fail();
                }
                return true;
            }
        });
    }

    @Test
    public void createClientTest(){
        try (SimpleUnixDomainSocketClient c = createClient()){
            // do something?
        } catch (IOException e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void setTimeoutTest(){
        try {
            server.setSoTimeout(10000);
        } catch (Exception e){
            e.printStackTrace();
            Assert.fail("Failed to set timeout on server socket");
        }

        try (SimpleUnixDomainSocketClient c = createClient()) {
            c.setSoTimeout(10000);
        } catch (Exception e){
            e.printStackTrace();
            Assert.fail("Failed to set timeout on client socket");
        }
    }

    SimpleUnixDomainSocketClient createClient() throws IOException{
        return new SimpleUnixDomainSocketClient(testSock, SOCK_STREAM);
    }


}
