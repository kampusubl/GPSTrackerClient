package ghv.splash.com.osmtested;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by Terminator on 05/12/2016.
 */

public class RabbitOSM extends AppCompatActivity {

    MapView osm;
    MapController mc;
    LocationManager locationManager;
    TextView text;
    Marker marker;

    String userName = "GPSTracker";
    String password = "GPSTracker";
    String virtualHost = "/GPSTracker";
    String serverIp = "167.205.7.229";
    int port = 5672;
    private static final String QUEUE_NAME = "bits";
    Connection connection;
    GeoPoint center;
    //double lat,lon;


    private double currentLatitude;
    private double currentLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.test);
        getMessages();
    }

    public void addMarker(double Latitude, double Longitut){
        center = new GeoPoint(Latitude, Longitut);

        osm = (MapView) findViewById(R.id.map);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mc = (MapController) osm.getController();
        mc.setZoom(20);

        mc.animateTo(center);

        marker = new Marker(osm);
        marker.setPosition(center);

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.marker_station));
        //marker.setTitle(a);
        //rabbit();
        osm.getOverlays().clear();
        osm.getOverlays().add(marker);
        osm.invalidate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    public static final ExecutorService threadPool;
    static {
        threadPool = Executors.newCachedThreadPool();
    }

    private void rabbit(){

        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    ConnectionFactory connectionFactory = new ConnectionFactory();
                    connectionFactory.setUsername(userName);
                    connectionFactory.setPassword(password);
                    connectionFactory.setVirtualHost(virtualHost);
                    connectionFactory.setHost(serverIp);
                    connectionFactory.setPort(port);
                    connectionFactory.setAutomaticRecoveryEnabled(true);
                    connection = connectionFactory.newConnection();

                    // creating a channel with first_queue
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare(QUEUE_NAME, "topic");
                    String nameQuee = channel.queueDeclare().getQueue();
                    channel.queueBind(nameQuee, QUEUE_NAME, "bits");
                    //System.out.println("{N|T} Waiting for messages.");

                    // creating the Consumer, that will be receive a message and convert to String
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                                   byte[] body) throws IOException {
                            final String message = new String(body, "UTF-8");
                            //System.out.println("Java Queue - Message Received '" + message + "'");
                            //text.append("\n"+message);
                          //marker.setTitle(message);
                            //.i("test", message);
                            //addMarker(-5.397140,105.266789);
                            //getMessages(message);
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //marker.setTitle(message);
                                        JSONObject object = new JSONObject(message);
                                        JSONArray jsonArray = object.getJSONArray("data");
                                        double lat = jsonArray.getDouble(0);
                                        double lon = jsonArray.getDouble(1);
                                        String speed = object.getString("Speed");
                                        String date  = object.getString("date");
                                        String time  = object.getString("time");
                                        String hasil = "Speed :"+" "+speed +" |Tgl :"+" "+ date +" |Jam :"+" "+time;
                                        addMarker(lat,lon);
                                        marker.setTitle(hasil);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    };
                                }
                            };threadPool.submit(runnable);

                        }
                    };
                    // loop that waits for message
                    channel.basicConsume(QUEUE_NAME, true, consumer);
                    return true;
                }catch (IOException e){
                    System.out.println("RabbitMQ server is Down !");
                    System.out.println(e.getMessage());
                }
                catch (TimeoutException e){
                    e.printStackTrace();
                    return false;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                //running = aBoolean;
            }

        }.execute();
        //getMessages();
    }


    private void getMessages(){
        try {
            rabbit();
            //JSONObject object = new JSONObject(msg);
            //JSONArray array = new JSONArray("data");
            //text.setText(array.toString());
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
