import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Doctor {

    private Integer ID;

    public Doctor(){
        this.ID = new Random().nextInt(100);
    }

    public static void main(String[] args) throws Exception{

        Doctor doctor = new Doctor();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String EXCHANGE_NAME = "exchange1";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        System.out.println("Doctor ID:" + doctor.getID().toString());

        String QUEUE_NAME = "ResultQueue" + doctor.getID().toString();
        System.out.println("created queue: " + QUEUE_NAME);

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, doctor.getID().toString());

        Receiver receiver = new Receiver(channel, QUEUE_NAME, null);
        Thread rec = new Thread(receiver);
        rec.start();

        while (true) {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            String msg = doctor.getID()+ ":" + message;

            String[] types = message.split(":");
            channel.basicPublish(EXCHANGE_NAME, types[1], null, msg.getBytes("UTF-8"));
            System.out.println("Sent: " + message);
        }

    }

    Integer getID(){
        return this.ID;
    }

    static class Receiver implements Runnable{

        Channel channel;
        Channel channel3;
        String QUEUE_NAME;
        public Receiver(Channel channel, String QUEUE_NAME, Channel channel3) {
            this.channel = channel;
            this.QUEUE_NAME = QUEUE_NAME;
            this.channel3 = channel3;
        }
        @Override
        public void run() {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println("Received: " + message);
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
