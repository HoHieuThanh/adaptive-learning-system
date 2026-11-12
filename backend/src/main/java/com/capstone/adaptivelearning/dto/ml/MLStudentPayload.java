package com.capstone.adaptivelearning.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLStudentPayload {

    @JsonProperty("student_id")
    private String studentId;

    @Builder.Default
    private String school = "GP";

    @Builder.Default
    private String sex = "F";

    @Builder.Default
    private Integer age = 17;

    @Builder.Default
    private String address = "U";

    @Builder.Default
    private String famsize = "GT3";

    @Builder.Default
    @JsonProperty("Pstatus")
    private String pstatus = "T";

    @Builder.Default
    @JsonProperty("Medu")
    private Integer medu = 3;

    @Builder.Default
    @JsonProperty("Fedu")
    private Integer fedu = 3;

    @Builder.Default
    @JsonProperty("Mjob")
    private String mjob = "other";

    @Builder.Default
    @JsonProperty("Fjob")
    private String fjob = "other";

    @Builder.Default
    private String reason = "course";

    @Builder.Default
    private String guardian = "mother";

    @Builder.Default
    private Integer traveltime = 1;

    @Builder.Default
    private Integer studytime = 2;

    @Builder.Default
    private Integer failures = 0;

    @Builder.Default
    private String schoolsup = "no";

    @Builder.Default
    private String famsup = "yes";

    @Builder.Default
    private String paid = "no";

    @Builder.Default
    private String activities = "yes";

    @Builder.Default
    private String nursery = "yes";

    @Builder.Default
    private String higher = "yes";

    @Builder.Default
    private String internet = "yes";

    @Builder.Default
    private String romantic = "no";

    @Builder.Default
    private Integer famrel = 4;

    @Builder.Default
    private Integer freetime = 3;

    @Builder.Default
    private Integer goout = 3;

    @Builder.Default
    @JsonProperty("Dalc")
    private Integer dalc = 1;

    @Builder.Default
    @JsonProperty("Walc")
    private Integer walc = 1;

    @Builder.Default
    private Integer health = 4;

    @Builder.Default
    private Integer absences = 4;

    @Builder.Default
    @JsonProperty("G1")
    private Double g1 = 10.0;

    @Builder.Default
    @JsonProperty("G2")
    private Double g2 = 10.0;
}

