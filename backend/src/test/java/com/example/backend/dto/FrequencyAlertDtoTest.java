package com.example.backend.dto;

import com.example.backend.model.FrequencyAlert;
import com.example.backend.model.enums.FrequencyIncidentState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrequencyAlertDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preservesIncidentLifecycleAndAffectedRegions() {
        FrequencyAlertDto dto = new FrequencyAlertDto(
                "frequency-incident-123",
                123L,
                100L,
                123L,
                "System",
                "UPDATE",
                Arrays.asList("East", "West"),
                49.7,
                49.6,
                49.8,
                -0.3,
                -0.4,
                0.01,
                "High Rate of Change of Frequency",
                "description",
                "message",
                "High",
                0.7,
                64
        );

        FrequencyAlert entity = dto.toEntity();
        FrequencyAlertDto roundTrip = FrequencyAlertDto.from(entity);

        assertEquals(FrequencyIncidentState.UPDATE, entity.getIncidentState());
        assertEquals("East,West", entity.getAffectedRegions());
        assertEquals(dto, roundTrip);
    }

    @Test
    void readsIncidentFieldsFromKafkaJson() throws Exception {
        String json = "{" +
                "\"alert_id\":\"frequency-incident-123\"," +
                "\"timestamp\":123," +
                "\"window_start\":100," +
                "\"window_end\":123," +
                "\"region\":\"System\"," +
                "\"incident_state\":\"START\"," +
                "\"affected_regions\":[\"East\"]," +
                "\"avg_frequency\":49.7," +
                "\"min_frequency\":49.6," +
                "\"max_frequency\":49.8," +
                "\"frequency_deviation\":-0.3," +
                "\"rocof\":-0.4," +
                "\"rocof_volatility\":0.01," +
                "\"alert_display_name\":\"High Rate of Change of Frequency\"," +
                "\"alert_description\":\"description\"," +
                "\"message\":\"message\"," +
                "\"severity_level\":\"High\"," +
                "\"severity_score\":0.7," +
                "\"measurement_count\":64" +
                "}";

        FrequencyAlertDto dto = objectMapper.readValue(json, FrequencyAlertDto.class);

        assertEquals("START", dto.incidentState());
        assertEquals(Arrays.asList("East"), dto.affectedRegions());
    }
}
