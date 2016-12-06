package ghv.splash.com.osmtested;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RabbitMQClient extends AppCompatActivity {

    RabbitMQTestted request;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rabbit_mqclient);

        request = new RabbitMQTestted(context);
        request.connectToRabbitMQ();
    }
}
