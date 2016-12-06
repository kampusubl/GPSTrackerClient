package ghv.splash.com.osmtested;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RabbitMQTestted {

    protected Channel mChannel = null;
    protected Connection mConnection;
    //private static final String EXCHANGE_NAME = AC.BROKER_OPANG_EXCHANGE_NAME;
    private static final String ACTION_STRING_ACTIVITY = "broadcast_event";


    private String userName;
    private String password;
    private String virtualHost;
    private String serverIp;
    private int port;
    //private PreferencesManager preferencesManager;
    private String corrId = java.util.UUID.randomUUID().toString();
    private String TAG = this.getClass().getSimpleName();
    private Channel pubChannel;


    protected boolean running;

    private Context context;
    private String REPLY_QUEUE;
    private String queueName;

    public RabbitMQTestted(Context context) {
        this.context = context;
        serverIp = context.getString(R.string.BROKER_IP_HOST);
        userName = context.getString(R.string.BROKER_USERNAME);
        password = context.getString(R.string.BROKER_PASSWORD);
        virtualHost = context.getString(R.string.BROKER_VIRTUAL_HOST);
        port = Integer.parseInt(context.getString(R.string.BROKER_PORT));
        //preferencesManager = new PreferencesManager(context);
        //corrId = preferencesManager.getString(AC.PREFS_PROFILE_ID);
    }

    public void dispose(){
        Log.i(TAG, "Dispose Subsciber");

        running = false;

        try {
            if (mConnection!=null)
                mConnection.close();
            if (mChannel != null)
                mChannel.abort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connectToRabbitMQ() {


        if (mChannel != null && mChannel.isOpen()){
            running = true;
        }

        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {

                try{

                    final ConnectionFactory connectionFactory = new ConnectionFactory();

                    connectionFactory.setUsername(userName);
                    connectionFactory.setPassword(password);
                    connectionFactory.setVirtualHost(virtualHost);
                    connectionFactory.setHost(serverIp);
                    connectionFactory.setPort(port);
                    connectionFactory.setAutomaticRecoveryEnabled(true);

                    mConnection = connectionFactory.newConnection();
                    mChannel = mConnection.createChannel();
                    Log.i("Connect To host", serverIp);
                    //        dialog.dismiss();
                    //sendBroadcastConnectStatus("connect to Broker");
                    //    registerChanelHost();
                    //    receiverRespond();

                    return true;

                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                running = aBoolean;
            }


        }.execute();
    }

    public Channel registerChanelHost(){

        try{

            //queueName = AC.BROKER_QUEUE_ID+preferencesManager.getString(AC.PREFS_PROFILE_ID);
            //  queueName = AC.BROKER_QUEUE_ID+"202";
            REPLY_QUEUE = mChannel.queueDeclare(queueName, false, false, true, null).getQueue();
            //mChannel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
            pubChannel = mConnection.createChannel();
        } catch (Exception e){
            e.printStackTrace();
        }
        return mChannel;
    }

    public String getREPLY_QUEUE(){
        return REPLY_QUEUE;
    }

    public void sendOrderMessage(String msg){
        Log.i(TAG, "Publish order");
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(REPLY_QUEUE)
                .correlationId(corrId)
                .build();
        /*try {
            try {
                pubChannel.basicPublish(EXCHANGE_NAME, AC.BROKER_OPANG_ROUTING_KEY_SET_ORDER, props, msg.getBytes("UTF-8"));
            }catch (AlreadyClosedException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void sendSelectedDriverMessage(String msg, String idOjek){
        Log.i(TAG, "Publish selected driver id : "+idOjek);
        //String ojekDriverQueue = AC.BROKER_QUEUE_ID_DRIVER + idOjek;
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(REPLY_QUEUE)
                .correlationId(idOjek)
                .type("approval")
                .build();
        /*try {
            /*try {
          //      pubChannel.basicPublish("", ojekDriverQueue, props, msg.getBytes("UTF-8"));
            }catch (AlreadyClosedException e){
                e.printStackTrace();
            }*//*
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /*private void sendBroadcastConnectStatus(String status) {
        Intent intent = new Intent();
        intent.setAction(AC.ACTION_OPANG_CONNECTED_TO_BROKER);
        intent.putExtra(AC.BROADCAST_OPANG_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }*/

    static final ExecutorService threadPool;

    static {
        threadPool = Executors.newCachedThreadPool();
    }

}
