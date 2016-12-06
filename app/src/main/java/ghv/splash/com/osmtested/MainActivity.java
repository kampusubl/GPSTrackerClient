package ghv.splash.com.osmtested;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.JsonParser;
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
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    MapView osm;
    MapController mc;
    LocationManager locationManager;
    TextView text;

    String userName = "GPSTracker";
    String password = "GPSTracker";
    String virtualHost = "/GPSTracker";
    String serverIp = "167.205.7.229";
    int port = 5672;

    GeoPoint center;
    private static final String QUEUE_NAME = "bits";
    Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.test);
        viewMap();
        rabbit();
    }

    private void viewMap(){
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        osm = (MapView) findViewById(R.id.map);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mc = (MapController) osm.getController();
        mc.setZoom(20);

        center = new GeoPoint(-5.397140, 105.266789);
        mc.animateTo(center);
        addMarker(center);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void addMarker(GeoPoint geoPoint) {

        //String a = text.getText().toString();
        marker = new Marker(osm);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.marker_station));
        //marker.setTitle(a);
        //rabbit();
        osm.getOverlays().clear();
        osm.getOverlays().add(marker);
        osm.invalidate();
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
                    Connection connection;
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
                            String message = new String(body, "UTF-8");
                            //System.out.println("Java Queue - Message Received '" + message + "'");
                            //text.append("\n"+message);
                            marker.setTitle(message);
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
    }
}
