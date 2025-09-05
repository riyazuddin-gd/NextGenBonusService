package com.example.nextgenBonus;

import com.example.nextgenBonus.Service.CourseService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class NextgenBonusApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextgenBonusApplication.class, args);
	}
	@Bean
	public List<ToolCallback> danTools(CourseService courseService) {
		return List.of(ToolCallbacks.from(courseService));
	}
}
