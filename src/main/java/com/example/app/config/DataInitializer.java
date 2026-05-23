package com.example.app.config;

import com.example.app.entity.PromptTemplate;
import com.example.app.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PromptTemplateRepository templateRepository;

    public DataInitializer(PromptTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        if (templateRepository.findByName("rag-default").isEmpty()) {
            PromptTemplate template = PromptTemplate.builder()
                    .name("rag-default")
                    .category("rag")
                    .content("你是一个智能问答助手。请根据以下参考资料回答问题。\n\n" +
                            "{{context}}\n\n" +
                            "请基于以上资料，用中文简洁明了地回答问题。" +
                            "如果资料中没有相关信息，请如实说明。" +
                            "回答时标注引用来源编号，如 [1][2]。")
                    .description("RAG 问答默认模板")
                    .variables("[\"context\"]")
                    .usageCount(0)
                    .build();
            templateRepository.save(template);
            log.info("Created default prompt template: rag-default");
        }
    }
}