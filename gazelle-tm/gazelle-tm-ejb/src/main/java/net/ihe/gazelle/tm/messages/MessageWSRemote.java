package net.ihe.gazelle.tm.messages;

import javax.ejb.Remote;
import java.util.List;

@Remote
public interface MessageWSRemote {

    public void sendMessage(String sendUsername, String message, String link, List<String> usernames);

}
