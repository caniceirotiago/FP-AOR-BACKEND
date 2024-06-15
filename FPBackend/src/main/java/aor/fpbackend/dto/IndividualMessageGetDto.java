package aor.fpbackend.dto;

public class IndividualMessageGetDto {

        private Long id;

        private String content;

        private Long senderId;

        private Long recipientId;

        private String subject;

        public IndividualMessageGetDto() {
        }

        public IndividualMessageGetDto(Long id, String content, Long senderId, Long recipientId, String subject) {
            this.id = id;
            this.content = content;
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.subject = subject;
        }

        // Getters and setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
}
