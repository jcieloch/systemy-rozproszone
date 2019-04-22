import java.util.HashMap;

public class DistributedMap implements SimpleStringMap {
    private HashMap<String, Integer> map = new HashMap();
    private Connection channel = new Connection();

    public DistributedMap(String channelName) throws Exception {
        this.channel.init(channelName, this);
    }

    public HashMap<String, Integer> getMapState() {
        return this.map;
    }

    public void setMapState(HashMap<String, Integer> map) {
        this.map = map;
    }

    public void clear() {
        this.map.clear();
    }

    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    public Integer get(String key) {
        return (Integer)this.map.get(key);
    }

    public void put(String key, Integer value) {
        try {
            this.channel.send("put " + key + " " + value);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        this.map.put(key, value);
    }

    public Integer remove(String key) {
        try {
            this.channel.send("remove " + key);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        Integer i = (Integer)this.map.remove(key);
        if (i == null) {
            System.out.println("ASHDA");
        }

        return i;
    }
}
