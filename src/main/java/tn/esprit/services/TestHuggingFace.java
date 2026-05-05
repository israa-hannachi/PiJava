package tn.esprit.services;

public class TestHuggingFace {
    public static void main(String[] args) {
        System.out.println("Testing HuggingFaceService...");
        HuggingFaceService service = HuggingFaceService.getInstance();
        System.out.println("Sending message...");
        String response = service.sendMessage("c'est quoi symfony?", "Français");
        System.out.println("Response: " + response);
    }
}
