package com.school_project.smart_mirror.domain.faceApi;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "elasticsearch/face-setting.json", shards = 1, replicas = 0)
@Mapping(mappingPath = "elasticsearch/face-vector-mapping.json")
@Document(indexName = "users", createIndex = true)
public class Face {
    @Id
    private String id;

    @Field(type = FieldType.Dense_Vector, dims = 128)
    private List<Float> faceEmbedding;

    private float similarityScore;

}
