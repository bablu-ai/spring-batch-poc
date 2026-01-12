package com.example.batchmongo.processor;

import com.example.batchmongo.domain.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Item processor that transforms Person records during batch processing.
 * Converts name to uppercase and adds processing timestamp.
 */
@Slf4j
@Component
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) throws Exception {
        // Transform the person record
        String transformedName = person.getName().toUpperCase();
        Person transformedPerson = new Person(
                transformedName,
                person.getEmail(),
                person.getAge(),
                LocalDateTime.now()
        );

        log.debug("Transforming person: {} -> {}", person.getName(), transformedName);

        return transformedPerson;
    }
}
