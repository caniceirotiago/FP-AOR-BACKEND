package aor.fpbackend.dto;

import java.util.List;

public class IndividualMessageGetPaginatedDto {
    private List<IndividualMessageGetDto> messages;
    private long totalMessages;

    // Getters and setters
    public List<IndividualMessageGetDto> getMessages() {
        return messages;
    }

    public void setMessages(List<IndividualMessageGetDto> messages) {
        this.messages = messages;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
}
