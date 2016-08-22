package ca.uwaterloo.watca;

import com.yahoo.ycsb.*;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Wojciech Golab
 */
public class YCSBConnectorWrapper extends DB {

    static private final String KEY_NOT_FOUND = "key_not_found";
    static private final String NO_DATA = "no_data";
    static private final Lock lock = new ReentrantLock();
    static private BufferedWriter out;
    static private int numThreads = 0;
    private DB innerDB;
    static private long readDelay;
    static private long writeDelay;

    static private long maxExecutionTime;
    static private long startTime;

    static private AtomicBoolean done = new AtomicBoolean(false);
    static private String connectorClassName;

    public YCSBConnectorWrapper() throws DBException {
        try {
            connectorClassName = System.getProperties().getProperty("analysis.ConnectorClass");
            innerDB = (DB) Class.forName(connectorClassName).newInstance();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new DBException("Unable to instantiate wrapped connector object.", e);
        }
    }

    public void setProperties(Properties p) {
        innerDB.setProperties(p);
    }

    public Properties getProperties() {
        return innerDB.getProperties();
    }

    @Override
    public void init() throws DBException {
        innerDB.init();

        try {
            lock.lock();

            if (startTime == 0) {
                String MAX_EXECUTION_TIME = "maxexecutiontime";
                maxExecutionTime = Integer.parseInt(getProperties().getProperty(MAX_EXECUTION_TIME, "0"));
                startTime = System.currentTimeMillis();
            }

            if (out == null) {
                readDelay = Integer.parseInt(getProperties().getProperty("readdelay", "0"));
                writeDelay = Integer.parseInt(getProperties().getProperty("writedelay", "0"));
                System.out.println("Read delay: " + readDelay);
                System.out.println("Write delay: " + writeDelay);
                System.out.println("Read consistency level: " + getProperties().getProperty("cassandra.readconsistencylevel", "ONE"));
                System.out.println("Write consistency level: " + getProperties().getProperty("cassandra.writeconsistencylevel", "ONE"));
                String logHost = System.getProperties().getProperty("analysis.LogHost");
                String logPort = System.getProperties().getProperty("analysis.LogPort");
                String logFileName = System.getProperties().getProperty("analysis.LogFile");
                if (logHost == null || logPort == null) {
		    System.out.println("Opening log file: " + logFileName);
                    out = new BufferedWriter(new FileWriter(logFileName, true));
		    System.out.println("Opened log file: " + logFileName);
                } else {
		    System.out.println("Opening log stream: " + logHost + ":" + logPort);
                    Socket socket = new Socket(logHost, Integer.parseInt(logPort));
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    System.out.println("Opened log stream: " + logHost + ":" + logPort);
                }
                numThreads++;
            }
        } catch (IOException e) {
            throw new DBException("Unable to open consistency log.", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cleanup() throws DBException {
        done.set(true);
        innerDB.cleanup();
        try {
            lock.lock();
            if (out != null) {
                out.flush();
            }
        } catch (IOException e) {
            throw new DBException("Unable to flush consistency log file.", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            try {
                lock.lock();
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to close consistency log file.", e);
            } finally {
                lock.unlock();
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public Status read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
        long start, finish;
        start = System.currentTimeMillis();
        try {
            Thread.sleep(readDelay);
        } catch (InterruptedException ex) {
        }
	long now = System.currentTimeMillis();
        Status ret = Status.OK;

	ret = innerDB.read(table, key, fields, result);
	finish = System.currentTimeMillis();
	String value = "key_not_found";
	if (!result.isEmpty()) {
	    ByteIterator b;
	    if (connectorClassName.contains("CassandraCQLClient")) {
		// for cassandra2-binding use "field0" as key to access
		// "value", "y_id" as key to access "key" in result:
		b = result.get("field0");
	    } else {
		b = result.get(result.keySet().iterator().next());
	    }
	    value = new String(b.toArray());
	}
	long endTime = System.currentTimeMillis();
	// only log operations that return proper values
	//if (!value.equals("key_not_found"))
	logOperation("R", key, value, start, finish);
        return ret;
    }

    @Override
    public Status scan(String table, String startkey, int recordcount, Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
        return innerDB.scan(table, startkey, recordcount, fields, result);
    }

    @Override
    public Status update(String table, String key, HashMap<String, ByteIterator> values) {
        long start, finish;
        start = System.currentTimeMillis();
        HashMap<String, String> values2 = StringByteIterator.getStringMap(values);
        Status ret = innerDB.update(table, key, StringByteIterator.getByteIteratorMap(values2));
        try {
            Thread.sleep(writeDelay);
        } catch (InterruptedException ex) {
        }
        finish = System.currentTimeMillis();
        String value = values2.get(values2.keySet().iterator().next());
        logOperation("U", key, value, start, finish);
        return ret;
    }

    @Override
    public Status insert(String table, String key, HashMap<String, ByteIterator> values) {
        long start, finish;
        start = System.currentTimeMillis();
        HashMap<String, String> values2 = StringByteIterator.getStringMap(values);
        Status ret = innerDB.insert(table, key, StringByteIterator.getByteIteratorMap(values2));
        try {
            Thread.sleep(writeDelay);
        } catch (InterruptedException ex) {
        }
        finish = System.currentTimeMillis();
        String value = values2.get(values2.keySet().iterator().next());
        logOperation("I", key, value, start, finish);
        return ret;
    }

    @Override
    public Status delete(String table, String key) {
        return innerDB.delete(table, key);
    }

    private void logOperation(String operationType, String key, String value, long startTime, long finishTime) {
        try {
            lock.lock();
            if (out != null) {
                out.write(key);
                out.write("\t");
                if (value.equals(KEY_NOT_FOUND)) {
                    out.write(KEY_NOT_FOUND);
                } else if (value.equals("")) {
                    out.write(NO_DATA);
                } else {
                    out.write(sha1(value));
                }
                out.write("\t");
                out.write(String.valueOf(startTime));
                out.write("\t");
                out.write(String.valueOf(finishTime));
                out.write("\t");
                out.write(operationType);
                out.newLine();
            }
        } catch (Exception e) {
            String logFileName = System.getProperties().getProperty("analysis.LogFile");
            e.printStackTrace();
            throw new RuntimeException("Unable to write consistency log file " + logFileName, e);
        } finally {
            lock.unlock();
        }
    }

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();

    }
}
