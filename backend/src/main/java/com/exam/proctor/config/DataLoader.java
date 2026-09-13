package com.exam.proctor.config;

import com.exam.proctor.entity.Question;
import com.exam.proctor.entity.User;
import com.exam.proctor.repository.QuestionRepository;
import com.exam.proctor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private QuestionRepository questionRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public void run(String... args) throws Exception {
        // Seed default users if empty
        if (userRepo.count() == 0) {
            System.out.println("Seeding default accounts (Admin and Demo Student)...");
            
            User admin = new User();
            admin.setName("Vijayapandian (Admin)");
            admin.setEmail("vijayapandian112007@gmail.com");
            admin.setPassword("1234567890");
            admin.setRole(User.Role.ADMIN);
            userRepo.save(admin);

            User student = new User();
            student.setName("Demo Student");
            student.setEmail("user@gmail.com");
            student.setPassword("1234");
            student.setRole(User.Role.STUDENT);
            userRepo.save(student);
        }

        // Seed questions for all topic categories if empty
        if (questionRepo.count() == 0) {
            System.out.println("Seeding sample questions for all topic categories...");
            
            Question.TopicCategory[] categories = Question.TopicCategory.values();
            for (Question.TopicCategory cat : categories) {
                for (int i = 1; i <= 20; i++) {
                    Question q = new Question();
                    q.setTopicCategory(cat);
                    q.setQuestionText("Sample " + cat.name() + " Question #" + i + ": What is the fundamental concept?");
                    q.setOptionA("Option A - Definition of " + cat.name());
                    q.setOptionB("Option B - Implementation detail for " + cat.name());
                    q.setOptionC("Option C - Edge case behavior");
                    q.setOptionD("Option D - None of the above");
                    q.setCorrectOption(i % 2 == 0 ? "A" : "B");
                    questionRepo.save(q);
                }
            }
            System.out.println("Loaded " + questionRepo.count() + " sample questions across " + categories.length + " topics.");
        }
    }
}
