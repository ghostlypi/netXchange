package directory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Directory {

    public static class User {
        public String name;
        public UUID uuid;

        public User(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    private final HashMap<String, User> users; //Migrate to SQLight DB
    private final HashSet<UUID> uuids;


    public Directory() {
        this.users = new HashMap<>();
        this.uuids = new HashSet<>();
    }

    public boolean add(User u, String code) {
        if (code.length() < 1024)
            return false;
        else if (code.length() > 1024)
            return false;
        else if (uuids.contains(u.uuid))
            return false;

        uuids.add(u.uuid);
        users.put(code, u);
        return true;
    }

    public User getOrDefault(String code) {
        User u = users.get(code);
        if (u == null) {
            u = new User(code, UUID.randomUUID());
            while(!add(u, code)){}
        }
        return u;
    }

    public boolean contains(UUID uuid) {
        return uuids.contains(uuid);
    }

    public String list() {
        String out = "";
        for (User u : users.values())
            out += u.name + ": " +u.uuid + "\n";

        return out;
    }

    public String get(String name) {
        String out = "";
        for (User u : users.values())
            if (u.name.equals(name))
                out += u.name + ": " +u.uuid + "\n";

        return out;
    }

    public String get(UUID uuid) {
        String out = "";
        for (User u : users.values())
            if (u.uuid.equals(uuid))
                out += u.name + ": " +u.uuid + "\n";

        return out;
    }
}
