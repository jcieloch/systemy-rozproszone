import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Technician {

    private String examination1;
    private String examination2;
    private Integer ID;

    public Technician(String examination1, String examination2){
        this.examination1 = examination1;
        this.examination2 = examination2;
        this.ID = new Random().nextInt(100);
    }

    public static void main(String[] args) throws Exception{

        String examination1 = "knee";
        //String examination1 = "hip";
        String examination2 = "elbow";
        Technician technician = new Technician(examination1, examination2);

        System.out.println("Technician ID: " + technician.getID());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        channel1.basicQos(1);
        channel2.basicQos(1);

        String EXCHANGE_NAME = "exchange1";
        channel1.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        String QUEUE_NAME1 = examination1 + "Queue";
        System.out.println("created queue: " + QUEUE_NAME1);

        channel1.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel1.queueBind(QUEUE_NAME1, EXCHANGE_NAME, examination1);

        String QUEUE_NAME2 = examination2 + "Queue";
        System.out.println("created queue: " + QUEUE_NAME2);

        channel2.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel2.queueBind(QUEUE_NAME2, EXCHANGE_NAME, examination2);

        Receiver receiver1 = new Receiver(channel1, QUEUE_NAME1, EXCHANGE_NAME, technician);
        Thread rec1 = new Thread(receiver1);
        rec1.start();

        Receiver receiver2 = new Receiver(channel2, QUEUE_NAME2, EXCHANGE_NAME, technician);
        Thread rec2 = new Thread(receiver2);
        rec2.start();
    }

    public Integer getID(){
        return this.ID;
    }

    static class Receiver implements Runnable{

        Channel channel;
        Technician technician;
        String QUEUE_NAME;
        String EXCHANGE_NAME;
        public Receiver(Channel channel, String QUEUE_NAME, String EXCHANGE_NAME, Technician technician) {
            this.channel = channel;
            this.QUEUE_NAME = QUEUE_NAME;
            this.EXCHANGE_NAME = EXCHANGE_NAME;
            this.technician = technician;
        }
        @Override
        public void run() {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");

                    String[] types = message.split(":");
                    String replay = types[1] + " " + types[2] + " Done from " + technician.getID();

                    channel.basicPublish(EXCHANGE_NAME, types[0], null, replay.getBytes("UTF-8"));
                    System.out.println("Sent: " + message);
                }
            };

            System.out.println("Waiting for messages...");
            try {
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

