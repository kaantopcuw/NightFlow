package com.nightflow.notificationservice.service;

import com.nightflow.notificationservice.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@nightflow.com}")
    private String fromEmail;

    @Autowired
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        log.info("Preparing email for order: {}", event.orderNumber());
        
        // Basit e-posta simülasyonu (Gerçek SMTP yoksa sadece log)
        StringBuilder body = new StringBuilder();
        body.append("Merhaba!\n\n");
        body.append("Siparişiniz başarıyla oluşturuldu.\n\n");
        body.append("Sipariş No: ").append(event.orderNumber()).append("\n");
        body.append("Toplam Tutar: ").append(event.totalAmount()).append(" TL\n\n");
        body.append("Bilet Detayları:\n");
        
        event.items().forEach(item -> {
            body.append("- ").append(item.eventName() != null ? item.eventName() : "Etkinlik")
                .append(" (").append(item.categoryName() != null ? item.categoryName() : "Kategori")
                .append(") x").append(item.quantity())
                .append(" = ").append(item.price()).append(" TL\n");
        });
        
        body.append("\nNightFlow'u tercih ettiğiniz için teşekkür ederiz!");
        
        log.info("=== EMAIL SIMULATION ===");
        log.info("To: user-{}", event.userId());
        log.info("Subject: NightFlow - Sipariş Onayı #{}", event.orderNumber());
        log.info("Body:\n{}", body);
        log.info("=== END EMAIL ===");
        
        // Gerçek SMTP varsa aşağıdaki kodu aktif et:
        // try {
        //     SimpleMailMessage message = new SimpleMailMessage();
        //     message.setFrom(fromEmail);
        //     message.setTo("user@example.com"); // Gerçekte kullanıcı email'i
        //     message.setSubject("NightFlow - Sipariş Onayı #" + event.orderNumber());
        //     message.setText(body.toString());
        //     mailSender.send(message);
        // } catch (Exception e) {
        //     log.warn("SMTP not configured, email simulated only");
        // }
    }
}
