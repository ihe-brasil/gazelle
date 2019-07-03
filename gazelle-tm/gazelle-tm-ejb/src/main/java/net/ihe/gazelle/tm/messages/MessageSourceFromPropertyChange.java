package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.messaging.MessagePropertyChanged;

public interface MessageSourceFromPropertyChange<U, V> {

    Class<U> getPropertyObjectClass();

    String getPropertyName();

    void receivePropertyMessage(MessagePropertyChanged<U, V> messagePropertyChanged);

}
