package ch.ethz.inf.asl.common;

public enum MessageType {
    // REPEATED CONSTANTS NAMES .. TODO FIXME combine them with names form MWMessagingProtocolImpl
    CREATE_QUEUE, DELETE_QUEUE, SEND_MESSAGE, RECEIVE_MESSAGE, READ_MESSAGE,
    RECEIVE_MESSAGE_FROM_SENDER, LIST_QUEUES
}
