package com.example.michiel.mobapp_michielvanbergen;

public class Contact {
    private String contactKey = null;
    private String name = null;
    private String lastMessage = null;
    private String conversationKey = null;

    public void setContactKey(String contactKey)     {
        this.contactKey = contactKey;
    }
    public String getContactKey() {
        return contactKey;
    }

    public void setName(String name)     {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setLastMessage(String lastMessage)     {
        this.lastMessage = lastMessage;
    }
    public String getLastMessage() {
        return lastMessage;
    }

    public void setConversationKey(String conversationKey)     {
        this.conversationKey = conversationKey;
    }
    public String getConversationKey() {
        return conversationKey;
    }

}
