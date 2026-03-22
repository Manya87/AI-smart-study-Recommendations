package com.example.demo.service.impl;

import com.example.demo.dto.QuizCreateRequest;
import com.example.demo.dto.QuizResponse;
import com.example.demo.dto.QuizResultResponse;
import com.example.demo.dto.QuizSubmitRequest;
import com.example.demo.entity.Quiz;
import com.example.demo.entity.QuizResult;
import com.example.demo.entity.Student;
import com.example.demo.entity.Topic;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.QuizRepository;
import com.example.demo.repository.QuizResultRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.service.QuizService;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final StudentRepository studentRepository;
    private final QuizResultRepository quizResultRepository;

    @Override
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request) {
        Long safeTopicId = Objects.requireNonNull(request.getTopicId(), "topicId must not be null");
        Topic topic = topicRepository.findById(safeTopicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle().trim());
        quiz.setTotalQuestions(request.getTotalQuestions());
        quiz.setTopic(topic);

        Quiz saved = quizRepository.save(quiz);
        log.info("Created quiz id={} for topic={}", saved.getId(), topic.getId());

        return QuizResponse.builder()
            .id(saved.getId())
            .title(saved.getTitle())
            .totalQuestions(saved.getTotalQuestions())
            .topicId(topic.getId())
            .topicName(topic.getName())
            .build();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "recommendations", key = "#request.studentId")
    public QuizResultResponse submitQuiz(QuizSubmitRequest request) {
        Long safeStudentId = Objects.requireNonNull(request.getStudentId(), "studentId must not be null");
        Student student = studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));

        Long safeQuizId = Objects.requireNonNull(request.getQuizId(), "quizId must not be null");
        Quiz quiz = quizRepository.findById(safeQuizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + request.getQuizId()));

        QuizResult result = new QuizResult();
        result.setStudent(student);
        result.setQuiz(quiz);
        result.setTopic(quiz.getTopic());
        result.setScore(request.getScore());
        result.setTimeTaken(request.getTimeTaken());
        result.setMistakesCount(request.getMistakesCount());
        result.setDate(LocalDate.now());

        QuizResult saved = quizResultRepository.save(result);
        log.info("Recorded quiz result id={} for student={} quiz={}", saved.getId(), student.getId(), quiz.getId());

        return QuizResultResponse.builder()
            .resultId(saved.getId())
            .studentId(student.getId())
            .quizId(quiz.getId())
            .topicId(quiz.getTopic().getId())
            .topicName(quiz.getTopic().getName())
            .score(saved.getScore())
            .timeTaken(saved.getTimeTaken())
            .mistakesCount(saved.getMistakesCount())
            .date(saved.getDate())
            .build();
    }
}
