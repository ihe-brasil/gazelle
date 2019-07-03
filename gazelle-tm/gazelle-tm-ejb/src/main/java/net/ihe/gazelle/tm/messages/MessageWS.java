package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

@Stateless
@Name("gazelleMessageWS")
@WebService(name = "GazelleMessageWS", serviceName = "GazelleMessageWSService", portName = "GazelleMessageWSPort")
public class MessageWS implements MessageWSRemote {

    @Override
    public void sendMessage(@WebParam(name = "sendUsername") String sendUsername,
                            @WebParam(name = "message") String message, @WebParam(name = "link") String link,
                            @WebParam(name = "usernames") List<String> usernames) {
        String usernameList = StringUtils.join(usernames, " ");
        User.remoteLogin.set(sendUsername);
        User user = User.FindUserWithUsername(sendUsername);
        MessageManager.addMessage(SimpleMessageSource.INSTANCE, user, usernameList, message, link);
    }

}
