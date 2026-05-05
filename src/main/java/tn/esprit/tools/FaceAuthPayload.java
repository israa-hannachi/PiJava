package tn.esprit.tools;

public class FaceAuthPayload {

    private String mode;
    private String email;
    private Object descriptor;

    public FaceAuthPayload() {
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescriptor() {
        if (descriptor == null) return null;
        if (descriptor instanceof String) return (String) descriptor;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(descriptor);
        } catch (Exception e) {
            return descriptor.toString();
        }
    }

    public void setDescriptor(Object descriptor) {
        this.descriptor = descriptor;
    }
}
