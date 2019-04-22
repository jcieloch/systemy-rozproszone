import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {


    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        DistributedMap map = new DistributedMap("c");
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inp);
        String msg = "";

        while(!msg.equals("q")) {
            msg = reader.readLine();
            String[] words = msg.split("\\s");
            if (words[0].equals("put")) {
                map.put(words[1], Integer.parseInt(words[2]));
                System.out.println("put " + words[2]);
            } else if (words[0].equals("get")) {
                System.out.println(map.get(words[1]));
            } else if (words[0].equals("remove")) {
                System.out.println(map.remove(words[1]));
            } else if (words[0].equals("containsKey")) {
                System.out.println(map.containsKey(words[1]));
            } else {
                if (msg.equals("q")) {
                    break;
                }

                System.out.println("Wrong message");
            }
        }

        reader.close();
    }
}
