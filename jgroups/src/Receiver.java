import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class Receiver extends ReceiverAdapter {
    private DistributedMap map;
    private JChannel channel;

    Receiver(JChannel channel, DistributedMap map) {
        this.channel = channel;
        this.map = map;
    }

    public void receive(Message msg) {
        String message = (String)msg.getObject();
        System.out.println("Received message from " + msg.getSrc() + " - " + message);
        String[] words = message.split("\\s");
        if (words[0].equals("put")) {
            this.map.getMapState().put(words[1], Integer.parseInt(words[2]));
        } else if (words[0].equals("remove")) {
            if (this.map.getMapState().remove(words[1]) == null) {
                System.out.println("ASFGADFA");
            }

            this.map.getMapState().remove(words[1]);
        } else if (words[0].equals("get")) {
            this.map.getMapState().remove(words[1]);
        } else if (words[0].equals("containsKey")) {
            this.map.getMapState().remove(words[1]);
        }

    }

    public void getState(OutputStream output) throws Exception {
        DistributedMap var2 = this.map;
        synchronized(this.map) {
            Util.objectToStream(this.map.getMapState(), new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        HashMap<String, Integer> newMap = (HashMap)Util.objectFromStream(new DataInputStream(input));
        DistributedMap var3 = this.map;
        synchronized(this.map) {
            this.map.clear();
            this.map.setMapState(newMap);
        }
    }

    public void viewAccepted(View view) {
        System.out.println("received view " + view);
        if (view instanceof MergeView) {
            Receiver.ViewHandler handler = new Receiver.ViewHandler(this.channel, (MergeView)view);
            handler.start();
        }

    }

    private static class ViewHandler extends Thread {
        JChannel ch;
        MergeView view;

        private ViewHandler(JChannel ch, MergeView view) {
            this.ch = ch;
            this.view = view;
        }

        public void run() {
            View tmp_view = (View)this.view.getSubgroups().get(0);
            Address local_addr = this.ch.getAddress();
            if (!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("Not member of the new primary partition (" + tmp_view + "), will re-acquire the state");

                try {
                    this.ch.getState((Address)null, 30000L);
                } catch (Exception var4) {
                    ;
                }
            } else {
                System.out.println("Not member of the new primary partition (" + tmp_view + "), will do nothing");
            }

        }
    }
}
