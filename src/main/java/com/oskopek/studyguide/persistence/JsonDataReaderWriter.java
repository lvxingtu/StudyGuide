package com.oskopek.studyguide.persistence;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.oskopek.studyguide.model.DefaultStudyPlan;
import com.oskopek.studyguide.model.StudyPlan;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Reads and writes the {@link com.oskopek.studyguide.model.StudyPlan} to a JSON formatted file.
 * <p>
 * Specifics of the format can be found in the README(.adoc)
 */
public class JsonDataReaderWriter implements DataReader, DataWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EventBus injectableEventBus;

    /**
     * Create an instance of the reader/writer, with all injectable values set to null.
     */
    public JsonDataReaderWriter() {
        this(null, null);
    }

    /**
     * Create an instance of the reader/writer, with the given parameters passed as injectable values to Jackson.
     *
     * @param messages the message resource bundle to inject
     * @param eventBus the event bus to inject
     * @see InjectableValues
     * @see com.fasterxml.jackson.annotation.JacksonInject
     */
    public JsonDataReaderWriter(ResourceBundle messages, EventBus eventBus) {
        this.injectableEventBus = eventBus;
        InjectableValues injectableValues = new InjectableValues.Std().addValue(ResourceBundle.class, messages)
                .addValue(EventBus.class, eventBus);
        objectMapper.setInjectableValues(injectableValues);
    }

    /**
     * Manually finish "injection" of the study plan itself into some of its objects (a final manual deserialization
     * step).
     *
     * @param studyPlan the study plan to set
     * @return the updated study plan
     */
    private StudyPlan finalizeInjection(StudyPlan studyPlan) {
        studyPlan.getConstraints().allConstraintStream().forEach(c -> c.setSemesterPlan(studyPlan.getSemesterPlan()));
        studyPlan.getCourseRegistry().courseMapValues().stream().forEach(c -> c.registerEventBus(injectableEventBus));
        studyPlan.getSemesterPlan().allCourseEnrollments().forEach(c -> c.registerEventBus(injectableEventBus));
        return studyPlan;
    }

    @Override
    public StudyPlan readFrom(String fileName) throws IOException, IllegalArgumentException {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName is null.");
        }
        try {
            return finalizeInjection(objectMapper.readValue(new File(fileName), DefaultStudyPlan.class));
        } catch (JsonParseException | JsonMappingException e) {
            throw new IOException("Failed to read StudyPlan from file (" + fileName + ").", e);
        }
    }

    @Override
    public StudyPlan readFrom(InputStream inputStream) throws IOException, IllegalArgumentException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream is null.");
        }
        try {
            return finalizeInjection(objectMapper.readValue(inputStream, DefaultStudyPlan.class));
        } catch (JsonMappingException | JsonParseException e) {
            throw new IOException("Failed to read StudyPlan from stream.", e);
        }
    }

    @Override
    public void writeTo(StudyPlan plan, String fileName) throws IOException, IllegalArgumentException {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName is null.");
        } else if (plan == null) {
            throw new IllegalArgumentException("Plan is null.");
        }
        try {
            writeTo(plan, Files.newOutputStream(Paths.get(fileName)));
        } catch (IOException e) {
            throw new IOException("Failed to write StudyPlan to file (" + fileName + ").", e);
        }
    }

    @Override
    public void writeTo(StudyPlan plan, OutputStream outputStream) throws IOException, IllegalArgumentException {
        if (outputStream == null) {
            throw new IllegalArgumentException("OutputStream is null.");
        } else if (plan == null) {
            throw new IllegalArgumentException("Plan is null.");
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, Charset.forName("utf-8")))) {
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan));
            writer.write('\n');
        } catch (IOException e) {
            throw new IOException("Failed to write StudyPlan to stream.", e);
        }
    }
}
