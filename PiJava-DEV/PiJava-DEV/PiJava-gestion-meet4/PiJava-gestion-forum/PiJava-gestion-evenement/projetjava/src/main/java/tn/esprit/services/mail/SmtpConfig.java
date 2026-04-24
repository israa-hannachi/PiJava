package tn.esprit.services.mail;

public class SmtpConfig {
    private final String host;
    private final int port;
    private final boolean startTls;

    public SmtpConfig(String host, int port, boolean startTls) {
        this.host = host;
        this.port = port;
        this.startTls = startTls;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public static SmtpConfig gmail() {
        return new SmtpConfig("smtp.gmail.com", 587, true);
    }
}
