package com.pronoplayer.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.pronoplayer.app.discord.DiscordListener;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

@SpringBootApplication
public class AppApplication {
	
	public static JDA jda;
	
	public static void main(String[] args) throws InterruptedException{
		ApplicationContext context = SpringApplication.run(AppApplication.class, args);
		Config config = context.getBean(Config.class);
		jda = JDABuilder.createDefault(config.getDiscordToken()).addEventListeners(new DiscordListener()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build().awaitReady();
	}
	
	
	@Component
	@Getter
	class Config {
		@Value("${app.discord.token}")
		private String discordToken;

	}
}
